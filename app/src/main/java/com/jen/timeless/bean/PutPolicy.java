package com.jen.timeless.bean;

/**
 * Created by Administrator on 2016/3/14.
 */
public class PutPolicy {
    public final String scope;
    public final long deadline;
    public final ReturnBody returnBody;

    public PutPolicy(String scope, long deadline, ReturnBody returnBody) {
        this.scope = scope;
        this.deadline = deadline;
        this.returnBody = returnBody;
    }
}
