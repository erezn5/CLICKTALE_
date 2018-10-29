package helpers;

public class RecordsToCompare {

  public RecordSignature previous;

  public RecordSignature current;

  public RecordsToCompare(RecordSignature previous, RecordSignature current) {

    this.previous = previous;
    this.current = current;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    return buf.append("RecordsToCompare: ")
        .append((previous != null) ? previous.getKey() : "null")
        .append(" vs. ")
        .append((current != null) ? current.getKey() : "null")
        .toString();
  }
}
