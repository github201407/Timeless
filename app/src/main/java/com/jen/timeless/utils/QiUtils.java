package com.jen.timeless.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.qiniu.android.utils.UrlSafeBase64;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by chenmingqun on 2016/3/14.
 */
public class QiUtils {

    private static final String SECRET_KEY = "5ac-9ahJ4XZ-1veIYpR7S5BXzZaIMN20qp_af7wY";
    private static final String ACCESS_KEY = "BuGr5bBZsBUdC-NAGKim9n52BfNPqhRo9fkzTUzP";

    @NonNull
    public static String getUpToken(long deadline, String scope) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
       return getUpToken(deadline, scope, SECRET_KEY, ACCESS_KEY);
    }

    public static String getUpToken(long deadline, String scope, String secretKey, String accessKey) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
//        long size = imageFile.length();
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opt);
//        int w = opt.outWidth;
//        int h = opt.outHeight;
//        String hash = Etag.file(imageFile);

        /* 1. 将上传策略序列化成为JSON格式：*/
//        ReturnBody returnBody = new ReturnBody("$(fname)", "$(fsize)", "$(imageInfo.width)", "$(imageInfo.height)", "$(etag)");
//        PutPolicy putPolicy = new PutPolicy(scope, deadline, returnBody);

        /* 2. 将上传策略序列化成为JSON格式：*/
//        String putPolicyJsonStr = JSON.toJSONString(putPolicy);
        String putPolicyJsonStr = "{\"scope\":\"" + scope + "\",\"deadline\":" + deadline + ",\"returnBody\":\"{\"name\":$(fname),\"size\":$(fsize),\"w\":$(imageInfo.width),\"h\":$(imageInfo.height),\"hash\":$(etag)}\"}";

        /* 3. 对JSON编码的上传策略进行URL安全的Base64编码，得到待签名字符串：*/
        String encodedPutPolicy = UrlSafeBase64.encodeToString(putPolicyJsonStr);

        /* 4. 使用SecretKey对上一步生成的待签名字符串计算HMAC-SHA1签名：*/
        byte[] sign = HmacSha1.hmacSha1byte(encodedPutPolicy, secretKey);

        /* 5. 对签名进行URL安全的Base64编码：*/
        String encodedSign = UrlSafeBase64.encodeToString(sign);

        /* 6. 将AccessKey、encodedSign和encodedPutPolicy用:连接起来：*/
        return accessKey + ':' + encodedSign + ':' + encodedPutPolicy;
    }

    public static void postImageFile(File filePath) throws IOException {
        /* input data*/
        Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());

        /*Static stuff*/
        String attachmentName = "bitmap";
        String attachmentFileName = "bitmap.bmp";
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        /*Setup the request:*/
        HttpURLConnection httpUrlConnection = null;
        URL url = new URL("http://upload.qiniu.com");
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        /*Start content wrapper:*/
        DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                attachmentName + "\";filename=\"" +
                attachmentFileName + "\"" + crlf);
        request.writeBytes(crlf);

       /* Convert Bitmap to ByteBuffer:*/
       //I want to send only 8 bit black & white bitmaps
        byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int i = 0; i < bitmap.getWidth(); ++i) {
            for (int j = 0; j < bitmap.getHeight(); ++j) {
                //we're interested only in the MSB of the first byte,
                //since the other 3 bytes are identical for B&W images
                pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
            }
        }
        request.write(pixels);

        /*End content wrapper:*/
        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

       /* Flush output buffer:*/
        request.flush();
        request.close();

       /* Get response:*/
        InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        String response = stringBuilder.toString();

        /*Close response stream:*/
        responseStream.close();

        /*Close the connection:*/
        httpUrlConnection.disconnect();
    }
}
