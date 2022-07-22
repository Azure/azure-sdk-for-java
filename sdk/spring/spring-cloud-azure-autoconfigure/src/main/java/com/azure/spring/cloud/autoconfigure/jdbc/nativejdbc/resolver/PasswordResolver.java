package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver;

public interface PasswordResolver<T> {

    T getPassword();
}
