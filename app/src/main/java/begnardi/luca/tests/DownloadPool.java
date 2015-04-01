package begnardi.luca.tests;

import java.util.ArrayList;

import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.DownloadEvent;
import begnardi.luca.events.StatusEvent;
import begnardi.luca.utils.Utils;

/**
 * Created by begno on 27/02/15.
 */

public class DownloadPool extends ClientEventDispatcher implements ClientEventListener {

    private ArrayList<Double> downloadResultsList; //list with result of each download test
    private ArrayList<DownloadTest> downloadList; //list of downloads entities
    private double result; //final result(NaN is invalid)

    //this class handle the four download speed test's results
    public DownloadPool(ArrayList<String> urlList) {
        downloadList = new ArrayList<DownloadTest>();
        downloadResultsList = new ArrayList<Double>();
        //one new runnable for each url
        for(String url : urlList) {
            downloadList.add(new DownloadTest(url));
            downloadList.get(downloadList.size() - 1).addClientListener(this);
        }
        result = 0;
    }

    public void startPool() {
        //initial situation
        downloadResultsList = new ArrayList<Double>();
        result = 0;

        for(DownloadTest d : downloadList)
            new Thread(d).start();
    }

    public void eventHandler(ClientEvent ce) {
        //handle events from download threads
        if(ce.getSource() instanceof DownloadTest) {
            if(ce instanceof StatusEvent) {
                //let the status event go up to speedtest but change source
                StatusEvent ce2 = new StatusEvent(((StatusEvent) ce).getValue(), this);
                comunicateAll(ce2);
            }
            else {
                if (ce instanceof DownloadEvent) {
                    //one of the download has finished
                    synchronized (downloadResultsList) {
                        downloadResultsList.add(((DownloadEvent) ce).getDownload());
                        if (downloadResultsList.size() == downloadList.size()) {
                            //all downloads ended
                            for (Double d : downloadResultsList) {
                                if (Double.isNaN(d)) {
                                    comunicateAll(new DownloadEvent(Double.NaN, this));
                                    return;
                                } else
                                    result += d;
                            }
                            //round to 2 decimal places and convert to Mbps multiplying by 0,008
                            comunicateAll(new DownloadEvent(Utils.round(result * 0.008, 2), this));
                        }
                    }
                }
            }
        }
    }
}