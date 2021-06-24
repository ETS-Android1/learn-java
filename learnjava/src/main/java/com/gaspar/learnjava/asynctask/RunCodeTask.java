package com.gaspar.learnjava.asynctask;

import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.playground.PlaygroundActivity;
import com.gaspar.learnjava.playground.ProgramPayload;
import com.gaspar.learnjava.playground.ProgramResponse;
import com.gaspar.learnjava.playground.RunApi;
import com.gaspar.learnjava.utils.LogUtils;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A background task that sends code the REST API to get it compiled and run. A dialog is
 * shown in the {@link com.gaspar.learnjava.playground.PlaygroundActivity} while this task is
 * in progress.
 */
public class RunCodeTask extends LjAsyncTask<RunCodeTask.Result> {

    /**
     * The {@link ProgramPayload} that is sent to the API.
     */
    private final ProgramPayload programPayload;

    /**
     * Creates a code runner task.
     * @param programPayload The {@link ProgramPayload}.
     */
    public RunCodeTask(ProgramPayload programPayload) {
        this.programPayload = programPayload;
    }

    /**
     * Performs the API call in the background and waits for the result.
     * @param objects Expected to be {@link PlaygroundActivity}.
     * @return The {@link Result}.
     */
    @Override
    protected Result doInBackground(@Size(1) Object... objects) {
        PlaygroundActivity activity = (PlaygroundActivity) objects[0];
        Retrofit retrofit = RunApi.getRetrofit();
        RunApi runApi = retrofit.create(RunApi.class);
        //select and API key randomly
        Random random = new Random();
        List<String> apiKeys = Arrays.asList(
                activity.getString(R.string.run_api_key_1),
                activity.getString(R.string.run_api_key_2),
                activity.getString(R.string.run_api_key_3)
        );
        String apiKey = apiKeys.get(random.nextInt(apiKeys.size()));
        apiKey = "Token " + apiKey;
        Call<ProgramResponse> programResponseCall = runApi.compileAndRunCode(programPayload, apiKey);

        ProgramResponse programResponse = null;
        String errorMessage = null;
        if(PlaygroundActivity.mockRunApi || "Token mock_api_key".equals(apiKey)) {
            //this is a mock api call
            LogUtils.log("Mocking run API...");
            programResponse = RunApi.compileAndRunCodeMock(programPayload);
        } else {
            //this is a real API call
            try {
                Response<ProgramResponse> apiResponse = programResponseCall.execute();
                if(apiResponse.isSuccessful()) {
                    programResponse = apiResponse.body();
                    LogUtils.log("Received successful response from run API.");
                } else {
                    LogUtils.logError("HTTP error response from API: " + apiResponse.code());
                    LogUtils.logError(apiResponse.errorBody().string());
                    //fail HTTP code
                    if(apiResponse.code() >= 500) {
                        errorMessage = activity.getString(R.string.playground_500_code);
                    } else if(apiResponse.code() == 401) {
                        errorMessage = activity.getString(R.string.playground_401_code);
                    } else {
                        errorMessage = activity.getString(R.string.playground_misc_code, String.valueOf(apiResponse.code()));
                    }
                }
            } catch (Throwable throwable) {
                //some error occurred while handling the network call
                if(throwable instanceof SocketTimeoutException) {
                    errorMessage = activity.getString(R.string.playground_timeout);
                    LogUtils.logError("Timeout while getting response from API!", (SocketTimeoutException) throwable);
                } else {
                    errorMessage = activity.getString(R.string.playground_misc_code, throwable.getClass().getSimpleName());
                    LogUtils.logError("Exception while getting response from API: " + throwable.getClass().getSimpleName());
                }
            }
        }
        return new Result(activity, programResponse, errorMessage);
    }

    /**
     * Called after networking is done, shows the error message if necessary. In case of no errors,
     * it hides the loading dialog and jumps to the output fragment of {@link PlaygroundActivity}.
     * @param result The result.
     */
    @Override
    protected void onPostExecute(Result result) {
        result.activity.findViewById(R.id.playgroundRunButton).setEnabled(true);
        if(result.errorMessage != null) {
            //there was an error of some kind
            result.activity.setLoadingDialogMessage(result.errorMessage);
        } else {
            //completed successfully
            result.activity.hideLoadingDialog();
            //go to output fragment
            result.activity.moveToOutputFragment();
            result.activity.getSupportFragmentManager().executePendingTransactions();
            //set text
            result.activity.sendDataToOutputFragment(result.programResponse);
        }
    }

    /**
     * Helper to store the results of the background task.
     */
    static class Result {

        /**
         * The playground activity.
         */
        public PlaygroundActivity activity;

        /**
         * Response that came from the server. If there was no response, this is null.
         */
        @Nullable
        public ProgramResponse programResponse;

        /**
         * An error message to displays if an error occurred. This is null if there was no error.
         */
        @Nullable
        public String errorMessage;

        /**
         * Creates a result object.
         * @param activity The playground activity.
         * @param programResponse Response that came from the server. If there was no response, this is null.
         * @param errorMessage An error message to displays if an error occurred. This is null if there was no error.
         */
        public Result(PlaygroundActivity activity, @Nullable ProgramResponse programResponse, @Nullable String errorMessage) {
            this.activity = activity;
            this.programResponse = programResponse;
            this.errorMessage = errorMessage;
        }
    }

}
