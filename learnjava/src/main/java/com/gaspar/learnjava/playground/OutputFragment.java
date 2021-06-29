package com.gaspar.learnjava.playground;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.formatter.Formatter;
import com.gaspar.learnjava.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A {@link Fragment} that displays the output of the program to the user in {@link PlaygroundActivity}.
 * Use the {@link OutputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutputFragment extends Fragment {

    /**
     * A formatter that is used to create multi lined, properly tabulated text.
     */
    private Formatter formatter;

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
        /*
        if (getArguments() != null) {
            //can process arguments here
        }
        */
        formatter = new Formatter();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View outputView = inflater.inflate(R.layout.fragment_output, container, false);
        //add listeners
        ImageView stdoutZoomIn = outputView.findViewById(R.id.stdoutZoomIn);
        ImageView stdoutZoomOut = outputView.findViewById(R.id.stdoutZoomOut);
        TextView stdout = outputView.findViewById(R.id.playgroundStdout);
        stdoutZoomIn.setOnClickListener(view -> CodeFragment.playgroundZoomInOnClick(stdoutZoomIn, stdoutZoomOut, stdout));
        stdoutZoomOut.setOnClickListener(view -> CodeFragment.playgroundZoomOutOnClick(stdoutZoomIn, stdoutZoomOut, stdout));

        ImageView stderrZoomIn = outputView.findViewById(R.id.stderrZoomIn);
        ImageView stderrZoomOut = outputView.findViewById(R.id.stderrZoomOut);
        TextView stderr = outputView.findViewById(R.id.playgroundStderr);
        stderrZoomIn.setOnClickListener(view -> CodeFragment.playgroundZoomInOnClick(stderrZoomIn, stderrZoomOut, stderr));
        stderrZoomOut.setOnClickListener(view -> CodeFragment.playgroundZoomOutOnClick(stderrZoomIn, stderrZoomOut, stderr));

        ImageView exceptionZoomIn = outputView.findViewById(R.id.exceptionZoomIn);
        ImageView exceptionZoomOut = outputView.findViewById(R.id.exceptionZoomOut);
        TextView exception = outputView.findViewById(R.id.playgroundException);
        exceptionZoomIn.setOnClickListener(view -> CodeFragment.playgroundZoomInOnClick(exceptionZoomIn, exceptionZoomOut, exception));
        exceptionZoomOut.setOnClickListener(view -> CodeFragment.playgroundZoomOutOnClick(exceptionZoomIn, exceptionZoomOut, exception));

        //set powered by
        TextView poweredByView = outputView.findViewById(R.id.poweredByTextView);
        String poweredBy = outputView.getContext().getString(R.string.playground_powered_by);
        poweredBy = poweredBy + " <a href=\"https://glot.io/\">Glot.io</a>";
        poweredByView.setText(Html.fromHtml(poweredBy, Html.FROM_HTML_MODE_COMPACT));
        poweredByView.setMovementMethod(LinkMovementMethod.getInstance());
        return outputView;
    }

    /**
     * Called when the {@link com.gaspar.learnjava.asynctask.RunCodeTask} finishes its work and the results of the
     * program are received.
     * @param programResponse The program response, containing the stdout, stderr and exceptions.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull ProgramResponse programResponse) {
        setStdout(programResponse.stdout);
        setStderr(programResponse.stderr);
        setExceptions(programResponse.error);
    }

    /**
     * Displays the given text in the stdout section of the output fragment.
     * @param stdoutContent The contents of stdout.
     */
    public void setStdout(String stdoutContent) {
        if(getView() == null) {
            LogUtils.logError("View was null when calling setStdout! Not setting anything.");
            return;
        }
        TextView stdout = getView().findViewById(R.id.playgroundStdout);
        String formattedStdout = formatter.formatWhitespaces(stdoutContent);
        stdout.setText(Html.fromHtml(formattedStdout, Html.FROM_HTML_MODE_COMPACT));
    }

    /**
     * Displays the given text in the stderr section of the output fragment.
     * @param stderrContent The contents of stderr.
     */
    public void setStderr(String stderrContent) {
        if(getView() == null) {
            LogUtils.logError("View was null when calling setStderr! Not setting anything.");
            return;
        }
        TextView stderr = getView().findViewById(R.id.playgroundStderr);
        String formattedStderr = formatter.formatWhitespaces(stderrContent);
        stderr.setText(Html.fromHtml(formattedStderr, Html.FROM_HTML_MODE_COMPACT));
    }

    /**
     * Displays the given text in the exceptions section of the output fragment.
     * @param exceptionsContent The exceptions as a string.
     */
    public void setExceptions(String exceptionsContent) {
        if(getView() == null) {
            LogUtils.logError("View was null when calling setExceptions! Not setting anything.");
            return;
        }
        TextView exceptions = getView().findViewById(R.id.playgroundException);
        String formattedExceptions = formatter.formatWhitespaces(exceptionsContent);
        exceptions.setText(Html.fromHtml(formattedExceptions, Html.FROM_HTML_MODE_COMPACT));
    }
}