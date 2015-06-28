package icechen1.com.blackbox.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import icechen1.com.blackbox.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecordActivityFragment extends Fragment {

    public RecordActivityFragment() {
    }

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }
}
