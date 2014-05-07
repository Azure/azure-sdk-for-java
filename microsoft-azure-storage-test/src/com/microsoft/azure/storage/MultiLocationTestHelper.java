/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

import static org.junit.Assert.*;

import java.util.List;

public class MultiLocationTestHelper {
    public final StorageLocation initialLocation;
    private final List<RetryInfo> retryInfoList;
    private final List<RetryContext> retryContextList;
    public int requestCounter;
    public String error;

    public OperationContext operationContext;

    public RetryPolicyFactory retryPolicy;

    public MultiLocationTestHelper(final StorageUri storageUri, final StorageLocation initialLocation,
            final List<RetryContext> retryContextList, final List<RetryInfo> retryInfoList) {
        this.initialLocation = initialLocation;
        this.retryContextList = retryContextList;
        this.retryInfoList = retryInfoList;

        this.operationContext = new OperationContext();
        this.operationContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                if (error == null) {
                    StorageLocation location = (requestCounter == 0) ? initialLocation : retryInfoList.get(
                            requestCounter - 1).getTargetLocation();
                    if (!eventArg.getRequestResult().getTargetLocation().equals(location)) {
                        error = String.format("Request %s was sent to %s while the location should have been %s",
                                requestCounter, eventArg.getRequestResult().getTargetLocation(), location);
                    }
                }

                requestCounter++;
            }
        });

        this.retryPolicy = new AlwaysRetry(this.retryContextList, this.retryInfoList);
    }

    public void close() {
        assertNull(this.error);
        assertEquals(this.initialLocation, this.operationContext.getRequestResults().get(0).getTargetLocation());
        assertEquals(this.retryInfoList.size() + 1, this.operationContext.getRequestResults().size());
        for (int i = 0; i < this.retryInfoList.size(); i++) {
            assertEquals(this.retryInfoList.get(i).getTargetLocation(),
                    this.operationContext.getRequestResults().get(i + 1).getTargetLocation());

            int retryInterval = (int) (this.operationContext.getRequestResults().get(i + 1).getStartDate().getTime() - this.operationContext
                    .getRequestResults().get(i).getStopDate().getTime());
            assertTrue(this.retryInfoList.get(i).getRetryInterval() <= retryInterval);
        }
    }
}
