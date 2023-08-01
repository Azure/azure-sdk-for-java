// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class DomainCredentials implements ServiceClientCredentials {
    private String domainKey;

    public DomainCredentials(String domainKey) {
        this.domainKey = domainKey;
    }

    @Override
    public void applyCredentialsFilter(Builder clientBuilder) {
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("aeg-sas-key", domainKey).build();
                return chain.proceed(newRequest);
            }
        });
    }
}
