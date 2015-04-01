package begnardi.luca.entity;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by begno on 11/02/15.
 */

public class Result {

	/*all values are in kB/s!!
	  a result is composed by one location, upload and download speed,
	  isp and date*/

    private double downloadResult;
    private double uploadResult;
    private String ispResult;
    private ClientLocation locationResult;
    private String date;

    //this is an invalid result
    public Result() {
        ispResult = "";
        downloadResult = Double.NaN;
        uploadResult = Double.NaN;
        locationResult = new ClientLocation(new LatLng(Double.NaN, Double.NaN), "");
        date = "";
    }

    public void setIspResult(String ispResult) {
        this.ispResult = ispResult;
    }

    public String getIspResult() {
        return ispResult;
    }

    public double getDownloadResult() {
        return downloadResult;
    }

    public void setDownloadResult(double downloadResult) {
        this.downloadResult = downloadResult;
    }

    public double getUploadResult() {
        return uploadResult;
    }

    public void setUploadResult(double uploadResult) {
        this.uploadResult = uploadResult;
    }

    public ClientLocation getLocationResult() {
        return locationResult;
    }

    public void setLocationResult(ClientLocation locationResult) {
        this.locationResult = locationResult;
    }

    public String getDate() {
        return date;
    }

    //set the date to a new date(never used yet)
    public void setDate(String date) {
        this.date = date;
    }

    //update to the current date
    public void updateDate() {
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    //this should be called before every operation with the server
    public boolean isValid() {
        if(ispResult.equals("") || Double.isNaN(downloadResult) ||
           Double.isNaN(uploadResult) || Double.isNaN(locationResult.getPosition().latitude) ||
           Double.isNaN(locationResult.getPosition().longitude) || locationResult.getCity().equals("") || date.equals(""))
           return false;
        return true;
    }

    public String toString() {
        return "ISP: "+ ispResult +
               "\nDownload speed: "+ downloadResult +" Mbps"+
               "\nUpload speed: "+ uploadResult +" Mbps"+
               "\nLocation: "+ locationResult.getPosition().latitude +", "+ locationResult.getPosition().longitude +
               "\nCity: "+ locationResult.getCity() +
               "\nDate: "+ date;
    }

    //return a comma-separated-value of the field, used for local results
    public String toCSV() {
        return ispResult +","
               +downloadResult +","
               +uploadResult +","
               +locationResult.getCity() +","
               +locationResult.getPosition().latitude +","
               +locationResult.getPosition().longitude +","
               +date;
    }

    //used to retrieve results from server and local results
    public static Result fromCSV(String csv) {
        Result result = new Result();
        String csvArray[] = csv.split(",");
        result.setIspResult(csvArray[0]);
        result.setDownloadResult(Double.parseDouble(csvArray[1]));
        result.setUploadResult(Double.parseDouble(csvArray[2]));
        result.setLocationResult(new ClientLocation(new LatLng(Double.parseDouble(csvArray[4]), Double.parseDouble(csvArray[5])), csvArray[3]));
        result.setDate(csvArray[6]);
        return result;
    }

    //used to send data to the server
    public String toQuery() {
        return "isp=" + ispResult.replaceAll(" ","+")
                + "&downloadSpeed=" + downloadResult
                + "&uploadSpeed=" + uploadResult
                + "&city=" + locationResult.getCity().replaceAll(" ","+")
                + "&lat=" + locationResult.getPosition().latitude
                + "&lng=" + locationResult.getPosition().longitude
                + "&date=" + date.replace(" ", "+");
    }
}