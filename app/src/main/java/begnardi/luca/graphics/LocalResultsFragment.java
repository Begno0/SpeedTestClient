package begnardi.luca.graphics;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import begnardi.luca.events.ClientEvent;
import begnardi.luca.events.ClientEventListener;
import begnardi.luca.events.ErrorEvent;
import begnardi.luca.events.SuccessEvent;
import begnardi.luca.io.FileReadWrite;
import begnardi.luca.tests.R;

/**
 * Created by begno on 14/02/15.
 */

public class LocalResultsFragment extends Fragment implements ClientEventListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private FileReadWrite readWrite;
    private ListView list;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static LocalResultsFragment newInstance(int sectionNumber) {
        LocalResultsFragment fragment = new LocalResultsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LocalResultsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_results_local, container, false);

        list = (ListView) v.findViewById(R.id.list_view);
        getLocalResults();

        return v;
    }

    public void getLocalResults() {

        readWrite = new FileReadWrite("/storage/sdcard0/Test/results.csv");
        readWrite.addClientListener(this);
        readWrite.fileRead();
    }

    private static final class ListAdapter extends BaseAdapter {

        final ArrayList<String> mItems;
        final int mCount;

        /**
         * Default constructor
         *
         * @param items to fill data to
         */

        private ListAdapter(final ArrayList<String> items) {
            mItems = items;
            mCount = items.size();
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
                mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_result_item, parent, false);
            }

            String rArray[] = mItems.get(position).split(",");

            TextView text = (TextView) mView.findViewById(R.id.text_isp);
            text.setText(rArray[0]);

            text = (TextView) mView.findViewById(R.id.text_date);
            text.setText(rArray[6]);

            text = (TextView) mView.findViewById(R.id.text_download);
            text.setText(rArray[1] + " Mbps");

            text = (TextView) mView.findViewById(R.id.text_upload);
            text.setText(rArray[2] + " Mbps");

            text = (TextView) mView.findViewById(R.id.text_city);
            text.setText(rArray[3]);

            return mView;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void eventHandler(ClientEvent ce) {
        if (ce.getSource().equals(readWrite) && ce instanceof SuccessEvent) {

            ArrayList<String> results = (ArrayList<String>) ((SuccessEvent) ce).getArrayList();
            final ArrayList<String> sArrayList = new ArrayList<String>();
            if (results.size() != 0) {
                for (int i = results.size() - 1; i >= 0; i--) {
                    String r = results.get(i);
                    sArrayList.add(r);
                }
                getActivity().runOnUiThread(new Runnable(){
                    public void run() {
                        if(list != null)
                            list.setAdapter(new ListAdapter(sArrayList));
                    }
                });
            }
            else
                ((MainActivity) getActivity()).showErrorDialog("No results found.");
        }
        if(ce.getSource().equals(readWrite) && ce instanceof ErrorEvent){
            ((MainActivity) getActivity()).showErrorDialog(((ErrorEvent) ce).getMessage());
        }
    }
}