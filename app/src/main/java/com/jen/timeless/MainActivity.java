package com.jen.timeless;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.jen.timeless.utils.QiUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mPhotoPresenter
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        upLoadImageThread();
    }

    private void upLoadImageThread() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    upLoadImage();
                } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).run();
    }

    public interface UploadApi {

        @Multipart
        @POST("/")
        Call<JSONObject> upLoadFile(@Part("file") RequestBody  file, @Part("token") String token);
    }

    private void upLoadImage() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String name = "test.jpg";
        File directory = Environment.getExternalStorageDirectory();
        File imageFile = new File(directory, name);

        String BASE_URL = "http://up.qiniu.com";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RequestBody file = RequestBody.create(MediaType.parse("image/*"), imageFile);

        UploadApi apiService = retrofit.create(UploadApi.class);

        long deadline = System.currentTimeMillis() / 1000L + 3600;
        String bucket = "timeless";
        String scope = bucket + ":" + name;
        String token = QiUtils.getUpToken(deadline, scope);
        Call<JSONObject> jsonObjectCall = apiService.upLoadFile(file, token);
        jsonObjectCall.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                Log.e(TAG, "onResponse: " + response.message());
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }
}
