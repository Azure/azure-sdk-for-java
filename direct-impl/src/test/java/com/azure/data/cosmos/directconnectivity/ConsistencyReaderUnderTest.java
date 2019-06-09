/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import org.mockito.Mockito;

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
        super(new Configs(), addressSelector, sessionContainer, transportClient, serviceConfigReader, authorizationTokenProvider);

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
    QuorumReader createQuorumReader(TransportClient transportClient,
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
    StoreReader createStoreReader(TransportClient transportClient,
                                  AddressSelector addressSelector,
                                  ISessionContainer sessionContainer) {
        this.origStoreReader = new StoreReaderUnderTest(transportClient, addressSelector, sessionContainer);
        this.spyStoreReader = Mockito.spy(this.origStoreReader);
        return this.spyStoreReader;
    }
}
