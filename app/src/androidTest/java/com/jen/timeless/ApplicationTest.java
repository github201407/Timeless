package com.jen.timeless;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jen.timeless.bean.PutPolicy;
import com.jen.timeless.bean.ReturnBody;
import com.jen.timeless.utils.HmacSha1;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.Etag;
import com.qiniu.android.utils.UrlSafeBase64;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

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
        Assert.assertTrue(imageFile.exists());
        long size = imageFile.length();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opt);
        int w = opt.outWidth;
        int h = opt.outHeight;
        String hash = Etag.file(imageFile);

        /* 1. 将上传策略序列化成为JSON格式：*/
        ReturnBody returnBody = new ReturnBody(bucket, name, size, w, h, hash);
        PutPolicy putPolicy = new PutPolicy(scope, deadline, returnBody);

        /* 2. 将上传策略序列化成为JSON格式：*/
        String putPolicyJsonStr = JSON.toJSONString(putPolicy);

        /* 3. 对JSON编码的上传策略进行URL安全的Base64编码，得到待签名字符串：*/
        String encodedPutPolicy = UrlSafeBase64.encodeToString(putPolicyJsonStr);
        String secretKey = "5ac-9ahJ4XZ-1veIYpR7S5BXzZaIMN20qp_af7wY";

        /* 4. 使用SecretKey对上一步生成的待签名字符串计算HMAC-SHA1签名：*/
        byte[] sign = HmacSha1.hmacSha1byte(encodedPutPolicy, secretKey);

        /* 5. 对签名进行URL安全的Base64编码：*/
        String encodedSign = UrlSafeBase64.encodeToString(sign);

        /* 6. 将AccessKey、encodedSign和encodedPutPolicy用:连接起来：*/
        String accessKey = "BuGr5bBZsBUdC-NAGKim9n52BfNPqhRo9fkzTUzP";
        String uploadToken = accessKey + ':' + encodedSign + ':' + encodedPutPolicy;


        UploadManager uploadManager = new UploadManager();
        uploadManager.put(imageFile, name, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                Log.e(TAG, "complete: " + key);
                Log.e(TAG, "complete: " + info.toString());
                Log.e(TAG, "complete: " + response.toString());
            }
        }, null);

    }

    public void testHmacSha1() throws Exception{
        String encodedPutPolicy = "eyJzY29wZSI6Im15LWJ1Y2tldDpzdW5mbG93ZXIuanBnIiwiZGVhZGxpbmUiOjE0NTE0OTEyMDAsInJldHVybkJvZHkiOiJ7XCJuYW1lXCI6JChmbmFtZSksXCJzaXplXCI6JChmc2l6ZSksXCJ3XCI6JChpbWFnZUluZm8ud2lkdGgpLFwiaFwiOiQoaW1hZ2VJbmZvLmhlaWdodCksXCJoYXNoXCI6JChldGFnKX0ifQ==";
        String secretKey = "MY_SECRET_KEY";
        /* 4. 使用SecretKey对上一步生成的待签名字符串计算HMAC-SHA1签名：*/
        byte[] sign = HmacSha1.hmacSha1byte(encodedPutPolicy, secretKey);

//        String expectedSign = "c10e287f2b1e7f547b20a9ebce2aada26ab20ef2";
//        Assert.assertEquals(expectedSign, sign);

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

    public void testHmacSha1WithString() throws Exception{
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
}