// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

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
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBodyContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateRuleBodyImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBodyContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateSubscriptionBodyImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBodyContentImpl;
import com.azure.messaging.servicebus.administration.implementation.models.CreateTopicBodyImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.ResponseLinkImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleActionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleFilterImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntryImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeedImpl;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionImpl;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
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

/**
 * Package-private class for transformations from public models to internal implementation for communication with
 * Service Bus administration API.
 */
class AdministrationModelConverter {
    static final String CONTENT_TYPE = "application/xml";

    private final ClientLogger logger;

    AdministrationModelConverter(ClientLogger logger) {
        this.logger = logger;
    }

    /**
     * Check that the additional headers field is present and add the additional auth header
     *
     * @param headerName name of the header to be added
     * @param context current request context
     */
    void addSupplementaryAuthHeader(HttpHeaderName headerName, String entity, Context context) {
        context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY)
            .ifPresent(headers -> {
                if (headers instanceof HttpHeaders) {
                    HttpHeaders customHttpHeaders = (HttpHeaders) headers;
                    customHttpHeaders.add(headerName, entity);
                }
            });
    }

    /**
     * Create Queue Body
     *
     * @param createQueueOptions Create Queue Body options
     * @return {@link CreateQueueBodyImpl}
     */
    CreateQueueBodyImpl getCreateQueueBody(QueueDescriptionImpl createQueueOptions) {
        final CreateQueueBodyContentImpl content = new CreateQueueBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setQueueDescription(createQueueOptions);
        return new CreateQueueBodyImpl()
            .setContent(content);
    }

    CreateTopicBodyImpl getUpdateTopicBody(TopicProperties topic) {
        final TopicDescriptionImpl implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContentImpl content = new CreateTopicBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setTopicDescription(implementation);
        return new CreateTopicBodyImpl()
            .setContent(content);
    }

    CreateTopicBodyImpl getCreateTopicBody(TopicDescriptionImpl topicOptions) {
        final CreateTopicBodyContentImpl content = new CreateTopicBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topicOptions);
        return new CreateTopicBodyImpl()
            .setContent(content);
    }

    CreateRuleBodyImpl getUpdateRuleBody(RuleProperties rule) {
        final RuleDescriptionImpl implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContentImpl content = new CreateRuleBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setRuleDescription(implementation);
        return new CreateRuleBodyImpl()
            .setContent(content);
    }

    CreateSubscriptionBodyImpl getUpdateSubscriptionBody(SubscriptionProperties subscription) {
        final SubscriptionDescriptionImpl implementation = EntityHelper.toImplementation(subscription);
        final CreateSubscriptionBodyContentImpl content = new CreateSubscriptionBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(implementation);
        return new CreateSubscriptionBodyImpl()
            .setContent(content);
    }

    CreateSubscriptionBodyImpl getCreateSubscriptionBody(SubscriptionDescriptionImpl subscriptionDescription) {
        final CreateSubscriptionBodyContentImpl content = new CreateSubscriptionBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscriptionDescription);
        return new CreateSubscriptionBodyImpl().setContent(content);
    }

    CreateRuleBodyImpl getCreateRuleBody(String ruleName, CreateRuleOptions ruleOptions) {
        final RuleActionImpl action = ruleOptions.getAction() != null
            ? EntityHelper.toImplementation(ruleOptions.getAction())
            : null;
        final RuleFilterImpl filter = ruleOptions.getFilter() != null
            ? EntityHelper.toImplementation(ruleOptions.getFilter())
            : null;
        final RuleDescriptionImpl rule = new RuleDescriptionImpl()
            .setAction(action)
            .setFilter(filter)
            .setName(ruleName);

        final CreateRuleBodyContentImpl content = new CreateRuleBodyContentImpl()
            .setType(CONTENT_TYPE)
            .setRuleDescription(rule);
        return new CreateRuleBodyImpl().setContent(content);
    }

    List<TopicProperties> getTopics(TopicDescriptionFeedImpl feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
            .map(e -> getTopicProperties(e))
            .collect(Collectors.toList());
    }

    List<QueueProperties> getQueues(QueueDescriptionFeedImpl feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
            .map(e -> getQueueProperties(e))
            .collect(Collectors.toList());
    }

    QueueProperties getQueueProperties(QueueDescriptionEntryImpl e) {
        final String queueName = e.getTitle().getContent();
        final QueueProperties queueProperties = EntityHelper.toModel(
            e.getContent().getQueueDescription());

        EntityHelper.setQueueName(queueProperties, queueName);

        return queueProperties;
    }

    List<RuleProperties> getRules(RuleDescriptionFeedImpl feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
            .map(e -> EntityHelper.toModel(e.getContent().getRuleDescription()))
            .collect(Collectors.toList());
    }

    List<SubscriptionProperties> getSubscriptions(String topicName, SubscriptionDescriptionFeedImpl feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
            .map(e -> getSubscriptionProperties(topicName, e))
            .collect(Collectors.toList());
    }

    SubscriptionProperties getSubscriptionProperties(String topicName, SubscriptionDescriptionEntryImpl entry) {
        final SubscriptionProperties subscription = EntityHelper.toModel(
            entry.getContent().getSubscriptionDescription());
        final String subscriptionName = entry.getTitle().getContent();
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);
        return subscription;
    }

    TopicProperties getTopicProperties(TopicDescriptionEntryImpl entry) {
        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = entry.getTitle().getContent();
        EntityHelper.setTopicName(result, topicName);
        return result;
    }

    SimpleResponse<SubscriptionProperties> getSubscriptionPropertiesSimpleResponse(String topicName,
        Response<SubscriptionDescriptionEntryImpl> response) {
        final SubscriptionDescriptionEntryImpl entry = response.getValue();

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

    SimpleResponse<RuleProperties> getRulePropertiesSimpleResponse(
        Response<RuleDescriptionEntryImpl> response) {
        final RuleDescriptionEntryImpl entry = response.getValue();
        // This was an empty response (ie. 204).
        if (entry == null) {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } else if (entry.getContent() == null) {
            logger.info("entry.getContent() is null. The entity may not exist. {}", entry);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        }

        final RuleDescriptionImpl description = entry.getContent().getRuleDescription();
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
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }
    }

    Context getContext(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
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
    @SuppressWarnings({"SimplifyOptionalCallChains"})
    <TResult, TFeed> FeedPage<TResult> extractPage(Response<TFeed> response, List<TResult> entities,
        List<ResponseLinkImpl> responseLinks)
        throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLinkImpl> nextLink = responseLinks.stream()
            .filter(link -> link.getRel().equalsIgnoreCase("next"))
            .findFirst();

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
