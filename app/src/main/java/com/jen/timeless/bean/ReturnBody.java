package com.jen.timeless.bean;

/**
 * Created by Administrator on 2016/3/14.
 */
public class ReturnBody {

    public final String bucket;
    public final String name;
    public final long size;
    public final int w;
    public final int h;
    public final String hash;

    public ReturnBody(String bucket, String name, long size, int w, int h, String hash) {

        this.bucket = bucket;
        this.name = name;
        this.size = size;
        this.w = w;
        this.h = h;
        this.hash = hash;
    }
}
