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

import com.microsoft.windowsazure.core.utils.CollectionStringBuilder;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.CommittingOutputStream;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class HttpURLConnectionClientHandler extends TerminatingClientHandler {

    private final int connectionTimeoutMillis;
    private final int readTimeoutMillis;

    public HttpURLConnectionClientHandler(ClientConfig clientConfig) {
        connectionTimeoutMillis = readTimeoutFromConfig(clientConfig,
                ClientConfig.PROPERTY_CONNECT_TIMEOUT);
        readTimeoutMillis = readTimeoutFromConfig(clientConfig,
                ClientConfig.PROPERTY_READ_TIMEOUT);
    }

    private static int readTimeoutFromConfig(ClientConfig config,
            String propertyName) {
        Integer property = (Integer) config.getProperty(propertyName);
        if (property != null) {
            return property.intValue();
        }
        throw new IllegalArgumentException(propertyName);
    }

    /**
     * Empty "no-op" listener if none registered
     */
    private static final EntityStreamingListener EMPTY_STREAMING_LISTENER = new EntityStreamingListener() {
        @Override
        public void onBeforeStreamingEntity(ClientRequest clientRequest) {
        }
    };

    /**
     * OutputStream used for buffering entity body when "Content-Length" is not
     * known in advance.
     */
    private final class BufferingOutputStream extends OutputStream {
        private final ByteArrayOutputStream outputStream;
        private final HttpURLConnection urlConnection;
        private final ClientRequest clientRequest;
        private final EntityStreamingListener entityStreamingListener;
        private boolean closed;

        private BufferingOutputStream(HttpURLConnection urlConnection,
                ClientRequest clientRequest,
                EntityStreamingListener entityStreamingListener) {
            this.outputStream = new ByteArrayOutputStream();
            this.urlConnection = urlConnection;
            this.clientRequest = clientRequest;
            this.entityStreamingListener = entityStreamingListener;
        }

        @Override
        public void close() throws IOException {
            outputStream.close();

            if (!closed) {
                closed = true;

                // Give the listener a last change to modify headers now that
                // the content length is known
                setContentLengthHeader(clientRequest, outputStream.size());
                entityStreamingListener.onBeforeStreamingEntity(clientRequest);

                // Write headers, then entity to the http connection.
                setURLConnectionHeaders(clientRequest.getHeaders(),
                        urlConnection);

                // Since we buffered the entity and we know the content size, we
                // might as well
                // use the "fixed length" streaming mode of HttpURLConnection to
                // stream
                // the buffer directly.
                urlConnection.setFixedLengthStreamingMode(outputStream.size());
                OutputStream httpOutputStream = urlConnection.getOutputStream();
                outputStream.writeTo(httpOutputStream);
                httpOutputStream.flush();
                httpOutputStream.close();
            }
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        @Override
        public void write(byte[] b, int off, int len) {
            outputStream.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(int b) {
            outputStream.write(b);
        }
    }

    /**
     * OutputStream used for directly streaming entity to url connection stream.
     * Headers are written just before sending the first bytes to the output
     * stream.
     */
    private final class StreamingOutputStream extends CommittingOutputStream {
        private final HttpURLConnection urlConnection;
        private final ClientRequest clientRequest;

        private StreamingOutputStream(HttpURLConnection urlConnection,
                ClientRequest clientRequest) {
            this.urlConnection = urlConnection;
            this.clientRequest = clientRequest;
        }

        @Override
        protected OutputStream getOutputStream() throws IOException {
            return urlConnection.getOutputStream();
        }

        @Override
        public void commit() throws IOException {
            setURLConnectionHeaders(clientRequest.getHeaders(), urlConnection);
        }
    }

    /**
     * Simple response implementation around an HttpURLConnection response
     */
    private final class URLConnectionResponse extends ClientResponse {
        private final String method;
        private final HttpURLConnection urlConnection;

        URLConnectionResponse(int status, InBoundHeaders headers,
                InputStream entity, String method,
                HttpURLConnection urlConnection) {
            super(status, headers, entity, getMessageBodyWorkers());
            this.method = method;
            this.urlConnection = urlConnection;
        }

        @Override
        public boolean hasEntity() {
            if (method.equals("HEAD") || getEntityInputStream() == null) {
                return false;
            }

            // Length "-1" means "unknown"
            int length = urlConnection.getContentLength();
            return length > 0 || length == -1;
        }

        @Override
        public String toString() {
            return urlConnection.getRequestMethod() + " "
                    + urlConnection.getURL()
                    + " returned a response status of " + this.getStatus()
                    + " " + this.getClientResponseStatus();
        }
    }

    @Override
    public ClientResponse handle(final ClientRequest ro) {
        try {
            return doHandle(ro);
        } catch (Exception e) {
            throw new ClientHandlerException(e);
        }
    }

    private ClientResponse doHandle(final ClientRequest clientRequest)
            throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) clientRequest
                .getURI().toURL().openConnection();
        urlConnection.setReadTimeout(readTimeoutMillis);
        urlConnection.setConnectTimeout(connectionTimeoutMillis);

        final EntityStreamingListener entityStreamingListener = getEntityStreamingListener(clientRequest);

        urlConnection.setRequestMethod(clientRequest.getMethod());

        // Write the request headers
        setURLConnectionHeaders(clientRequest.getHeaders(), urlConnection);

        // Write the entity (if any)
        Object entity = clientRequest.getEntity();

        // If no entity and "PUT method, force an empty entity to force the
        // underlying
        // connection to set the "Content-Length" header to 0.
        // This is needed because some web servers require a "Content-Length"
        // field for
        // all PUT method calls (unless chunked encoding is used).
        if (entity == null && "PUT".equals(clientRequest.getMethod())) {
            entity = new byte[0];
            clientRequest.setEntity(entity);
        }

        // Send headers and entity on the wire
        if (entity != null) {
            urlConnection.setDoOutput(true);

            writeRequestEntity(clientRequest,
                    new RequestEntityWriterListener() {
                        private boolean inStreamingMode;

                        @Override
                        public void onRequestEntitySize(long size) {
                            if (size != -1 && size < Integer.MAX_VALUE) {
                                inStreamingMode = true;
                                setContentLengthHeader(clientRequest,
                                        (int) size);
                                entityStreamingListener
                                        .onBeforeStreamingEntity(clientRequest);

                                urlConnection
                                        .setFixedLengthStreamingMode((int) size);
                            } else {
                                Integer chunkedEncodingSize = (Integer) clientRequest
                                        .getProperties()
                                        .get(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE);
                                if (chunkedEncodingSize != null) {
                                    inStreamingMode = true;
                                    entityStreamingListener
                                            .onBeforeStreamingEntity(clientRequest);

                                    urlConnection
                                            .setChunkedStreamingMode(chunkedEncodingSize);
                                }
                            }
                        }

                        @Override
                        public OutputStream onGetOutputStream()
                                throws IOException {
                            if (inStreamingMode) {
                                return new StreamingOutputStream(urlConnection,
                                        clientRequest);
                            } else {
                                return new BufferingOutputStream(urlConnection,
                                        clientRequest, entityStreamingListener);
                            }
                        }
                    });
        } else {
            entityStreamingListener.onBeforeStreamingEntity(clientRequest);
            setURLConnectionHeaders(clientRequest.getHeaders(), urlConnection);
        }

        // Return the in-bound response
        return new URLConnectionResponse(urlConnection.getResponseCode(),
                getInBoundHeaders(urlConnection),
                getInputStream(urlConnection), clientRequest.getMethod(),
                urlConnection);
    }

    private EntityStreamingListener getEntityStreamingListener(
            final ClientRequest clientRequest) {
        EntityStreamingListener result = (EntityStreamingListener) clientRequest
                .getProperties().get(EntityStreamingListener.class.getName());

        if (result != null) {
            return result;
        }

        return EMPTY_STREAMING_LISTENER;
    }

    private void setContentLengthHeader(ClientRequest clientRequest, int size) {
        // Skip if already set
        if (clientRequest.getHeaders().getFirst("Content-Length") != null) {
            return;
        }

        // Skip if size is unknown
        if (size < 0) {
            return;
        }

        clientRequest.getHeaders().putSingle("Content-Length", size);
    }

    private void setURLConnectionHeaders(
            MultivaluedMap<String, Object> headers,
            HttpURLConnection urlConnection) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                urlConnection.setRequestProperty(e.getKey(),
                        ClientRequest.getHeaderValue(vs.get(0)));
            } else {
                CollectionStringBuilder sb = new CollectionStringBuilder();
                for (Object v : e.getValue()) {
                    sb.add(ClientRequest.getHeaderValue(v));
                }
                urlConnection.setRequestProperty(e.getKey(), sb.toString());
            }
        }
    }

    private InBoundHeaders getInBoundHeaders(HttpURLConnection urlConnection) {
        InBoundHeaders headers = new InBoundHeaders();
        for (Map.Entry<String, List<String>> e : urlConnection
                .getHeaderFields().entrySet()) {
            if (e.getKey() != null) {
                headers.put(e.getKey(), e.getValue());
            }
        }
        return headers;
    }

    private InputStream getInputStream(HttpURLConnection urlConnection)
            throws IOException {
        if (urlConnection.getResponseCode() < 300) {
            return urlConnection.getInputStream();
        } else {
            InputStream ein = urlConnection.getErrorStream();
            return (ein != null) ? ein : new ByteArrayInputStream(new byte[0]);
        }
    }
}
