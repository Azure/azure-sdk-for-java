// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;


import org.apache.qpid.jms.JmsConnectionFactory;

/**
 * ServiceBus JmsConnectionFactory implementation.
 */
public class ServiceBusJmsConnectionFactory extends JmsConnectionFactory {
    public ServiceBusJmsConnectionFactory() {
        super();
    }

    public ServiceBusJmsConnectionFactory(String remoteURI) {
        super(remoteURI);
    }

    public ServiceBusJmsConnectionFactory(String userName, String password, String remoteURI) {
        super(userName, password, remoteURI);
    }
}
