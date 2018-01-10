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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlwaysRetry extends RetryPolicy implements RetryPolicyFactory {

    private final List<RetryContext> retryContextList;
    private final List<RetryInfo> retryInfoList;
    private int retryCount = 0;

    public AlwaysRetry(List<RetryContext> retryContextList, List<RetryInfo> retryInfoList) {
        this.retryContextList = new ArrayList<RetryContext>(retryContextList);
        this.retryInfoList = new ArrayList<RetryInfo>(retryInfoList);
        assertEquals(this.retryContextList.size(), this.retryInfoList.size() + 1);
    }

    @Override
    public RetryPolicy createInstance(OperationContext opContext) {
        return new AlwaysRetry(this.retryContextList, this.retryInfoList);
    }

    @Override
    public RetryInfo evaluate(RetryContext retryContext, OperationContext operationContext) {
        assertTrue("Executor should not try to evaluate more retries after we return null",
                retryContext.getCurrentRetryCount() < this.retryContextList.size());

        assertEquals(this.retryContextList.get(retryContext.getCurrentRetryCount()).getNextLocation(),
                retryContext.getNextLocation());
        assertEquals(this.retryContextList.get(retryContext.getCurrentRetryCount()).getLocationMode(),
                retryContext.getLocationMode());

        assertEquals(this.retryCount++, retryContext.getCurrentRetryCount());

        if (retryContext.getCurrentRetryCount() < this.retryInfoList.size()) {
            return this.retryInfoList.get(retryContext.getCurrentRetryCount());
        }

        return null;
    }
}
