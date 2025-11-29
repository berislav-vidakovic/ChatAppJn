package chatappjn.Controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;


import com.fasterxml.jackson.databind.ObjectMapper;

import chatappjn.Config.JwtBuilder;
import chatappjn.Models.Chat;
import chatappjn.Models.Message;
import chatappjn.Models.RefreshToken;
import chatappjn.Models.User;
import chatappjn.Repositories.RefreshTokenRepository;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.ChatRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Services.Authentication;
import chatappjn.Services.ModelService;
import chatappjn.Services.UserMonitor;
import chatappjn.Auth.AuthUser;
import chatappjn.WebSockets.WebSocketHandler;

// POST /api/auth/refresh
// POST /api/auth/login
// POST /api/auth/logout
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMonitor userMonitor;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository  chatRepository;

    @Autowired
    private MessageRepository messageRepository;


    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Authentication authService;

    public AuthController() {
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam("id") String clientId, @RequestBody Map<String, String> body) {
      try {        
        // Validate clientId
        UUID parsedClientId;
        try {
          parsedClientId = UUID.fromString(clientId);
        } 
        catch (IllegalArgumentException e) {
          Map<String, Object> response = Map.of(
                  "acknowledged", false,
                  "error", "Missing or invalid ID"
            );
          return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
        }
        System.out.println("Received POST /auth/refresh with valid ID: " + parsedClientId.toString());
        String refreshToken = body.get("refreshToken");

        AuthUser authUser = authService.authenticate(refreshToken);
        if( !authUser.isOK() ) return new ResponseEntity<>(
            Map.of("error", authUser.getErrorMsg()), 
            HttpStatus.BAD_REQUEST); // 400  
        
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

        Map<String, Object> response = Map.of(
            "userId", userId,
            "isOnline", true,
            "accessToken", authUser.getAccessToken(),           
            "refreshToken", authUser.getRefreshToken(),
            "chats", userChats,
            "messages", messages
        );

        Map<String, Object> wsMessage = Map.of(
            "type", "userSessionUpdate",
            "status", "WsStatus.OK",
            "data", response
        );
        // Convert Map to JSON string
        String wsJson = mapper.writeValueAsString(wsMessage);
        // Broadcast via WebSocket
        webSocketHandler.broadcast(wsJson);

        return ResponseEntity.status(HttpStatus.OK).body(response);
      } 
      catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login( @RequestParam("id") String clientId, 
      @RequestBody Map<String, Object> body) {
      try {
        // Validate clientId
        UUID parsedClientId;
        try {
          parsedClientId = UUID.fromString(clientId);
        } 
        catch (IllegalArgumentException e) {
          Map<String, Object> response = Map.of(
                  "acknowledged", false,
                  "error", "Missing or invalid ID"
            );
          return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
        }
        System.out.println("Received POST /api/auth/login with valid ID: " + parsedClientId.toString());

        // Validate userId
        if (!body.containsKey("userId")) {
          Map<String, Object> response = Map.of(
              "acknowledged", false,
              "error", "Missing 'userId' field"
            );
          return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
        }
        String userId = body.get("userId").toString();

        // Extract password field
        if (!body.containsKey("password")) {
          Map<String, Object> response = Map.of(
              "acknowledged", false,
              "error", "Missing 'password' field"
            );
          return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
        }
        String password = body.get("password").toString();
        System.out.println("Password received for login: " + password);

        AuthUser authUser = authService.authenticate(userId, password);
        if( !authUser.isOK() ) return new ResponseEntity<>(
          Map.of("error", authUser.getErrorMsg()), 
          HttpStatus.BAD_REQUEST); // 400

        User user = authUser.getUser();

        // Set user online
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

        Map<String, Object> response = Map.of(
            "userId", userId,
            "isOnline", true,
            "accessToken", authUser.getAccessToken(),           
            "refreshToken", authUser.getRefreshToken(),
            "chats", userChats,
            "messages", messages
        );

        // var response = new { userId, isOnline = true };
        Map<String, Object> wsMessage = Map.of(
            "type", "userSessionUpdate",
            "status", "WsStatus.OK",
            "data", response
        );
        // Convert Map to JSON string
        String wsJson = mapper.writeValueAsString(wsMessage);
        // Broadcast via WebSocket
        webSocketHandler.broadcast(wsJson);

        return new ResponseEntity<>(response, HttpStatus.OK); // 200
      } 
      catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = Map.of( 
          "acknowledged", false, "error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500   
      }
    }


  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestParam("id") String clientId, @RequestBody Map<String, Object> body) {
    try {
      // Validate clientId
      System.out.println("Received POST /api/auth/logout ");
      UUID parsedClientId;
      try {
        parsedClientId = UUID.fromString(clientId);
      } catch (IllegalArgumentException e) {
        Map<String, Object> response = Map.of(
          "acknowledged", false,
          "error", "Missing or invalid ID"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
      }
      System.out.println("Received POST /api/auth/logout with valid ID: " + parsedClientId.toString());

      // Validate userId
      if (!body.containsKey("userId")) {
        Map<String, Object> response = Map.of(
          "acknowledged", false,
          "error", "Missing 'userId' field"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
      }
      String userId = body.get("userId").toString();

      // Find user
      Optional<User> optionalUser = userRepository.findById(userId);
      if (optionalUser.isEmpty()) {
        Map<String, Object> response = Map.of(
          "acknowledged", false,
          "error", "UserID Not found"
        );
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); // 204
      }
      User user = optionalUser.get();
      user.setOnline(false);
      userRepository.save(user);

      // Clear refresh token from DB
      System.out.println("Deleting refresh tokens for userId: " + userId);
      refreshTokenRepository.deleteByUserId(userId);
      System.out.println("Deleting done ");

      userMonitor.removeUser(userId);       

      Map<String, Object> response = Map.of(
        "userId", userId,
        "isOnline", false
      );

      Map<String, Object> wsMessage = Map.of(
        "type", "userSessionUpdate",
        "status", "WsStatus.OK",
        "data", response
      );
      // Convert Map to JSON string
      String wsJson = mapper.writeValueAsString(wsMessage);
      // Broadcast via WebSocket
      webSocketHandler.broadcast(wsJson);

      return new ResponseEntity<>(response, HttpStatus.OK); // 200
    } 
    catch (Exception e) {
      e.printStackTrace();
      Map<String, Object> errorResponse = Map.of( 
        "acknowledged", false, "error", e.getMessage());
      return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500   
    }
  }
}