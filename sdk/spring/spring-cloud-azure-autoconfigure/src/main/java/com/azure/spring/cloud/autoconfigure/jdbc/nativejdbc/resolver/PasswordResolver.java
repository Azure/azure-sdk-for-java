package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver;

/**
 * Interface to be implemented by classes that wish to provide the password.
 */
public interface PasswordResolver<T> {

    T getPassword();
}
