package begnardi.luca.entity;

import com.google.android.gms.maps.model.LatLng;

/**
* Created by begno on 13/02/15.
*/

public class Filter {

    /*
     * provides a filter for the server result, composed
     * by upload, download speed, isp name and time filter,
     * and the name of the city which I want to get results.
     */

    private double downloadSpeed;
    private double uploadSpeed;
    private String timeFilter;
    private String isp;
    private ClientLocation location;

    public Filter() {
        downloadSpeed = 0;
        uploadSpeed = 0;
        isp = "All";
        location = new ClientLocation(new LatLng(Double.NaN,Double.NaN),"");
        timeFilter = "All";
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public double getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public void setLocation(ClientLocation location) {
        this.location = location;
    }

    public ClientLocation getLocation() {
        return location;
    }

    public String getTimeFilter() {
        return timeFilter;
    }

    public void setTimeFilter(String timeFilter) {
        this.timeFilter = timeFilter;
    }

    public void copy(Filter filter) {
        this.downloadSpeed = filter.getDownloadSpeed();
        this.uploadSpeed = filter.getUploadSpeed();
        this.timeFilter = filter.getTimeFilter();
        this.location = filter.getLocation();
        this.isp = filter.getIsp();
    }

    public String toQuery() {
        return "downloadSpeed="+ downloadSpeed +
               "&uploadSpeed="+ uploadSpeed+
               "&city="+ location.getCity().replaceAll(" ", "+")+
               "&isp="+ isp.replaceAll(" ", "+")+
               "&timeFilter="+ timeFilter.replaceAll(" ", "+");
    }

    public boolean isEmpty() {
        return (isp.equals("All") ||
                location.getCity().equals(""));
    }
}