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
    if (!body.containsKey("userId")) 
      return new Credentials("Missing [userId] field");    
    String userId = body.get("userId").toString();

    if (!body.containsKey("password")) 
      return new Credentials("Missing [password] field");
    
    return new Credentials(userId, true);
  }

  
  public String parseRefreshToken(Map<String, String> body){
    if (!body.containsKey("refreshToken")) 
      return null;   

    String refreshToken = body.get("refreshToken");
    if( refreshToken == null )
      return null;

    return refreshToken.toString();
  }
}
