package com.azure.spring.cloud.autoconfigure.jdbc.resolver;

public interface PasswordResolver<T> {

    T getPassword();
}
