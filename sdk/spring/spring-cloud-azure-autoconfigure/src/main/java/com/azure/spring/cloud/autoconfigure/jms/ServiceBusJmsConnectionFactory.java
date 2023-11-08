// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;


import org.apache.qpid.jms.JmsConnectionFactory;

/**
 * ServiceBus JmsConnectionFactory implementation.
 */
public class ServiceBusJmsConnectionFactory extends JmsConnectionFactory {

    /**
     * Construct a {@link ServiceBusJmsConnectionFactory} instance with default value.
     */
    public ServiceBusJmsConnectionFactory() {
        super();
    }

    /**
     * Construct a {@link ServiceBusJmsConnectionFactory} instance with given remote uri.
     * @param remoteUri The remote uri.
     */
    public ServiceBusJmsConnectionFactory(String remoteUri) {
        super(remoteUri);
    }

    /**
     * Construct a {@link ServiceBusJmsConnectionFactory} instance with given remote uri, username and password.
     * @param userName The username.
     * @param password The password.
     * @param remoteUri The remote uri.
     */
    public ServiceBusJmsConnectionFactory(String userName, String password, String remoteUri) {
        super(userName, password, remoteUri);
    }
}
