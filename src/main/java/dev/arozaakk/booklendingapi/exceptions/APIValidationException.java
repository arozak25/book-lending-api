package dev.arozaakk.booklendingapi.exceptions;

public class APIValidationException extends RuntimeException {
  private final String key;
  private final Object[] args;

  public APIValidationException(String key, Object... args) {
    super(key);
    this.key = key;
    this.args = args == null ? new Object[0] : args.clone();
  }

  public String getKey() {
    return key;
  }

  public Object[] getArgs() {
    return args.clone();
  }
}
