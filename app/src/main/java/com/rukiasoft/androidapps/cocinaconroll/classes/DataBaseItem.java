package com.rukiasoft.androidapps.cocinaconroll.classes;

import java.io.Serializable;

/**
 * Created by Ruler on 2014.
 */

public class DataBaseItem implements Serializable {


    private Integer Id;
    private String name;
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
