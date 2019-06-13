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
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RxDocumentClientUnderTest;
import com.azure.data.cosmos.internal.directconnectivity.ReflectionUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientUnderTestBuilder extends CosmosClientBuilder {

    public ClientUnderTestBuilder(CosmosClientBuilder builder) {
        this.configs(builder.getConfigs());
        this.connectionPolicy(builder.getConnectionPolicy());
        this.consistencyLevel(builder.getDesiredConsistencyLevel());
        this.key(builder.getKeyOrResourceToken());
        this.endpoint(builder.getServiceEndpoint());
    }

    @Override
    public CosmosClient build() {
        RxDocumentClientUnderTest rxClient;
        try {
            rxClient = new RxDocumentClientUnderTest(
                new URI(this.getServiceEndpoint()),
                this.getKeyOrResourceToken(),
                this.getConnectionPolicy(),
                this.getDesiredConsistencyLevel(),
                this.getConfigs());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        CosmosClient cosmosClient = super.build();
        ReflectionUtils.setAsyncDocumentClient(cosmosClient, rxClient);
        return cosmosClient;
    }
}
