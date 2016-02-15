package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;


@Root
public class RecipeItem implements Parcelable {

    private Integer _id = -1;
    @Element
    private String name = "";
    @Element
    private String type = "";
    private Integer icon = -1;
    @Element
    private String picture = Constants.DEFAULT_PICTURE_NAME;
    @ElementList
    private List<String> ingredients = new ArrayList<>();
    @ElementList
    private List<String> steps = new ArrayList<>();
    @Element (required=false)
    private String author = "";
    @Element
    private Boolean vegetarian = false;
    @Element  (required=false)
    private Boolean favourite = false;
    @Element  (required=false)
    private Integer state = 0;
    @Element
    private Integer portions = 0;
    @Element
    private Integer minutes = 0;
    @Element  (required=false)
    private String tip = "";
    private String pathRecipe = "";
    private String pathPicture = Constants.ASSETS_PATH + Constants.DEFAULT_PICTURE_NAME;
    @Element  (required=false)
    private Long date = -1L;
    @Element
    private String language = "Spanish";
    private Integer version = 0;


    public RecipeItem(Parcel in){
        this._id = in.readInt();
        this.name = in.readString();
        this.type = in.readString();
        this.icon = in.readInt();
        this.picture = in.readString();
        this.ingredients = new ArrayList<>();
        in.readStringList(ingredients);
        this.steps = new ArrayList<>();
        in.readStringList(steps);
        this.author = in.readString();
        this.vegetarian = in.readByte() != 0;
        this.favourite = in.readByte() != 0;
        this.state = in.readInt();
        this.portions = in.readInt();
        this.minutes = in.readInt();
        this.tip = in.readString();
        this.pathRecipe = in.readString();
        this.pathPicture = in.readString();
        this.language = in.readString();
        this.date = in.readLong();
        this.version = in.readInt();


    }

    public RecipeItem() {

        ingredients = new ArrayList<>();
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Boolean getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(Boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = this.state | state;
    }
    public void removeState(Integer state) {
        this.state = this.state ^ state;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getPathRecipe() {
        return pathRecipe;
    }

    public void setPathRecipe(String pathRecipe) {
        this.pathRecipe = pathRecipe;
    }

    public String getPathPicture() {
        return pathPicture;
    }

    public void setPathPicture(String pathPicture) {
        this.pathPicture = pathPicture;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(get_id());
        dest.writeString(getName());
        dest.writeString(getType());
        dest.writeInt(getIcon());
        dest.writeString(getPicture());
        dest.writeStringList(getIngredients());
        dest.writeStringList(getSteps());
        dest.writeString(getAuthor());
        dest.writeByte((byte) (getVegetarian() ? 1 : 0));
        dest.writeByte((byte) (getFavourite() ? 1 : 0));
        dest.writeInt(getState());
        dest.writeInt(getPortions());
        dest.writeInt(getMinutes());
        dest.writeString(getTip());
        dest.writeString(getPathRecipe());
        dest.writeString(getPathPicture());
        dest.writeString(getLanguage());
        if(getDate() != null)
            dest.writeLong(getDate());
        else
            dest.writeLong(-1);
        dest.writeInt(getVersion());
    }

    public static final Parcelable.Creator<RecipeItem> CREATOR = new Parcelable.Creator<RecipeItem>() {
        public RecipeItem createFromParcel(Parcel in) {
            return new RecipeItem(in);
        }

        public RecipeItem[] newArray(int size) {
            return new RecipeItem[size];
        }
    };

}