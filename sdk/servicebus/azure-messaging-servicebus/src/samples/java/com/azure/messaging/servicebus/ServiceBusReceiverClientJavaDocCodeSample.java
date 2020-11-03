// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverClient} scenarios.
 */
public class ServiceBusReceiverClientJavaDocCodeSample {
    /**
     * Code snippet for creating an ServiceBusReceiverClient
     *
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={eh-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key};"
                    + "Entity-Path={hub-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation

        receiver.close();
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session.
     */
    public void sessionReceiverSingleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
        sessionReceiver.close();
    }

    /**
     * Demonstrates how to create a session receiver for a single know session id.
     */
    public void sessionReceiverSessionIdInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        ServiceBusReceiverClient receiver = sessionReceiver.acceptSession("<< my-session-id >>");
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId

        sessionReceiver.close();
    }
}
