package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.provider;


interface AccessTokenProvider<T> {
    T getAccessToken();
}
