package chatappjn.Services;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class ClientIdChecker extends MiddlewareCommon {
  public UUID parseClientId (String clientId){
    UUID parsedClientId = null;
    try {
      parsedClientId = UUID.fromString(clientId);
    } 
    catch (IllegalArgumentException e) {
      return null;
    }
    System.out.println("======================= clientId OK");
    return parsedClientId;
  }

}