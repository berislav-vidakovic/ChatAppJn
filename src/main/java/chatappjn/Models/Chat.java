package chatappjn.Models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "chats") // maps to "chats" collection
public class Chat {

    @Id
    private ObjectId id; // MongoDB ObjectId 

    @Field("userIds")
    private List<ObjectId> userIds; // List of user ObjectIds

    @Field("chatName")
    private String chatName; // human-readable chat name

    // Constructors
    public Chat() {}

    public Chat(List<ObjectId> userIds, String chatName) {
        this.userIds = userIds;
        this.chatName = chatName;
    }

    // Getters and Setters
    //public ObjectId getId() {
      //  return id;
    //}

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<ObjectId> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<ObjectId> userIds) {
        this.userIds = userIds;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
