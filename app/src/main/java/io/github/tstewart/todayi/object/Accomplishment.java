package io.github.tstewart.todayi.object;

import java.util.Date;

//TODO Rename package?
public class Accomplishment {

    //ID of the post as it appears in the DB. if this is not provided then the post cannot be edited
    int id;
    Date postedOn;
    String content;

    public Accomplishment(int id, Date postedOn, String content) {
        this.id = id;
        this.postedOn = postedOn;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(Date postedOn) {
        this.postedOn = postedOn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
