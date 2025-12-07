package chatappjn.Common;

import chatappjn.Models.User;

public class AuthUser extends Atom{
  private final String accessToken;
  private final String refreshToken;
  private final User user;

  public AuthUser(String accessToken, String refreshToken, 
      User user) {
    super();
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.user = user;
  }

  public AuthUser(String err) {
    super(err);
    this.accessToken = "";
    this.refreshToken = "";
    this.user = null;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public User getUser() {
    return user;
  }
}
