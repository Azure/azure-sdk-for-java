// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public class RetryExtension implements TestExecutionExceptionHandler, BeforeEachCallback {

    private static final int MAX_RETRIES = 3;
    private int retriesLeft = MAX_RETRIES;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        retriesLeft = MAX_RETRIES;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (retriesLeft > 0) {
            retriesLeft--;
            System.out.println("Retrying test: " + context.getDisplayName() + ", " + retriesLeft + " retries left.");
            throw throwable; // Throw again to re-execute the test
        } else {
            throw throwable; // Exhausted retries, throw exception to fail the test
        }
    }
}
