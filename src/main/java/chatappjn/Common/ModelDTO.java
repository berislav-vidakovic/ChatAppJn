package chatappjn.Common;

import java.util.List;

import chatappjn.Models.Chat;
import chatappjn.Models.Message;

public class ModelDTO extends Atom {
  private final String userId; 
  private final List<Chat> userChats;
  private final List<Message> messages;

  public ModelDTO(String userId, 
    List<Chat> userChats, List<Message> messages
  ) {
    super();
    this.userId = userId;
    this.userChats = userChats;
    this.messages = messages;
  }

  public ModelDTO(String err) {
    super(err);
    this.userId = null;
    this.userChats = null;
    this.messages = null;
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
