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

package com.microsoft.windowsazure.tracing;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;

public class ClientRequestTrackingHandler implements ServiceRequestFilter,
        ServiceResponseFilter {
    private final String trackingId;

    public String getTrackingId() {
        return trackingId;
    }

    public ClientRequestTrackingHandler(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public void filter(ServiceRequestContext request) {
        request.setHeader("client-tracking-id", trackingId);
    }

    @Override
    public void filter(ServiceRequestContext request,
            ServiceResponseContext response) {
        response.setHeader("client-tracking-id", trackingId);
    }
}
