/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.windowsazure.core.pipeline.filter;

import java.net.URI;

public interface ServiceRequestContext
{
    public String getMethod();

    public void setMethod(String method);

    public URI getURI();

    public void setURI(URI uri);

    public String getHeader(String name);

    public void setHeader(String name, String value);

    public void removeHeader(String name);

    public Object getEntity();

    public void setEntity(Object entity);

    public Object getProperty(String name);

    public void setProperty(String name, Object value);
}
