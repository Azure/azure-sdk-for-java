// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

public class SpyClientBuilder extends AsyncDocumentClient.Builder {

    public SpyClientBuilder(AsyncDocumentClient.Builder builder) {
        super();
        super.configs = builder.configs;
        super.connectionPolicy = builder.connectionPolicy;
        super.desiredConsistencyLevel = builder.desiredConsistencyLevel;
        super.masterKeyOrResourceToken = builder.masterKeyOrResourceToken;
        super.serviceEndpoint = builder.serviceEndpoint;
        super.azureKeyCredential = builder.azureKeyCredential;
        super.contentResponseOnWriteEnabled = builder.contentResponseOnWriteEnabled;
    }

    public SpyClientUnderTestFactory.ClientUnderTest build() {
        return SpyClientUnderTestFactory.createClientUnderTest(
            serviceEndpoint,
            masterKeyOrResourceToken,
            connectionPolicy,
            desiredConsistencyLevel,
            configs,
                azureKeyCredential,
            contentResponseOnWriteEnabled);
    }

    public SpyClientUnderTestFactory.ClientWithGatewaySpy buildWithGatewaySpy() {
        return SpyClientUnderTestFactory.createClientWithGatewaySpy(
            serviceEndpoint,
            masterKeyOrResourceToken,
            connectionPolicy,
            desiredConsistencyLevel,
            configs,
                azureKeyCredential,
            contentResponseOnWriteEnabled);
    }

    public SpyClientUnderTestFactory.DirectHttpsClientUnderTest buildWithDirectHttps() {
        return SpyClientUnderTestFactory.createDirectHttpsClientUnderTest(
            serviceEndpoint,
            masterKeyOrResourceToken,
            connectionPolicy,
            desiredConsistencyLevel,
                azureKeyCredential,
            contentResponseOnWriteEnabled);
    }
}
