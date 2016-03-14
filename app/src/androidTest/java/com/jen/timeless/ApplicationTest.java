package com.jen.timeless;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.jen.timeless.utils.HmacSha1;
import com.jen.timeless.utils.QiUtils;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.UrlSafeBase64;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends InstrumentationTestCase {
    private static final String TAG = "ApplicationTest";

    public void testGenToken() throws Exception {
        long deadline = System.currentTimeMillis() / 1000L;

        String bucket = "timeless";
        String name = "test.jpg";
        String scope = bucket + ":" + name;

        File directory = Environment.getExternalStorageDirectory();
        File imageFile = new File(directory, name);
        String uploadToken = QiUtils.getUpToken(deadline, scope);

        //
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());


    }


    public void testHmacSha1() throws Exception {
        String encodedPutPolicy = "eyJzY29wZSI6Im15LWJ1Y2tldDpzdW5mbG93ZXIuanBnIiwiZGVhZGxpbmUiOjE0NTE0OTEyMDAsInJldHVybkJvZHkiOiJ7XCJuYW1lXCI6JChmbmFtZSksXCJzaXplXCI6JChmc2l6ZSksXCJ3XCI6JChpbWFnZUluZm8ud2lkdGgpLFwiaFwiOiQoaW1hZ2VJbmZvLmhlaWdodCksXCJoYXNoXCI6JChldGFnKX0ifQ==";
        String secretKey = "MY_SECRET_KEY";
        /* 4. 使用SecretKey对上一步生成的待签名字符串计算HMAC-SHA1签名：*/
        byte[] sign = HmacSha1.hmacSha1byte(encodedPutPolicy, secretKey);

        /* 5. 对签名进行URL安全的Base64编码：*/
        String encodedSign = UrlSafeBase64.encodeToString(sign);

        String expectedEncodedSign = "wQ4ofysef1R7IKnrziqtomqyDvI=";
        Assert.assertEquals(expectedEncodedSign, encodedSign);

        /* 6. 将AccessKey、encodedSign和encodedPutPolicy用:连接起来：*/
        String accessKey = "MY_ACCESS_KEY";
        String uploadToken = accessKey + ':' + encodedSign + ':' + encodedPutPolicy;

        String expectedUploadToken = "MY_ACCESS_KEY:wQ4ofysef1R7IKnrziqtomqyDvI=:eyJzY29wZSI6Im15LWJ1Y2tldDpzdW5mbG93ZXIuanBnIiwiZGVhZGxpbmUiOjE0NTE0OTEyMDAsInJldHVybkJvZHkiOiJ7XCJuYW1lXCI6JChmbmFtZSksXCJzaXplXCI6JChmc2l6ZSksXCJ3XCI6JChpbWFnZUluZm8ud2lkdGgpLFwiaFwiOiQoaW1hZ2VJbmZvLmhlaWdodCksXCJoYXNoXCI6JChldGFnKX0ifQ==";
        Assert.assertEquals(expectedUploadToken, uploadToken);
    }

    public void testHmacSha1WithString() throws Exception {
        String base = "Computes";
        String key = "abc";
        String expected = "17324ac07fe47cc8cb151f59741445fe0baa98cb";
        String actual = HmacSha1.hmacSha1(base, key);
        Assert.assertEquals(expected, actual);
    }

    public void testEncode() throws UnsupportedEncodingException {
        String data = "c10e287f2b1e7f547b20a9ebce2aada26ab20ef2";
        String result = UrlSafeBase64.encodeToString(data);
//        String result = Base64.encodeToString(data.getBytes("utf-8"), Base64.DEFAULT);
        Assert.assertEquals("wQ4ofysef1R7IKnrziqtomqyDvI=", result);
    }

    public void testUpLoad() throws Throwable {

//        runTestOnUiThread(new Runnable() {
//            public void run() {
        String uploadToken = "BuGr5bBZsBUdC-NAGKim9n52BfNPqhRo9fkzTUzP:h0mXD9InPN3SVxokTSfu8bOyAE4=:eyJzY29wZSI6InRpbWVsZXNzIiwiZGVhZGxpbmUiOjE0NTc5NTE5MDB9";
        String name = "hello";
//                File directory = Environment.getExternalStorageDirectory();
//                File imageFile = new File(directory, name);
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(name.getBytes(), name, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                Log.e(TAG, "complete: " + key);
                Log.e(TAG, "complete: " + info.toString());
                Log.e(TAG, "complete: " + response.toString());
            }
        }, null);
//            }
//        });
    }

    public interface UploadApi {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

       /* @GET("/users/{username}")
        Call<User> getUser(@Path("username") String username);

        @GET("/group/{id}/users")
        Call<List<User>> groupList(@Path("id") int groupId, @Query("sort") String sort);

        @POST("/users/new")
        Call<User> createUser(@Body User user);*/

        @Multipart
        @POST("/some/endpoint")
        Call<JSONObject> upLoadFile(@Part("file") RequestBody file, @Part("token") String token);
    }

    public void testRetrofit() throws Exception {
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

    private void upLoadImage() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String name = "test.jpg";
        File directory = Environment.getExternalStorageDirectory();
        File imageFile = new File(directory, name);

        String BASE_URL = "http://upload.qiniu.com";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RequestBody file = RequestBody.create(MediaType.parse("image/*"), imageFile);

        UploadApi apiService = retrofit.create(UploadApi.class);

        long deadline = System.currentTimeMillis() / 1000L;
        String bucket = "timeless";
        String scope = bucket + ":" + name;
        String token = QiUtils.getUpToken(deadline, scope);
        Call<JSONObject> jsonObjectCall = apiService.upLoadFile(file, token);
//        Response<JSONObject> execute = jsonObjectCall.execute();
//        assertEquals(200, execute.code());
        jsonObjectCall.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                assertEquals(200, response.code());
                Log.e(TAG, "onResponse: " + response.message());
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    public void testUpTokenIsRight() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String name = "sunflower.jpg";
        long deadline = 1451491200;
        String bucket = "my-bucket";
        String scope = bucket + ":" + name;
        String secretKey = "MY_SECRET_KEY";
        String accessKey = "MY_ACCESS_KEY";
        String token = QiUtils.getUpToken(deadline, scope, secretKey, accessKey);
        String expected = "MY_ACCESS_KEY:wQ4ofysef1R7IKnrziqtomqyDvI=:eyJzY29wZSI6Im15LWJ1Y2tldDpzdW5mbG93ZXIuanBnIiwiZGVhZGxpbmUiOjE0NTE0OTEyMDAsInJldHVybkJvZHkiOiJ7XCJuYW1lXCI6JChmbmFtZSksXCJzaXplXCI6JChmc2l6ZSksXCJ3XCI6JChpbWFnZUluZm8ud2lkdGgpLFwiaFwiOiQoaW1hZ2VJbmZvLmhlaWdodCksXCJoYXNoXCI6JChldGFnKX0ifQ==";
        Assert.assertEquals(expected, token);
    }

    public void testIsEqual() {
        String name = "sunflower.jpg";
        long deadline = 1451491200;
        String bucket = "my-bucket";
        String scope = bucket + ":" + name;
        String putPolicyJsonStr = "{\"scope\":\"" + scope + "\",\"deadline\":" + deadline + ",\"returnBody\":\"{\"name\":$(fname),\"size\":$(fsize),\"w\":$(imageInfo.width),\"h\":$(imageInfo.height),\"hash\":$(etag)}\"}";
        String putPolicy = "{\"scope\":\"my-bucket:sunflower.jpg\",\"deadline\":1451491200,\"returnBody\":\"{\\\"name\\\":$(fname),\\\"size\\\":$(fsize),\\\"w\\\":$(imageInfo.width),\\\"h\\\":$(imageInfo.height),\\\"hash\\\":$(etag)}\"}";
//        Assert.assertEquals(putPolicy, putPolicyJsonStr);

        String encodedPutPolicy1 = UrlSafeBase64.encodeToString(putPolicyJsonStr);
        String encodedPutPolicy2 = UrlSafeBase64.encodeToString(putPolicy);
        String expected = "eyJzY29wZSI6Im15LWJ1Y2tldDpzdW5mbG93ZXIuanBnIiwiZGVhZGxpbmUiOjE0NTE0OTEyMDAsInJldHVybkJvZHkiOiJ7XCJuYW1lXCI6JChmbmFtZSksXCJzaXplXCI6JChmc2l6ZSksXCJ3XCI6JChpbWFnZUluZm8ud2lkdGgpLFwiaFwiOiQoaW1hZ2VJbmZvLmhlaWdodCksXCJoYXNoXCI6JChldGFnKX0ifQ==";
        assertEquals(expected,encodedPutPolicy2);
    }
}