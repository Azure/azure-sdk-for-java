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

package com.microsoft.windowsazure.core.pipeline.filter;

import java.net.URI;
import java.util.Map;

public interface ServiceRequestContext {
    String getMethod();

    void setMethod(String method);

    URI getURI();

    void setURI(URI uri);

    URI getFullURI();

    String getHeader(String name);

    void setHeader(String name, String value);

    void removeHeader(String name);

    Object getEntity();

    void setEntity(Object entity);

    Object getProperty(String name);

    void setProperty(String name, Object value);

    Map<String, String> getAllHeaders();
}
