package com.gaspar.learnjava.playground;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit interface that defines the methods for the API that can compile and run code. API mocking is
 * also implemented here.
 * @see ProgramPayload
 * @see ProgramResponse
 */
public interface RunApi {

    /**
     * The amount of milliseconds the mock API call waits.
     */
    int MOCK_TIME = 3000;

    /**
     * Timeout amounts for the API calls, in seconds.
     */
    int TIMEOUT = 5;

    /**
     * Base URL for the run API.
     */
    String BASE_URL = "https://glot.io";

    /**
     * Creates a retrofit client, that is used to access the API.
     * @return The retrofit client.
     */
    static Retrofit getRetrofit() {
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS);
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .callFactory(okHttpBuilder.build())
                .build();
    }

    /**
     * Send a {@link ProgramPayload} to the API to get it compiled and run.
     * @param programPayload The payload, containing the list of files and optionally the standard input.
     * @param apiKey API key to access the run API. This must be concatenated from 'Token ' and the api key from the string resources.
     * @return The {@link ProgramResponse} containing the results.
     */
    @POST(value = "/api/run/java/latest")
    Call<ProgramResponse> compileAndRunCode(@NonNull @Body ProgramPayload programPayload, @Header("Authorization") String apiKey);

    /**
     * The mock API call returns this stdout.
     */
    String MOCK_STDOUT = "Mock stdout!";

    /**
     * The mock API call returns this stderr.
     */
    String MOCK_STDERR = "Mock stderr!";

    /**
     * The mock API call returns this exit code.
     */
    String MOCK_EXIT_CODE = "Mock exit code 0!";

    /**
     * A method that mocks the actual API call, {@link #compileAndRunCode(ProgramPayload, String)}. This is used in testing, and
     * when the build variant 'noads' is active.
     * @param programPayload Won't be used, a mock response will be returned.
     * @return A mock response.
     */
    static ProgramResponse compileAndRunCodeMock(@NonNull ProgramPayload programPayload) {
        //simulate waiting
        try {
            Thread.sleep(MOCK_TIME);
        } catch (InterruptedException ignored) {}
        ProgramResponse programResponse = new ProgramResponse();
        programResponse.stdout = MOCK_STDOUT;
        programResponse.stderr = MOCK_STDERR;
        programResponse.error = MOCK_EXIT_CODE;
        return programResponse;
    }
}
