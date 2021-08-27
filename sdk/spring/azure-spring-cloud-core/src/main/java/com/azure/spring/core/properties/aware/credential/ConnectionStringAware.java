package com.azure.spring.core.properties.aware.credential;

public interface ConnectionStringAware {

    void setConnectionString(String connectionString);

    String getConnectionString();

}
