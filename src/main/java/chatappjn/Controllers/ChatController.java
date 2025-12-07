package chatappjn.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chatappjn.Models.Chat;
import chatappjn.Models.User;
import chatappjn.Repositories.UserRepository;
import chatappjn.Services.ClientIdChecker;
import chatappjn.Services.WebSocketService;
import chatappjn.Repositories.ChatRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

// POST /api/chat/new
@RestController
@RequestMapping("/api")
public class ChatController {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private WebSocketService  webSocketService;
  
  @Autowired
  private ClientIdChecker clientIdChecker;

  @PostMapping("/chat/new")
  public ResponseEntity<?> createNewChat(@RequestParam("id") String clientId, @RequestBody Map<String, Object> body,
            @RequestAttribute("userId") String userId, @RequestAttribute("username") String username ) {
    try {        
      System.out.println("RequestAttribute(userId): " + userId);
      System.out.println("RequestAttribute(username): " + username);

      UUID parsedClientId = clientIdChecker.parseClientId(clientId);
      if( parsedClientId == null )
        return clientIdChecker.buildResponse(HttpStatus.BAD_REQUEST,
          "Mimssing or invalind client ID");

      // Request:  { creatorId,  memberIds: [userId1,userId2] }
      String creatorId = (String)body.get("creatorId");
      List<String> memberIds = (List<String>) body.get("memberIds"); // cast to List
      if (creatorId == null || creatorId.isBlank() || 
        memberIds == null || memberIds.isEmpty() ) {
          Map<String, Object> response = Map.of(
            "acknowledged", false,
            "error", "Missing creatorId or memberIds"
          );
          return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
      }

      // construct a mutable list
      List<String> allUserIds = new java.util.ArrayList<>(memberIds);
      // Include creator
      if (!allUserIds.contains(creatorId)) 
        allUserIds.add(creatorId);      

    // Response  { creatorId, newChatId,  newChatName, userIds: [userId1,userId2] }

    // Convert request userIds → ObjectId list (MUTABLE)
    List<ObjectId> userIdsAsObj = allUserIds
      .stream()
      .map(ObjectId::new)
      .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

    // Normalize order (important for match)
    userIdsAsObj.sort(java.util.Comparator.comparing(ObjectId::toHexString));

    // Check if chat already exists
    List<Chat> existing = chatRepository.findByUserIds(userIdsAsObj);
    if (!existing.isEmpty()) {
      Chat existingChat = existing.get(0);
      
      //  HashMap allows null (Map.of throws Exception for null value)
      Map<String, Object> response = new java.util.HashMap<>();
      response.put("creatorId", creatorId);
      response.put("newChatId", existingChat.getId());
      response.put("newChatName", existingChat.getChatName());
      response.put("userIds", allUserIds);

      return new ResponseEntity<>(response, HttpStatus.OK);  // 200        
    }  

    // Create new chat
    // Fetch users from DB
    List<User> users = userRepository.findAllById(allUserIds);
    // Map to usernames
    List<String> usernames = users.stream()
        .map(User::getFullName)  
        .toList();
    // Join with commas
    String newChatName = String.join(",", usernames);

    Chat newChat = new Chat(userIdsAsObj, newChatName);
    chatRepository.save(newChat);
    Map<String, Object> response = Map.of(
      "creatorId", creatorId,
      "newChatId", newChat.getId(),
      "newChatName", newChatName,
      "userIds", allUserIds
    );
    
    webSocketService.broadcastMessage("newChatCreated", "WsStatus.OK", response);

    return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
  } 
    catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = Map.of( 
          "acknowledged", false, "error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500   
    }
  }

}


