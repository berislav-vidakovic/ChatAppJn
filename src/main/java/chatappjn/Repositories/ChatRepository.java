package chatappjn.Repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import chatappjn.Models.Chat;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Chat, ObjectId> {

    // Find all chats a user participates in
    List<Chat> findByUserIdsContaining(ObjectId userId);

    // Find a chat by exact combination of users (optional, custom query might be needed for exact match)
    List<Chat> findByUserIds(List<ObjectId> userIds);

    // Find a chat by its name
    Chat findByChatName(String chatName);

    // Optional: delete chat by id
    void deleteById(ObjectId id);
}
