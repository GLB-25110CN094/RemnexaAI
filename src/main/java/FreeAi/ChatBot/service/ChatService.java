
package FreeAi.ChatBot.service;

import FreeAi.ChatBot.model.Conversation;
import FreeAi.ChatBot.model.Message;
import FreeAi.ChatBot.model.UserMemory;
import FreeAi.ChatBot.repository.ConversationRepository;
import FreeAi.ChatBot.repository.MessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final GeminiService geminiService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemoryService memoryService;

    public ChatService(
            GeminiService geminiService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MemoryService memoryService) {

        this.geminiService = geminiService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.memoryService = memoryService;
    }

    // Get the logged-in user's ID from JWT authentication
    private String getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new RuntimeException("User is not authenticated");
        }

        return authentication.getPrincipal().toString();
    }

    // Verify that the conversation belongs to the logged-in user
    private Conversation getUserConversation(Long conversationId) {
        String userId = getCurrentUserId();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new RuntimeException("Conversation not found"));

        if (!userId.equals(conversation.getUserId())) {
            throw new RuntimeException(
                    "You are not allowed to access this conversation");
        }

        return conversation;
    }

    // Chat with Gemini using history and personal memories
    public String chat(Long conversationId, String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty");
        }

        Conversation conversation =
                getUserConversation(conversationId);

        String userId = getCurrentUserId();

        // Get the current conversation's history
        List<Message> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId);

        // Retrieve only this user's saved memories
        List<String> memories = memoryService
                .getMemories(userId)
                .stream()
                .map(UserMemory::getMemory)
                .toList();

        // Generate an answer using history and memories
        String answer = geminiService.askGemini(
                history,
                question,
                memories
        );

        // Save the user's message
        Message userMessage = new Message();
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setConversation(conversation);
        messageRepository.save(userMessage);

        // Save the AI's response
        Message aiMessage = new Message();
        aiMessage.setRole("assistant");
        aiMessage.setContent(answer);
        aiMessage.setConversation(conversation);
        messageRepository.save(aiMessage);

        // Extract and save useful long-term memories
        try {
            List<String> facts =
                    geminiService.extractMemories(question);

            memoryService.saveMemories(userId, facts);

        } catch (Exception e) {
            // Memory failure should not stop an otherwise successful chat
            System.err.println(
                    "Memory update failed: " + e.getMessage());
        }

        return answer;
    }

    // Create a conversation for the logged-in user
    public Long createConversation() {
        String userId = getCurrentUserId();

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        return savedConversation.getId();
    }

    // Get history only if the conversation belongs to this user
    public List<Message> getHistory(Long conversationId) {
        getUserConversation(conversationId);

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId);
    }
    public List<Conversation> getUserConversations() {
        String userId = getCurrentUserId();

        return conversationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
}

