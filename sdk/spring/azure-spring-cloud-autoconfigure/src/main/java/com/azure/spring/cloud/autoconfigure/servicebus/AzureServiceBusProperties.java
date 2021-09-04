// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties(prefix = AzureServiceBusProperties.PREFIX)
public class AzureServiceBusProperties extends AzureProperties {

    public static final String PREFIX = "spring.cloud.azure.servicebus";

    // https://help.boomi.com/bundle/connectors/page/r-atm-Microsoft_Azure_Service_Bus_connection.html
    // https://docs.microsoft.com/en-us/rest/api/servicebus/addressing-and-protocol
    private String domainName = "servicebus.windows.net";

    private String namespace;

    private String connectionString;

    private boolean crossEntityTransactions;

    private AmqpClientProperties client = new AmqpClientProperties();

    public String getFQDN() {
        return this.namespace + "." + this.domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public boolean isCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    public void setCrossEntityTransactions(boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }

    public void setClient(AmqpClientProperties client) {
        this.client = client;
    }

    // TODO (xiada) we removed these properties, and not mark them as deprecated, should we mention them in the migration docs?
//    public AmqpRetryOptions getRetryOptions() {
//        return retryOptions;
//    }
//
//    public void setRetryOptions(AmqpRetryOptions retryOptions) {
//        this.retryOptions = retryOptions;
//    }
//
//    @DeprecatedConfigurationProperty(reason = "Use ", replacement = "")
//    public AmqpTransportType getTransportType() {
//        return transportType;
//    }
//
//    public void setTransportType(AmqpTransportType transportType) {
//        this.transportType = transportType;
//    }
}
