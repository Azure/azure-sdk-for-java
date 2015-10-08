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

package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.sun.jersey.api.client.ClientResponse;

import java.io.InputStream;
import java.util.List;

public class JerseyServiceResponseContext implements ServiceResponseContext {
    private final ClientResponse clientResponse;

    protected ClientResponse getClientResponse() {
        return this.clientResponse;
    }

    public JerseyServiceResponseContext(final ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
    }

    @Override
    public Object getProperty(String name) {
        return clientResponse.getProperties().get(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        clientResponse.getProperties().put(name, value);
    }

    @Override
    public int getStatus() {
        return clientResponse.getStatus();
    }

    @Override
    public void setStatus(int status) {
        clientResponse.setStatus(status);
    }

    @Override
    public String getHeader(String name) {
        List<String> headers = clientResponse.getHeaders().get(name);
        if (headers != null && headers.size() > 0) {
            return headers.get(0);
        }

        return null;
    }

    @Override
    public void setHeader(String name, String value) {
        clientResponse.getHeaders().add(name, value);
    }

    @Override
    public void removeHeader(String name) {
        clientResponse.getHeaders().remove(name);
    }

    @Override
    public boolean hasEntity() {
        return clientResponse.hasEntity();
    }

    @Override
    public InputStream getEntityInputStream() {
        return clientResponse.getEntityInputStream();
    }

    @Override
    public void setEntityInputStream(InputStream entity) {
        clientResponse.setEntityInputStream(entity);
    }
}
