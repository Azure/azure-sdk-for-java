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
}
