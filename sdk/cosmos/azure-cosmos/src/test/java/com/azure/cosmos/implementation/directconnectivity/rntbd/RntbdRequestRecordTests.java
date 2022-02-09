// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RntbdRequestRecordTests {

    @DataProvider
    public static Object[][] rntbdRequestArgs() {
        return new Object[][]{
            // OperationType, request sent, expected exception
            { OperationType.Read, true, GoneException.class },
            { OperationType.Create, false, GoneException.class },
            { OperationType.Create, true, RequestTimeoutException.class }
        };
    }

    @Test(groups = { "unit" }, dataProvider = "rntbdRequestArgs")
    public void expireRecord(OperationType operationType, boolean requestSent, Class<?> exceptionType) throws URISyntaxException {

        RntbdRequestArgs requestArgs = new RntbdRequestArgs(
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, ResourceType.Document),
            new URI("http://localhost/replica-path")
        );

        RntbdRequestTimer requestTimer = new RntbdRequestTimer(5000, 5000);
        RntbdRequestRecord record = new AsyncRntbdRequestRecord(requestArgs, requestTimer);
        if (requestSent) {
            record.setSendingRequestHasStarted();
        }
        record.expire();

        try{
            record.get();
            fail("RntbdRequestRecord should complete with exception");
        } catch (ExecutionException e) {
            Throwable innerException = e.getCause();
            assertThat(innerException).isInstanceOf(exceptionType);
        } catch (Exception e) {
            fail("Wrong exception");
        }
    }
}
