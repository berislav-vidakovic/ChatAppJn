package chatappjn.Services;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class RequestChecker {
  public RequestChecker() {

  }

  public ResponseEntity<?> buildResponse (
      HttpStatusCode statusCode, String message){
    Map<String, Object> response = Map.of(
      "acknowledged", false,
      "error", message
    );
    return new ResponseEntity<>(response, statusCode); 
  }
  
}
