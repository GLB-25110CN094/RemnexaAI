package FreeAi.ChatBot.model;

import lombok.Data;

@Data
public class ChatRequest {
    private Long conversationId;
    private String question;
}
