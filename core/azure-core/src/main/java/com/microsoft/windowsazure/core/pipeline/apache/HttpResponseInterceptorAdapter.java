/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core.pipeline.apache;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.util.LinkedList;

public class HttpResponseInterceptorAdapter implements HttpResponseInterceptor {
    private LinkedList<ServiceResponseFilter> filters;

    public HttpResponseInterceptorAdapter() {
        filters = new LinkedList<ServiceResponseFilter>();
    }

    public LinkedList<ServiceResponseFilter> getFilterList()
    {
        return filters;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        HttpServiceResponseContext serviceResponseContext = new HttpServiceResponseContext(response, context);
        for (ServiceResponseFilter filter : filters) {
            filter.filter(null, serviceResponseContext);
        }
    }
}
