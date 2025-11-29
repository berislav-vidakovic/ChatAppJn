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
import chatappjn.Services.UserMonitor;
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
    private ObjectMapper mapper;

    public AuthController() {
    }

    private RefreshToken checkReceivedToken(String refreshToken) {
      if (refreshToken == null || refreshToken.isEmpty()) 
          return null; // Missing token
        
      Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
      if (tokenOpt.isEmpty()) 
          return null; // Token not found in DB

      RefreshToken token = tokenOpt.get();
      if (token.getExpiresAt().isBefore(Instant.now())) 
          return null; // Token expired

      return token; // Valid Token
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
        
        RefreshToken refToken = checkReceivedToken(refreshToken);
        if( refToken == null ) { // invalid or expired token
          return ResponseEntity
                  .status(HttpStatus.UNAUTHORIZED)
                  .body(Map.of("error", "Refresh token missing, invalid or expired"));
        }
        // valid token
        // 1- get user by userId from refreshToken and set isOnline = true
        var userOpt = userRepository.findById(refToken.getUserId());
        if (userOpt.isEmpty()) {
          return ResponseEntity
                  .status(HttpStatus.UNAUTHORIZED)
                  .body(Map.of("error", "User not found for the provided refresh token"));
        }
        User user = userOpt.get();
        String userId = user.getId();
        user.setOnline(true);
        userRepository.save(user);
        userMonitor.updateUserActivity(user.getId(), parsedClientId);

        // 2-renew refreshToken and Expiry date and save to MongoDB collection (update)
        refreshToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(14 * 24 * 60 * 60); // 14 days
        refToken.setToken(refreshToken);
        refToken.setExpiresAt(expiresAt);
        refreshTokenRepository.save(refToken);

        // 3 - renew accessToken
        String newAccessToken = JwtBuilder.generateToken(
          user.getId(), user.getLogin());

        // Return new tokens
        Map<String, Object> response = Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshToken,
                "userId", userId,
                "isOnline", true
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
    public ResponseEntity<?> login(@RequestParam("id") String clientId, @RequestBody Map<String, Object> body) {
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

      // Password validation 
      // if no hashed pwd in DB => new user, first time password hashing
      if( user.getPwd().isEmpty() ){
        // Hash the password using BCrypt
        String hashedPwd = passwordEncoder.encode(password);
        user.setPwd(hashedPwd);
        userRepository.save(user);
      }       
      else {
        boolean passwordsMatch = passwordEncoder.matches(password, user.getPwd());
        if( !passwordsMatch ) {
          Map<String, Object> response = Map.of(
                "acknowledged", false,
                "error", "Invalid password"
          );
          return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401
        }
      }
      
      // Issue access token
      String accessToken = JwtBuilder.generateToken(user.getId(), user.getLogin());

      // Issue and store refresh token 
      String refreshToken = java.util.UUID.randomUUID().toString();
      LocalDateTime expiryDate = LocalDateTime.now().plusDays(7); // valid for 7 days
      Instant expiryInstant = expiryDate.atZone(ZoneId.systemDefault()).toInstant();

      refreshTokenRepository.deleteByUserId(user.getId());
      RefreshToken tokenEntity = new RefreshToken();
      tokenEntity.setUserId(user.getId());
      tokenEntity.setToken(refreshToken);
      tokenEntity.setExpiresAt(expiryInstant);
      refreshTokenRepository.save(tokenEntity);

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
          "accessToken", accessToken,           
          "refreshToken", refreshToken,
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