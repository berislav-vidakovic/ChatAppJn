package chatappjn.Services;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import chatappjn.Common.Credentials;

public class MiddlewareCommon {
  public MiddlewareCommon() {

  }

  public ResponseEntity<?> buildResponse (
      HttpStatusCode statusCode, String message){
    Map<String, Object> response = Map.of(
      "acknowledged", false,
      "error", message
    );
    return new ResponseEntity<>(response, statusCode); 
  }

  public Credentials parseCredentials(Map<String, Object> body){
    // Validate userId
    /*
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
    return new Credentials(userId, password);*/
    return new Credentials("","");
  }
}
