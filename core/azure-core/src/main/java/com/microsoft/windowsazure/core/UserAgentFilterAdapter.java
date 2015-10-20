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
package com.microsoft.windowsazure.core;

import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceRequestContext;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class UserAgentFilterAdapter implements HttpRequestInterceptor {
    private UserAgentFilter filter;

    public UserAgentFilterAdapter(UserAgentFilter filter) {
        this.filter = filter;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        filter.filter(new HttpServiceRequestContext(request, context));
    }
}