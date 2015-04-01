package begnardi.luca.graphics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import begnardi.luca.entity.Result;
import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.tests.GeoLocation;
import begnardi.luca.tests.R;
import begnardi.luca.utils.AutoCompletion;

/**
 * Created by begno on 17/02/15.
 */
public class SearchActivity extends ActionBarActivity implements ClientEventListener, SearchView.OnQueryTextListener, ListView.OnItemClickListener {

    ListView listView;
    ArrayList<String> suggestions = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    AutoCompletion ac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ac = new AutoCompletion();
        ac.addClientListener(this);

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                suggestions);

        listView = (ListView)(findViewById(R.id.search_list));
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        SearchView searchView = (SearchView)(menu.findItem(R.id.search_bar)).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", s);
        setResult(RESULT_OK, returnIntent);
        finish();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        arrayAdapter.clear();
        ac.setString(s);
        new Thread(ac).start();
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent returnIntent = new Intent();
        String s = (String) parent.getItemAtPosition(position);
        returnIntent.putExtra("result", s);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void eventHandler(ClientEvent ce) {
        if(ce.getSource().equals(ac) && ce instanceof ErrorEvent)
        {
            ErrorEvent error = (ErrorEvent)ce;
            ((MainActivity) getParent()).showErrorDialog(error.getMessage());
        }

        if(ce.getSource().equals(ac) && ce instanceof SuccessEvent)
        {
            suggestions = (ArrayList<String>) ((SuccessEvent)ce).getArrayList();
            runOnUiThread(new Runnable() {
                public void run() {
                    arrayAdapter.addAll(suggestions);
                }
            });
        }
    }
}