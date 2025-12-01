package chatappjn.Models;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
public class Role {

    @Id
    private String id;

    private String role;            // e.g. "Admin", "Basic", "Prime"
    private List<String> claims;    // e.g. ["createChat","sendMessage"]

    public Role() {}

    public Role(String role, List<String> claims) {
        this.role = role;
        this.claims = claims;
    }

    public String getId() { return id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getClaims() { return claims; }
    public void setClaims(List<String> claims) { this.claims = claims; }
}
