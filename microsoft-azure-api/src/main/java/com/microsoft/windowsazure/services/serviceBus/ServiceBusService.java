package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.services.core.Configuration;

/**
 * 
 * Access service bus functionality.
 * 
 */
public class ServiceBusService {

    private ServiceBusService() {
        // class is not instantiated
    }

    /**
     * Creates an instance of the <code>ServiceBusContract</code> API.
     * 
     */
    public static ServiceBusContract create() {
        return Configuration.getInstance().create(ServiceBusContract.class);
    }

    /**
     * Creates an instance of the <code>ServiceBusContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static ServiceBusContract create(Configuration config) {
        return config.create(ServiceBusContract.class);
    }

    /**
     * Creates an instance of the <code>ServiceBusContract</code> API.
     * 
     */
    public static ServiceBusContract create(String profile) {
        return Configuration.getInstance().create(profile, ServiceBusContract.class);
    }

    /**
     * Creates an instance of the <code>ServiceBusContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static ServiceBusContract create(String profile, Configuration config) {
        return config.create(profile, ServiceBusContract.class);
    }
}
