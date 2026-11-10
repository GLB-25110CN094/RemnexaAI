
package FreeAi.ChatBot.controller;

import FreeAi.ChatBot.model.Conversation;
import FreeAi.ChatBot.model.ChatRequest;
import FreeAi.ChatBot.model.Message;
import FreeAi.ChatBot.service.ChatService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Send message to Gemini
    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(
                request.getConversationId(),
                request.getQuestion()
        );
    }

    // Create a new conversation for logged-in user
    @PostMapping("/chat/conversation")
    public Long createConversation() {
        return chatService.createConversation();
    }

    // Get conversation history
    @GetMapping("/chat/history/{conversationId}")
    public List<Message> getHistory(
            @PathVariable Long conversationId) {
        return chatService.getHistory(conversationId);
    }

    @GetMapping("/chat/conversations")
    public List<Conversation> getConversations() {
        return chatService.getUserConversations();
    }

}
