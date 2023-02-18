package kr.megaptera.assignment;

public enum HttpStatusType {
  OK(200, "OK"),
  CREATED(201, "Created"),
  BAD_REQUEST(400, "Bad Request"),
  NOT_FOUND(404, "Not Found"),
  INTERNAL_SERVER_ERROR(500, "Internal Server Error");

  private final int value;

  private final String reasonPhrase;


  HttpStatusType(int value, String reasonPhrase) {
    this.value = value;
    this.reasonPhrase = reasonPhrase;
  }

  public int value() {
    return this.value;
  }
  public String getReasonPhrase() {
    return this.reasonPhrase;
  }
}
