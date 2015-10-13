package com.rukiasoft.androidapps.cocinaconroll.classes;


import java.io.Serializable;

public class RecipesListNameComparator implements java.util.Comparator<RecipeItem>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(RecipeItem p1, RecipeItem p2) {
        return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
    }
}
