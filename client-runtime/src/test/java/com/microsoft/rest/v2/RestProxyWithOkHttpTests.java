package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.OkHttpAdapter;

public class RestProxyWithOkHttpTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        final okhttp3.OkHttpClient.Builder okHttpClientBuilder = new okhttp3.OkHttpClient.Builder();
        final okhttp3.OkHttpClient innerHttpClient = okHttpClientBuilder.build();
        return new OkHttpAdapter(innerHttpClient);
    }
}
