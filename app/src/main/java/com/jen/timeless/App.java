package com.jen.timeless;

import android.app.Application;


/**
 * Created by Administrator on 2016/3/14.
 */
public class App extends Application{

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
//        Configuration config = new Configuration.Builder()
//                .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认 256K
//                .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认 512K
//                .connectTimeout(10) // 链接超时。默认 10秒
//                .responseTimeout(60) // 服务器响应超时。默认 60秒
//                .recorder(recorder)  // recorder 分片上传时，已上传片记录器。默认 null
//                .recorder(recorder, keyGen)  // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
//                .zone(Zone.zone0) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。默认 Zone.zone0
//                .build();
//        // 重用 uploadManager。一般地，只需要创建一个 uploadManager 对象
//        UploadManager uploadManager = new UploadManager(config);

    }
}
