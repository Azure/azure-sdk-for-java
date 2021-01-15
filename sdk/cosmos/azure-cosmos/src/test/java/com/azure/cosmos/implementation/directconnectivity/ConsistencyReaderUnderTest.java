// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import org.mockito.Mockito;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ConsistencyReaderUnderTest extends ConsistencyReader {
    private QuorumReader origQuorumReader;
    private QuorumReader spyQuorumReader;

    private StoreReaderUnderTest origStoreReader;
    private StoreReaderUnderTest spyStoreReader;

    public ConsistencyReaderUnderTest(AddressSelector addressSelector,
                                      ISessionContainer sessionContainer,
                                      TransportClient transportClient,
                                      GatewayServiceConfigurationReader serviceConfigReader,
                                      IAuthorizationTokenProvider authorizationTokenProvider) {
        super(mockDiagnosticsClientContext(), new Configs(), addressSelector, sessionContainer, transportClient, serviceConfigReader, authorizationTokenProvider);

    }

    public QuorumReader getOrigQuorumReader() {
        return origQuorumReader;
    }

    public QuorumReader getSpyQuorumReader() {
        return spyQuorumReader;
    }

    public StoreReaderUnderTest getOrigStoreReader() {
        return origStoreReader;
    }

    public StoreReaderUnderTest getSpyStoreReader() {
        return spyStoreReader;
    }

    @Override
    public QuorumReader createQuorumReader(TransportClient transportClient,
                                    AddressSelector addressSelector,
                                    StoreReader storeReader,
                                    GatewayServiceConfigurationReader serviceConfigurationReader,
                                    IAuthorizationTokenProvider authorizationTokenProvider) {
        this.origQuorumReader = super.createQuorumReader(transportClient,
                                                         addressSelector,
                                                         storeReader,
                                                         serviceConfigurationReader,
                                                         authorizationTokenProvider);
        this.spyQuorumReader = Mockito.spy(this.origQuorumReader);
        return this.spyQuorumReader;
    }

    @Override
    public StoreReader createStoreReader(TransportClient transportClient,
                                  AddressSelector addressSelector,
                                  ISessionContainer sessionContainer) {
        this.origStoreReader = new StoreReaderUnderTest(transportClient, addressSelector, sessionContainer);
        this.spyStoreReader = Mockito.spy(this.origStoreReader);
        return this.spyStoreReader;
    }
}
