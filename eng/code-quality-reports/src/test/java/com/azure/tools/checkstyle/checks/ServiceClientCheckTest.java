// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for annotation @ServiceClientCheck.
 */
public class ServiceClientCheckTest extends AbstractModuleTestSupport {
    private static final String COLLECTION_RETURN_TYPE = "ReturnType.COLLECTION";
    private static final String SINGLE_RETURN_TYPE = "ReturnType.SINGLE";
    private static final String LONG_RUNNING_OPERATION_RETURN_TYPE = "ReturnType.LONG_RUNNING_OPERATION";
    private static final String RETURN_TYPE_ERROR =
        "'%s' service client with '%s' should use type '%s' as the return type.";
    private static final String ERROR_MSG = "The variable field '%s' of class '%s' should be final. Classes "
        + "annotated with @ServiceClient are supposed to be immutable.";
    private static final String PAGED_FLUX = "PagedFlux";
    private static final String POLLER_FLUX = "PollerFlux";
    private static final String SYNC_POLLER = "SyncPoller";
    private static final String MONO = "Mono";
    private static final String RESPONSE = "Response";
    private static final String PAGED_ITERABLE = "PagedIterable";

    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(ServiceClientCheck.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/ServiceClientCheck";
    }


    @Test
    public void serviceClientCheckTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(9, 5, String.format(RETURN_TYPE_ERROR, "Asynchronous", SINGLE_RETURN_TYPE,
                MONO)),
            expectedErrorMessage(83, 5, String.format(ERROR_MSG, "pageRetrieverProvider",
                "ServiceClientCheckTestDataAsyncClient"))
        };
        verify(checker, getPath("ServiceClientCheckTestDataAsyncClient.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }
}
