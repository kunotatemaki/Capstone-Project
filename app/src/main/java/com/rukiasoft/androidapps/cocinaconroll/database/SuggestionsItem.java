package com.rukiasoft.androidapps.cocinaconroll.database;

/**
 * Created by Raúl Feliz Alonso on 6/10/15.
 */
public class SuggestionsItem {
    Integer _id;
    String name;
    String normalized;
    Integer icon;
    boolean favorite;

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalized() {
        return normalized;
    }

    public void setNormalized(String normalized) {
        this.normalized = normalized;
    }
}
