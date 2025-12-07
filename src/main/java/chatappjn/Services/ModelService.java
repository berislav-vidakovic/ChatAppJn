package chatappjn.Services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import chatappjn.Common.AuthUser;
import chatappjn.Common.ModelDTO;
import chatappjn.Models.Chat;
import chatappjn.Models.Message;
import chatappjn.Models.User;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Repositories.RefreshTokenRepository;
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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserMonitor userMonitor;

    public ModelDTO handleLogin(AuthUser authUser, UUID parsedClientId ){
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

    public ModelDTO handleLogout(Map<String,Object> body, UUID parsedClientId){
       // Validate userId
      if (!body.containsKey("userId"))
        return new ModelDTO("Missing [userId] field");    
 
      Object userIdContent = body.get("userId");
      if( userIdContent == null )
        return new ModelDTO("Empty [userId] field");   

      String userId = userIdContent.toString();

      // Find user
      Optional<User> optionalUser = userRepository.findById(userId);
      if (optionalUser.isEmpty()) 
        return new ModelDTO("UserId not found ");   

      User user = optionalUser.get();
      user.setOnline(false);
      userRepository.save(user);

      // Clear refresh token from DB
      System.out.println("Deleting refresh tokens for userId: " + userId);
      refreshTokenRepository.deleteByUserId(userId);
      System.out.println("Deleting done ");

      userMonitor.removeUser(userId);

      return new ModelDTO( userId, null, null );
    } 
}
