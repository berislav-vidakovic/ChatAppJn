package chatappjn.Common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class Endpoints {

    public Endpoints() {
      this.endpointsPublic.addAll( List.of(
        "/api/ping",
        "/api/pingdb",
        "/api/users/register",
        "/api/users/all",
        "/api/auth/refresh",
        "/api/auth/login",
        "/api/auth/logout",
        "/websocket"));
      this.endpointsProtected.addAll(List.of(
        "/api/chat/new",
        "/api/users/roles"));
    } 

    private List<String> endpointsPublic = new ArrayList<>();
    private List<String> endpointsProtected = new ArrayList<>();

    public List<String> getPublicEndpoints(){
      return endpointsPublic;
    }
}
