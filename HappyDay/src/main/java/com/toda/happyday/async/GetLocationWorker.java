package com.toda.happyday.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.toda.happyday.models.Picture;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import br.com.condesales.models.Venue;

/**
 * Created by fpgeek on 2014. 2. 27..
 */
public class GetLocationWorker extends AsyncTask<Picture, Void, String> {

    private static final String FOURSQUARE_URL = "https://api.foursquare.com/v2/venues/search";
    private static final String FOURSQUARE_CLIENT_ID = "CX1HRRFGA20JK5BDIQ34ZKRXLWTABKYNC1DYBUNRWPTY30EP";
    private static final String FOURSQUARE_CLIENT_SECRET = "VZLNNXQTLNXWPVQTEDTRT3QBPZLSJT111V531K3W5EWJRARP";
    private static final String FOURSQUARE_VERSION = "20140101";
    private static final String FOURSQUARE_LIMIT = "1";

    public GetLocationWorker() {
    }

    @Override
    protected String doInBackground(Picture... pictures) {

        Picture picture = pictures[0];

        if (picture.getLocation() == null) {
            if (picture.hasValidLocationInfo()) {
                String location = getLocationFromFoursquare(picture.getLatitude(), picture.getLongitude());
                // Todo - Picture db 업데이트

                Log.i("FORSQUAR", "location : " + location);
                picture.setLocation(location);
                return location;
            }
            return null;
        } else {
            return picture.getLocation();
        }
    }

    @Override
    protected void onPostExecute(String location) {
        super.onPostExecute(location);
    }

    private static String getLocationFromFoursquare(double latitude, double longitude) {

        ArrayList<Venue> venues = new ArrayList<Venue>();
        try {
            JSONObject venuesJson = executeHttpGet(FOURSQUARE_URL
                    + "?"
                    + "ll=" + latitude + "," + longitude
                    + "&client_id=" + FOURSQUARE_CLIENT_ID
                    + "&client_secret=" + FOURSQUARE_CLIENT_SECRET
                    + "&v=" + FOURSQUARE_VERSION
                    + "&limit=" + FOURSQUARE_LIMIT);

            // Get return code
            int returnCode = Integer.parseInt(venuesJson.getJSONObject("meta").getString("code"));

            // 200 = OK
            if (returnCode == 200) {
                Gson gson = new Gson();
                JSONArray json = venuesJson.getJSONObject("response")
                        .getJSONArray("venues");
                for (int i = 0; i < json.length(); i++) {
                    Venue venue = gson.fromJson(json.getJSONObject(i)
                            .toString(), Venue.class);
                    venues.add(venue);
                }
            } else {
                return null;
            }

        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }

        return venues.get(0).getName();
    }

    // Calls a URI and returns the answer as a JSON object
    private static JSONObject executeHttpGet(String uri) throws Exception {
        HttpGet req = new HttpGet(uri);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);

        DefaultHttpClient client = new DefaultHttpClient();
        client.setParams(httpParameters);
        HttpResponse resLogin = client.execute(req);
        BufferedReader r = new BufferedReader(new InputStreamReader(resLogin
                .getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = r.readLine()) != null) {
            sb.append(s);
        }

        return new JSONObject(sb.toString());
    }
}
