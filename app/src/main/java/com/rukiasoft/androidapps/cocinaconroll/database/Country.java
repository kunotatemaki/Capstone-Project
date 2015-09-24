package com.rukiasoft.androidapps.cocinaconroll.database;

import com.rukiasoft.androidapps.cocinaconroll.R;

/** Country details are stored in this class and is used to populate the table countries
	 * in CountryDb.java
	 */
	public class Country {
		 // Array of strings storing country names
	    static String[] countries = new String[] {
	            "India",
	            "Pakistan",
	            "Sri Lanka",
	            "China",
	            "Bangladesh",
	            "Nepal",
	            "Afghanistan",
	            "North Korea",
	            "South Korea",
	            "Japan"
	    };
	    
	    // Array of integers points to images stored in /res/drawable
	    static int[] flags = new int[]{
	                R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24,
				R.drawable.ic_all_24

	    };
	    
	    // Array of strings to store currencies
	    static String[] currency = new String[]{
	        "Indian Rupee",
	        "Pakistani Rupee",
	        "Sri Lankan Rupee",
	        "Renminbi",
	        "Bangladeshi Taka",
	        "Nepalese Rupee",
	        "Afghani",
	        "North Korean Won",
	        "South Korean Won",
	        "Japanese Yen"
	    };
}