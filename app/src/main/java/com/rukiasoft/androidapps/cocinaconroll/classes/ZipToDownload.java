package com.rukiasoft.androidapps.cocinaconroll.classes;

import java.io.Serializable;

// Do not modify 

public class ZipToDownload implements Serializable {
    private static final long serialVersionUID = 1L;

	private String name = "";
    private String link = "";
    
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
	
}

//TODO eliminar y utilizar zipdatabaseitem