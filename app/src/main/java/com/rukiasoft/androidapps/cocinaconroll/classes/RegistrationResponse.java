package com.rukiasoft.androidapps.cocinaconroll.classes;

/**
 * Created by iRuler on 20/10/15.
 */
public class RegistrationResponse {
    private Integer error;

    public RegistrationResponse(){
        error = -1;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }
}
