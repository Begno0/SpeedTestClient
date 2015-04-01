package begnardi.luca.entity;

import android.location.Geocoder;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.DownloadEvent;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.StatusEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.tests.DownloadPool;
import begnardi.luca.tests.GeoLocation;
import begnardi.luca.utils.Utils;

/**
 * Created by begno on 11/02/15.
 */

public class Test extends ClientEventDispatcher implements Runnable, ClientEventListener {

    /* Test contains a result and a download and upload class,
    extends clientEventDispatcher to communicate with the ui and
    implements runnable to work in a thread
    */

    //private DownloadTest downloadTest;
    private DownloadPool downloadPool;
    //private UploadTest uploadTest;
    private Result result;
    private GoogleApiClient client;
    private GeoLocation locator;
    private Geocoder geocoder;

    public Test(GoogleApiClient client, Geocoder geocoder, String urlUp) {

        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add("http://download.thinkbroadband.com/512MB.zip");
        urlList.add("http://www.wswd.net/testdownloadfiles/512MB.zip");
        urlList.add("http://azspnortheurope.blob.core.windows.net/azurespeed/100MB.bin");
        urlList.add("http://azspwesteurope.blob.core.windows.net/azurespeed/100MB.bin");

        downloadPool = new DownloadPool(urlList);
        downloadPool.addClientListener(this);

        //uploadTest = new UploadTest(urlUp);

        this.client = client;
        //downloadTest = new DownloadTest(urlDown);
        locator = new GeoLocation();
        this.geocoder = geocoder;
        result = new Result();
    }

    public DownloadPool getDownloadTest() {
        return downloadPool;
    }

//    public UploadTest getUploadTest() {
//        return uploadTest;
//    }

    //extends addClientListener to avoid exposing download and upload entity to ui
//    public void addClientListener(ClientEventListener cl) {
//
//        addClientListener(cl);
//        super.addClientListener(cl);
//        //uploadTest.addClientListener(cl);
//    }

    public void run() {
        try {
            //date
            result.updateDate();
            //download
            //result.setDownloadResult(downloadTest.getDownloadSpeed());
            downloadPool.startPool();
            //upload
            result.setUploadResult(1.20);
            //isp
            result.setIspResult(getISP());
            //location
            result.setLocationResult(locator.getLocation(client, geocoder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getISP() throws IOException {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://ip-api.com/json/").openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String buf = "";
            String json;
            while((json = in.readLine()) != null){
                buf += json;
            }

            JSONObject ob = new JSONObject(buf);

            return ob.get("isp").toString();
        }
        catch (JSONException e1){
            System.out.println("JSON exception");
            return "";
        }
        catch (MalformedURLException e1){
            System.out.println("Malformed URL exception");
            return "";
        } catch (UnknownHostException e1){
            System.out.println("Unknown host exception");
            return "";
        }
    }

    @Override
    public void eventHandler(ClientEvent ce) {
        if(ce.getSource().equals(downloadPool)) {
            if(ce instanceof StatusEvent) {
                //let event goes up to speedtest
                StatusEvent ce2 = new StatusEvent(((StatusEvent) ce).getValue(), this);
                comunicateAll(ce2);
            }
            else {
                if (ce instanceof DownloadEvent) {
                    //we have a new download result(NaN if not valid)
                    result.setDownloadResult(((DownloadEvent) ce).getDownload());
                    try {
                        if (!result.isValid()) {
                            //communicate which part has not worked
                            StringBuffer buf = new StringBuffer();
                            if (Double.isNaN(result.getDownloadResult()))
                                buf.append("downloadTest:X\n");
                            else
                                buf.append("downloadTest:V\n");
                            if (Double.isNaN(result.getUploadResult()))
                                buf.append("uploadTest:X\n");
                            else
                                buf.append("uploadTest:V\n");
                            if (result.getIspResult().equals(""))
                                buf.append("isp:X\n");
                            else
                                buf.append("isp:V\n");
                            if (Double.isNaN(result.getLocationResult().getPosition().longitude) || Double.isNaN(result.getLocationResult().getPosition().latitude))
                                buf.append("location:X\n");
                            else
                                buf.append("location:V\n");
                            if (result.getDate().equals(""))
                                buf.append("date:X\n");
                            else
                                buf.append("date:V\n");
                            //comunicateAll(new ErrorEvent(1, buf.toString(), this));
                            comunicateAll(new ErrorEvent(1, "Unable to connect.\nPlease check the network.", this));
                        } else {
                            System.out.println(result.toQuery());
                            HttpURLConnection connection = (HttpURLConnection) new URL("http://" + Utils.IP + ":8182/1/add_value?" + result.toQuery()).openConnection();
                            //connection.setConnectTimeout(5000);
                            connection.setRequestMethod("GET");
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                //communicate the new result
                                comunicateAll(new SuccessEvent(result, this));
                            } else {
                                //server did not understand the request
                                comunicateAll(new ErrorEvent(1, "Error connecting to database", this));
                            }
                        }
                    } catch (IOException e) {
                        comunicateAll(new ErrorEvent(2, "Error connecting to database.", this));
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}