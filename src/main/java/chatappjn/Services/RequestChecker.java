package chatappjn.Services;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class RequestChecker {
  public RequestChecker() {

  }

  public ResponseEntity<?> buildResponse (HttpStatusCode statusCode){
    Map<String, Object> response = Map.of(
      "acknowledged", false,
      "error", "Missing or invalid ID"
    );
    return new ResponseEntity<>(response, statusCode); 
  }
  
}
