package chatappjn.Common;

public class Credentials extends Atom{
  private String userId;
  private String password;

  public Credentials( String userId, String password ){
    this.userId = userId;
    this.password = password;
  }

  public Credentials( String err ){
    super(err);
  }

  public String getuserId(){
    return this.userId;
  }

  public String getPassword(){
    return this.password;
  }
  
}
