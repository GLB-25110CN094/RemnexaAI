
package FreeAi.ChatBot.service;

import FreeAi.ChatBot.model.UserMemory;
import FreeAi.ChatBot.repository.UserMemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemoryService {

    private final UserMemoryRepository memoryRepository;

    public MemoryService(UserMemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }

    public List<UserMemory> getMemories(String userId) {
        return memoryRepository
                .findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional
    public void saveMemories(String userId, List<String> facts) {
        for (String fact : facts) {
            if (fact == null || fact.isBlank()) {
                continue;
            }

            String cleanedFact = fact.trim();

            // Avoid storing duplicate facts.
            boolean alreadyExists = memoryRepository
                    .findByUserIdOrderByUpdatedAtDesc(userId)
                    .stream()
                    .anyMatch(m -> m.getMemory()
                            .equalsIgnoreCase(cleanedFact));

            if (!alreadyExists) {
                UserMemory memory = new UserMemory();
                memory.setUserId(userId);
                memory.setMemory(cleanedFact);

                memoryRepository.save(memory);
            }
        }
    }
}
