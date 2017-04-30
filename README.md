# Streaming sensor information into Google BigQuery from an Android device #

This sample Android app shows the [gcloud-java library for BigQuery](https://github.com/GoogleCloudPlatform/gcloud-java#google-cloud-bigquery-alpha) (alpha) working on an Android device.
The app streams data coming from the Android magnetic sensor (X, Y, Z) into an existing table/dataset in BigQuery.


## Configuration

1) Android Studio 2.1

2) Android plugin for gradle


## Authentication

I use a service account of my cloud project. Replace with yours by downloading the corresponding json file
into assets/ file. You can use alternative authentication mechanisms, like OAuth2,  if desired.

Remember to enable BigQuery API on your project. You also need to have a dataset and a table (see code).


## Dependencies
It is required to exclude some files on the **packagingOptions** block on **build.gradle**.

```groovy  
     packagingOptions {
         exclude 'META-INF/INDEX.LIST'
     }

```

The following dependency needs to be added for proper compilation and deployment. It must be noted that the google-api-client-appengine artifact (a dependency of gcloud-java-core) does not work on Android (and is not needed). 
So, it's mandatory to exclude that dependency when adding **gcloud-java-bigquery** as follows:

```groovy  
   compile ('com.google.cloud:gcloud-java-bigquery:0.2.1') {
       exclude group: 'com.google.api-client', module: 'google-api-client-appengine'
   }
```

## Screenshots

<img src="https://raw.githubusercontent.com/rafaelsf80/cloud-bigquery-android/master/screenshots/android.png" alt="alt text" width="100" height="200">
<img src="https://raw.githubusercontent.com/rafaelsf80/cloud-bigquery-android/master/screenshots/bq_console.png" alt="alt text" width="100" height="200">
