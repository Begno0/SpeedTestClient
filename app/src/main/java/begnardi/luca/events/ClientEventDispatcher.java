package begnardi.luca.events;

import java.util.ArrayList;

/**
 * Created by begno on 11/02/15.
 */

public class ClientEventDispatcher {
	/** every event's source extends this class,
	 * which provide method to send the event and register
	 * listeners
	 */

    private static final long serialVersionUID = 1L;
    ArrayList<ClientEventListener> listenersList;


    public ClientEventDispatcher() {
        super();
        listenersList = new ArrayList<ClientEventListener>();
    }

    public void addClientListener(ClientEventListener cl) {
        //add the listeners only if not already in list
        for(ClientEventListener clInList : listenersList){
            if(clInList.equals(cl))
                return;
        }
        listenersList.add(cl);
    }

    public void comunicateAll(ClientEvent ce) {
        //send an infoEvent if the ClientEvent is an ErrorEvent
        for(ClientEventListener cl : listenersList)
            cl.eventHandler(ce);
    }
}