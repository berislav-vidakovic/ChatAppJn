package chatappjn.Controllers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import chatappjn.Models.RefreshToken;
import chatappjn.Models.User;
import chatappjn.Repositories.RefreshTokenRepository;
import chatappjn.Repositories.UserRepository;
import chatappjn.WebSockets.WebSocketHandler;

// POST /api/auth/refresh
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper mapper;

    public AuthController() {
    }

    private RefreshToken checkReceivedToken(String refreshToken) {
      if (refreshToken == null || refreshToken.isEmpty()) {
          return null; // Treat missing token as "created/new" status
      }

      var tokenOpt = refreshTokenRepository.findByToken(refreshToken);

      if (tokenOpt.isEmpty()) {
          return null; // token does not exist
      }

      RefreshToken token = tokenOpt.get();
      if (token.getExpiresAt().isBefore(Instant.now())) {
          return null; // token expired
      }

      return token; // token exists and is valid
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

        // 2-renew refreshToken and Expiry date and save to MongoDB collection (update)
        refreshToken = "newShellyRefreshToken";
        Instant expiresAt = Instant.now().plusSeconds(14 * 24 * 60 * 60); // 14 days
        refToken.setToken(refreshToken);
        refToken.setExpiresAt(expiresAt);
        refreshTokenRepository.save(refToken);

        // Return new tokens
        Map<String, Object> response = Map.of(
                "accessToken", "dummyAccessToken",
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
}
