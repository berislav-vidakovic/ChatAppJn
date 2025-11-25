package chatappjn.Models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "refreshTokens")
public class RefreshToken {

    @Id
    private String id;           // MongoDB ObjectId

    @Field("userid")
    private String userId;       // store user ID (from Users collection)

    @Field("token")
    private String token;        // refresh token string
    
    @Field("expires")
    private Instant expiresAt;   // expiration timestamp

    // Constructors
    public RefreshToken() {}

    public RefreshToken(String userId, String token, Instant expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
