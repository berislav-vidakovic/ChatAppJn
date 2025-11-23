package chatappjn.Models;

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

    @Field("full_name")
    private String fullName;

    @Field("isonline")
    private boolean isOnline = false; // default false

    // Constructors
    public User() {}

    public User(String login, String fullName, boolean isOnline) {
        this.login = login;
        this.fullName = fullName;
        this.isOnline = isOnline;
    }

    // Getters and Setters
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
