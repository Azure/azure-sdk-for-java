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

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.rx.FailureValidator;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.net.UnknownHostException;
import java.time.Instant;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class NetworkFailureTest extends TestSuiteBase {
    private static final int TIMEOUT = ClientRetryPolicy.MaxRetryCount * ClientRetryPolicy.RetryIntervalInMS + 60000;
    private final DocumentCollection collectionDefinition;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public NetworkFailureTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
        this.collectionDefinition = getCollectionDefinition();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithUnreachableHost() {
        SpyClientUnderTestFactory.ClientWithGatewaySpy client = null;

        try {
            client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder());

            Database database = SHARED_DATABASE;

            Flux<ResourceResponse<DocumentCollection>> createObservable = client
                    .createCollection(database.selfLink(), collectionDefinition, null);


            final RxGatewayStoreModel origGatewayStoreModel = client.getOrigGatewayStoreModel();

            Mockito.doAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgumentAt(0, RxDocumentServiceRequest.class);

                if (request.getResourceType() == ResourceType.DocumentCollection) {
                    return Flux.error(new UnknownHostException());
                }

                return origGatewayStoreModel.processMessage(request);

            }).when(client.getSpyGatewayStoreModel()).processMessage(Mockito.any());


            FailureValidator validator = new FailureValidator.Builder().instanceOf(UnknownHostException.class).build();
            Instant start = Instant.now();
            validateFailure(createObservable, validator, TIMEOUT);
            Instant after = Instant.now();
            assertThat(after.toEpochMilli() - start.toEpochMilli())
                    .isGreaterThanOrEqualTo(ClientRetryPolicy.MaxRetryCount * ClientRetryPolicy.RetryIntervalInMS);

        } finally {
            safeClose(client);
        }
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        safeDeleteCollection(client, collectionDefinition);
        client.close();
    }
}