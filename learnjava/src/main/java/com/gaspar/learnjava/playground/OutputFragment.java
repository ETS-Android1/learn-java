package com.gaspar.learnjava.playground;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.gaspar.learnjava.R;

/**
 * A {@link Fragment} that displays the output of the program to the user in {@link PlaygroundActivity}.
 * Use the {@link OutputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutputFragment extends Fragment {

    public OutputFragment() { }  // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment OutputFragment.
     */
    public static OutputFragment newInstance() {
        OutputFragment fragment = new OutputFragment();
        Bundle args = new Bundle();
        //can add arguments here
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //can process arguments here
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_output, container, false);
    }
}