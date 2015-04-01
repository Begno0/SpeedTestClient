package begnardi.luca.events;

import java.util.ArrayList;

import begnardi.luca.entity.Result;

/**
 * Created by begno on 11/02/15.
 */

public class SuccessEvent extends ClientEvent{

    private Result result;
    private ArrayList<?> arrayList;
    private String s;

    public Result getResult() {
        return result;
    }

    public ArrayList<?> getArrayList(){
        return arrayList;
    }

    public String getString(){
        return s;
    }

    public SuccessEvent(Result result, ClientEventDispatcher source) {
        super(source);
        this.result = result;
    }

    public SuccessEvent(ArrayList<?> arrayList, ClientEventDispatcher source){
        super(source);
        this.arrayList = arrayList;
    }

    public SuccessEvent(String s, ClientEventDispatcher source){
        super(source);
        this.s = s;
    }
}