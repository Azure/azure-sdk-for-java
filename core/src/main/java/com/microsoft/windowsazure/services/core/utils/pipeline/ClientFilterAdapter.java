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
package com.microsoft.windowsazure.services.core.utils.pipeline;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.ServiceFilter.Request;
import com.microsoft.windowsazure.services.core.ServiceFilter.Response;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ClientFilterAdapter extends ClientFilter {
    ServiceFilter filter;

    public ClientFilterAdapter(ServiceFilter filter) {
        this.filter = filter;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {

        final ClientRequest cr = clientRequest;
        try {
            Response resp = filter.handle(new ServiceFilterRequest(clientRequest), new ServiceFilter.Next() {
                public Response handle(Request request) {
                    return new ServiceFilterResponse(getNext().handle(cr));
                }
            });

            return ((ServiceFilterResponse) resp).clientResponse;
        }
        catch (Exception e) {
            throw new ClientHandlerException(e);
        }
    }
}

class ServiceFilterRequest implements ServiceFilter.Request {
    ClientRequest clientRequest;

    public ServiceFilterRequest(ClientRequest clientRequest) {
        this.clientRequest = clientRequest;
    }

    public Map<String, Object> getProperties() {
        return clientRequest.getProperties();
    }

    public void setProperties(Map<String, Object> properties) {
        clientRequest.setProperties(properties);
    }

    public URI getURI() {
        return clientRequest.getURI();
    }

    public void setURI(URI uri) {
        clientRequest.setURI(uri);
    }

    public String getMethod() {
        return clientRequest.getMethod();
    }

    public void setMethod(String method) {
        clientRequest.setMethod(method);
    }

    public Object getEntity() {
        return clientRequest.getEntity();
    }

    public void setEntity(Object entity) {
        clientRequest.setEntity(entity);
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return clientRequest.getHeaders();
    }

}

class ServiceFilterResponse implements ServiceFilter.Response {
    ClientResponse clientResponse;

    public ServiceFilterResponse(ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
    }

    public Map<String, Object> getProperties() {
        return clientResponse.getProperties();
    }

    public int getStatus() {
        return clientResponse.getStatus();
    }

    public void setStatus(int status) {
        clientResponse.setStatus(status);
    }

    public MultivaluedMap<String, String> getHeaders() {
        return clientResponse.getHeaders();
    }

    public boolean hasEntity() {
        return clientResponse.hasEntity();
    }

    public InputStream getEntityInputStream() {
        return clientResponse.getEntityInputStream();
    }

    public void setEntityInputStream(InputStream entity) {
        clientResponse.setEntityInputStream(entity);
    }
}
