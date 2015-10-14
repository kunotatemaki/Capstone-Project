package com.rukiasoft.androidapps.cocinaconroll.utilities;


public class Constants {
    public static final String PROPERTY_HIDE_SWIPE_DIALOG = "hide_swipe_dialog";
    public static final String SENDER_ID = "962580870211";
    public static final String PACKAGE_NAME = "com.rukiasoft.androidapps.cocinaconroll";

    public static final String PREFERENCE_INTERSTITIAL = PACKAGE_NAME + ".intertitial";
    public static final String RECIPES_DIR = "recipes";
    public static final String BASE_DIR = "CookingWihCookeo";
    public static final String ZIPS_DIR = "zips";
    public static final Integer FLAG_ORIGINAL = 2;
    public static final Integer FLAG_ASSETS = 1;
    public static final Integer FLAG_EDITED = 4;
    public static final Integer FLAG_EDITED_PICTURE = 8;
    public static final Integer FLAG_OWN = 16;

    public static final String TYPE_STARTERS = "starter";
    public static final String TYPE_MAIN = "main";
    public static final String TYPE_DESSERTS = "dessert";

    public static final String KEY_RECIPE = Constants.PACKAGE_NAME + "." + ".recipe";
    public static final String KEY_STARTED = Constants.PACKAGE_NAME + "." + ".started";
    //public static final String KEY_RELOAD = Constants.PACKAGE_NAME + "." + ".reload";
    public static final String KEY_DELETE_OLD_PICTURE = Constants.PACKAGE_NAME + "." + ".deleteoldpicture";
    public static final String KEY_DELETE_OLD_RECIPE = Constants.PACKAGE_NAME + "." + ".deleteoldrecipe";
    public static final String KEY_TYPE = Constants.PACKAGE_NAME + "." + ".type";
    public static final int REQUEST_DETAILS = 200;
    public static final int REQUEST_CREATE_RECIPE = 201;
    public static final int REQUEST_EDIT_RECIPE = 202;
    public static final int RESULT_UPDATE_RECIPE = 300;
    public static final int RESULT_DELETE_RECIPE = 301;

    public static final String PROPERTY_NUMBER_STARTERS = "numberstarters";
    public static final String PROPERTY_NUMBER_MAIN = "numbermains";
    public static final String PROPERTY_NUMBER_DESSERTS = "numberdesserts";
    public static final String PROPERTY_INIT_DATABASE = "initdatabase";
    public static final String PROPERTY_RELOAD_NEW_ORIGINALS = "reloadneworiginals";

    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd_HH-mm-ss";
    public static final String DEFAULT_PICTURE_NAME = "default_picture";
    public static final String ASSETS_PATH = "file:///android_asset/";
    public static final String FILE_PATH = "file://";


    public static final String TEMP_CAMERA_NAME = "tmp_avatar_";
    public static final String START_DOWNLOAD_ACTION_INTENT = "com.rukiasoft.androidapps.cocinaconroll.action.START_DOWNLOAD";

    public static final Integer STATE_NOT_DOWNLOADED = 0;
    public static final Integer STATE_DOWNLOADED_NOT_UNZIPED = 1;
    public static final Integer STATE_DOWNLOADED_UNZIPED_NOT_ERASED = 2;
    public static final Integer STATE_DOWNLOADED_UNZIPED_ERASED = 3;

    public static final Integer PATH_TYPE_ASSETS = 0;
    public static final Integer PATH_TYPE_ORIGINAL = 1;
    public static final Integer PATH_TYPE_EDITED = 2;

    public static final Integer TIMEFRAME_NEW_RECIPE_MILISECONDS_DAY = 1000 * 3600 * 24;
    public static final Integer TIMEFRAME_NEW_RECIPE_DAYS = 7;

    public static final String FILTER_ALL_RECIPES = PACKAGE_NAME + ".allrecipes";
    public static final String FILTER_STARTER_RECIPES = PACKAGE_NAME + ".starters";
    public static final String FILTER_MAIN_COURSES_RECIPES = PACKAGE_NAME + ".maincourses";
    public static final String FILTER_DESSERT_RECIPES = PACKAGE_NAME + ".desserts";
    public static final String FILTER_VEGETARIAN_RECIPES = PACKAGE_NAME + ".vegetarians";
    public static final String FILTER_FAVOURITE_RECIPES = PACKAGE_NAME + ".favourites";
    public static final String FILTER_OWN_RECIPES = PACKAGE_NAME + ".ownrecipes";
    public static final String FILTER_LATEST_RECIPES = PACKAGE_NAME + ".latest";

    public static final int LOADER_ID = 1;
    public static final int N_RECIPES_TO_INTERSTICIAL = 5;


    public static final String EMAIL = "rukiasoft@gmail.com";

}