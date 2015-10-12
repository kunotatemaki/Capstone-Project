package com.rukiasoft.androidapps.cocinaconroll.database;


/**
 * Created by Raúl Feliz Alonso on 6/10/15.
 */
public class RecipeDatabaseItem {
    private Integer _id;
    private String name;
    private String normalized;
    private Integer icon;
    private Integer favorite;
    private String pathPicture;
    private String pathRecipe;
    private Integer vegetarian;
    private Integer own;

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
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

    public String getPathPicture() {
        return pathPicture;
    }

    public void setPathPicture(String pathPicture) {
        this.pathPicture = pathPicture;
    }

    public String getPathRecipe() {
        return pathRecipe;
    }

    public void setPathRecipe(String pathRecipe) {
        this.pathRecipe = pathRecipe;
    }

    public Integer getFavorite() {
        return favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public Integer getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(Integer vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Integer getOwn() {
        return own;
    }

    public void setOwn(Integer own) {
        this.own = own;
    }
}
