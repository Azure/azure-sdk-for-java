package com.azure.spring.cloud.autoconfigure.jdbc.provider;


interface AccessTokenProvider<T> {
    T getAccessToken();
}
