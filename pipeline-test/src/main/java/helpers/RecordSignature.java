package helpers;

import org.json.simple.JSONObject;

public class RecordSignature {

  public String subscriber;
  public String project;
  public String session;

  public RecordSignature(JSONObject jsonObject) {

      this.subscriber = jsonObject.get("SubscriberId").toString();
      this.session = jsonObject.get("SID").toString();
      this.project = jsonObject.get("PID").toString();

  }

  public String getKey() {
    StringBuilder buf = new StringBuilder();
    return buf.append(subscriber)
        .append(".").append(project)
        .append(".").append(session).toString();
  }

  public String toString() {
    return getKey();
  }
}
