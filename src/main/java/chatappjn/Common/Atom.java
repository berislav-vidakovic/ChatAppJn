package chatappjn.Common;

public class Atom {
  private final boolean isOK;
  private final String errorMessage;

  public Atom(String err) {
    this.isOK = false;
    this.errorMessage = err;
  }

  public Atom() {
    this.isOK = false;
    this.errorMessage = null;
  }

  public String getErrorMsg(){
    return this.errorMessage;
  }

  public boolean isOK(){
    return this.isOK;
  }
}
