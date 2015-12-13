# Capstone-Project
Final project for the Udacity's [Android Developer Nanodegree](https://www.udacity.com/course/android-developer-nanodegree--nd801).

Cuissine application for the *Moulinex Cuissine Robot* **_Cookeo_**.

### Notifications
The app uses _Google Cloud Messaging_ to receive new recipes from server. To test this feature, choose one of the following options:

- #### Use the provide with the project:
    **recipesserver** is an _App Engine Backend with Goolge cloud Messaging_ module. Folow the [instructions](https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints) to create a project in your _developers console_ and either run the server locally to test notifications in a real device or in the emulator, or deploy that module to the App Engine.
All the code you need to add or change is in the **ResgistrationIntentService.class**, in _com.rukiasoft.androidapps.cocinaconroll.gcm_ package.

    ##### Run locally in a emulator:
    uncomment the following code in **sendRegistrationToServer function**, in *ResgistrationIntentService.class*
    ```
    if (regService == null) {
            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                    // otherwise they can be skipped
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
        regService = builder.build();
    }
    ```
    ##### Deploy to App Engine and test in real devices:
    uncomment the following code in **sendRegistrationToServer function**, in *ResgistrationIntentService.class*
    ```
    if (regService == null) {
            //Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
            //        .setRootUrl("https://your-project-name/_ah/api/");
            regService = builder.build();
        }
    ```
- #### Use your own server:
    In **strings.xml** replace:
    ```
    <string name="server_url" translatable="false">http://comunioelpuntal.no-ip.biz:8080/cukio-server/rest/cukio_server_non_secure/"</string>
    <string name="registration_method" translatable="false">register_device/"</string>
    ```
    by your own url and method name.
### Other settings:
This app uses Google Admob, Google Drive and Goole Analitycs. You need to configure theese services in your developer console and change ids provided in **strings.xml**-
```
<string name="banner_ad_unit_id_list" translatable="false">your own key</string>
    <string name="banner_ad_unit_id_details" translatable="false">your own key</string>
    <string name="banner_ad_unit_id_intersticial" translatable="false">your own key</string>
```
**track_app.xml**
```
<string name="ga_trackingId">your own key</string>
```
    
    
