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

package com.microsoft.windowsazure.core.pipeline.apache;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;

public class HttpServiceResponseContext implements ServiceResponseContext {
    private HttpResponse clientResponse;
    private HttpContext httpContext;

    public HttpServiceResponseContext(HttpResponse clientResponse,
            HttpContext httpContext) {
        this.clientResponse = clientResponse;
        this.httpContext = httpContext;
    }

    @Override
    public Object getProperty(String name) {
        return httpContext.getAttribute(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        httpContext.setAttribute(name, value);
    }

    @Override
    public int getStatus() {
        return clientResponse.getStatusLine().getStatusCode();
    }

    @Override
    public void setStatus(int status) {
        clientResponse.setStatusCode(status);
    }

    @Override
    public boolean hasEntity() {
        return clientResponse.getEntity() != null;
    }

    @Override
    public String getHeader(String name) {
        Header first = clientResponse.getFirstHeader(name);
        if (first != null) {
            return first.getValue();
        }

        return null;
    }

    @Override
    public void setHeader(String name, String value) {
        clientResponse.setHeader(name, value);
    }

    @Override
    public void removeHeader(String name) {
        clientResponse.removeHeaders(name);
    }

    @Override
    public InputStream getEntityInputStream() {
        try {
            return clientResponse.getEntity().getContent();
        } catch (IOException e) {
            return null;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    public void setEntityInputStream(InputStream entity) {
        clientResponse.setEntity(new InputStreamEntity(entity));
    }
}
