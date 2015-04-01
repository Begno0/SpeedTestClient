package begnardi.luca.tests;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.DownloadEvent;
import begnardi.luca.events.StatusEvent;
import begnardi.luca.utils.Utils;

public class DownloadTest extends ClientEventDispatcher implements Runnable {

    private String urlString;
    private int SLICE_SIZE = 5;
    private int DISCARDED_FROM_START = 1; //samples discarded(low) from every slice
    private int DISCARDED_FROM_END = 2; //samples discarded(high) from every slice

    public DownloadTest(String urlString) {
        this.urlString = urlString;
    }

    private ArrayList<Double> sliceFilter(ArrayList<Double> listTemp) {
        ArrayList<Double> list = new ArrayList<Double>();
        //check if it's possible to remove discarded from each slice
        if(DISCARDED_FROM_START + DISCARDED_FROM_END < listTemp.size()) {
            Collections.sort(listTemp);
            for(int i = DISCARDED_FROM_START; i < listTemp.size() - DISCARDED_FROM_END; i++)
                list.add(listTemp.get(i));
            return list;
        }
        //else return the list
        return listTemp;
    }

    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            //connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            if(connection.getResponseCode() == 200) {
                DataInputStream in = new DataInputStream(connection.getInputStream());
                long bytes = 0; //total bytes
                byte b[] = new byte[128]; //every time try to read 128 bytes
                int byteRead = 0; //effective bytes read every time
                long byteReadin50 = 0; //bytes read in 50 ms
                Double tempTime; //time used every cycle
                long start = System.nanoTime(); //effective start
                long startOf50 = start; //start of 50 ms

                ArrayList<Double> resultListTemp = new ArrayList<Double>();
                ArrayList<Double> resultList = new ArrayList<Double>();

                double oldPercent = 0;
                double actualPercent;
                //read for 10 seconds or until end of stream is reached
                while((byteRead = in.read(b)) != -1 && Utils.milliFromNano(System.nanoTime() - start) < 10000) {
                    byteReadin50 += byteRead;
                    tempTime = Utils.milliFromNano((System.nanoTime() - startOf50));
                    if(tempTime > 50) {
                        //add a sample every 50 ms
                        resultListTemp.add(byteReadin50 / tempTime);
                        bytes += byteReadin50;
                        byteReadin50 = 0;
                        actualPercent = Utils.secondFromNano((System.nanoTime() - start)) * 10;
                        comunicateAll(new StatusEvent(actualPercent - oldPercent, this));
                        oldPercent = actualPercent;
                        //System.out.println(oldPercent);
                        startOf50 = System.nanoTime();
                    }
                }
                //filter the samples
                for(int i = 0; i < resultListTemp.size() / SLICE_SIZE; i++) {
                    ArrayList<Double> listTemp = new ArrayList<Double>();
                    for(int j = i * SLICE_SIZE; j < i * SLICE_SIZE + SLICE_SIZE; j++) {
                        listTemp.add(resultListTemp.get(j));
                    }
                    resultList.addAll(sliceFilter(listTemp));
                }
                //return the average of the samples
                comunicateAll(new DownloadEvent(Utils.average(resultList), this));
            }
            else {
                comunicateAll(new DownloadEvent(Double.NaN, this));
            }
        } catch (IOException e) {
            comunicateAll(new DownloadEvent(Double.NaN, this));
        }
    }
}