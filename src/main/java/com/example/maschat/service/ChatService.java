package com.example.maschat.service;

import com.example.maschat.domain.*;
import com.example.maschat.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final AgentRepository agentRepository;

    public ChatService(ConversationRepository conversationRepository,
                       ConversationParticipantRepository participantRepository,
                       MessageRepository messageRepository,
                       AgentRepository agentRepository) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.agentRepository = agentRepository;
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

        ConversationParticipant userP = new ConversationParticipant();
        userP.setConversationId(c.getId());
        userP.setParticipantType("user");
        userP.setUserId(createdByUserId);
        userP.setRoleKey("user");
        userP.setJoinedAt(Instant.now());
        participantRepository.save(userP);

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

    @Transactional(readOnly = true)
    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message sendUserMessage(String conversationId, String userId, String content) {
        Message m = new Message();
        m.setId(Ids.newUuid());
        m.setConversationId(conversationId);
        m.setSenderType("user");
        m.setSenderUserId(userId);
        m.setRoleKey("user");
        m.setContent(content);
        m.setContentType("text/markdown");
        m.setCreatedAt(Instant.now());
        messageRepository.save(m);

        triggerFakeAgents(conversationId);
        return m;
    }

    private void triggerFakeAgents(String conversationId) {
        List<ConversationParticipant> ps = participantRepository.findByConversationIdOrderByJoinedAtAsc(conversationId);
        for (ConversationParticipant p : ps) {
            if (!"agent".equals(p.getParticipantType())) continue;
            agentRepository.findById(p.getAgentId()).ifPresent(agent -> {
                String reply = generateFakeReply(agent.getKind());
                Message bot = new Message();
                bot.setId(Ids.newUuid());
                bot.setConversationId(conversationId);
                bot.setSenderType("agent");
                bot.setSenderAgentId(agent.getId());
                bot.setRoleKey("agent");
                bot.setContent(reply);
                bot.setContentType("text/markdown");
                bot.setCreatedAt(Instant.now());
                messageRepository.save(bot);
            });
        }
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
}


