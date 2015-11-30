package com.rukiasoft.androidapps.cocinaconroll.classes;

import android.content.Context;
import android.provider.Settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Raúl Feliz Alonso on 20/10/15.
 */
public class RegistrationClass {
    @Expose @SerializedName("date")
    private String date;
    @Expose @SerializedName("gcm_regid")
    private String gcm_regid;
    @Expose @SerializedName("unique_id")
    private String unique_id;
    @Expose @SerializedName("version")
    private Integer version;
    @Expose @SerializedName("email")
    private String email;
    @Expose @SerializedName("name")
    private String name;

    public RegistrationClass(Context context){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.FORMAT_DATE_TIME,
                context.getResources().getConfiguration().locale);
        date = df.format(c.getTime());

        unique_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void setGcm_regid(String gcm_regid) {
        this.gcm_regid = gcm_regid;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }
}
