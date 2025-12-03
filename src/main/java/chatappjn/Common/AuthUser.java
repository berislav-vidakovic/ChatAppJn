package chatappjn.Common;

import chatappjn.Models.User;

public class AuthUser {
  private final String accessToken;
  private final String refreshToken;
  private final User user;
  private final boolean authSuccessful;
  private final String errorMessage;

  public AuthUser(String accessToken, String refreshToken, 
      User user) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.user = user;
    this.authSuccessful = true;
    this.errorMessage = "";
  }

  public AuthUser(String err) {
    this.accessToken = "";
    this.refreshToken = "";
    this.user = null;
    this.authSuccessful = false;
    this.errorMessage = err;
  }

  public String getErrorMsg(){
    return this.errorMessage;
  }

  public boolean isOK(){
    return this.authSuccessful;
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
