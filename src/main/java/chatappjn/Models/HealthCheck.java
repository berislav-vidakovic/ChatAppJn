package chatappjn.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "healthcheck")
public class HealthCheck {

    @Id
    private String id;      // MongoDB _id

    private String pingdb;  // matches the key in the document

    // Constructors
    public HealthCheck() {}

    public HealthCheck(String pingdb) {
        this.pingdb = pingdb;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPingdb() {
        return pingdb;
    }

    public void setPingdb(String pingdb) {
        this.pingdb = pingdb;
    }
}
