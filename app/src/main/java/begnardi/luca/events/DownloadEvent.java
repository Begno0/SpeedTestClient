package begnardi.luca.events;

/**
 * Created by begno on 27/02/15.
 */

public class DownloadEvent extends ClientEvent {

    private double download;

    public double getDownload() {
        return download;
    }
    public DownloadEvent(double download, ClientEventDispatcher source) {
        super(source);
        this.download = download;
    }
}