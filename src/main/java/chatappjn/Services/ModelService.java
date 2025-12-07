package chatappjn.Services;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import chatappjn.Common.AuthUser;
import chatappjn.Common.ModelDTO;
import chatappjn.Models.Chat;
import chatappjn.Models.Message;
import chatappjn.Models.User;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Repositories.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


// Spring-managed singleton 
@Service
public class ModelService extends MiddlewareCommon {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserMonitor userMonitor;

    public ModelDTO getModel(AuthUser authUser, UUID parsedClientId ){
      User user = authUser.getUser();
      String userId = user.getId();
      user.setOnline(true);
      userRepository.save(user);

      userMonitor.updateUserActivity(user.getId(), parsedClientId);

      // Fetch chats where user participates
      ObjectId userObjectId = new ObjectId(userId); 
      List<Chat> userChats = chatRepository.findByUserIdsContaining(userObjectId);

      // Fetch all messages from those chats
      List<ObjectId> chatIds = userChats.stream()
        .map(chat -> new ObjectId(chat.getId()))
        .toList();

      List<Message> messages = messageRepository.findByChatIdInOrderByDatetimeAsc(chatIds);
            
      return new ModelDTO(userId, userChats, messages);
    }
}
