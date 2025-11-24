package chatappjn.Controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import chatappjn.WebSockets.WebSocketHandler;

// POST /api/auth/refresh
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper mapper;

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
        if (refreshToken == null) refreshToken = "dummyRefreshToken";

        // Return new tokens
        Map<String, Object> response = Map.of(
                "accessToken", "dummyAccessToken",
                "refreshToken", refreshToken
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

        return ResponseEntity.ok(response);
      } 
      catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
