package com.azure.spring.core.properties.aware.credential;

public interface KeyAware {

    void setKey(String key);

    String getKey();
}
