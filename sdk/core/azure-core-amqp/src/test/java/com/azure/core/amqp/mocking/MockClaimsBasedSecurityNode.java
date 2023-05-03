// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.mocking;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Basic implementation of {@link ClaimsBasedSecurityNode} that is used for mocking in tests.
 */
public class MockClaimsBasedSecurityNode implements ClaimsBasedSecurityNode {
    @Override
    public Mono<OffsetDateTime> authorize(String audience, String scopes) {
        return null;
    }

    @Override
    public void close() {

    }
}
