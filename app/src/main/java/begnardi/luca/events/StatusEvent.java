package begnardi.luca.events;

/**
* Created by begno on 11/02/15.
*/
public class StatusEvent extends ClientEvent{

    private double value; //value for progress bar update

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public StatusEvent(double value,ClientEventDispatcher source) {
        super(source);
        this.value = value;
    }
}
