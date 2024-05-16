// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlGroup;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides synchronous methods for reading, deleting, and replacing existing Containers
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosContainer {

    private static final Logger logger = LoggerFactory.getLogger(CosmosContainer.class);
    final CosmosAsyncContainer asyncContainer;
    private final CosmosDatabase database;
    private final String id;
    private CosmosScripts scripts;

    /**
     * Instantiates a new Cosmos sync container.
     *
     * @param id the container id.
     * @param database the database.
     * @param container the container.
     */
    CosmosContainer(String id, CosmosDatabase database, CosmosAsyncContainer container) {
        this.id = id;
        this.database = database;
        this.asyncContainer = container;
    }

    /**
     * Gets the current container id.
     *
     * @return the container id.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the current container.
     *
     * @return the Cosmos container response with the read container.
     */
    public CosmosContainerResponse read() {
        return database.blockContainerResponse(this.asyncContainer.read());
    }

    /**
     * Reads the current container while specifying additional options such as If-Match.
     *
     * @param options the options.
     * @return the Cosmos container response.
     */
    public CosmosContainerResponse read(CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.read(options));
    }

    /**
     * Deletes the current Cosmos container while specifying additional options such as If-Match.
     *
     * @param options the options.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse delete(CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.delete(options));
    }

    /**
     * Deletes the current cosmos container.
     *
     * @return the cosmos container response.
     */
    public CosmosContainerResponse delete() {
        return database.blockContainerResponse(this.asyncContainer.delete());
    }

    /**
     * Replaces the current container properties.
     *
     * @param containerProperties the container properties.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties) {
        return database.blockContainerResponse(this.asyncContainer.replace(containerProperties));
    }

    /**
     * Replaces the current container properties while specifying additional options such as If-Match.
     *
     * @param containerProperties the container properties.
     * @param options the options.
     * @return the cosmos container response.
     */
    public CosmosContainerResponse replace(CosmosContainerProperties containerProperties,
                                           CosmosContainerRequestOptions options) {
        return database.blockContainerResponse(this.asyncContainer.replace(containerProperties, options));
    }

    /**
     * Sets the throughput for the current container.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.replaceThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties =
     *     ThroughputProperties.createAutoscaledThroughput&#40;1000&#41;;
     * try &#123;
     *     ThroughputResponse throughputResponse =
     *         cosmosContainer.replaceThroughput&#40;throughputProperties&#41;;
     *     System.out.println&#40;throughputResponse&#41;;
     * &#125; catch &#40;CosmosException ce&#41; &#123;
     *     ce.printStackTrace&#40;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     e.printStackTrace&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.replaceThroughput -->
     * @param throughputProperties the throughput properties (Optional).
     * @return the throughput response.
     */
    public ThroughputResponse replaceThroughput(ThroughputProperties throughputProperties) {
        return database.throughputResponseToBlock(this.asyncContainer.replaceThroughput(throughputProperties));
    }

    /**
     * Gets the throughput for the current container.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readThroughput -->
     * <pre>
     * try &#123;
     *     ThroughputResponse throughputResponse = cosmosContainer.readThroughput&#40;&#41;;
     *     System.out.println&#40;throughputResponse&#41;;
     * &#125; catch &#40;CosmosException ce&#41; &#123;
     *     ce.printStackTrace&#40;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     e.printStackTrace&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readThroughput -->
     * @return the throughput response.
     */
    public ThroughputResponse readThroughput() {
        return database.throughputResponseToBlock(this.asyncContainer.readThroughput());
    }

    /* Cosmos item operations */

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response.
     *
     * @param <T> the type parameter
     * @param item the item
     * @return the Cosmos item response
     */
    public <T> CosmosItemResponse<T> createItem(T item) {
        return this.blockItemResponse(this.asyncContainer.createItem(item));
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response
     * while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> createItem(T item,
                                                PartitionKey partitionKey,
                                                CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.createItem(item, partitionKey, options));
    }

    /**
     * Creates a new item synchronously and returns its respective Cosmos item response
     * while specifying additional options.
     * <p>
     * The partition key value will be automatically extracted from the item's content.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the options.
     * @return the cosmos item response.
     */

    public <T> CosmosItemResponse<T> createItem(T item, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.createItem(item, options));
    }

    /**
     * Upserts an Cosmos item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item));
    }

    /**
     * Upserts a item Cosmos sync item while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item, options));
    }

    /**
     * Upserts an item Cosmos sync item while specifying additional options.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param partitionKey the partitionKey.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> upsertItem(T item, PartitionKey partitionKey, CosmosItemRequestOptions options) {
        return this.blockItemResponse(this.asyncContainer.upsertItem(item, partitionKey, options));
    }

    /**
     * Block cosmos item response.
     *
     * @param itemMono the item mono.
     * @return the cosmos item response.
     */
    <T> CosmosItemResponse<T> blockItemResponse(Mono<CosmosItemResponse<T>> itemMono) {
        try {
            return itemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Block cosmos item response.
     *
     * @param itemMono the item mono.
     * @return the cosmos item response.
     */
    <T> FeedResponse<T> blockFeedResponse(Mono<FeedResponse<T>> itemMono) {
        try {
            return itemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private CosmosItemResponse<Object> blockDeleteItemResponse(Mono<CosmosItemResponse<Object>> deleteItemMono) {
        try {
            return deleteItemMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private CosmosBatchResponse blockBatchResponse(Mono<CosmosBatchResponse> batchResponseMono) {
        try {
            return batchResponseMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    private <TContext> List<CosmosBulkOperationResponse<TContext>> blockBulkResponse(
        Flux<CosmosBulkOperationResponse<TContext>> bulkResponse) {

        try {
            return bulkResponse.collectList().block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Read all items as {@link CosmosPagedIterable} in the current container.
     *
     * @param <T> the type parameter.
     * @param options the options.
     * @param classType the classType.
     * @return the {@link CosmosPagedIterable}.
     */
    <T> CosmosPagedIterable<T> readAllItems(CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.readAllItems(options, classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.queryItems -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * String query = &quot;SELECT * FROM Passenger WHERE Passenger.departure IN &#40;'SEA', 'IND'&#41;&quot;;
     * Iterable&lt;FeedResponse&lt;Passenger&gt;&gt; queryResponses = cosmosContainer.queryItems&#40;query, options, Passenger.class&#41;
     *     .iterableByPage&#40;&#41;;
     *
     * for &#40;FeedResponse&lt;Passenger&gt; feedResponse : queryResponses&#41; &#123;
     *     List&lt;Passenger&gt; results = feedResponse.getResults&#40;&#41;;
     *     System.out.println&#40;results&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.queryItems -->
     * @param <T> the type parameter.
     * @param query the query.
     * @param options the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(String query, CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(query, options, classType));
    }

    /**
     * Query items in the current container returning the results as {@link CosmosPagedIterable}.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.SqlQuerySpec.queryItems -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * String query = &quot;SELECT * FROM Passenger p WHERE &#40;p.departure = &#64;departure&#41;&quot;;
     * List&lt;SqlParameter&gt; parameters = Collections.singletonList&#40;new SqlParameter&#40;&quot;&#64;departure&quot;, &quot;SEA&quot;&#41;&#41;;
     * SqlQuerySpec sqlQuerySpec = new SqlQuerySpec&#40;query, parameters&#41;;
     *
     * Iterable&lt;FeedResponse&lt;Passenger&gt;&gt; queryResponses = cosmosContainer.queryItems&#40;sqlQuerySpec, options, Passenger.class&#41;
     *     .iterableByPage&#40;&#41;;
     *
     * for &#40;FeedResponse&lt;Passenger&gt; feedResponse : queryResponses&#41; &#123;
     *     List&lt;Passenger&gt; results = feedResponse.getResults&#40;&#41;;
     *     System.out.println&#40;results&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.SqlQuerySpec.queryItems -->
     * @param <T> the type parameter.
     * @param querySpec the query spec.
     * @param options the options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> queryItems(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> classType) {
        return getCosmosPagedIterable(this.asyncContainer.queryItems(querySpec, options, classType));
    }

    /**
     * Query for items in the change feed of the current container using the {@link CosmosChangeFeedRequestOptions}.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.queryChangeFeed -->
     * <pre>
     * CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
     *     .createForProcessingFromNow&#40;FeedRange.forFullRange&#40;&#41;&#41;
     *     .allVersionsAndDeletes&#40;&#41;;
     *
     * Iterable&lt;FeedResponse&lt;Passenger&gt;&gt; feedResponses = cosmosContainer.queryChangeFeed&#40;options, Passenger.class&#41;
     *     .iterableByPage&#40;&#41;;
     * for &#40;FeedResponse&lt;Passenger&gt; feedResponse : feedResponses&#41; &#123;
     *     List&lt;Passenger&gt; results = feedResponse.getResults&#40;&#41;;
     *     System.out.println&#40;results&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.queryChangeFeed -->
     * <p>
     * The next page can be retrieved by calling queryChangeFeed again with a new instance of
     * {@link CosmosChangeFeedRequestOptions} created from the continuation token of the previously returned
     * {@link FeedResponse} instance.
     *
     * @param <T> the type parameter.
     * @param options the change feed request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one feed response page
     */
    public <T> CosmosPagedIterable<T> queryChangeFeed(
        CosmosChangeFeedRequestOptions options,
        Class<T> classType) {

        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        options.setMaxPrefetchPageCount(1);

        return getCosmosPagedIterable(
            this.asyncContainer
                .queryChangeFeed(options, classType));
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * FeedResponse&lt;Passenger&gt; passengerFeedResponse = cosmosContainer.readMany&#40;itemIdentityList, Passenger.class&#41;;
     * for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *     System.out.println&#40;passenger&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> FeedResponse<T> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        Class<T> classType) {

        return this.readMany(itemIdentityList, (CosmosReadManyRequestOptions)null, classType);
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * FeedResponse&lt;Passenger&gt; passengerFeedResponse = cosmosContainer.readMany&#40;itemIdentityList, Passenger.class&#41;;
     * for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *     System.out.println&#40;passenger&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param sessionToken the optional Session token - null if the read can be made without specific session token
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> FeedResponse<T> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String sessionToken,
        Class<T> classType) {

        return this.blockFeedResponse(
            this.asyncContainer.readMany(
                itemIdentityList,
                sessionToken,
                classType));
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * FeedResponse&lt;Passenger&gt; passengerFeedResponse = cosmosContainer.readMany&#40;itemIdentityList, Passenger.class&#41;;
     * for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *     System.out.println&#40;passenger&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param options the optional request options
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> FeedResponse<T> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        CosmosReadManyRequestOptions options,
        Class<T> classType) {

        return this.blockFeedResponse(
            this.asyncContainer.readMany(
                itemIdentityList,
                options,
                classType));
    }

    /**
     * Reads all the items of a logical partition returning the results as {@link CosmosPagedIterable}.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readAllItems -->
     * <pre>
     * CosmosPagedIterable&lt;Passenger&gt; passengers = cosmosContainer
     *     .readAllItems&#40;new PartitionKey&#40;partitionKey&#41;, Passenger.class&#41;;
     *
     * passengers.forEach&#40;passenger -&gt; &#123;
     *     System.out.println&#40;passenger&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readAllItems -->
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> readAllItems(
        PartitionKey partitionKey,
        Class<T> classType) {

        return this.readAllItems(partitionKey, new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Reads all the items of a logical partition returning the results as {@link CosmosPagedIterable}.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readAllItems -->
     * <pre>
     * CosmosPagedIterable&lt;Passenger&gt; passengers = cosmosContainer
     *     .readAllItems&#40;new PartitionKey&#40;partitionKey&#41;, Passenger.class&#41;;
     *
     * passengers.forEach&#40;passenger -&gt; &#123;
     *     System.out.println&#40;passenger&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readAllItems -->
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param options the feed options.
     * @param classType the class type.
     * @return the {@link CosmosPagedIterable}.
     */
    public <T> CosmosPagedIterable<T> readAllItems(
        PartitionKey partitionKey,
        CosmosQueryRequestOptions options,
        Class<T> classType) {

        return getCosmosPagedIterable(this.asyncContainer.readAllItems(partitionKey, options, classType));
    }

    /**
     * Reads an item in the current container.
     * <br/>
     * This operation is used to retrieve a single item from a container based on its unique identifier (ID) and partition key.
     * The readItem operation provides direct access to a specific item using its unique identifier, which consists of the item's ID and the partition key value. This operation is efficient for retrieving a known item by its ID and partition key without the need for complex querying.
     * <!-- src_embed com.azure.cosmos.CosmosContainer.readItem -->
     * <pre>
     * &#47;&#47; Read an item
     * try &#123;
     *     CosmosItemResponse&lt;Passenger&gt; response = cosmosContainer.readItem&#40;
     *         passenger.getId&#40;&#41;,
     *         new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *         Passenger.class
     *     &#41;;
     *     Passenger passengerItem = response.getItem&#40;&#41;;
     * &#125; catch &#40;NotFoundException e&#41; &#123;
     *     &#47;&#47; catch exception if item not found
     *     System.out.printf&#40;&quot;Passenger with item id %s not found&#92;n&quot;,
     *         passenger.getId&#40;&#41;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     System.out.println&#40;e.getMessage&#40;&#41;&#41;;
     * &#125;
     *
     * &#47;&#47; ...
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.readItem -->
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param itemType the class type of item.
     * @return the Cosmos item response.
     * @throws com.azure.cosmos.implementation.NotFoundException if document with the specified itemId does not exist
     */
    public <T> CosmosItemResponse<T> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) {
        return this.blockItemResponse(asyncContainer.readItem(itemId,
                                                                    partitionKey,
                                                                    new CosmosItemRequestOptions(),
                                                                    itemType));
    }

    /**
     * Reads an item in the current container while specifying additional options.
     * <br/>
     * This operation is used to retrieve a single item from a container based on its unique identifier (ID) and partition key.
     * The readItem operation provides direct access to a specific item using its unique identifier, which consists of the item's ID and the partition key value. This operation is efficient for retrieving a known item by its ID and partition key without the need for complex querying.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options (Optional).
     * @param itemType the class type of item.
     * @return the Cosmos item response.
     * @throws com.azure.cosmos.implementation.NotFoundException if document with the specified itemId does not exist
     */
    public <T> CosmosItemResponse<T> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) {
        return this.blockItemResponse(asyncContainer.readItem(itemId, partitionKey, options, itemType));
    }

    /**
     * Replaces an existing item in a container with a new item.
     * It performs a complete replacement of the item,
     * replacing all its properties with the properties of the new item
     * <!-- src_embed com.azure.cosmos.CosmosContainer.replaceItem -->
     * <pre>
     * CosmosItemResponse&lt;Passenger&gt; response = cosmosContainer.replaceItem&#40;
     *     newPassenger,
     *     oldPassenger.getId&#40;&#41;,
     *     new PartitionKey&#40;oldPassenger.getId&#40;&#41;&#41;,
     *     new CosmosItemRequestOptions&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.replaceItem -->
     * @param <T> the type parameter.
     * @param item the item.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<T> replaceItem(T item,
                                                 String itemId,
                                                 PartitionKey partitionKey,
                                                 CosmosItemRequestOptions options) {
        return this.blockItemResponse(asyncContainer.replaceItem(item, itemId, partitionKey, options));
    }

    /**
     * Run partial update that modifies specific properties or fields of the item without replacing the entire item.
     *
     * <!-- src_embed com.azure.cosmos.CosmosContainer.patchItem -->
     * <pre>
     * CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create&#40;&#41;;
     *
     * cosmosPatchOperations
     *     .add&#40;&quot;&#47;departure&quot;, &quot;SEA&quot;&#41;
     *     .increment&#40;&quot;&#47;trips&quot;, 1&#41;;
     *
     * CosmosItemResponse&lt;Passenger&gt; response = cosmosContainer.patchItem&#40;
     *     passenger.getId&#40;&#41;,
     *     new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *     cosmosPatchOperations,
     *     Passenger.class&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.patchItem -->
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param itemType the item type.
     *
     * @return the Cosmos item resource response with the patched item or an exception.
     */
    public <T> CosmosItemResponse<T> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        Class<T> itemType) {

        return this.blockItemResponse(asyncContainer.patchItem(itemId, partitionKey, cosmosPatchOperations, itemType));
    }

    /**
     * Run partial update that modifies specific properties or fields of the item without replacing the entire item.
     *
     * <!-- src_embed com.azure.cosmos.CosmosContainer.patchItem -->
     * <pre>
     * CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create&#40;&#41;;
     *
     * cosmosPatchOperations
     *     .add&#40;&quot;&#47;departure&quot;, &quot;SEA&quot;&#41;
     *     .increment&#40;&quot;&#47;trips&quot;, 1&#41;;
     *
     * CosmosItemResponse&lt;Passenger&gt; response = cosmosContainer.patchItem&#40;
     *     passenger.getId&#40;&#41;,
     *     new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *     cosmosPatchOperations,
     *     Passenger.class&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.patchItem -->
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param options the request options.
     * @param itemType the item type.
     *
     * @return the Cosmos item resource response with the patched item or an exception.
     */
    public <T> CosmosItemResponse<T> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Class<T> itemType) {

        return this.blockItemResponse(asyncContainer.patchItem(itemId, partitionKey, cosmosPatchOperations, options, itemType));
    }

    /**
     * Deletes an item in the current container.
     *
     * <!-- src_embed com.azure.cosmos.CosmosContainer.deleteItem -->
     * <pre>
     * try &#123;
     *     CosmosItemRequestOptions options = new CosmosItemRequestOptions&#40;&#41;;
     *     CosmosItemResponse&lt;Object&gt; deleteItemResponse = cosmosContainer.deleteItem&#40;
     *         passenger.getId&#40;&#41;,
     *         new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *         options
     *     &#41;;
     *     System.out.println&#40;deleteItemResponse&#41;;
     * &#125; catch &#40;NotFoundException e&#41; &#123;
     *     &#47;&#47; catch exception if item not found
     *     System.out.printf&#40;&quot;Passenger with item id %s not found&#92;n&quot;,
     *         passenger.getId&#40;&#41;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     System.out.println&#40;e.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.deleteItem -->
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public CosmosItemResponse<Object> deleteItem(String itemId, PartitionKey partitionKey,
                                                 CosmosItemRequestOptions options) {
        return  this.blockDeleteItemResponse(asyncContainer.deleteItem(itemId, partitionKey, options));
    }

    /**
     * Deletes all items in the Container with the specified partitionKey value.
     * Starts an asynchronous Cosmos DB background operation which deletes all items in the Container with the specified value.
     * The asynchronous Cosmos DB background operation runs using a percentage of user RUs.
     *
     * @param partitionKey the partition key.
     * @param options the options.
     * @return the Cosmos item response
     */
    public CosmosItemResponse<Object> deleteAllItemsByPartitionKey(PartitionKey partitionKey, CosmosItemRequestOptions options) {
        return this.blockDeleteItemResponse(asyncContainer.deleteAllItemsByPartitionKey(partitionKey, options));
    }

    /**
     * Deletes an item in the current container.
     *
     * @param <T> the type parameter.
     * @param item the item to be deleted.
     * @param options the options.
     * @return the Cosmos item response.
     */
    public <T> CosmosItemResponse<Object> deleteItem(T item, CosmosItemRequestOptions options) {
        return  this.blockDeleteItemResponse(asyncContainer.deleteItem(item, options));
    }

    /**
     * Executes the transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     *
     * @return A TransactionalBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public CosmosBatchResponse executeCosmosBatch(CosmosBatch cosmosBatch) {
        return this.blockBatchResponse(asyncContainer.executeCosmosBatch(cosmosBatch));
    }

    /**
     * Executes the transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A CosmosBatchResponse which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then this will throw an exception instead of returning a CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public CosmosBatchResponse executeCosmosBatch(
        CosmosBatch cosmosBatch,
        CosmosBatchRequestOptions requestOptions) {

        return this.blockBatchResponse(asyncContainer.executeCosmosBatch(cosmosBatch, requestOptions));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     * @param operations list of operation which will be executed by this container.
     *
     * @return An Iterable of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Iterable<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Iterable<CosmosItemOperation> operations) {

        return this.blockBulkResponse(asyncContainer.executeBulkOperations(Flux.fromIterable(operations)));
    }

    /**
     * Executes list of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     *
     * @param operations list of operation which will be executed by this container.
     * @param bulkOptions Options that apply for this Bulk request which specifies options regarding execution like
     *                    concurrency, batching size, interval and context.
     *
     * @return An Iterable of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Iterable<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Iterable<CosmosItemOperation> operations,
        CosmosBulkExecutionOptions bulkOptions) {

        return this.blockBulkResponse(asyncContainer.executeBulkOperations(Flux.fromIterable(operations), bulkOptions));
    }

    /**
     * Gets the Cosmos scripts using the current container as context.
     *
     * @return the Cosmos sync scripts.
     */
    public CosmosScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosScripts(this, asyncContainer.getScripts());
        }
        return this.scripts;
    }

    // TODO: should make partitionkey public in CosmosAsyncItem and fix the below call

    private <T> CosmosPagedIterable<T> getCosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }

    /**
     * Obtains a list of {@link FeedRange} that can be used to parallelize Feed
     * operations.
     *
     * <!-- src_embed com.azure.cosmos.CosmosContainer.getFeedRanges -->
     * <pre>
     * List&lt;FeedRange&gt; feedRanges = cosmosContainer.getFeedRanges&#40;&#41;;
     * for &#40;FeedRange feedRange : feedRanges&#41; &#123;
     *     System.out.println&#40;&quot;Feed range: &quot; + feedRange&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosContainer.getFeedRanges -->
     * @return An unmodifiable list of {@link FeedRange}
     */
    public List<FeedRange> getFeedRanges() {
        try {
            return asyncContainer.getFeedRanges().block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Enable the throughput control group with local control mode.
     *
     * <!-- src_embed com.azure.cosmos.throughputControl.localControl -->
     * <pre>
     * ThroughputControlGroupConfig groupConfig =
     *     new ThroughputControlGroupConfigBuilder&#40;&#41;
     *         .groupName&#40;&quot;localControlGroup&quot;&#41;
     *         .targetThroughputThreshold&#40;0.1&#41;
     *         .build&#40;&#41;;
     *
     * container.enableLocalThroughputControlGroup&#40;groupConfig&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.throughputControl.localControl -->
     *
     * @param groupConfig A {@link GlobalThroughputControlConfig}.
     */
    public void enableLocalThroughputControlGroup(ThroughputControlGroupConfig groupConfig) {
        this.asyncContainer.enableLocalThroughputControlGroup(groupConfig);
    }

    /**
     * Enable the throughput control group with global control mode.
     * The defined throughput limit will be shared across different clients.
     *
     * <!-- src_embed com.azure.cosmos.throughputControl.globalControl -->
     * <pre>
     * ThroughputControlGroupConfig groupConfig =
     *     new ThroughputControlGroupConfigBuilder&#40;&#41;
     *         .groupName&#40;&quot;localControlGroup&quot;&#41;
     *         .targetThroughputThreshold&#40;0.1&#41;
     *         .build&#40;&#41;;
     *
     * GlobalThroughputControlConfig globalControlConfig =
     *     this.client.createGlobalThroughputControlConfigBuilder&#40;database.getId&#40;&#41;, container.getId&#40;&#41;&#41;
     *         .setControlItemRenewInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .setControlItemExpireInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;
     *         .build&#40;&#41;;
     *
     * container.enableGlobalThroughputControlGroup&#40;groupConfig, globalControlConfig&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.throughputControl.globalControl -->
     *
     * @param groupConfig The throughput control group configuration, see {@link GlobalThroughputControlGroup}.
     * @param globalControlConfig The global throughput control configuration, see {@link GlobalThroughputControlConfig}.
     */
    public void enableGlobalThroughputControlGroup(ThroughputControlGroupConfig groupConfig, GlobalThroughputControlConfig globalControlConfig) {
        this.asyncContainer.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);
    }

    /**
     *  Initializes the container by warming up the caches and connections for the current read region.
     *
     *  <p>
     *  <br>NOTE: This API ideally should be called only once during application initialization before any workload.
     *  <br>In case of any transient error, caller should consume the error and continue the regular workload.
     *  </p>
     *
     *  @deprecated use {@link CosmosClientBuilder#openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig)} instead.
     *
     */
    @Deprecated
    public void openConnectionsAndInitCaches() {
        blockVoidResponse(this.asyncContainer.openConnectionsAndInitCaches());
    }

    /**
     *  Initializes the container by warming up the caches and connections to a specified no. of proactive connection regions.
     *  For more information about proactive connection regions, see {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()}
     *
     *  <p>
     *  <br>NOTE: This API ideally should be called only once during application initialization before any workload.
     *  <br>In case of any transient error, caller should consume the error and continue the regular workload.
     *  </p>
     *
     * @param numProactiveConnectionRegions the no of regions to proactively connect to from the preferred list of regions
     * @deprecated use {@link CosmosClientBuilder#openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig)} instead.
     */
    @Deprecated
    public void openConnectionsAndInitCaches(int numProactiveConnectionRegions) {
        blockVoidResponse(this.asyncContainer.openConnectionsAndInitCaches(numProactiveConnectionRegions));
    }

    private void blockVoidResponse(Mono<Void> voidMono) {
        try {
            voidMono.block();
        } catch (Exception ex) {
            // swallow exceptions here
            logger.warn("The void flux did not complete successfully", ex);
        }
    }
}
