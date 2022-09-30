package com.azure.messaging.servicebus.administration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
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
import com.azure.messaging.servicebus.administration.implementation.models.RuleActionImpl;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.RuleFilterImpl;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.SubscriptionDescriptionFeed;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescriptionFeed;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.core.http.policy.AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

public class Utility {
    public static final String CONTENT_TYPE = "application/xml";
    public static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    // Name of the entity type when listing queues and topics.
    public static final String QUEUES_ENTITY_TYPE = "queues";
    public static final String TOPICS_ENTITY_TYPE = "topics";

    public static final int NUMBER_OF_ELEMENTS = 100;
    /**
     * Check that the additional headers field is present and add the additional auth header
     *
     * @param headerName name of the header to be added
     * @param context current request context
     */
    public static void addSupplementaryAuthHeader(String headerName, String entity, Context context) {
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
     * @param createQueueOptions Create Queue Body options
     * @return {@link CreateQueueBody}
     */
    public static CreateQueueBody getCreateQueueBody(QueueDescription createQueueOptions) {
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType(CONTENT_TYPE)
            .setQueueDescription(createQueueOptions);
        return new CreateQueueBody()
            .setContent(content);
    }

    public static CreateTopicBody getUpdateTopicBody(TopicProperties topic) {
        final TopicDescription implementation = EntityHelper.toImplementation(topic);
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(implementation);
        return new CreateTopicBody()
            .setContent(content);
    }
    public static CreateTopicBody getCreateTopicBody(TopicDescription topicOptions) {
        final CreateTopicBodyContent content = new CreateTopicBodyContent()
            .setType(CONTENT_TYPE)
            .setTopicDescription(topicOptions);
        return new CreateTopicBody()
            .setContent(content);
    }

    public static CreateRuleBody getUpdateRuleBody(RuleProperties rule) {
        final RuleDescription implementation = EntityHelper.toImplementation(rule);
        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(implementation);
        return new CreateRuleBody()
            .setContent(content);
    }

    public static CreateSubscriptionBody getUpdateSubscriptionBody(SubscriptionProperties subscription) {
        final SubscriptionDescription implementation = EntityHelper.toImplementation(subscription);
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(implementation);
        return new CreateSubscriptionBody()
            .setContent(content);
    }

    public static CreateSubscriptionBody getCreateSubscriptionBody(SubscriptionDescription subscriptionDescription) {
        final CreateSubscriptionBodyContent content = new CreateSubscriptionBodyContent()
            .setType(CONTENT_TYPE)
            .setSubscriptionDescription(subscriptionDescription);
        return new CreateSubscriptionBody().setContent(content);
    }

    public static CreateRuleBody getCreateRuleBody(String ruleName, CreateRuleOptions ruleOptions) {
        final RuleActionImpl action = ruleOptions.getAction() != null
            ? EntityHelper.toImplementation(ruleOptions.getAction())
            : null;
        final RuleFilterImpl filter = ruleOptions.getFilter() != null
            ? EntityHelper.toImplementation(ruleOptions.getFilter())
            : null;
        final RuleDescription rule = new RuleDescription()
            .setAction(action)
            .setFilter(filter)
            .setName(ruleName);

        final CreateRuleBodyContent content = new CreateRuleBodyContent()
            .setType(CONTENT_TYPE)
            .setRuleDescription(rule);
        return new CreateRuleBody().setContent(content);
    }

    public static List<TopicProperties> getTopicPropertiesList(TopicDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getTopicDescription() != null)
            .map(Utility::getTopicProperties)
            .collect(Collectors.toList());
    }

    public static List<QueueProperties> getQueuePropertiesList(QueueDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getQueueDescription() != null)
            .map(Utility::getQueueProperties)
            .collect(Collectors.toList());
    }

    public static QueueProperties getQueueProperties(QueueDescriptionEntry e) {
        final String queueName = getTitleValue(e.getTitle());
        final QueueProperties queueProperties = EntityHelper.toModel(
            e.getContent().getQueueDescription());

        EntityHelper.setQueueName(queueProperties, queueName);

        return queueProperties;
    }

    public static List<RuleProperties> getRulePropertiesList(RuleDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getRuleDescription() != null)
            .map(e -> EntityHelper.toModel(e.getContent().getRuleDescription()))
            .collect(Collectors.toList());
    }

    public static List<SubscriptionProperties> getSubscriptionPropertiesList(String topicName,
                                                                      SubscriptionDescriptionFeed feed) {
        return feed.getEntry().stream()
            .filter(e -> e.getContent() != null && e.getContent().getSubscriptionDescription() != null)
            .map(e -> getSubscriptionProperties(topicName, e))
            .collect(Collectors.toList());
    }

    public static SubscriptionProperties getSubscriptionProperties(String topicName, SubscriptionDescriptionEntry entry) {
        final SubscriptionProperties subscription = EntityHelper.toModel(
            entry.getContent().getSubscriptionDescription());
        final String subscriptionName = getTitleValue(entry.getTitle());
        EntityHelper.setSubscriptionName(subscription, subscriptionName);
        EntityHelper.setTopicName(subscription, topicName);
        return subscription;
    }

    public static TopicProperties getTopicProperties(TopicDescriptionEntry entry) {
        final TopicProperties result = EntityHelper.toModel(entry.getContent().getTopicDescription());
        final String topicName = getTitleValue(entry.getTitle());
        EntityHelper.setTopicName(result, topicName);
        return result;
    }

    /**
     * Given an XML title element, returns the XML text inside. Jackson deserializes Objects as LinkedHashMaps. XML text
     * is represented as an entry with an empty string as the key.
     * <p>
     * For example, the text returned from this {@code <title text="text/xml">QueueName</title>} is "QueueName".
     *
     * @param responseTitle XML title element.
     * @return The XML text inside the title. {@code null} is returned if there is no value.
     */
    @SuppressWarnings("unchecked")
    public static String getTitleValue(Object responseTitle) {
        if (!(responseTitle instanceof Map)) {
            return null;
        }

        final Map<String, String> map;
        try {
            map = (Map<String, String>) responseTitle;
            return map.get("");
        } catch (ClassCastException error) {
            LOGGER.warning("Unable to cast to Map<String,String>. Title: {}", responseTitle, error);
            return null;
        }
    }
    public static void validateQueueName(String queueName) {
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'queueName' cannot be null or empty."));
        }
    }

    public static void validateRuleName(String ruleName) {
        if (CoreUtils.isNullOrEmpty(ruleName)) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'ruleName' cannot be null or empty."));
        }
    }

    public static void validateTopicName(String topicName) {
        if (CoreUtils.isNullOrEmpty(topicName)) {
            throw LOGGER.logExceptionAsError( new IllegalArgumentException("'topicName' cannot be null or empty."));
        }
    }

    public static void validateSubscriptionName(String subscriptionName) {
        if (CoreUtils.isNullOrEmpty(subscriptionName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'subscriptionName' cannot be null or empty."));
        }
    }

    public static Context getTracingContext(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
            .addData(AZURE_REQUEST_HTTP_HEADERS_KEY, new HttpHeaders());
    }

    /**
     * A page of Service Bus entities.
     *
     * @param <T> The entity description from Service Bus.
     */
    public static final class FeedPage<T> implements PagedResponse<T> {
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
        public FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries) {
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
        public FeedPage(int statusCode, HttpHeaders header, HttpRequest request, List<T> entries, int skip) {
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

    public static final class EntityNotFoundHttpResponse<T> extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;

        public EntityNotFoundHttpResponse(Response<T> response) {
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
     *
     * @return A {@link FeedPage} indicating whether this can be continued or not.
     * @throws MalformedURLException if the "next" page link does not contain a well-formed URL.
     */
    public static <TResult, TFeed> FeedPage<TResult> extractPage(Response<TFeed> response, List<TResult> entities,
                                                          List<ResponseLink> responseLinks)
        throws MalformedURLException, UnsupportedEncodingException {
        final Optional<ResponseLink> nextLink = responseLinks.stream()
            .filter(link -> link.getRel().equalsIgnoreCase("next"))
            .findFirst();

        if (nextLink.isEmpty()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }

        final URL url = new URL(nextLink.get().getHref());
        final String decode = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8);
        final Optional<Integer> skipParameter = Arrays.stream(decode.split("&amp;|&"))
            .map(part -> part.split("=", 2))
            .filter(parts -> parts[0].equalsIgnoreCase("$skip") && parts.length == 2)
            .map(parts -> Integer.valueOf(parts[1]))
            .findFirst();

        if (skipParameter.isPresent()) {
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities,
                skipParameter.get());
        } else {
            LOGGER.warning("There should have been a skip parameter for the next page.");
            return new FeedPage<>(response.getStatusCode(), response.getHeaders(), response.getRequest(), entities);
        }
    }
}
