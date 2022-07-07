// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DocumentServiceRequestContextTests {


    @DataProvider(name = "exceptionArgProvider")
    private Object[][] exceptionArgProvider() {
        return new Object[][]{
                { new PartitionKeyRangeGoneException(), true },
                { new PartitionKeyRangeIsSplittingException(), true },
                { new PartitionKeyRangeGoneException(), true },
                { new PartitionIsMigratingException(), true },
                { new GoneException(), true },
                { new NotFoundException(), false },
                { new ServiceUnavailableException(), false },
                { new RequestRateTooLargeException(), false },
                { new BadRequestException(), false },
                { new ConflictException(), false },
                { new ForbiddenException(), false },
                { new LockedException(), false },
                { new NotFoundException(), false },
                { new PreconditionFailedException(), false },
                { new RequestTimeoutException(), false },
                { new UnauthorizedException(), false },
                { new IllegalStateException(), false }
        };
    }

    @Test(groups = "unit", dataProvider = "exceptionArgProvider")
    public void addFailedEndpointsTests(Exception exception, boolean shouldAdded) {
        DocumentServiceRequestContext documentServiceRequestContext = new DocumentServiceRequestContext();

        Uri testUri = new Uri("http:127.0.0.1:1");
        documentServiceRequestContext.addToFailedEndpoints(exception, testUri);

        if (shouldAdded) {
            documentServiceRequestContext.getFailedEndpoints().contains(testUri);
        } else {
            documentServiceRequestContext.getFailedEndpoints().isEmpty();
        }
    }
}
