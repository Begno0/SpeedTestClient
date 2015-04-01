package begnardi.luca.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.SuccessEvent;

/**
 * Created by begno on 17/02/15.
 */
public class AutoCompletion extends ClientEventDispatcher implements Runnable{

    String place;

    public AutoCompletion(){}

    public void setString(String place){
        this.place = place;
    }

    @Override
    public void run() {
        try {
            place = place.replace(" ", "+");
            ArrayList<String> predictionsList = new ArrayList<String>();
            HttpURLConnection connection = (HttpURLConnection)new URL("https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+place+"&types=geocode&types=locality&components=country:IT&language=it&key="+ Utils.APIKey).openConnection();
            connection.setRequestMethod("GET");
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String buf = "";
                String json;
                while((json = in.readLine()) != null) {
                    buf += json;
                }
                JSONArray predictionsArr = new JSONObject(buf).getJSONArray("predictions");
                if(predictionsArr.length() > 0)
                    for(int i = 0; i < predictionsArr.length(); i++) {

                        predictionsList.add(predictionsArr.getJSONObject(i).get("description").toString());
                    }
            }
            comunicateAll(new SuccessEvent(predictionsList, this));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}