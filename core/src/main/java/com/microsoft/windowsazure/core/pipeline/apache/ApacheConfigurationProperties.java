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

public abstract class ApacheConfigurationProperties {
    public static final String PROPERTY_SSL_CONNECTION_SOCKET_FACTORY = "com.microsoft.windowsazure.Configuration.sslConnectionSocketFactory";

    public static final String PROPERTY_CONNECTION_MANAGER = "com.microsoft.windowsazure.Configuration.connectionManager";

    public static final String PROPERTY_PROXY_URI = "com.microsoft.windowsazure.Configuration.proxy";

    public static final String PROPERTY_RETRY_HANDLER = "com.microsoft.windowsazure.Configuration.retryHandler";

    public static final String PROPERTY_HTTP_CLIENT_BUILDER = "com.microsoft.windowsazure.Configuration.httpClientBuilder";
    
    public static final String PROPERTY_REDIRECT_STRATEGY = "com.microsoft.windowsazure.Configuration.redirectStrategy";
}
