package begnardi.luca.io;

import android.location.Geocoder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import begnardi.luca.entity.ClientLocation;
import begnardi.luca.entity.Filter;
import begnardi.luca.entity.Result;
import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.tests.GeoLocation;
import begnardi.luca.utils.Utils;

/**
 * Created by begno on 15/02/15.
 */

public class DBConnection extends ClientEventDispatcher implements Runnable {

    private HttpURLConnection connection;
    private GoogleApiClient client;
    private Geocoder geocoder;
    private Filter filter;
    private ArrayList<String> ispList;
    private int section;

    public DBConnection(int section) {
        this.section = section;
        ispList = new ArrayList<String>();
        filter = new Filter();
    }

    public void setClient(GoogleApiClient client) {
        this.client = client;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public ArrayList<String> getIspList() {
        return ispList;
    }

    @Override
    public void run() {
        switch (section) {
            case 0: {
                GeoLocation locator = new GeoLocation();

                if (ispList.size() == 0) {
                    connect("http://" + Utils.IP + ":8182/1/get_isp_names");
                    ispList = getIspNames();
                }
                if (filter.getLocation().getCity().equals("")) {
                    filter.setLocation(locator.getLocation(client, geocoder));
                    if(!filter.getLocation().getCity().equals(""))
                        connect("http://" + Utils.IP + ":8182/1/get_values?" + filter.toQuery());
                    else
                        comunicateAll(new ErrorEvent(2, "Unable to locate the request.", this));
                } else {
                    List<LatLng> positions = GeoLocation.geoCode(geocoder, filter.getLocation().getCity());
                    if (positions.size() > 0)
                        filter.setLocation(new ClientLocation(positions.get(0), filter.getLocation().getCity()));
                    else
                        filter.setLocation(new ClientLocation(null, filter.getLocation().getCity()));
                    connect("http://"+ Utils.IP +":8182/1/get_values?"+ filter.toQuery());
                }
                dbResultsRead();
            } break;
            case 1: {
                connect("http://" + Utils.IP + ":8182/1/get_isp");
                dbChartsRead();
            } break;
            case 2:{
                connect("http://" + Utils.IP + ":8182/1/get_city");
                dbChartsRead();
            } break;
            case 3: {
                if (ispList.size() == 0) {
                    connect("http://"+ Utils.IP +":8182/1/get_isp_names");
                    ispList = getIspNames();
                    if(ispList.size() == 0)
                        comunicateAll(new ErrorEvent(4, "Error connecting to database.", this));
                    else
                        comunicateAll(new SuccessEvent(ispList, this));
                }
                if(!filter.getIsp().equals("All") && !filter.getLocation().getCity().equals("")) {
                    connect("http://" + Utils.IP + ":8182/1/get_average?" + filter.toQuery());
                    dbChartsRead();
                }
            } break;
        }
    }

    public void connect(String url) {
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(5000);
        } catch (IOException e) {
            comunicateAll(new ErrorEvent(1, "Unable to connect.", this));
            e.printStackTrace();
        }
    }

    public ArrayList<String> getIspNames() {
        ArrayList<String> values = new ArrayList<String>();

        try {
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String s;
                while ((s = in.readLine()) != null)
                    values.add(s);
            }
        } catch (IOException e) {
            //nothing
        }
        return values;
    }

    public void dbChartsRead() {

        ArrayList<String> values = new ArrayList<String>();

        try {
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String s;
                while ((s = in.readLine()) != null)
                    values.add(s);
                comunicateAll(new SuccessEvent(values, this));
            } else if ((connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)) {
                comunicateAll(new SuccessEvent(values, this));
            } else {
                comunicateAll(new ErrorEvent(5, "Result response:" + connection.getResponseCode(), this));
            }
        } catch (IOException e) {
            e.printStackTrace();
            comunicateAll(new ErrorEvent(4, "Error connecting to database.", this));
        }
    }

    public void dbResultsRead() {

        ArrayList<Result> resultList = new ArrayList<Result>();

        try {
            connection.setRequestMethod("GET");
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String s;
                while((s = in.readLine())!= null) {
                    Result r = Result.fromCSV(s);
                    resultList.add(r);
                }
                comunicateAll(new SuccessEvent(resultList, this));
            }
            else if(connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                comunicateAll(new SuccessEvent(resultList, this));
            }
            else {
                comunicateAll(new ErrorEvent(5, "Result response: " + connection.getResponseCode(), this));
            }
        } catch (IOException e) {
            e.printStackTrace();
            comunicateAll(new ErrorEvent(4, "Error connecting to database.", this));
        }
    }
}