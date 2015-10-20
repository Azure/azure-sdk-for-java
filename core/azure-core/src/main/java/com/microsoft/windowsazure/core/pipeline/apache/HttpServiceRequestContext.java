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

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpServiceRequestContext implements ServiceRequestContext {
    private final HttpRequest clientRequest;
    private final HttpContext httpContext;

    public HttpServiceRequestContext(HttpRequest clientRequest,
            HttpContext httpContext) {
        this.clientRequest = clientRequest;
        this.httpContext = httpContext;
    }

    @Override
    public Object getProperty(final String name) {
        return httpContext.getAttribute(name);
    }

    @Override
    public void setProperty(final String name, final Object value) {
        httpContext.setAttribute(name, value);
    }

    @Override
    public Map<String, String> getAllHeaders() {
        Map<String, String> allHeaders = new HashMap<String, String>();
        for (Header header : clientRequest.getAllHeaders()) {
            allHeaders.put(header.getName(), header.getValue());
        }
        return allHeaders;
    }

    @Override
    public URI getURI() {
        try {
            return new URI(clientRequest.getRequestLine().getUri());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void setURI(final URI uri) {
        // Do nothing. not supported
    }

    @Override
    public URI getFullURI() {
        HttpRequest request = clientRequest;
        for (;;) {
            URI result = tryGetFullURI(request);
            if (result != null) {
                return result;
            }
            if (!(request instanceof HttpRequestWrapper)) {
                break;
            }
            HttpRequestWrapper wrapper = (HttpRequestWrapper) request;
            request = wrapper.getOriginal();
        }
        throw new UnsupportedOperationException("The full URI is not available");
    }

    private static URI tryGetFullURI(HttpRequest request) {
        if (!(request instanceof HttpUriRequest)) {
            return null;
        }
        HttpUriRequest uriRequest = (HttpUriRequest) request;
        URI uri = uriRequest.getURI();
        return isFullURI(uri) ? uri : null;
    }

    private static boolean isFullURI(URI uri) {
        if (uri == null) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.length() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String getMethod() {
        return clientRequest.getRequestLine().getMethod();
    }

    @Override
    public void setMethod(String method) {
        // Do nothing. not supported
    }

    @Override
    public Object getEntity() {
        // Do nothing. not supported
        return null;
    }

    @Override
    public void setEntity(final Object entity) {
        // Do nothing. not supported
    }

    @Override
    public String getHeader(final String name) {
        final Header first = clientRequest.getFirstHeader(name);
        return first != null ? first.getValue() : null;
    }

    @Override
    public void setHeader(final String name, final String value) {
        clientRequest.setHeader(name, value);
    }

    @Override
    public void removeHeader(final String name) {
        clientRequest.removeHeaders(name);
    }
}