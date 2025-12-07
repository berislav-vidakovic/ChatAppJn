package chatappjn.Common;

public class Atom {
  protected boolean isOK;
  protected String errorMessage;

  public Atom(String err) {
    this.isOK = false;
    this.errorMessage = err;
  }

  public Atom() {
    this.isOK = true;
    this.errorMessage = null;
  }

  public String getErrorMsg(){
    return this.errorMessage;
  }

  public boolean isOK(){
    return this.isOK;
  }
}
