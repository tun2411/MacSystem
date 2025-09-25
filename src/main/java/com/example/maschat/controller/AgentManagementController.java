package com.example.maschat.controller;

import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.ConversationParticipant;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.ConversationParticipantRepository;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class AgentManagementController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping("/{conversationId}/agents")
    public String manageAgents(@PathVariable String conversationId, Model model, jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";

        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff == null || !isStaff) {
            model.addAttribute("error", "Chỉ có staff mới được chỉnh sửa agent tham gia");
            return "redirect:/chat/" + conversationId;
        }

        // Kiểm tra quyền truy cập cuộc trò chuyện cho staff
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return "redirect:/login"; // Cuộc trò chuyện không tồn tại
        }
        
        // Staff có quyền nếu cuộc trò chuyện đã có staff participants hoặc được đánh dấu isStaffEngaged
        boolean hasAccess = conversationRepository.findAllHavingStaffParticipants()
                .stream()
                .anyMatch(c -> c.getId().equals(conversationId));
        if (!hasAccess && Boolean.TRUE.equals(conversation.getIsStaffEngaged())) {
            hasAccess = true;
        }
        
        if (!hasAccess) {
            return "redirect:/login"; // Không có quyền truy cập
        }

        model.addAttribute("conversationId", conversationId);
        model.addAttribute("allAgents", agentRepository.findByActiveTrue());

        List<ConversationParticipant> currentAgents = participantRepository.findByConversationIdAndParticipantType(conversationId, "agent");
        String currentAgentId = currentAgents.stream()
                .filter(p -> !"supervisor".equals(p.getRoleKey()))
                .map(ConversationParticipant::getAgentId)
                .findFirst()
                .orElse(null);
        model.addAttribute("currentAgentId", currentAgentId);
        return "conversation_agents";
    }

    @PostMapping("/{conversationId}/agents")
    public String updateAgents(@PathVariable String conversationId,
                               @RequestParam(required = false, name = "agentId") String agentId,
                               jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";

        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff == null || !isStaff) {
            return "redirect:/chat/" + conversationId;
        }

        // Kiểm tra quyền truy cập cuộc trò chuyện cho staff
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return "redirect:/login"; // Cuộc trò chuyện không tồn tại
        }
        
        // Staff có quyền nếu cuộc trò chuyện đã có staff participants hoặc được đánh dấu isStaffEngaged
        boolean hasAccess = conversationRepository.findAllHavingStaffParticipants()
                .stream()
                .anyMatch(c -> c.getId().equals(conversationId));
        if (!hasAccess && Boolean.TRUE.equals(conversation.getIsStaffEngaged())) {
            hasAccess = true;
        }
        
        if (!hasAccess) {
            return "redirect:/login"; // Không có quyền truy cập
        }

        conversationService.updateConversationAgents(conversationId, agentId);
        return "redirect:/conversations";
    }
}


