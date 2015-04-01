package begnardi.luca.events;

/**
 * Created by begno on 11/02/15.
 */

public abstract class ClientEvent {

    /**abstract class extended by all events
	 * provide useful information to the UI
	 */

    private ClientEventDispatcher source;

    public ClientEventDispatcher getSource() {
        return source;
    }

    public ClientEvent(ClientEventDispatcher source) {
        this.source = source;
    }
}