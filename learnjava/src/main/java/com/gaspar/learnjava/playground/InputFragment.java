package com.gaspar.learnjava.playground;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.LogUtils;

/**
 * A {@link Fragment} which provides the user with ways to enter input, inside {@link PlaygroundActivity}.
 * Use the {@link InputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InputFragment extends Fragment {

    public InputFragment() { }  // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment InputFragment.
     */
    public static InputFragment newInstance() {
        InputFragment fragment = new InputFragment();
        Bundle args = new Bundle();
        //can add arguments here
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            //can use arguments here
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inputView = inflater.inflate(R.layout.fragment_input, container, false);
        //add listeners
        ImageButton zoomInButton = inputView.findViewById(R.id.inputZoomInButton);
        ImageButton zoomOutButton = inputView.findViewById(R.id.inputZoomOutButton);
        final EditText inputArea = inputView.findViewById(R.id.playgroundInputArea);
        zoomInButton.setOnClickListener(view -> CodeFragment.playgroundZoomInOnClick(zoomInButton, zoomOutButton, inputArea));
        zoomOutButton.setOnClickListener(view -> CodeFragment.playgroundZoomOutOnClick(zoomInButton, zoomOutButton, inputArea));

        ImageButton deleteButton = inputView.findViewById(R.id.inputDeleteButton);
        deleteButton.setOnClickListener(view -> inputArea.setText(""));
        return inputView;
    }

    /**
     * Gets the input that the user typed in.
     * @return The input.
     */
    public String getInput() {
        if(getView() == null) {
            LogUtils.logError("View wa null when calling getInput! Returning empty string...");
            return "";
        }
        EditText inputArea = getView().findViewById(R.id.playgroundInputArea);
        return inputArea.getText().toString();
    }
}