package begnardi.luca.graphics;

import android.app.Activity;

import android.location.Geocoder;

import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import begnardi.luca.entity.Result;
import begnardi.luca.entity.Test;
import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.StatusEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.io.FileReadWrite;
import begnardi.luca.tests.R;

/**
 * Created by begno on 05/02/15.
 */

public class SpeedTestFragment extends Fragment implements ClientEventListener, View.OnClickListener {

    private GoogleApiClient client;
    private Geocoder geocoder;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Test speedTest;
    private FileReadWrite fileWriter;
    private RoundButtonWithLoading b;
    private View resultView;
    private double percent;

    public SpeedTestFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        speedTest = new Test(client, geocoder, "http://localhost:8181");
        speedTest.addClientListener(this);

        if(client != null)
            client.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_speedtest, container, false);

        b = (RoundButtonWithLoading) v.findViewById((R.id.new_test));
        b.setOnClickListener(this);

        resultView = getActivity().getLayoutInflater().inflate(R.layout.table_result_item,(ViewGroup) v.findViewById(R.id.container), false);
        ((ViewGroup) v.findViewById(R.id.container)).addView(resultView);
        resultView.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_test: {
                percent = 0;
                newTest(v);
            } break;
        }
    }

    public void setClient(GoogleApiClient client) {
        this.client = client;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public void newTest(View view) {
        fileWriter = new FileReadWrite("/storage/sdcard0/Test/results.csv");
        new Thread(speedTest).start();
        b.setEnabled(false);
    }

    public void printResult(final String result){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                b.setPercent(100);

                String rArray[] = result.split(",");

                TextView text = (TextView) resultView.findViewById(R.id.text_isp);
                text.setText(rArray[0]);

                text = (TextView) resultView.findViewById(R.id.text_date);
                text.setText(rArray[6]);

                text = (TextView) resultView.findViewById(R.id.text_download);
                text.setText(rArray[1] + " Mbps");

                text = (TextView) resultView.findViewById(R.id.text_upload);
                text.setText(rArray[2] + " Mbps");

                text = (TextView) resultView.findViewById(R.id.text_city);
                text.setText(rArray[3]);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.setPercent(0);
                        resultView.setVisibility(View.VISIBLE);
                    }
                }, 100);

                b.setEnabled(true);
            }
        });
    }

    @Override
    public void eventHandler(ClientEvent ce) {

        if(ce.getSource().equals(speedTest) && ce instanceof ErrorEvent) {
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    b.setEnabled(true);
                }
            });
            ((MainActivity) getActivity()).showErrorDialog(((ErrorEvent) ce).getMessage());
        }
        if(ce.getSource().equals(speedTest) && ce instanceof SuccessEvent) {
            Result result = ((SuccessEvent) ce).getResult();
            //fileWrite the new result to the file
            fileWriter.fileWrite(result);
            printResult(result.toCSV());
        }
        if(ce.getSource().equals(speedTest) && ce instanceof StatusEvent) {
            percent += ((StatusEvent) ce).getValue() / 4;
            getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                b.setPercent(percent);
                                            }
                                        });
        }
    }

    public static SpeedTestFragment newInstance(int sectionNumber, GoogleApiClient client, Geocoder geocoder) {
        SpeedTestFragment fragment = new SpeedTestFragment();
        fragment.setClient(client);
        fragment.setGeocoder(geocoder);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}