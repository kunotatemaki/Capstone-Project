package com.rukiasoft.androidapps.cocinaconroll.classes;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Raúl Feliz Alonso on 2014.
 */

public class ZipItem implements Serializable {


    private Integer Id;
    @Expose @SerializedName("name")
    private String name;
    @Expose @SerializedName("link")
    private String link;
    private Integer state;

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
