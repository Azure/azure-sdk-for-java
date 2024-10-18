// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

/**
 * Tests non-REST call cases.
 */
@Execution(ExecutionMode.CONCURRENT)
public class NonRestCallTests {
    @ParameterizedTest
    @MethodSource("apiCallReturnsErrorSupplier")
    public void apiCallReturnsError(Publisher<?> apiCall) {
        StepVerifier.create(apiCall).verifyError(NullPointerException.class);
    }

    static Stream<Publisher<?>> apiCallReturnsErrorSupplier() {
        SearchIndexerAsyncClient client = new SearchIndexerClientBuilder()
            .endpoint("https://fake.com")
            .credential(new AzureKeyCredential("fake"))
            .buildAsyncClient();

        return Stream.of(
            client.createOrUpdateDataSourceConnection(null),
            client.createOrUpdateDataSourceConnectionWithResponse(null, true),
            client.deleteDataSourceConnectionWithResponse(null, true),

            client.createOrUpdateIndexer(null),
            client.createOrUpdateIndexerWithResponse(null, true),
            client.deleteIndexerWithResponse(null, true),

            client.createSkillset(null),
            client.createSkillsetWithResponse(null),
            client.createOrUpdateSkillset(null),
            client.createOrUpdateSkillsetWithResponse(null, true),
            client.deleteSkillsetWithResponse(null, true)
        );
    }
}
