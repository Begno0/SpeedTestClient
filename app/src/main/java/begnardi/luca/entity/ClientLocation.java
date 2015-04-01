package begnardi.luca.entity;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by begno on 20/02/15.
 */
public class ClientLocation {

    private LatLng position;
    private String city;

    public ClientLocation(){
    }

    public ClientLocation(LatLng position, String city){
        this.position = position;
        this.city = city;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LatLng getPosition(){
        return position;
    }

    public String getCity(){
        return city;
    }
}