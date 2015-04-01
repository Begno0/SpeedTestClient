package begnardi.luca.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import begnardi.luca.entity.Result;
import begnardi.luca.events.ClientEventDispatcher;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;

/**
 * Created by begno on 11/02/15.
 */

public class FileReadWrite extends ClientEventDispatcher {

    /**
     * this is a wrapping class
     * for reading and writing to file
     * in CSV,can communicate event and has
     * a file name resource.
     */

    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public FileReadWrite(String file) {
            this.file = file;
    }

    public void fileWrite(Result r) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
            out.write(r.toCSV());
            out.write("\n");
            out.close();
            //this is useful to update the resultTab list
            comunicateAll(new SuccessEvent(r, this));

        } catch (IOException e) {
            comunicateAll(new ErrorEvent(2, "File output error.", this));
        }
    }

    public void fileRead() {
        ArrayList<String> resultList = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String s;
            while((s = in.readLine()) != null) {
                resultList.add(s);
            }
            in.close();
            comunicateAll(new SuccessEvent(resultList, this));

        } catch (FileNotFoundException e) {
            //raised if no permission or no file found
            comunicateAll(new ErrorEvent(3, "Results' file doesn't exist or you don't have the right permissions.",this));
        } catch (IOException e) {
            comunicateAll(new ErrorEvent(4, "File input error.", this));
        }
    }
}