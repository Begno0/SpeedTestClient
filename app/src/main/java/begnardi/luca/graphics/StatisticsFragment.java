package begnardi.luca.graphics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import begnardi.luca.entity.Filter;
import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.io.DBConnection;
import begnardi.luca.tests.R;
import begnardi.luca.utils.AutoCompletion;

/**
 * Created by begno on 12/03/15.
 */
public class StatisticsFragment extends Fragment implements ClientEventListener, AdapterView.OnItemSelectedListener, View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private DBConnection dBCon;
    private Spinner ispSpinner;
    private ArrayList<String> ispList;
    private ProgressDialog progressDialog;
    private TextView text;
    private Filter filter;
    private SearchView searchView;
    private AutoCompletion ac;
    private SimpleCursorAdapter suggestionAdapter;

    public static StatisticsFragment newInstance(int sectionNumber) {

        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public StatisticsFragment(){
        ispList = new ArrayList<String>();
        filter = new Filter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_statistics, container, false);

        progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);

        ac = new AutoCompletion();
        ac.addClientListener(this);

        dBCon = new DBConnection(3);
        dBCon.addClientListener(this);
        new Thread(dBCon).start();

        searchView = (SearchView) v.findViewById(R.id.view_search);
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);

        ispSpinner = (Spinner) v.findViewById(R.id.spinner_isp);
        Spinner timeSpinner = (Spinner) v.findViewById(R.id.spinner_time);
        String timeArray[] = {"All", "One hour", "One day", "One week"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, timeArray);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(this);

        text = (TextView) v.findViewById(R.id.text_average);

        Button searchButton = (Button) v.findViewById(R.id.button_search);
        searchButton.setOnClickListener(this);

        return v;
    }

        @Override
    public void eventHandler(ClientEvent ce) {

        if(progressDialog != null)
            progressDialog.dismiss();

        if (ce.getSource().equals(dBCon) && ce instanceof ErrorEvent) {
            ErrorEvent error = (ErrorEvent) ce;
            ((MainActivity) getActivity()).showErrorDialog(error.getMessage());
        }

        if (ce.getSource().equals(dBCon) && ce instanceof SuccessEvent) {
            if (ispList.size() == 0) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        ispList.addAll(dBCon.getIspList());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ispList);
                        ispSpinner.setAdapter(adapter);
                        ispSpinner.setOnItemSelectedListener(StatisticsFragment.this);
                    }
                });
            } else {
                final String average[];
                if(((SuccessEvent) ce).getArrayList().size() == 0) {
                    average = new String[2];
                    average[0] = "0.00";
                    average[1] = "0.00";
                }
                else
                    average = (((ArrayList<String>) ((SuccessEvent) ce).getArrayList()).get(0)).split(",");

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        text.setText("Download average: "+ average[0] +" Mbps \n" +
                                     "Upload average: "+ average[1]+ " Mbps");
                    }
                });
            }
        }
        if(ce.getSource().equals(ac) && ce instanceof SuccessEvent) {
            ArrayList<String> suggArrayList = (ArrayList<String>) ((SuccessEvent)ce).getArrayList();
            int i = 0;
            String[] suggestions = new String[suggArrayList.size()];
            for(String s : suggArrayList) {
                suggestions[i] = s;
                i++;
            }
            String[] columnNames = {"_id","text"};
            MatrixCursor cursor = new MatrixCursor(columnNames);
            String[] temp = new String[2];
            int id = 0;
            for(String item : suggestions){
                temp[0] = Integer.toString(id++);
                temp[1] = item;
                cursor.addRow(temp);
            }
            String[] from = {"text"};
            int[] to = {android.R.id.text1};
            suggestionAdapter = new SimpleCursorAdapter(getActivity(),
                                                        android.R.layout.simple_list_item_1,
                                                        cursor,
                                                        from,
                                                        to,
                                                        0);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    searchView.setSuggestionsAdapter(suggestionAdapter);
                }
            });
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = (String) parent.getItemAtPosition(position);

        switch(parent.getId()) {
            case R.id.spinner_isp:
                filter.setIsp(item);
                break;
            case R.id.spinner_time:
                filter.setTimeFilter(item);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_search: {
                (filter.getLocation()).setCity(searchView.getQuery().toString());
                if(filter.getIsp().equals("All") || filter.getLocation().getCity().equals("")) {
                    ((MainActivity) getActivity()).showErrorDialog("Please, fill all the fields.");
                }
                else {
                    dBCon.setFilter(filter);
                    progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);
                    new Thread(dBCon).start();
                }
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        (filter.getLocation()).setCity(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        ac.setString(s);
        new Thread(ac).start();
        return false;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        System.out.println("SELECT");
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        MatrixCursor cursor = (MatrixCursor) searchView.getSuggestionsAdapter().getCursor();
        cursor.moveToPosition(position);
        String s = cursor.getString(1);
        String sArray[] = s.split(",");
        searchView.setQuery(sArray[0], false);
        return false;
    }
}