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
package com.microsoft.windowsazure.services.queue.implementation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.queue.QueueConfiguration;
import com.microsoft.windowsazure.services.queue.QueueContract;
import com.microsoft.windowsazure.services.queue.models.CreateMessageOptions;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.QueueServiceOptions;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class QueueRestProxy implements QueueContract {
    //private static Log log = LogFactory.getLog(QueueRestProxy.class);

    private static final String API_VERSION = "2011-08-18";
    private final HttpURLConnectionClient channel;
    private final String accountName;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ServiceFilter[] filters;
    private final SharedKeyFilter filter;

    @Inject
    public QueueRestProxy(HttpURLConnectionClient channel, @Named(QueueConfiguration.ACCOUNT_NAME) String accountName,
            @Named(QueueConfiguration.URI) String url, SharedKeyFilter filter) {

        this.channel = channel;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.filters = new ServiceFilter[0];
        channel.addFilter(filter);
    }

    public QueueRestProxy(HttpURLConnectionClient channel, ServiceFilter[] filters, String accountName, String url,
            SharedKeyFilter filter, RFC1123DateConverter dateMapper) {

        this.channel = channel;
        this.filters = filters;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = dateMapper;
    }

    public QueueContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new QueueRestProxy(this.channel, newFilters, this.accountName, this.url, this.filter, this.dateMapper);
    }

    private void ThrowIfError(ClientResponse r) {
        PipelineHelpers.ThrowIfError(r);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value, defaultValue);
    }

    private Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        return PipelineHelpers.addOptionalMetadataHeader(builder, metadata);
    }

    private HashMap<String, String> getMetadataFromHeaders(ClientResponse response) {
        return PipelineHelpers.getMetadataFromHeaders(response);
    }

    private WebResource getResource(QueueServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout", options.getTimeout());
        for (ServiceFilter filter : filters) {
            webResource.addFilter(new ClientFilterAdapter(filter));
        }

        return webResource;
    }

    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return getServiceProperties(new QueueServiceOptions());
    }

    public GetServicePropertiesResult getServiceProperties(QueueServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        GetServicePropertiesResult result = new GetServicePropertiesResult();
        result.setValue(builder.get(ServiceProperties.class));
        return result;
    }

    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        setServiceProperties(serviceProperties, new QueueServiceOptions());
    }

    public void setServiceProperties(ServiceProperties serviceProperties, QueueServiceOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.put(serviceProperties);
    }

    public void createQueue(String queue) throws ServiceException {
        createQueue(queue, new CreateQueueOptions());

    }

    public void createQueue(String queue, CreateQueueOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue);

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalMetadataHeader(builder, options.getMetadata());

        builder.put();
    }

    public void deleteQueue(String queue) throws ServiceException {
        deleteQueue(queue, new QueueServiceOptions());
    }

    public void deleteQueue(String queue, QueueServiceOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue);

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.delete();
    }

    public ListQueuesResult listQueues() throws ServiceException {
        return listQueues(new ListQueuesOptions());
    }

    public ListQueuesResult listQueues(ListQueuesOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("comp", "list");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
        if (options.isIncludeMetadata()) {
            webResource = webResource.queryParam("include", "metadata");
        }

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(ListQueuesResult.class);
    }

    public GetQueueMetadataResult getQueueMetadata(String queue) throws ServiceException {
        return getQueueMetadata(queue, new QueueServiceOptions());
    }

    public GetQueueMetadataResult getQueueMetadata(String queue, QueueServiceOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).queryParam("comp", "metadata");

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetQueueMetadataResult result = new GetQueueMetadataResult();
        result.setApproximateMessageCount(Integer.parseInt(response.getHeaders().getFirst(
                "x-ms-approximate-messages-count")));
        result.setMetadata(getMetadataFromHeaders(response));

        return result;
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata) throws ServiceException {
        setQueueMetadata(queue, metadata, new QueueServiceOptions());
    }

    public void setQueueMetadata(String queue, HashMap<String, String> metadata, QueueServiceOptions options)
            throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).queryParam("comp", "metadata");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalMetadataHeader(builder, metadata);

        builder.put();
    }

    public void createMessage(String queue, String messageText) throws ServiceException {
        createMessage(queue, messageText, new CreateMessageOptions());
    }

    public void createMessage(String queue, String messageText, CreateMessageOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages");
        webResource = addOptionalQueryParam(webResource, "visibilitytimeout", options.getVisibilityTimeoutInSeconds());
        webResource = addOptionalQueryParam(webResource, "messagettl", options.getTimeToLiveInSeconds());

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setMessageText(messageText);

        builder.post(queueMessage);
    }

    public UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText,
            int visibilityTimeoutInSeconds) throws ServiceException {
        return updateMessage(queue, messageId, popReceipt, messageText, visibilityTimeoutInSeconds,
                new QueueServiceOptions());
    }

    public UpdateMessageResult updateMessage(String queue, String messageId, String popReceipt, String messageText,
            int visibilityTimeoutInSeconds, QueueServiceOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();
        if (messageId == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages").path(messageId);
        webResource = addOptionalQueryParam(webResource, "popreceipt", popReceipt);
        webResource = addOptionalQueryParam(webResource, "visibilitytimeout", visibilityTimeoutInSeconds);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setMessageText(messageText);

        ClientResponse response = builder.put(ClientResponse.class, queueMessage);
        ThrowIfError(response);

        UpdateMessageResult result = new UpdateMessageResult();
        result.setPopReceipt(response.getHeaders().getFirst("x-ms-popreceipt"));
        result.setTimeNextVisible(dateMapper.parse(response.getHeaders().getFirst("x-ms-time-next-visible")));
        return result;
    }

    public ListMessagesResult listMessages(String queue) throws ServiceException {
        return listMessages(queue, new ListMessagesOptions());
    }

    public ListMessagesResult listMessages(String queue, ListMessagesOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages");
        webResource = addOptionalQueryParam(webResource, "visibilitytimeout", options.getVisibilityTimeoutInSeconds());
        webResource = addOptionalQueryParam(webResource, "numofmessages", options.getNumberOfMessages());

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(ListMessagesResult.class);
    }

    public PeekMessagesResult peekMessages(String queue) throws ServiceException {
        return peekMessages(queue, new PeekMessagesOptions());
    }

    public PeekMessagesResult peekMessages(String queue, PeekMessagesOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages").queryParam("peekonly", "true");
        webResource = addOptionalQueryParam(webResource, "numofmessages", options.getNumberOfMessages());

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(PeekMessagesResult.class);
    }

    public void deleteMessage(String queue, String messageId, String popReceipt) throws ServiceException {
        deleteMessage(queue, messageId, popReceipt, new QueueServiceOptions());
    }

    public void deleteMessage(String queue, String messageId, String popReceipt, QueueServiceOptions options)
            throws ServiceException {
        if (queue == null)
            throw new NullPointerException();
        if (messageId == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages").path(messageId);
        webResource = addOptionalQueryParam(webResource, "popreceipt", popReceipt);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.delete();
    }

    public void clearMessages(String queue) throws ServiceException {
        clearMessages(queue, new QueueServiceOptions());
    }

    public void clearMessages(String queue, QueueServiceOptions options) throws ServiceException {
        if (queue == null)
            throw new NullPointerException();

        WebResource webResource = getResource(options).path(queue).path("messages");

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.delete();
    }
}
