package com.yui.libhttp.interceptor;

import android.content.Context;

import com.yui.libbase.utils.NetworkUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 缓存拦截器
 *
 * @author liaoyuhuan
 * @date on  2018/1/23
 * @email
 * @org
 * @describe 添加描述
 */


public class CacheInterceptor implements Interceptor {
    Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        /**如果网络没有连接则设为强制缓存*/
        if (!NetworkUtils.isConnected(context)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        Response response = chain.proceed(request);
        if (NetworkUtils.isConnected(context)) {
            int maxAge = 0;
            /**有网络时 设置缓存超时时间0个小时*/
            response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    /**清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效*/
                    .removeHeader("Pragma")
                    .build();
        } else {
            /**无网络时，设置超时为7天*/
            int maxStale = 60 * 60 * 24 * 7;
            response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();
        }
        return response;

    }
}
