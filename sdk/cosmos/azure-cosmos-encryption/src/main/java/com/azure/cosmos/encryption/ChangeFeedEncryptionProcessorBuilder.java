// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Helper class to build a {@link ChangeFeedProcessor} instance for encryption feed container.
 *
 * <pre>
 * ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder&#40;&#41;
 *     .hostName&#40;hostName&#41;
 *     .feedContainer&#40;feedContainer&#41; // {@link CosmosEncryptionAsyncContainer}
 *     .leaseContainer&#40;leaseContainer&#41;
 *     .handleChanges&#40;docs -&gt; &#123;
 *         for &#40;JsonNode item : docs&#41; &#123;
 *             &#47;&#47; Implementation for handling and processing of each JsonNode item goes here
 *         &#125;
 *     &#125;&#41;
 *     .buildChangeFeedProcessor&#40;&#41;;
 * </pre>
 */
public final class ChangeFeedEncryptionProcessorBuilder {

    private String hostName ;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private Consumer<List<JsonNode>> encryptionConsumer;
    private CosmosEncryptionAsyncContainer feedContainer = null;
    private CosmosAsyncContainer leaseContainer = null;

    /**
     * Helper class to build a encryption supported {@link ChangeFeedProcessor} instance.
     *
     */
    public ChangeFeedEncryptionProcessorBuilder() {
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique
     * name.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets an existing {@link CosmosEncryptionAsyncContainer} to be used to read from the monitored container.
     *
     * @param feedContainer the instance of {@link CosmosEncryptionAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder feedContainer(CosmosEncryptionAsyncContainer feedContainer) {
        this.feedContainer = feedContainer;

        return this;
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases container.
     *
     * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder leaseContainer(CosmosAsyncContainer leaseContainer) {
        this.leaseContainer = leaseContainer;

        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes.
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder handleChanges(Consumer<List<JsonNode>> consumer) {
        this.encryptionConsumer = jsonNodes -> {
            List<Mono<JsonNode>> objectNodeMonoList =
                jsonNodes.stream().map(jsonNode -> {
                    if (jsonNode.isObject()) {
                        return feedContainer.decryptResponseNode((ObjectNode) jsonNode);
                    } else {
                        throw new IllegalStateException("Current operation not supported in change feed encryption");
                    }
                }).collect(Collectors.toList());
            Flux.concat(objectNodeMonoList).publishOn(Schedulers.boundedElastic()).
                collectList().doOnSuccess(consumer).block(); //TODO: https://github.com/Azure/azure-sdk-for-java/issues/23738
        };

        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     * Unless specifically set the default values that will be used are:
     * <ul>
     * <li>maximum items per page or FeedResponse: 100</li>
     * <li>lease renew interval: 17 seconds</li>
     * <li>lease acquire interval: 13 seconds</li>
     * <li>lease expiration interval: 60 seconds</li>
     * <li>feed poll delay: 5 seconds</li>
     * <li>maximum scale count: unlimited</li>
     * </ul>
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedEncryptionProcessorBuilder options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor buildChangeFeedProcessor() {
        ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
            .hostName(this.hostName)
            .feedContainer(this.feedContainer.getCosmosAsyncContainer())
            .leaseContainer(this.leaseContainer)
            .handleChanges(this.encryptionConsumer)
            .options(this.changeFeedProcessorOptions);

        return changeFeedProcessorBuilder.buildChangeFeedProcessor();
    }
}
