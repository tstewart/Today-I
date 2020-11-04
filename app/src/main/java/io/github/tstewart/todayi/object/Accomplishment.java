package io.github.tstewart.todayi.object;

import java.util.Date;

//TODO Rename package?
public class Accomplishment {

    Date postedOn;
    String content;

    public Accomplishment(Date postedOn, String content) {
        this.postedOn = postedOn;
        this.content = content;
    }

    public Accomplishment(String content) {
        this(new Date(), content);
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
