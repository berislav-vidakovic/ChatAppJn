package chatappjn.Models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "users") // maps to "users" collection
public class User {

    @Id
    private String id; // MongoDB generates this automatically
    
    @Field("login")
    private String login;

    @Field("password")
    private String password;

    @Field("fullName")
    private String fullName;

    @Field("isonline")
    private boolean isOnline = false; // default false

    @Field("roles")
    private List<String> roles; 

    // Constructors
    public User() {}

    public User(String login, String fullName, boolean isOnline, List<String> roles) {
        this.login = login;
        this.fullName = fullName;
        this.isOnline = isOnline;
        this.roles = roles;
    }

    // Getters and Setters
    public List<String> getRoles() {
      return roles;                  
    }

    public void setRoles(List<String> roles) {
      this.roles = roles;            
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPwd() {
        return password;
    }

    public void setPwd(String password) { 
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonProperty("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @JsonProperty("isOnline")
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
