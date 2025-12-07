package chatappjn.Common;

import java.util.List;

import chatappjn.Models.Chat;
import chatappjn.Models.Message;

public class ModelDTO {
  private final boolean isOK;
  private final String errorMessage;
  private final String userId; 
  private final List<Chat> userChats;
  private final List<Message> messages;

  public ModelDTO(String userId, 
    List<Chat> userChats, List<Message> messages
  ) {
    this.userId = userId;
    this.userChats = userChats;
    this.messages = messages;
    this.isOK = true;
    this.errorMessage = "";
  }

  public ModelDTO(String err) {
    this.userId = null;
    this.userChats = null;
    this.messages = null;
    this.isOK = false;
    this.errorMessage = err;
  }

  public String getErrorMsg(){
    return this.errorMessage;
  }

  public boolean isOK(){
    return this.isOK;
  }

  public String  getUserId(){
    return this.userId;
  }

  public List<Chat> getUserChats(){
    return this.userChats;
  } 
  
  public List<Message> getMessages(){
    return this.messages;
  }
}
