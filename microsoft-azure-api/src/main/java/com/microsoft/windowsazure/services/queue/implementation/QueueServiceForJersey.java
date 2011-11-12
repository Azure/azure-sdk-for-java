package com.microsoft.windowsazure.services.queue.implementation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.http.ClientFilterAdapter;
import com.microsoft.windowsazure.http.ServiceFilter;
import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.queue.QueueConfiguration;
import com.microsoft.windowsazure.services.queue.QueueServiceContract;
import com.microsoft.windowsazure.services.queue.models.CreateMessageOptions;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.QueueServiceOptions;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class QueueServiceForJersey implements QueueServiceContract {
    private static Log log = LogFactory.getLog(QueueServiceForJersey.class);

    private static final String API_VERSION = "2011-08-18";
    private final Client channel;
    private final String accountName;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ServiceFilter[] filters;
    private final SharedKeyLiteFilter filter;

    /*
     * TODO: How can we make "timeout" optional? TODO: How to make "filter"
     * configurable though code?
     */
    @Inject
    public QueueServiceForJersey(Client channel, @Named(QueueConfiguration.ACCOUNT_NAME) String accountName, @Named(QueueConfiguration.URL) String url,
            SharedKeyLiteFilter filter) {

        this.channel = channel;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.filters = new ServiceFilter[0];
        channel.addFilter(filter);
    }

    public QueueServiceForJersey(Client channel, ServiceFilter[] filters, String accountName, String url, SharedKeyLiteFilter filter,
            RFC1123DateConverter dateMapper) {

        this.channel = channel;
        this.filters = filters;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = dateMapper;
    }

    public QueueServiceContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new QueueServiceForJersey(this.channel, newFilters, this.accountName, this.url, this.filter, this.dateMapper);
    }

    private void ThrowIfError(ClientResponse r) {
        if (r.getStatus() >= 300) {
            throw new UniformInterfaceException(r);
        }
    }

    private class EnumCommaStringBuilder<E extends Enum<E>> {
        private final StringBuilder sb = new StringBuilder();

        public void addValue(EnumSet<E> enumSet, E value, String representation) {
            if (enumSet.contains(value)) {
                if (sb.length() >= 0) {
                    sb.append(",");
                }
                sb.append(representation);
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        if (value != null) {
            webResource = webResource.queryParam(key, value.toString());
        }
        return webResource;
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            webResource = webResource.queryParam(key, Integer.toString(value));
        }
        return webResource;
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value) {
        if (value != null) {
            builder = builder.header(name, value);
        }
        return builder;
    }

    private Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        for (Entry<String, String> entry : metadata.entrySet()) {
            builder = builder.header("x-ms-meta-" + entry.getKey(), entry.getValue());
        }
        return builder;
    }

    private HashMap<String, String> getMetadataFromHeaders(ClientResponse response) {
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().startsWith("x-ms-meta-")) {
                String name = entry.getKey().substring("x-ms-meta-".length());
                String value = entry.getValue().get(0);
                metadata.put(name, value);
            }
        }
        return metadata;
    }

    private WebResource getResource(QueueServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout", options.getTimeout());
        for (ServiceFilter filter : filters) {
            webResource.addFilter(new ClientFilterAdapter(filter));
        }

        return webResource;
    }

    private WebResource setCanonicalizedResource(WebResource webResource, String operation) {
        // Resource path
        String value = "/" + this.accountName;
        value += webResource.getURI().getPath();

        // "comp" param
        if (operation != null) {
            value += "?comp=" + operation;
        }

        webResource.setProperty("canonicalizedResource", value);

        return webResource;
    }

    public ServiceProperties getServiceProperties() throws ServiceException {
        return getServiceProperties(new QueueServiceOptions());
    }

    public ServiceProperties getServiceProperties(QueueServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service").queryParam("comp", "properties");
        webResource = setCanonicalizedResource(webResource, "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(ServiceProperties.class);
    }

    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        setServiceProperties(serviceProperties, new QueueServiceOptions());
    }

    public void setServiceProperties(ServiceProperties serviceProperties, QueueServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service").queryParam("comp", "properties");
        webResource = setCanonicalizedResource(webResource, "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        // Note: Add content type here to enable proper HMAC signing
        builder.type("application/xml").put(serviceProperties);
    }

    public void createQueue(String queue) throws ServiceException {
        createQueue(queue, new CreateQueueOptions());

    }

    public void createQueue(String queue, CreateQueueOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(queue);
        webResource = setCanonicalizedResource(webResource, null);

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalMetadataHeader(builder, options.getMetadata());

        // Note: Add content type here to enable proper HMAC signing
        builder.type("text/plain").put("");
    }

    public void deleteQueue(String queue) throws ServiceException {
        deleteQueue(queue, new QueueServiceOptions());
    }

    public void deleteQueue(String queue, QueueServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(queue);
        webResource = setCanonicalizedResource(webResource, null);

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.delete();
    }

    public ListQueuesResult listQueues() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public GetQueueMetadataResult getQueueMetadata(String queue) throws ServiceException {
        return getQueueMetadata(queue, new QueueServiceOptions());
    }

    public GetQueueMetadataResult getQueueMetadata(String queue, QueueServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(queue).queryParam("comp", "metadata");
        webResource = setCanonicalizedResource(webResource, "metadata");

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetQueueMetadataResult result = new GetQueueMetadataResult();
        result.setApproximateMessageCount(Integer.parseInt(response.getHeaders().getFirst("x-ms-approximate-messages-count")));
        result.setMetadata(getMetadataFromHeaders(response));

        return result;
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata, QueueServiceOptions options) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void createMessage(String queue, String message) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void createMessage(String queue, String message, CreateMessageOptions options) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds)
            throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public UpdateMessageResult updateMessage(String queue, String message, String popReceipt, String text, int visibilityTimeoutInSeconds,
            QueueServiceOptions options) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public ListMessagesResult listMessages(String queue) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public PeekMessagesResult peekMessages(String queue) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteMessage(String queue, String message, String popReceipt) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void deleteMessage(String queue, String message, String popReceipt, QueueServiceOptions options) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void clearMessages(String queue) throws ServiceException {
        // TODO Auto-generated method stub

    }

    public void clearMessages(String queue, QueueServiceOptions options) throws ServiceException {
        // TODO Auto-generated method stub

    }
}
