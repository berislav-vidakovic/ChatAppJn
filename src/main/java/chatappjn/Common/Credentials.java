package chatappjn.Common;

public class Credentials extends Atom{
  private String userId;

  public Credentials( String userId, boolean isPasswordPresent ){
    super();
    if( isPasswordPresent )
      this.userId = userId;
    else {
      this.isOK = false;
      this.errorMessage = "Missing password";
    }
  }

  public Credentials( String err ){
    super(err);
  }

  public String getuserId(){
    return this.userId;
  }

}
