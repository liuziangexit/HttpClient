package com.game.http;

import java.util.Map;

public interface IHttpRequestCallback {

    // 可以抛出异常
    void onRespond(int statusCode, Map<String, String> headers, String body);

    // 绝不能抛出异常
    void onFailed(Throwable arg0);

}
