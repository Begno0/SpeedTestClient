package begnardi.luca.graphics;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.io.DBConnection;
import begnardi.luca.tests.R;

/**
 * Created by begno on 14/02/15.
 */

public class ChartFragment extends Fragment implements ClientEventListener {

    private static final String ARG_TAB_NUMBER = "tab_number";

    private DBConnection dBCon;
    private ArrayList<String> items;
    private GridView grid;
    private ProgressDialog progressDialog;

    public static ChartFragment newInstance(int sectionNumber) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChartFragment(){

    }

    public int getSection() {
        return getArguments().getInt(ARG_TAB_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chart, container, false);

        grid = (GridView) v.findViewById(R.id.grid_title);
        items = new ArrayList<String>();

        progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading results...", true);

        int section = getSection();
        switch(section) {
            case 1: {
                items.add("ISP,Download speed average (Mbps),Upload speed average (Mbps)");
            } break;
            case 2: {
                items.add("City,Download speed average (Mbps),Upload speed average (Mbps)");
            } break;
        }

        grid.setAdapter(new GridAdapter(items, true));
        grid = (GridView) v.findViewById(R.id.grid_view);

        dBCon = new DBConnection(section);
        dBCon.addClientListener(this);
        new Thread(dBCon).start();

        return v;
    }

    private static final class GridAdapter extends BaseAdapter {

        final ArrayList<String> mItems;
        final int mCount;
        final boolean title;

        /**
         * Default constructor
         * @param items to fill data to
         */

        private GridAdapter(final ArrayList<String> items, boolean title) {

            mCount = items.size() * 3;
            mItems = new ArrayList<String>(mCount);
            this.title = title;

            // for small size of items it's ok to do it here, sync way
            for (String item : items) {
                // get separate string parts, divided by ,
                final String[] parts = item.split(",");

                mItems.addAll(Arrays.asList(parts));
            }
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(final int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

            View mView = convertView;

            if (convertView == null) {
                if(title)
                    mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_title_item, parent, false);
                else
                    mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
            }

            final TextView text = (TextView) mView.findViewById(R.id.grid_text);

            text.setText(mItems.get(position));

            if(title)
                text.setBackgroundColor(Color.LTGRAY);

            return mView;
        }
    }

    @Override
    public void eventHandler(ClientEvent ce) {
        if(progressDialog != null)
            progressDialog.dismiss();
        if(ce.getSource().equals(dBCon) && ce instanceof ErrorEvent) {
            if(getSection() == 1)
                ((MainActivity) getActivity()).showErrorDialog(((ErrorEvent) ce).getMessage());
        }
        if(ce.getSource().equals(dBCon) && ce instanceof SuccessEvent) {
            items = (ArrayList<String>) ((SuccessEvent) ce).getArrayList();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    grid.setAdapter(new GridAdapter(items, false));
                }
            });
        }
    }
}