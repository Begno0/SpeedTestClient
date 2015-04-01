package begnardi.luca.events;

/**
 * Created by begno on 11/02/15.
 */

public class ErrorEvent extends ClientEvent{
    private int code; //code of the error
    private String message; //message of the error

    public ErrorEvent(int code, String message,ClientEventDispatcher source) {
        super(source);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}