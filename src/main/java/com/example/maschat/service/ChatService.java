package com.example.maschat.service;

import com.example.maschat.domain.*;
import com.example.maschat.repo.*;
import jakarta.persistence.EntityManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final AgentRepository agentRepository;
    private final EntityManager entityManager;
    
    // Thread-safe counter for message ordering
    private final java.util.concurrent.atomic.AtomicLong messageCounter = new java.util.concurrent.atomic.AtomicLong(0);

    public ChatService(ConversationRepository conversationRepository,
                       ConversationParticipantRepository participantRepository,
                       MessageRepository messageRepository,
                       AgentRepository agentRepository,
                       EntityManager entityManager) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.agentRepository = agentRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Conversation startConversation(String title, String createdByUserId, List<String> agentIds) {
        Conversation c = new Conversation();
        c.setId(Ids.newUuid());
        c.setTitle(title);
        c.setCreatedByUser(createdByUserId);
        c.setCreatedAt(Instant.now());
        c.setStatus("active");
        conversationRepository.save(c);

        // Add user participant
        ConversationParticipant userP = new ConversationParticipant();
        userP.setConversationId(c.getId());
        userP.setParticipantType("user");
        userP.setUserId(createdByUserId);
        userP.setRoleKey("user");
        userP.setJoinedAt(Instant.now());
        participantRepository.save(userP);

        // Add supervisor agent by default
        List<Agent> supervisors = agentRepository.findByKind("Supervisor");
        if (!supervisors.isEmpty()) {
            Agent supervisor = supervisors.get(0);
            ConversationParticipant supervisorP = new ConversationParticipant();
            supervisorP.setConversationId(c.getId());
            supervisorP.setParticipantType("agent");
            supervisorP.setAgentId(supervisor.getId());
            supervisorP.setRoleKey("supervisor");
            supervisorP.setJoinedAt(Instant.now());
            participantRepository.save(supervisorP);
            
            // Send welcome message from supervisor
            long welcomeMessageOrder = messageCounter.incrementAndGet();
            Message welcomeMsg = new Message();
            welcomeMsg.setId(Ids.newUuid());
            welcomeMsg.setConversationId(c.getId());
            welcomeMsg.setSenderType("agent");
            welcomeMsg.setSenderAgentId(supervisor.getId());
            welcomeMsg.setRoleKey("supervisor");
            welcomeMsg.setContent("Xin chào, tôi có thể giúp gì cho bạn");
            welcomeMsg.setContentType("text/markdown");
            welcomeMsg.setCreatedAt(Instant.now());
            messageRepository.save(welcomeMsg);
            System.out.println("DEBUG: Welcome message order: " + welcomeMessageOrder);
        }

        // Add other agents if specified (for staff management)
        for (String agentId : agentIds) {
            ConversationParticipant p = new ConversationParticipant();
            p.setConversationId(c.getId());
            p.setParticipantType("agent");
            p.setAgentId(agentId);
            p.setRoleKey("agent");
            p.setJoinedAt(Instant.now());
            participantRepository.save(p);
        }
        return c;
    }

    @Transactional
    public void updateConversationAgents(String conversationId, String agentId) {
        // Remove all existing agents (except supervisor) manually
        List<ConversationParticipant> participants = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        for (ConversationParticipant p : participants) {
            if ("agent".equals(p.getParticipantType()) && !"supervisor".equals(p.getRoleKey())) {
                participantRepository.delete(p);
            }
        }
        
        // Force flush to ensure deletions are committed
        entityManager.flush();
        
        // Add the selected agent if provided
        if (agentId != null && !agentId.isEmpty()) {
            ConversationParticipant p = new ConversationParticipant();
            p.setConversationId(conversationId);
            p.setParticipantType("agent");
            p.setAgentId(agentId);
            p.setRoleKey("agent");
            p.setJoinedAt(Instant.now());
            participantRepository.save(p);
        }
        
        // Supervisor leaves after staff manually assigns agent
        removeSupervisorFromConversation(conversationId);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message sendUserMessage(String conversationId, String userId, String content) {
        Instant userMessageTime = Instant.now();
        long userMessageOrder = messageCounter.incrementAndGet();
        
        System.out.println("DEBUG: User message time: " + userMessageTime + ", order: " + userMessageOrder);
        
        Message m = new Message();
        m.setId(Ids.newUuid());
        m.setConversationId(conversationId);
        m.setSenderType("user");
        m.setSenderUserId(userId);
        m.setRoleKey("user");
        m.setContent(content);
        m.setContentType("text/markdown");
        m.setCreatedAt(userMessageTime);
        messageRepository.save(m);
        
        // Force flush to ensure user message is saved first
        entityManager.flush();

        try {
            // Check if this is the first user message and route to appropriate agent
            List<Message> userMessages = messageRepository.findByConversationIdAndSenderTypeOrderByCreatedAtAsc(conversationId, "user");
            if (userMessages.size() == 1) { // First user message
                routeToAppropriateAgent(conversationId, content);
            } else {
                // Check subsequent messages for agent switching
                checkAndSwitchAgent(conversationId, content);
            }
            
            // Send agent response with proper timing and retry mechanism
            // Ensure agent response is always after user message with sufficient delay
            // Use a longer delay to ensure proper ordering
            sendAgentResponseWithRetry(conversationId, userMessageTime.plusMillis(1000));
        } catch (Exception e) {
            // Log error but don't fail the user message
            System.err.println("Error processing agent response for conversation " + conversationId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return m;
    }

    @Transactional
    public Message sendStaffMessage(String conversationId, String userId, String content) {
        Instant staffMessageTime = Instant.now();
        long staffMessageOrder = messageCounter.incrementAndGet();
        
        System.out.println("DEBUG: Staff message time: " + staffMessageTime + ", order: " + staffMessageOrder);
        
        Message m = new Message();
        m.setId(Ids.newUuid());
        m.setConversationId(conversationId);
        m.setSenderType("staff");
        m.setSenderUserId(userId);
        m.setRoleKey("staff");
        m.setContent(content);
        m.setContentType("text/markdown");
        m.setCreatedAt(staffMessageTime);
        messageRepository.save(m);
        
        // Force flush to ensure staff message is saved
        entityManager.flush();
        
        // Staff messages don't trigger agent responses
        System.out.println("DEBUG: Staff message sent - no agent response triggered");
        
        return m;
    }

    private void sendAgentResponseWithRetry(String conversationId, Instant responseTime) {
        int maxRetries = 3;
        int retryCount = 0;
        Instant currentResponseTime = responseTime;
        
        while (retryCount < maxRetries) {
            try {
                sendAgentResponse(conversationId, currentResponseTime);
                break; // Success, exit retry loop
            } catch (Exception e) {
                retryCount++;
                System.err.println("Attempt " + retryCount + " failed for conversation " + conversationId + ": " + e.getMessage());
                
                if (retryCount >= maxRetries) {
                    System.err.println("Max retries reached for conversation " + conversationId);
                    // Try to add a default agent as last resort
                    try {
                        addDefaultAgent(conversationId);
                        // Ensure final attempt has a later timestamp
                        currentResponseTime = currentResponseTime.plusMillis(100);
                        sendAgentResponse(conversationId, currentResponseTime);
                    } catch (Exception finalException) {
                        System.err.println("Final attempt failed for conversation " + conversationId + ": " + finalException.getMessage());
                    }
                } else {
                    // Wait before retry and increment timestamp
                    try {
                        Thread.sleep(100 * retryCount); // Progressive delay
                        // Ensure each retry has a later timestamp
                        currentResponseTime = currentResponseTime.plusMillis(100 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private void sendAgentResponse(String conversationId, Instant responseTime) {
        // Check if there's already an agent response for this conversation within the last 2 seconds
        // This prevents duplicate responses from retry mechanism
        Instant twoSecondsAgo = responseTime.minusSeconds(2);
        List<Message> recentAgentMessages = messageRepository.findByConversationIdAndSenderTypeOrderByCreatedAtAsc(conversationId, "agent");
        boolean hasRecentResponse = recentAgentMessages.stream()
            .anyMatch(msg -> msg.getCreatedAt().isAfter(twoSecondsAgo));
        
        if (hasRecentResponse) {
            System.out.println("DEBUG: Skipping agent response - recent response already exists for conversation " + conversationId);
            return;
        }
        
        // Ensure agent response time is always after the latest user message
        List<Message> userMessages = messageRepository.findByConversationIdAndSenderTypeOrderByCreatedAtAsc(conversationId, "user");
        final Instant finalResponseTime;
        if (!userMessages.isEmpty()) {
            Instant latestUserMessageTime = userMessages.get(userMessages.size() - 1).getCreatedAt();
            if (responseTime.isBefore(latestUserMessageTime) || responseTime.equals(latestUserMessageTime)) {
                finalResponseTime = latestUserMessageTime.plusMillis(100);
                System.out.println("DEBUG: Adjusted agent response time to be after user message: " + finalResponseTime);
            } else {
                finalResponseTime = responseTime;
            }
        } else {
            finalResponseTime = responseTime;
        }
        
        List<ConversationParticipant> ps = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        
        // Debug: Count active agents
        long agentCount = ps.stream()
            .filter(p -> "agent".equals(p.getParticipantType()) && !"supervisor".equals(p.getRoleKey()))
            .count();
        
        System.out.println("DEBUG: Found " + agentCount + " active agents in conversation " + conversationId);
        System.out.println("DEBUG: Agent response time: " + finalResponseTime);
        
        // If no active agents, try to add a default agent
        if (agentCount == 0) {
            System.out.println("DEBUG: No active agents found, adding default agent");
            addDefaultAgent(conversationId);
            // Refresh participants list
            ps = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        }
        
        boolean responseSent = false;
        
        // Find the first active agent (excluding supervisor)
        for (ConversationParticipant p : ps) {
            if (!"agent".equals(p.getParticipantType()) || "supervisor".equals(p.getRoleKey())) continue;
            
            agentRepository.findById(p.getAgentId()).ifPresent(agent -> {
                long agentMessageOrder = messageCounter.incrementAndGet();
                System.out.println("DEBUG: Sending response from agent: " + agent.getKind() + ", order: " + agentMessageOrder);
                String reply = generateFakeReply(agent.getKind());
                Message bot = new Message();
                bot.setId(Ids.newUuid());
                bot.setConversationId(conversationId);
                bot.setSenderType("agent");
                bot.setSenderAgentId(agent.getId());
                bot.setRoleKey("agent");
                bot.setContent(reply);
                bot.setContentType("text/markdown");
                bot.setCreatedAt(finalResponseTime);
                messageRepository.save(bot);
                
                // Force flush to ensure agent message is saved
                entityManager.flush();
            });
            responseSent = true;
            break; // Only send one agent message
        }
        
        // If still no response was sent, try supervisor as fallback
        if (!responseSent) {
            for (ConversationParticipant p : ps) {
                if ("agent".equals(p.getParticipantType()) && "supervisor".equals(p.getRoleKey())) {
                    agentRepository.findById(p.getAgentId()).ifPresent(agent -> {
                        long supervisorMessageOrder = messageCounter.incrementAndGet();
                        System.out.println("DEBUG: Sending response from supervisor as fallback, order: " + supervisorMessageOrder);
                        String reply = generateFakeReply(agent.getKind());
                        Message bot = new Message();
                        bot.setId(Ids.newUuid());
                        bot.setConversationId(conversationId);
                        bot.setSenderType("agent");
                        bot.setSenderAgentId(agent.getId());
                        bot.setRoleKey("supervisor");
                        bot.setContent(reply);
                        bot.setContentType("text/markdown");
                        bot.setCreatedAt(finalResponseTime);
                        messageRepository.save(bot);
                        
                        // Force flush to ensure agent message is saved
                        entityManager.flush();
                    });
                    break;
                }
            }
        }
    }

    private void triggerFakeAgents(String conversationId, Instant afterTime) {
        // This method is now deprecated, use sendAgentResponse instead
        sendAgentResponse(conversationId, afterTime);
    }

    private void routeToAppropriateAgent(String conversationId, String content) {
        String agentKind = analyzeMessageContent(content);
        if (!"Supervisor".equals(agentKind)) {
            switchToAgent(conversationId, agentKind);
            // Supervisor leaves after routing
            removeSupervisorFromConversation(conversationId);
        }
    }
    
    private void checkAndSwitchAgent(String conversationId, String content) {
        String agentKind = analyzeMessageContent(content);
        if (!"Supervisor".equals(agentKind)) {
            switchToAgent(conversationId, agentKind);
            // Supervisor leaves after routing
            removeSupervisorFromConversation(conversationId);
        }
    }
    
    private void switchToAgent(String conversationId, String agentKind) {
        // First, add the new agent if it's not already participating
        List<Agent> agents = agentRepository.findByKind(agentKind);
        if (!agents.isEmpty()) {
            Agent agent = agents.get(0);
            
            // Check if this agent is already participating
            List<ConversationParticipant> participants = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
            boolean agentExists = participants.stream()
                .anyMatch(p -> "agent".equals(p.getParticipantType()) && agent.getId().equals(p.getAgentId()));
            
            if (!agentExists) {
                ConversationParticipant p = new ConversationParticipant();
                p.setConversationId(conversationId);
                p.setParticipantType("agent");
                p.setAgentId(agent.getId());
                p.setRoleKey("agent");
                p.setJoinedAt(Instant.now());
                participantRepository.save(p);
                System.out.println("DEBUG: Added agent " + agentKind + " to conversation " + conversationId);
            }
        }
        
        // Then remove all existing agents (except supervisor and the new one) manually
        List<ConversationParticipant> participants = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        for (ConversationParticipant p : participants) {
            if ("agent".equals(p.getParticipantType()) && !"supervisor".equals(p.getRoleKey())) {
                // Don't delete the agent we just added
                if (agents.isEmpty() || !agents.get(0).getId().equals(p.getAgentId())) {
                    participantRepository.delete(p);
                    System.out.println("DEBUG: Removed old agent from conversation " + conversationId);
                }
            }
        }
        
        // Force flush to ensure changes are committed
        entityManager.flush();
    }
    
    private void removeSupervisorFromConversation(String conversationId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        
        // Only remove supervisor if there are other agents available
        long otherAgentCount = participants.stream()
            .filter(p -> "agent".equals(p.getParticipantType()) && !"supervisor".equals(p.getRoleKey()))
            .count();
            
        if (otherAgentCount > 0) {
            for (ConversationParticipant p : participants) {
                if ("agent".equals(p.getParticipantType()) && "supervisor".equals(p.getRoleKey())) {
                    participantRepository.delete(p);
                    System.out.println("DEBUG: Removed supervisor from conversation " + conversationId);
                    break;
                }
            }
        } else {
            System.out.println("DEBUG: Keeping supervisor in conversation " + conversationId + " - no other agents available");
        }
    }
    
    private void addDefaultAgent(String conversationId) {
        // Add a default Neutral agent if no agents are available
        List<Agent> neutralAgents = agentRepository.findByKind("Neutral");
        if (!neutralAgents.isEmpty()) {
            Agent agent = neutralAgents.get(0);
            ConversationParticipant p = new ConversationParticipant();
            p.setConversationId(conversationId);
            p.setParticipantType("agent");
            p.setAgentId(agent.getId());
            p.setRoleKey("agent");
            p.setJoinedAt(Instant.now());
            participantRepository.save(p);
            System.out.println("DEBUG: Added default Neutral agent to conversation " + conversationId);
        } else {
            // If no Neutral agent, try to add any available agent
            List<Agent> allAgents = agentRepository.findByActiveTrue();
            if (!allAgents.isEmpty()) {
                Agent agent = allAgents.get(0);
                ConversationParticipant p = new ConversationParticipant();
                p.setConversationId(conversationId);
                p.setParticipantType("agent");
                p.setAgentId(agent.getId());
                p.setRoleKey("agent");
                p.setJoinedAt(Instant.now());
                participantRepository.save(p);
                System.out.println("DEBUG: Added fallback agent " + agent.getKind() + " to conversation " + conversationId);
            }
        }
    }
    
    private String analyzeMessageContent(String content) {
        String lowerContent = content.toLowerCase();
        
        // Positive keywords
        String[] positiveKeywords = {
            "tuyệt vời", "tốt", "cảm ơn", "hài lòng", "yêu thích", "xuất sắc", "hoàn hảo", 
            "thích", "ưng ý", "ok", "okay", "tốt lắm", "hay", "đẹp", "chất lượng",
            "recommend", "giới thiệu", "khen", "khen ngợi", "thích thú"
        };
        
        // Negative keywords  
        String[] negativeKeywords = {
            "lỗi", "hỏng", "không hài lòng", "tệ", "khiếu nại", "muộn", "chậm", "xấu",
            "thất vọng", "bực mình", "khó chịu", "không ổn", "sai", "lỗi", "hỏng hóc",
            "complaint", "problem", "issue", "bad", "terrible", "awful", "disappointed"
        };
        
        // Neutral keywords
        String[] neutralKeywords = {
            "giá bao nhiêu", "cách sử dụng", "thông tin", "địa chỉ", "liên hệ", "hỏi",
            "tư vấn", "hướng dẫn", "giá", "price", "cost", "how to", "information",
            "address", "contact", "help", "hỗ trợ", "tư vấn", "câu hỏi"
        };
        
        // Check for positive keywords
        for (String keyword : positiveKeywords) {
            if (lowerContent.contains(keyword)) {
                return "Positive";
            }
        }
        
        // Check for negative keywords
        for (String keyword : negativeKeywords) {
            if (lowerContent.contains(keyword)) {
                return "Negative";
            }
        }
        
        // Check for neutral keywords
        for (String keyword : neutralKeywords) {
            if (lowerContent.contains(keyword)) {
                return "Neutral";
            }
        }
        
        // Default to Neutral if no keywords found
        return "Neutral";
    }
    
    private void addAgentToConversation(String conversationId, String agentKind) {
        List<Agent> agents = agentRepository.findByKind(agentKind);
        if (!agents.isEmpty()) {
            Agent agent = agents.get(0);
            
            // Check if this agent is already participating
            List<ConversationParticipant> existingParticipants = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
            boolean agentExists = existingParticipants.stream()
                .anyMatch(p -> "agent".equals(p.getParticipantType()) && agent.getId().equals(p.getAgentId()));
            
            if (!agentExists) {
                ConversationParticipant p = new ConversationParticipant();
                p.setConversationId(conversationId);
                p.setParticipantType("agent");
                p.setAgentId(agent.getId());
                p.setRoleKey("agent");
                p.setJoinedAt(Instant.now());
                participantRepository.save(p);
            }
        }
    }
    
    private String getAgentKindById(String agentId) {
        return agentRepository.findById(agentId)
            .map(Agent::getKind)
            .orElse("Unknown");
    }

    private String generateFakeReply(String kind) {
        Map<String, String> canned = Map.of(
                "Positive", "😊 Positive Agent: Sản phẩm rất phù hợp, đánh giá 5 sao!",
                "Negative", "😞 Negative Agent: Chúng ta cần xử lý khiếu nại gấp.",
                "Neutral",  "😐 Neutral Agent: Tôi đề xuất kiểm tra tồn kho và thời gian giao.",
                "Supervisor", "🧭 Supervisor: Tổng hợp ý kiến và đưa ra phương án tối ưu."
        );
        return canned.getOrDefault(kind, "Agent: Đã ghi nhận yêu cầu.");
    }

    @Transactional
    public Message editMessage(String messageId, String requesterRoleKey, String newContent) {
        Message m = messageRepository.findById(messageId).orElseThrow();
        if (!"user".equals(requesterRoleKey) && !"staff".equals(requesterRoleKey)) {
            throw new IllegalStateException("Only user or staff can edit messages");
        }
        m.setContent(newContent);
        m.setEditedAt(Instant.now());
        return messageRepository.save(m);
    }
    
    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    @Transactional
    public void checkForMissedResponses() {
        try {
            Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
            
            // Find conversations with user messages that don't have agent responses
            List<Conversation> conversations = conversationRepository.findAll();
            
            for (Conversation conversation : conversations) {
                List<Message> userMessages = messageRepository.findByConversationIdAndSenderTypeOrderByCreatedAtAsc(conversation.getId(), "user");
                List<Message> agentMessages = messageRepository.findByConversationIdAndSenderTypeOrderByCreatedAtAsc(conversation.getId(), "agent");
                
                // Check if there are user messages without corresponding agent responses
                for (Message userMsg : userMessages) {
                    if (userMsg.getCreatedAt().isAfter(fiveMinutesAgo)) {
                        // Check if there's an agent response after this user message
                        boolean hasResponse = agentMessages.stream()
                            .anyMatch(agentMsg -> agentMsg.getCreatedAt().isAfter(userMsg.getCreatedAt()));
                        
                        if (!hasResponse) {
                            System.out.println("DEBUG: Found missed response for conversation " + conversation.getId() + ", user message at " + userMsg.getCreatedAt());
                            
                            // Try to send a response with proper timing to ensure it comes after user message
                            try {
                                // Ensure agent response is always after user message with sufficient delay
                                Instant responseTime = userMsg.getCreatedAt().plusMillis(2000);
                                sendAgentResponseWithRetry(conversation.getId(), responseTime);
                            } catch (Exception e) {
                                System.err.println("Failed to send missed response for conversation " + conversation.getId() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in checkForMissedResponses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


