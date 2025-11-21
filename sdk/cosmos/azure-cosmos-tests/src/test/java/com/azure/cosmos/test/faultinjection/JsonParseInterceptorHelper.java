// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.test.faultinjection;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.directconnectivity.JsonNodeStorePayload;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to inject faults into JSON parsing for testing purposes.
 * This should only be used in test scenarios.
 *
 * <p><strong>IMPORTANT:</strong> This uses a GLOBAL interceptor that affects all threads.
 * Tests using these methods should NOT run in parallel with other tests using interceptors.
 * Use TestNG's singleThreaded configuration or similar mechanisms for test isolation.</p>
 */
public class JsonParseInterceptorHelper {
    private static final Logger logger = LoggerFactory.getLogger(JsonParseInterceptorHelper.class);

    /**
     * Sets an interceptor that throws StreamConstraintsException once, then delegates to default parser.
     * Useful for testing retry logic in change feed processor.
     *
     * <p><strong>WARNING:</strong> This sets a GLOBAL interceptor. The test using this method
     * should NOT run in parallel with other tests to avoid interference.</p>
     *
     * @return AutoCloseable that clears the interceptor when closed
     */
    public static AutoCloseable injectStreamConstraintsExceptionOnce(OperationType operationType, ResourceType resourceType) {
        AtomicInteger callCount = new AtomicInteger(0);

        JsonNodeStorePayload.TestOnlyJsonParseInterceptor interceptor = (bytes, responseHeaders, defaultParser, actualOperationType, actualResourceType) -> {
            if (operationType.equals(actualOperationType) && resourceType.equals(actualResourceType)) {

                int count = callCount.incrementAndGet();

                if (count == 1) {
                    logger.info("JsonParseInterceptor: Injecting StreamConstraintsException (call #{})", count);
                    throw new StreamConstraintsException("Test-injected StreamConstraintsException");
                }
            }

            logger.debug("JsonParseInterceptor: Delegating to default parser (call #{})", callCount.get());
            return defaultParser.parse(bytes, responseHeaders);
        };

        JsonNodeStorePayload.setTestOnlyJsonParseInterceptor(interceptor);

        return () -> {
            logger.info("JsonParseInterceptor: Clearing interceptor after {} calls", callCount.get());
            JsonNodeStorePayload.clearTestOnlyJsonParseInterceptor();
        };
    }
}
