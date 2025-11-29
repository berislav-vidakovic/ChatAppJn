package chatappjn.Models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;


@Document(collection = "messages") // maps to "messages" collection
public class Message {

    @Id
    private String id; // MongoDB ObjectId as String

    @Field("chatId")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId chatId; // ObjectId of the chat

    @Field("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId userId; // ObjectId of the user who sent the message

    @Field("datetime")
    private Date datetime; // message timestamp

    @Field("text")
    private String text; // message content

    // Constructors
    public Message() {}

    public Message(ObjectId chatId, ObjectId userId, Date datetime, String text) {
        this.chatId = chatId;
        this.userId = userId;
        this.datetime = datetime;
        this.text = text;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId getChatId() {
        return chatId;
    }

    public void setChatId(ObjectId chatId) {
        this.chatId = chatId;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getText() {
        return text;
    }
  }
