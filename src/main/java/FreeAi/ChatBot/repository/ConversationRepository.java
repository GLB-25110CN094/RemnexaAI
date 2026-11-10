package FreeAi.ChatBot.repository;

import FreeAi.ChatBot.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(
            String userId
    );
}
