package begnardi.luca.events;

/**
 * Created by begno on 11/02/15.
 */

public interface ClientEventListener {
	/*this class is implemented by
	 * all listeners and have only the
	 * raw handler method
	 */

    public void eventHandler(ClientEvent ce);

}