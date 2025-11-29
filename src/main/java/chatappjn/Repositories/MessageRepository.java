package chatappjn.Repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import chatappjn.Models.Message;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, ObjectId> {

    // Find all messages for a specific chat, ordered by datetime
    List<Message> findByChatIdOrderByDatetimeAsc(ObjectId chatId);

    // Find all messages by a specific user
    List<Message> findByUserId(ObjectId userId);

    // Find all messages containing certain text
    List<Message> findByTextContainingIgnoreCase(String keyword);

    // Optional: delete all messages for a specific chat
    void deleteByChatId(ObjectId chatId);

    // Find all messages for multiple chats
    List<Message> findByChatIdInOrderByDatetimeAsc(List<ObjectId> chatIds);
}
