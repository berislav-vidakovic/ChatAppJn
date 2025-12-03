package chatappjn.Common;

import java.util.HashSet;

import chatappjn.Models.User;

public class UserDTO {
  
  private User user;
  private HashSet<String> claims;

  public UserDTO() {}
  
  public UserDTO(User user, HashSet<String> claims){
    this.user = user;
    this.claims = claims;
  }

  public User getUser() {
    return user;
  }

  public HashSet<String> getClaims() {
    return claims;
  }
}
