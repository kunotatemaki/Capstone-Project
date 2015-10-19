package com.rukiasoft.androidapps.cocinaconroll.classes;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Raúl Feliz Alonso on 21/09/2015 for the Udacity Nanodegree.
 */
@Root(name="elementList")
public class PreinstalledRecipeNamesList {

    @ElementList(required=true, inline=true)
    private List<PreinstalledRecipeName> mPreinstalledRecipeNames = new ArrayList<>();

    public List<String> getPreinstalledRecipeNameListAsListOfStrings(){
        List<String> list = new ArrayList<>();
        for(PreinstalledRecipeName recipeName : mPreinstalledRecipeNames){
            list.add(recipeName.getName());
        }
        return list;
    }
}


@Root(name="item")
class PreinstalledRecipeName
{
    public String getName() {
        return name;
    }

    @Attribute(name="name", required=true)
    private String name;


    public PreinstalledRecipeName(String name)
    {
        this.name = name;
    }


    public PreinstalledRecipeName() { }


    // Getter / Setter
}