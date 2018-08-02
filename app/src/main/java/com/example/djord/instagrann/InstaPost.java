package com.example.djord.instagrann;


import java.sql.Timestamp;
import java.util.Date;

public class InstaPost extends InstaPostId{
    public String user_id, desc, image_url, image_thumb;
    public Date timestamp;

    public InstaPost() {
    }

    public InstaPost(String user_id, String desc, String image_url, String image_thumb, Date timestamp) {
        this.user_id = user_id;
        this.desc = desc;
        this.image_url = image_url;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }
}
