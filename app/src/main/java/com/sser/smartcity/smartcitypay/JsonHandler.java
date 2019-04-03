package com.sser.smartcity.smartcitypay;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

class JsonHandler {

    private static final String LOG_TAG = JsonHandler.class.getName();

    // add query parameters in string request
    private static String makeNewPlateDataUrl(String request, String userId, String plate, boolean addPlate) {

        Uri baseUri = Uri.parse(request);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("field1", userId);
        uriBuilder.appendQueryParameter("field2", plate);
        uriBuilder.appendQueryParameter("field3", addPlate ? "1" : "0");

        return uriBuilder.toString();
    }

    // Make an HTTP request to the given URL and return a String as the response.
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if(url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Problem retrieving a JSON results.", e);
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }


    // Convert the InputStream into a String which contains the whole JSON response from the server.
    private static String readFromStream(InputStream inputStream) throws Exception{
        StringBuilder output = new StringBuilder();
        if(inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while(line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString();
    }


    // Saves data from JSON
    static void getAllUserPlates() throws Exception {

        String jsonResponse = null;

        // Create URL object
        URL url = null;

        try {
            url = new URL("https://api.thingspeak.com/channels/749743/feeds.json?api_key=P1WAZNYZ5NHSGENY");
        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }

        // Perform HTTP request to the URL and receive a JSON response back
        try {
            jsonResponse = JsonHandler.makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return;
        }

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            JSONObject jsonObject = new JSONObject(jsonResponse);

            JSONArray feeds = jsonObject.getJSONArray("feeds");

            String userEmail = "";
            String userPlate = "";

            for(int i = 0; i < feeds.length(); i++) {
                JSONObject obj2 = feeds.getJSONObject(i);

                userEmail = obj2.getString("field1");

                if(userEmail != null && userEmail.equals(AppData.firebaseUser.getUid()) && obj2.getString("field3").equals("1")) {
                    userPlate = obj2.getString("field2");
                    if(userPlate != null && !userPlate.isEmpty()) {
                        AppData.userPlates.add(new Plate(userPlate));
                    }
                }

            }

        } catch (Exception e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing JSON results", e);
        }

    }


    // Saves data from JSON
    static void addUserPlate(String plateText) throws Exception {

        String jsonResponse = "-1";

        // Create URL object
        URL url = null;

        try {
            url = new URL(makeNewPlateDataUrl("https://api.thingspeak.com/update?api_key=JY4A7T52CUAJ0BDW",
                    AppData.firebaseUser.getUid(), plateText, true));
        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }

        boolean firstTime = true;

        while(Integer.parseInt(jsonResponse) <= 0) {

            // Perform HTTP request to the URL and receive a JSON response back
            try {
                jsonResponse = JsonHandler.makeHttpRequest(url);
                if(!firstTime) Thread.sleep(1000);
                firstTime = false;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }
        }

    }





}
