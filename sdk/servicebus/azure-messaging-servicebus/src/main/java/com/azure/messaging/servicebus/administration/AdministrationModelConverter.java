// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLink;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementError;
import com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeed;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;

/**
 * Package-private class for transformations from public models to internal implementation for communication with
 * Service Bus administration API.
 */
class AdministrationModelConverter {
    static final String CONTENT_TYPE = "application/xml";

    private final ClientLogger logger;
    private final String serviceBusNamespace;

    AdministrationModelConverter(ClientLogger logger, String serviceBusNamespace) {
        this.logger = logger;
        this.serviceBusNamespace = serviceBusNamespace;
    }

    /**
     * Check that the additional headers field is present and add the additional auth header
     *
     * @param headerName name of the header to be added
     * @param context current request context
     */
    void addSupplementaryAuthHeader(HttpHeaderName headerName, String entity, Context context) {
        context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY).ifPresent(headers -> {
            if (headers instanceof HttpHeaders) {
                HttpHeaders customHttpHeaders = (HttpHeaders) headers;
                customHttpHeaders.add(headerName, entity);
            }
        });
    }

    //region Create entity methods

    CreateQueueBody getCreateQueueBody(QueueDescription queueDescription) {
        final CreateQueueBodyContent content
            = new CreateQueueBodyContent().setType(CONTENT_TYPE).setQueueDescription(queueDescription);
        return new CreateQueueBody().setContent(content);
    }

    /**
     * Generates a create queue request based on the create options.
     *
     * @param createQueueOptions Options for queue creation.
     * @param context Context.
     *
     * @return The queue create request.
     */
    CreateQueueBody getCreateQueueBody(CreateQueueOptions createQueueOptions, Context context) {

        final String forwardTo = getForwardToEntity(createQueueOptions.getForwardTo(), context);
        if (forwardTo != null) {
            createQueueOptions.setForwardTo(forwardTo);
        }

        final String forwardDlq = getForwardDlqEntity(createQueueOptions.getForwardDeadLetteredMessagesTo(), context);
        if (forwardDlq != null) {
            createQueueOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        return getCreateQueueBody(EntityHelper.getQueueDescription(createQueueOptions));
    }

    CreateRuleBody getCreateRuleBody(String ruleName, CreateRuleOptions ruleOptions) {
        final com.azure.messaging.servicebus.administration.implementation.models.RuleAction action
            = ruleOptions.getAction() != null ? EntityHelper.toImplementation(ruleOptions.getAction()) : null;
        final com.azure.messaging.servicebus.administration.implementation.models.RuleFilter filter
            = ruleOptions.getFilter() != null ? EntityHelper.toImplementation(ruleOptions.getFilter()) : null;
        final RuleDescription rule = new RuleDescription().setAction(action).setFilter(filter).setName(ruleName);

        final CreateRuleBodyContent content
            = new CreateRuleBodyContent().setType(CONTENT_TYPE).setRuleDescription(rule);
        return new CreateRuleBody().setContent(content);
    }

    CreateSubscriptionBody getCreateSubscriptionBody(SubscriptionDescription subscriptionDescription) {
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent().setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscriptionDescription);

        return new CreateSubscriptionBody().setContent(content);
    }

    /**
     * Generates a create subscription request based on options. {@code ruleName} and {@code ruleOptions} are optional.
     * No rules are created if they are null.
     *
     * @param subscriptionOptions Options associated with creating the subscription.
     * @param ruleName Optional, rule name.
     * @param ruleOptions Optional, operations associated with rule.
     * @param context Context with request.
     *
     * @return A create subscription request.
     *
     * @throws IllegalArgumentException if {@code ruleOptions} is not null but the filter is null.
     */
    CreateSubscriptionBody getCreateSubscriptionBody(CreateSubscriptionOptions subscriptionOptions, String ruleName,
        CreateRuleOptions ruleOptions, Context context) {

        final String forwardTo = getForwardToEntity(subscriptionOptions.getForwardTo(), context);
        if (forwardTo != null) {
            subscriptionOptions.setForwardTo(forwardTo);
        }
        final String forwardDlq = getForwardDlqEntity(subscriptionOptions.getForwardDeadLetteredMessagesTo(), context);
        if (forwardDlq != null) {
            subscriptionOptions.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        if (ruleOptions != null) {
            if (ruleOptions.getFilter() == null) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'RuleFilter' cannot be null."));
            }

            final RuleDescription rule = new RuleDescription()
                .setAction(
                    ruleOptions.getAction() != null ? EntityHelper.toImplementation(ruleOptions.getAction()) : null)
                .setFilter(EntityHelper.toImplementation(ruleOptions.getFilter()))
                .setName(ruleName);
            subscriptionOptions.setDefaultRule(EntityHelper.toModel(rule));
        }

        return getCreateSubscriptionBody(EntityHelper.getSubscriptionDescription(subscriptionOptions));
    }

    CreateTopicBody getCreateTopicBody(TopicDescription topicOptions) {
        final CreateTopicBodyContent content
            = new CreateTopicBodyContent().setType(CONTENT_TYPE).setTopicDescription(topicOptions);
        return new CreateTopicBody().setContent(content);
    }

    //endregion

    //region Update entity methods

    /**
     * Generates a create queue request based on the existing queue properties.
     *
     * @param queue Queue to create request for.
     * @param context Context.
     * @return A create queue request with the corresponding properties.
     */
    CreateQueueBody getUpdateQueueBody(QueueProperties queue, Context context) {
        final String forwardTo = getForwardToEntity(queue.getForwardTo(), context);
        if (forwardTo != null) {
            queue.setForwardTo(forwardTo);
        }

        final String forwardDlq = getForwardDlqEntity(queue.getForwardDeadLetteredMessagesTo(), context);
        if (forwardDlq != null) {
            queue.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        return getCreateQueueBody(EntityHelper.toImplementation(queue));
    }

    CreateRuleBody getUpdateRuleBody(RuleProperties rule) {
        final RuleDescription implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContent content
            = new CreateRuleBodyContent().setType(CONTENT_TYPE).setRuleDescription(implementation);
        return new CreateRuleBody().setContent(content);
    }

    CreateSubscriptionBody getUpdateSubscriptionBody(SubscriptionProperties subscription, Context context) {
        final String forwardTo = getForwardToEntity(subscription.getForwardTo(), context);
        if (forwardTo != null) {
            subscription.setForwardTo(forwardTo);
        }
        final String forwardDlq = getForwardDlqEntity(subscription.getForwardDeadLetteredMessagesTo(), context);
        if (forwardDlq != null) {
            subscription.setForwardDeadLetteredMessagesTo(forwardDlq);
        }

        // Set read-only properties on the subscription to null so they are not serialized.  The service will not
        // properly update fields if it encounters MessageCountDetails in the serialized XML.  Mirrors behaviour in
        // Track 1 library.
        final SubscriptionDescription implementation = EntityHelper.toImplementation(subscription)
            .setDefaultMessageTimeToLive(null)
            .setMessageCount(null)
            .setCreatedAt(null)
            .setUpdatedAt(null)
            .setAccessedAt(null)
            .setMessageCountDetails(null)
            .setEntityAvailabilityStatus(null);

        return getCreateSubscriptionBody(implementation);
    }

    CreateTopicBody getUpdateTopicBody(TopicProperties topic) {
        final TopicDescription implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContent content
            = new CreateTopicBodyContent().setType(CONTENT_TYPE).setTopicDescription(implementation);
        return new CreateTopicBody().setContent(content);
    }

    //endregion

    //region List entity methods

    List<TopicProperties> getTopics(TopicDescriptionFeed feed) {
        return feed.getEntry()
            .stream()
            .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
            .map(e -> getTopicProperties(e))
            .collect(Collectors.toList());
    }

    List<QueueProperties> getQueues(QueueDescriptionFeed feed) {
        return feed.getEntry()
            .stream()
            .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
            .map(e -> getQueueProperties(e))
            .collect(Collectors.toList());
    }

    List<RuleProperties> getRules(RuleDescriptionFeed feed) {
        return feed.getEntry()
            .stream()
            .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
            .map(e -> EntityHelper.toModel(e.getContent().getRuleDescription()))
            .collect(Collectors.toList());
    }

    //endregion

    List<SubscriptionProperties> getSubscriptions(String topicName, SubscriptionDescriptionFeed feed) {
        return feed.getEntry()
            .stream()
            .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
            .map(e -> getSubscriptionProperties(topicName, e))
            .collect(Collectors.toList());
    }

    QueueProperties getQueueProperties(QueueDescriptionEntry e) {
        final String queueName = e.getTitle().getContent();
        final QueueProperties queueProperties = EntityHelper.toModel(e.getContent().getQueueDescription());

        EntityHelper.setQueueName(queueProperties, queueName);

        return queueProperties;
    }

    SubscriptionProperties getSubscriptionProperties(String topicName, SubscriptionDescriptionEntry entry) {
        final SubscriptionProperties subscription
            = EntityHelper.toModel(entry.getContent().getSubscriptionDescription());
        final String subscriptionName = entry.getTitle().getContent();
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);
        return subscription;
    }

    TopicProperties getTopicProperties(TopicDescriptionEntry entry) {
        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = entry.getTitle().getContent();
        EntityHelper.setTopicName(result, topicName);
        return result;
    }

    SimpleResponse<SubscriptionProperties> getSubscriptionPropertiesSimpleResponse(String topicName,
        Response<SubscriptionDescriptionEntry> response) {
        final SubscriptionDescriptionEntry entry = response.getValue();

        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.warning("entry.getContent() is null. There should have been content returned. Entry: {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }
        final SubscriptionProperties subscription = getSubscriptionProperties(topicName, entry);
        final String subscriptionName = entry.getTitle().getContent();
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            subscription);
    }

    SimpleResponse<RuleProperties> getRulePropertiesSimpleResponse(Response<RuleDescriptionEntry> response) {
        final RuleDescriptionEntry entry = response.getValue();
        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final RuleDescription description = entry.getContent().getRuleDescription();
        final RuleProperties result = EntityHelper.toModel(description);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), result);
    }

    void validateQueueName(String queueName) {
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
    }

    void validateRuleName(String ruleName) {
        if (CoreUtils.isNullOrEmpty(ruleName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'ruleName' cannot be null or empty."));
        }
    }

    void validateTopicName(String topicName) {
        if (CoreUtils.isNullOrEmpty(topicName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
    }

    void validateSubscriptionName(String subscriptionName) {
        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }
    }

    Response<QueueProperties> deserializeQueue(Response<Object> response) {
        return EntityHelper.deserializeQueue(response, logger);
    }

    Response<QueueDescriptionFeed> deserializeQueueFeed(Response<Object> response) {
        return EntityHelper.deserializeQueueFeed(response, logger);
    }

    Response<TopicProperties> deserializeTopic(Response<Object> response) {
        return EntityHelper.deserializeTopic(response, logger);
    }

    Response<TopicDescriptionFeed> deserializeTopicFeed(Response<Object> response) {
        return EntityHelper.deserializeTopicFeed(response, logger);
    }

    Context getContext(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
    }

    /**
     * Checks if the given entity is an absolute URL, if so return it. Otherwise, construct the URL from the given
     * entity and return that.
     *
     * @param entity Entity to generate absolute URL from.
     *
     * @return Forward to Entity represented as an absolute URL. null if a valid URL could not be constructed.
     */
    String getAbsoluteUrlFromEntity(String entity) {
        // Check if passed entity is an absolute URL
        try {
            URL url = new URL(entity);
            return url.toString();
        } catch (MalformedURLException ex) {
            // Entity is not a URL, continue.
        }
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setScheme("https");
        urlBuilder.setHost(serviceBusNamespace);
        urlBuilder.setPath(entity);

        try {
            URL url = urlBuilder.toUrl();
            return url.toString();
        } catch (MalformedURLException ex) {
            // This is not expected.
            logger.error("Failed to construct URL using the endpoint:'{}' and entity:'{}'", serviceBusNamespace,
                entity);
            logger.logThrowableAsError(ex);
        }
        return null;
    }

    private String getForwardDlqEntity(String forwardDlqToEntity, Context contextWithHeaders) {
        if (!CoreUtils.isNullOrEmpty(forwardDlqToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardDlqToEntity,
                contextWithHeaders);
            return getAbsoluteUrlFromEntity(forwardDlqToEntity);
        }
        return null;
    }

    private String getForwardToEntity(String forwardToEntity, Context contextWithHeaders) {
        if (!CoreUtils.isNullOrEmpty(forwardToEntity)) {
            addSupplementaryAuthHeader(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, forwardToEntity,
                contextWithHeaders);
            return getAbsoluteUrlFromEntity(forwardToEntity);
        }
        return null;
    }

    /**
     * Maps an exception to its associated {@link HttpResponseException}. If it is not an ATOM API exception, the
     * exception is returned as-is.
     *
     * @param exception Exception from the ATOM API.
     *
     * @return The corresponding {@link HttpResponseException} or {@code throwable} if it is not an instance of
     *     {@link ServiceBusManagementErrorException}.
     */
    static Throwable mapException(Throwable exception) {
        if (!(exception instanceof ServiceBusManagementErrorException)) {
            return exception;
        }

        return mapException((ServiceBusManagementErrorException) exception);
    }

    /**
     * Maps an exception from the ATOM APIs to its associated {@link HttpResponseException}.
     *
     * @param exception The ATOM API exception.
     * @return Remapped exception.
     */
    static RuntimeException mapException(ServiceBusManagementErrorException exception) {
        final ServiceBusManagementError error = exception.getValue();
        final HttpResponse errorHttpResponse = exception.getResponse();

        final int statusCode
            = error != null && error.getCode() != null ? error.getCode() : errorHttpResponse.getStatusCode();
        final String errorDetail
            = error != null && error.getDetail() != null ? error.getDetail() : exception.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, errorHttpResponse, exception);

            case 404:
                return new ResourceNotFoundException(errorDetail, errorHttpResponse, exception);

            case 409:
                return new ResourceExistsException(errorDetail, errorHttpResponse, exception);

            case 412:
                return new ResourceModifiedException(errorDetail, errorHttpResponse, exception);

            default:
                return new HttpResponseException(errorDetail, errorHttpResponse, exception);
        }
    }

    /**
     * A page of Service Bus entities.
     *
     * @param <T> The entity description from Service Bus.
     */
    static final class FeedPage<T> implements PagedResponse<T> {
        private final int statusCode;
        private final HttpHeaders header;
        private final HttpRequest request;
        private final IterableStream<T> entries;
        private final String continuationToken;

        /**
         * Creates a page that does not have any more pages.
         *
         * @param entries Items in the page.
         */
        FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries) {
            this.statusCode = statusCode;
            this.header = header;
            this.request = request;
            this.entries = new IterableStream<>(entries);
            this.continuationToken = null;
        }

        /**
         * Creates an instance that has additional pages to fetch.
         *
         * @param entries Items in the page.
         * @param skip Number of elements to "skip".
         */
        FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries, int skip) {
            this.statusCode = statusCode;
            this.header = header;
            this.request = request;
            this.entries = new IterableStream<>(entries);
            this.continuationToken = String.valueOf(skip);
        }

        @Override
        public IterableStream<T> getElements() {
            return entries;
        }

        @Override
        public String getContinuationToken() {
            return continuationToken;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public HttpHeaders getHeaders() {
            return header;
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public void close() {
        }
    }

    static final class EntityNotFoundHttpResponse<T> extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;

        EntityNotFoundHttpResponse(Response<T> response) {
            super(response.getRequest());
            this.headers = response.getHeaders();
            this.statusCode = response.getStatusCode();
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.empty();
        }
    }

    /**
     * Creates a {@link FeedPage} given the elements and a set of response links to get the next link from.
     *
     * @param entities Entities in the feed.
     * @param responseLinks Links returned from the feed.
     * @param <TResult> Type of Service Bus entities in page.
     * @return A {@link FeedPage} indicating whether this can be continued or not.
     * @throws MalformedURLException if the "next" page link does not contain a well-formed URL.
     */
    @SuppressWarnings({ "SimplifyOptionalCallChains" })
    <TResult, TFeed> FeedPage<TResult> extractPage(Response<TFeed> response, List<TResult> entities,
        List<ResponseLink> responseLinks) throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLink> nextLink
            = responseLinks.stream().filter(link -> link.getRel().equalsIgnoreCase("next")).findFirst();

        if (!nextLink.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }

        final URL url = new URL(nextLink.get().getHref());
        final String decode = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8.toString());
        final Optional<Integer> skipParameter = Arrays.stream(decode.split("&amp;|&"))
            .map(part -> part.split("=", 2))
            .filter(parts -> parts[0].equalsIgnoreCase("$skip") && parts.length == 2)
            .map(parts -> Integer.valueOf(parts[1]))
            .findFirst();

        if (skipParameter.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities,
                skipParameter.get());
        } else {
            logger.warning("There should have been a skip parameter for the next page.");
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }
    }
}
