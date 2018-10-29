package helpers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tag {

    @SerializedName("TagName")
    @Expose
    private String tagName;
    @SerializedName("TagTypeID")
    @Expose
    private int tagTypeID;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Tag withTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public int getTagTypeID() {
        return tagTypeID;
    }

    public void setTagTypeID(int tagTypeID) {
        this.tagTypeID = tagTypeID;
    }

    public Tag withTagTypeID(int tagTypeID) {
        this.tagTypeID = tagTypeID;
        return this;
    }

}
