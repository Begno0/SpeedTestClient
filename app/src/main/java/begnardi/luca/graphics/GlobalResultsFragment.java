package begnardi.luca.graphics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

import begnardi.luca.entity.ClientLocation;
import begnardi.luca.entity.Filter;
import begnardi.luca.entity.Result;
import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.events.TouchHandler;
import begnardi.luca.io.DBConnection;
import begnardi.luca.tests.R;

/**
 * Created by begno on 14/02/15.
 */

public class GlobalResultsFragment extends Fragment implements ClientEventListener, GoogleApiClient.ConnectionCallbacks, AdapterView.OnItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private GoogleMap map;

    private TileOverlay mOverlay;
    private Gradient gradient;
    private ArrayList<WeightedLatLng> heatList;
    private ArrayList<Result> results;
    private boolean heatFlag;

    private DBConnection dBCon;

    private ProgressDialog progressDialog;

    private Geocoder geocoder;
    private GoogleApiClient client;

    private Filter filter;
    private Filter filterTemp;
    private TextView downloadText;
    private TextView uploadText;
    private ArrayList<String> ispList;
    private Spinner ispSpinner;

    private RoundButton heatMapButton;

    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static GlobalResultsFragment newInstance(int sectionNumber, GoogleApiClient client, Geocoder geocoder) {

        GlobalResultsFragment fragment = new GlobalResultsFragment();
        fragment.setClient(client);
        fragment.setGeocoder(geocoder);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public GlobalResultsFragment(){
        results = new ArrayList<Result>();
        ispList = new ArrayList<String>();
        filter = new Filter();
        filterTemp = new Filter();
    }

    public void setClient(GoogleApiClient client){
        this.client = client;
    }

    public void setGeocoder(Geocoder geocoder){
        this.geocoder = geocoder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        heatList = new ArrayList<WeightedLatLng>();

        if(client != null && !client.isConnected())
            client.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_results_global, container, false);

        setHasOptionsMenu(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.global_map);
        v.findViewById(R.id.linear).setOnTouchListener(new TouchHandler(this, v.findViewById(R.id.linear), v.findViewById(R.id.fragment_bottom)));

        int colors[] = {Color.argb(0, 0, 255, 0), //transparent
                        Color.argb(255 / 3 * 2, 0, 255, 0), //green
                        Color.rgb(255, 255, 0), //yellow
                        Color.rgb(255, 153, 0), //orange
                        Color.rgb(255, 0, 0)}; //red
        float[] startPoints = {0.0f, 0.0025f, 0.01f, 0.04f, 0.1f};
        gradient = new Gradient(colors, startPoints);
        map = mapFragment.getMap();
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View v = inflater.inflate(R.layout.marker, null);

                TextView title = (TextView) v.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView address = (TextView) v.findViewById(R.id.snippet);
                address.setText(marker.getSnippet());

                return v;
            }
        });

        progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);

        if(dBCon == null) {
            dBCon = new DBConnection(0);
            dBCon.setGeocoder(geocoder);
            dBCon.setClient(client);
            dBCon.setFilter(filter);
            dBCon.addClientListener(this);
            new Thread(dBCon).start();
        }

        ispSpinner = (Spinner) v.findViewById(R.id.spinner_isp);
        Spinner timeSpinner = (Spinner) v.findViewById(R.id.spinner_time);
        SeekBar downBar = (SeekBar) v.findViewById(R.id.seek_download);
        SeekBar upBar = (SeekBar) v.findViewById(R.id.seek_upload);
        downloadText = (TextView) v.findViewById(R.id.value_download);
        downloadText.setText("0.0 Mbps");
        uploadText = (TextView) v.findViewById(R.id.value_upload);
        uploadText.setText("0.0 Mbps");
        Button applyButton = (Button) v.findViewById(R.id.button_apply);

        heatMapButton = (RoundButton) v.findViewById(R.id.button_heatmap);

        String timeArray[] = {"All", "One hour", "One day", "One week"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, timeArray);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(this);
        downBar.setOnSeekBarChangeListener(this);
        upBar.setOnSeekBarChangeListener(this);

        applyButton.setOnClickListener(this);
        heatMapButton.setOnClickListener(this);

        return v;
    }

    public void drawMarkers(final ArrayList<Result> results, final float zoom){
        getActivity().runOnUiThread(new Runnable() {
            public void run()
            {
                LatLng pos;
                map.clear();
                heatList = new ArrayList<WeightedLatLng>();
                heatList.add(new WeightedLatLng(new LatLng(90, 0), 500));

                for(Result r : results){
                    pos = new LatLng(r.getLocationResult().getPosition().latitude, r.getLocationResult().getPosition().longitude);
                    map.addMarker(new MarkerOptions()
                            .title(r.getIspResult().replaceAll("_", " "))
                            .snippet(r.getDate() + "\n"
                                    + "Download: " + r.getDownloadResult() + " Mbps\n"
                                    + "Upload: " + r.getUploadResult() + " Mbps")
                            .position(pos));
                    if(r.getDownloadResult() > 16)
                        map.addCircle(new CircleOptions().center(pos).radius(2).fillColor(Color.argb(80,255,0,0)).strokeWidth(0));
                    else
                        map.addCircle(new CircleOptions().center(pos).radius(2).fillColor(Color.argb(80,0,255,0)).strokeWidth(0));
                    heatList.add(new WeightedLatLng(pos, r.getDownloadResult()));
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(filter.getLocation().getPosition(), zoom));

                if(heatFlag)
                    if(heatList.size() > 1)
                        setHeatMap(heatList);

                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void eventHandler(ClientEvent ce) {

        if(ispList.size() == 0) {
            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    ispList.add("All");
                    ispList.addAll(dBCon.getIspList());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ispList);
                    ispSpinner.setAdapter(adapter);
                    ispSpinner.setOnItemSelectedListener(GlobalResultsFragment.this);
                }
            });
        }
        if(ce.getSource().equals(dBCon) && ce instanceof ErrorEvent) {
            ErrorEvent error = (ErrorEvent) ce;
            if(progressDialog != null)
                progressDialog.dismiss();
            ((MainActivity) getActivity()).showErrorDialog(error.getMessage());
        }
        if(ce.getSource().equals(dBCon) && ce instanceof SuccessEvent) {
            results = (ArrayList<Result>) ((SuccessEvent)ce).getArrayList();
            filter = dBCon.getFilter();
            if(filter.getLocation().getPosition().latitude != Double.NaN && filter.getLocation().getPosition().longitude != Double.NaN ) {
                drawMarkers(results, 13);
                ((MainActivity) getActivity()).showToast(results.size() + " results found.");
                if(filter.getLocation().getCity().equals("")) {
                    filter = dBCon.getFilter();
                    filterTemp.setLocation(filter.getLocation());
                }
            }
            else {
                if(progressDialog != null)
                    progressDialog.dismiss();
                ((MainActivity) getActivity()).showErrorDialog("No locations found.");
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)) {
            inflater.inflate(R.menu.global_results, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == MainActivity.RESULT_OK) {
                String result = data.getStringExtra("result");
                String sArray[] = result.split(",");
                filter.setLocation(new ClientLocation(filter.getLocation().getPosition(), sArray[0]));
                filterTemp.setLocation(filter.getLocation());
                dBCon.setFilter(filter);
                new Thread(dBCon).start();
                progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        dBCon = new DBConnection(0);
        dBCon.setGeocoder(geocoder);
        dBCon.setClient(client);
        dBCon.setFilter(filter);
        dBCon.addClientListener(this);
        new Thread(dBCon).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = (String) parent.getItemAtPosition(position);

        switch(parent.getId()) {
            case R.id.spinner_isp:
                filterTemp.setIsp(item);
                break;
            case R.id.spinner_time:
                filterTemp.setTimeFilter(item);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_apply: {
                filter.copy(filterTemp);
                dBCon.setFilter(filter);
                progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);
                new Thread(dBCon).start();
            } break;
            case R.id.button_heatmap: {
                if(!heatFlag) {
                    if(heatList.size() > 1)
                        setHeatMap(heatList);
                    heatFlag = true;
                }
                else {
                    if(mOverlay != null)
                        removeHeatMap();
                    heatFlag = false;
                    if(results.size() > 0) {
                        filter.getLocation().setPosition(map.getCameraPosition().target);
                        filterTemp.getLocation().setPosition(map.getCameraPosition().target);
                        drawMarkers(results, map.getCameraPosition().zoom);
                    }
                }
                heatMapButton.setState(heatFlag);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case R.id.seek_download: {
                downloadText.setText(Double.toString(progress) +" Mbps");
                filterTemp.setDownloadSpeed(progress);
                break;
            }
            case R.id.seek_upload: {
                uploadText.setText(Double.toString(progress) +" Mbps");
                filterTemp.setUploadSpeed(progress);
                break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setHeatMap(ArrayList<WeightedLatLng> heatList) {
        HeatmapTileProvider heatMap = new HeatmapTileProvider.Builder().weightedData(heatList).gradient(gradient).build();
        map.clear();
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatMap));
    }

    public void removeHeatMap() {
        mOverlay.remove();
    }
}