package begnardi.luca.tests;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import begnardi.luca.entity.ClientLocation;

/**
 * Created by begno on 11/02/15.
 */

public class GeoLocation {

    private ClientLocation clientLocation;

    public GeoLocation() {
        clientLocation = new ClientLocation();
    }

    public ClientLocation getLocation(GoogleApiClient client, Geocoder geocoder) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        clientLocation.setPosition(myPos);
        clientLocation.setCity(fetchAddress(geocoder, myPos));
        return clientLocation;
    }

    public String fetchAddress(Geocoder geocoder, LatLng myPos){
        String errorMessage = "";
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    myPos.latitude,
                    myPos.longitude,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available.";
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid lat/long values.";
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No addresses found.";
            }
            return(errorMessage);
        }
        else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            String sArray[];
            String sAddress = "";

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            sArray = addressFragments.get(1).split(" ");
            for(int i = 1; i < sArray.length - 1; i++){
                if(i < sArray.length - 2)
                    sAddress += sArray[i] +" ";
                else
                    sAddress += sArray[i];
            }

            return sAddress;
        }
    }

    public static List<LatLng> geoCode(Geocoder geocoder, String searchPattern) {

        List<Address> addresses = null;
        ArrayList<LatLng> positions = new ArrayList<LatLng>();

        if (searchPattern.equals("")){
            positions.add(new LatLng(Double.NaN, Double.NaN));
            return positions;
        }

        try {
            //trying to get all possible addresses by search pattern
            addresses = geocoder.getFromLocationName(searchPattern, Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses == null) {
            // location service unavailable or incorrect address
            positions.add(new LatLng(Double.NaN, Double.NaN));
            return positions;
        }

        for(Address a : addresses)
            positions.add(new LatLng(a.getLatitude(),a.getLongitude()));

        return positions;
    }
}