package com.azure.spring.core.properties.aware.credential;

public interface SasTokenAware {

    void setSasToken(String sasToken);

    String getSasToken();

}
