package chatappjn.Controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties.Credential;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;


import com.fasterxml.jackson.databind.ObjectMapper;

import chatappjn.Common.AuthUser;
import chatappjn.Common.Credentials;
import chatappjn.Common.ModelDTO;
import chatappjn.Models.Chat;
import chatappjn.Models.Message;
import chatappjn.Models.User;
import chatappjn.Repositories.RefreshTokenRepository;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.ChatRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Services.Authentication;
import chatappjn.Services.ClientIdChecker;
import chatappjn.Services.ModelService;
import chatappjn.Services.UserMonitor;
import chatappjn.Services.WebSocketService;

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
    private WebSocketService webSocketService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Authentication authService;

    @Autowired
    private ClientIdChecker clientIdChecker;


    public AuthController() {
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam("id") String clientId, @RequestBody Map<String, String> body) {
      try {        
        UUID parsedClientId = clientIdChecker.parseClientId(clientId);
        if( parsedClientId == null )
          return clientIdChecker.buildResponse(
            HttpStatus.BAD_REQUEST, "Missing or invalid clientId");

        String refreshToken = authService.parseRefreshToken(body);
        AuthUser authUser = authService.authenticate(refreshToken);
        if( !authUser.isOK() ) 
          return authService.buildResponse(
            HttpStatus.BAD_REQUEST, authUser.getErrorMsg());

        ModelDTO model = modelService.getModel(authUser, parsedClientId);
        if( !model.isOK() )
          return modelService.buildResponse(
            HttpStatus.BAD_REQUEST, model.getErrorMsg());

        Map<String, Object> response = Map.of(
            "userId", model.getUserId(),
            "isOnline", true,
            "accessToken", authUser.getAccessToken(),           
            "refreshToken", authUser.getRefreshToken(),
            "chats", model.getUserChats(),
            "messages", model.getMessages()
        );

        webSocketService.broadcastMessage("userSessionUpdate",
           "WsStatus.OK", response);

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
        UUID parsedClientId = clientIdChecker.parseClientId(clientId);
        if( parsedClientId == null )
          return clientIdChecker.buildResponse(
            HttpStatus.BAD_REQUEST, "Missing or invalid clientId");

        Credentials c = authService.parseCredentials(body);
        if( !c.isOK() )
            return authService.buildResponse(
              HttpStatus.BAD_REQUEST, c.getErrorMsg());

        AuthUser authUser = authService.authenticate(c.getuserId(), c.getPassword());
        if( !authUser.isOK() ) 
          return authService.buildResponse(
            HttpStatus.BAD_REQUEST, authUser.getErrorMsg());

        ModelDTO model = modelService.getModel(authUser, parsedClientId);
        if( !model.isOK() )
          return modelService.buildResponse(
            HttpStatus.BAD_REQUEST, model.getErrorMsg());
        /*
        User user = authUser.getUser();

        // Set user online
        user.setOnline(true);
        userRepository.save(user);
        userMonitor.updateUserActivity(user.getId(), parsedClientId);

        // Fetch chats where user participates
        ObjectId userObjectId = new ObjectId(c.getuserId()); 
        List<Chat> userChats = chatRepository.findByUserIdsContaining(userObjectId);

        // Fetch all messages from those chats
        List<ObjectId> chatIds = userChats.stream()
                .map(chat -> new ObjectId(chat.getId()))
                .toList();

        List<Message> messages = messageRepository.findByChatIdInOrderByDatetimeAsc(chatIds);
            */
        Map<String, Object> response = Map.of(
            "userId", model.getUserId(),
            "isOnline", true,
            "accessToken", authUser.getAccessToken(),           
            "refreshToken", authUser.getRefreshToken(),
            "chats", model.getUserChats(),
            "messages", model.getMessages()
        );

        webSocketService.broadcastMessage("userSessionUpdate",
        "WsStatus.OK", response);

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
      UUID parsedClientId = clientIdChecker.parseClientId(clientId);
      if( parsedClientId == null )
        return clientIdChecker.buildResponse(
          HttpStatus.BAD_REQUEST, "Missing or invalid clientId");

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

      webSocketService.broadcastMessage("userSessionUpdate",
        "WsStatus.OK", response);

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