// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.IPartitionKeyRangeCache;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManagerSettings;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.ServiceItemLeaseUpdater;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedHelper;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides flexible way to buildAsyncClient lease manager constructor parameters.
 * For the actual creation of lease manager instance, delegates to lease manager factory.
 */
public class LeaseStoreManagerImpl implements LeaseStoreManager, LeaseStoreManager.LeaseStoreManagerBuilderDefinition {
    private final String LEASE_STORE_MANAGER_LEASE_SUFFIX = "..";

    private final Logger logger = LoggerFactory.getLogger(LeaseStoreManagerImpl.class);
    private LeaseStoreManagerSettings settings;
    private ChangeFeedContextClient leaseDocumentClient;
    private RequestOptionsFactory requestOptionsFactory;
    private ServiceItemLeaseUpdater leaseUpdater;
    private LeaseStore leaseStore;

    public static LeaseStoreManagerBuilderDefinition builder() {
        return new LeaseStoreManagerImpl();
    }

    public LeaseStoreManagerImpl() {
        this.settings = new LeaseStoreManagerSettings();
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leaseContextClient(ChangeFeedContextClient leaseContextClient) {
        checkNotNull(leaseContextClient, "Argument 'leaseContextClient' can not be null");

        this.leaseDocumentClient = leaseContextClient;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leasePrefix(String leasePrefix) {
        checkNotNull(leasePrefix, "Argument 'leasePrefix' can not be null");

        this.settings.withContainerNamePrefix(leasePrefix);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leaseContainer(CosmosAsyncContainer leaseContainer) {
        checkNotNull(leaseContainer, "Argument 'leaseContainer' can not be null");

        this.settings.withLeaseContainer(leaseContainer);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition requestOptionsFactory(RequestOptionsFactory requestOptionsFactory) {
        checkNotNull(requestOptionsFactory, "Argument 'requestOptionsFactory' can not be null");

        this.requestOptionsFactory = requestOptionsFactory;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition hostName(String hostName) {
        checkNotNull(hostName, "Argument 'hostName' can not be null");

        this.settings.withHostName(hostName);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition monitoredContainer(CosmosAsyncContainer monitoredContainer) {
        checkNotNull(monitoredContainer, "Argument 'monitoredContainer' can not be null");

        this.settings.withMonitoredContainer(monitoredContainer);
        return this;
    }

    @Override
    public Mono<LeaseStoreManager> build() {

        checkNotNull(this.leaseDocumentClient, "LeaseDocumentClient can not be null");
        checkNotNull(this.requestOptionsFactory, "RequestOptionsFactory can not be null");
        checkNotNull(this.settings, "Settings can not be null");

        this.settings.validate();

        if (this.leaseUpdater == null) {
            this.leaseUpdater = new DocumentServiceLeaseUpdaterImpl(leaseDocumentClient);
        }

        this.leaseStore = new LeaseStoreImpl(
            this.leaseDocumentClient,
            this.settings.getContainerNamePrefix(),
            this.settings.getLeaseContainer(),
            this.requestOptionsFactory);

        return Mono.just(this);
    }

    @Override
    public Flux<Lease> getAllLeases() {
        return this.listDocuments(this.getPartitionLeasePrefix())
            .flatMap(lease -> this.tryToPopulateLeaseWithFeedRange(lease));
    }

    @Override
    public Flux<Lease> getOwnedLeases() {
        return this.getAllLeases()
            .filter(lease -> lease.getOwner() != null && lease.getOwner().equalsIgnoreCase(this.settings.getHostName()));
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(PartitionKeyRange partitionKeyRange, String continuationToken) {
        checkNotNull(partitionKeyRange, "Argument 'partitionKeyRange' should not be null");

        String leaseToken = partitionKeyRange.getId();
        String leaseDocId = this.getDocumentId(leaseToken);

        Lease lease = Lease.builder()
                .id(leaseDocId)
                .leaseToken(leaseToken)
                .continuationToken(continuationToken)
                .feedRange(new FeedRangeEpkImpl(partitionKeyRange.toRange()))
                .buildPartitionBasedLease();

        return this.createLeaseIfNotExistInternal(lease);
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(FeedRangeEpkImpl feedRange, String continuationToken) {
        checkNotNull(feedRange, "Argument 'feedRanges' should not be null");

        String leaseToken = feedRange.getRange().getMin() + "-" + feedRange.getRange().getMax();
        String leaseDocId = this.getDocumentId(leaseToken);

        Lease lease = Lease.builder()
                .id(leaseDocId)
                .leaseToken(leaseToken)
                .continuationToken(continuationToken)
                .feedRange(feedRange)
                .buildEpkBasedLease();

        return createLeaseIfNotExistInternal(lease);
    }

    private Mono<Lease> createLeaseIfNotExistInternal(Lease lease) {
        checkNotNull(lease, "Argument 'lease' should not be null");

        return this.leaseDocumentClient.createItem(this.settings.getLeaseContainer(), lease, null, false)
                .onErrorResume( ex -> {
                    Exception unwrappedException = Utils.as(Exceptions.unwrap(ex), Exception.class);
                    if (unwrappedException instanceof CosmosException) {
                        CosmosException e = (CosmosException) unwrappedException;
                        if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                            logger.info("Lease with token {}: Failed to create. Some other host has created lease.", lease.getLeaseToken());
                            return Mono.empty();
                        }
                    }

                    return Mono.error(ex);
                })
                .map(documentResourceResponse -> {
                    if (documentResourceResponse == null) {
                        return null;
                    }

                    InternalObjectNode document = BridgeInternal.getProperties(documentResourceResponse);

                    logger.info("Lease with token {}: Created.", lease.getLeaseToken());
                    lease.setServiceItemLease(Lease.builder().buildFromDocument(document));
                    return lease;
                });
    }

    @Override
    public Mono<Void> delete(Lease lease) {
        checkNotNull(lease, "Argument 'lease' should not be null");
        checkArgument(!StringUtils.isEmpty(lease.getId()), "Lease.id can not be null nor empty");

        return this.leaseDocumentClient
            .deleteItem(lease.getId(), new PartitionKey(lease.getId()),
                        this.requestOptionsFactory.createItemRequestOptions(lease))
            .onErrorResume( ex -> {
                Exception unwrappedException = Utils.as(Exceptions.unwrap(ex), Exception.class);
                if (unwrappedException instanceof CosmosException) {
                    CosmosException e = (CosmosException) unwrappedException;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        // Ignore - document was already deleted.
                        return Mono.empty();
                    }
                }

                return Mono.error(ex);
            })
            // return some add-hoc value since we don't actually care about the result.
            .map( documentResourceResponse -> true)
            .then();
    }

    /***
     * In order to support merge, the schema of the lease has been changed.
     * We will need to add the range information to the old leases.
     * For newly created leases, feedRange will be populated automatically, we only need to do this for back compact.
     *
     *
     * @param lease the {@link Lease}.
     *
     * @return the {@link Lease} with feedRange.
     */
    public Mono<Lease> tryToPopulateLeaseWithFeedRange(Lease lease) {
        if (lease.getFeedRange() != null) {
            return Mono.just(lease);
        }

        if (lease instanceof ServiceItemLeaseCore) {
            IPartitionKeyRangeCache partitionKeyRangeCache =
                    ImplementationBridgeHelpers
                            .CosmosAsyncContainerHelper
                            .getCosmosAsyncContainerAccessor()
                            .getPartitionKeyRangeCache(this.settings.getMonitoredContainer());
            return Mono.just(partitionKeyRangeCache)
                    .flatMap(pkRangeCache -> {
                        return pkRangeCache.tryGetPartitionKeyRangeByIdAsync(
                                null,
                                this.settings.getMonitoredContainerRid(),
                                lease.getLeaseToken(),
                                false,
                                null
                        );
                    })
                    .flatMap(pkRangeValueHolder -> {
                        if (pkRangeValueHolder != null && pkRangeValueHolder.v != null) {
                            lease.setFeedRange(new FeedRangeEpkImpl(pkRangeValueHolder.v.toRange()));
                        }

                        return Mono.just(lease);
                    });
        }

        return Mono.error(new IllegalStateException("Do not know how to populate feedRange for lease type " + lease.getClass()));
    }

    @Override
    public Mono<Lease> acquire(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        String oldOwner = lease.getOwner();
        return Mono.just(lease)
                .flatMap(l -> this.tryToPopulateLeaseWithFeedRange(l))
                .flatMap(updatedLease -> {
                    return this.leaseUpdater.updateLease(
                            lease,
                            lease.getId(),
                            new PartitionKey(lease.getId()),
                            this.requestOptionsFactory.createItemRequestOptions(lease),
                            serverLease -> {
                                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(oldOwner)) {
                                    logger.info(
                                            "Lease with token {}: Failed to acquire. Lease was acquired already by owner '{}'",
                                            lease.getLeaseToken(),
                                            serverLease.getOwner());
                                    throw new LeaseLostException(lease);
                                }
                                serverLease.setOwner(this.settings.getHostName());
                                serverLease.setProperties(lease.getProperties());

                                return serverLease;
                            });
                });
    }

    @Override
    public Mono<Void> release(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        return this.leaseDocumentClient.readItem(
                    lease.getId(),
                    new PartitionKey(lease.getId()),
                    this.requestOptionsFactory.createItemRequestOptions(lease),
                    InternalObjectNode.class)
            .onErrorResume( ex -> {
                Exception unwrappedException = Utils.as(Exceptions.unwrap(ex), Exception.class);
                if (unwrappedException instanceof CosmosException) {
                    CosmosException e = (CosmosException) unwrappedException;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        logger.info("Lease with token {}: Failed to release. The lease is gone already.", lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                }

                return Mono.error(ex);
            })
            .map(documentResourceResponse -> Lease.builder().buildFromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap(refreshedLease -> this.leaseUpdater.updateLease(
                refreshedLease,
                lease.getId(),
                new PartitionKey(lease.getId()),
                this.requestOptionsFactory.createItemRequestOptions(lease),
                serverLease ->
                {
                    if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        logger.info(
                                "Lease with token {}: No need to release lease. The lease was already taken by another host '{}'.",
                                lease.getLeaseToken(),
                                serverLease.getOwner());
                        throw new LeaseLostException(lease);
                    }
                    serverLease.setOwner(null);
                    return serverLease;
                })
            ).then();
    }

    @Override
    public Mono<Lease> renew(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        // Get fresh lease. The assumption here is that check-pointing is done with higher frequency than lease renewal so almost
        // certainly the lease was updated in between.

        return this.leaseDocumentClient.readItem(lease.getId(),
                                                 new PartitionKey(lease.getId()),
                                                 this.requestOptionsFactory.createItemRequestOptions(lease),
                                                 InternalObjectNode.class)
            .onErrorResume(ex -> {
                Exception unwrappedException = Utils.as(Exceptions.unwrap(ex), Exception.class);

                if (unwrappedException instanceof CosmosException) {
                    CosmosException e = (CosmosException) unwrappedException;
                    if (e.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        logger.info(
                                "Lease with token {}: Failed to renew. The lease is gone already.",
                                lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                }

                return Mono.error(ex);
            })
            .map(documentResourceResponse -> Lease.builder().buildFromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap(refreshedLease -> this.leaseUpdater.updateLease(
                refreshedLease,
                lease.getId(),
                new PartitionKey(lease.getId()),
                this.requestOptionsFactory.createItemRequestOptions(lease),
                serverLease ->
                {
                    if (serverLease.getOwner() == null) {
                        logger.info(
                                "Lease with token {}: Failed to renew. Lease was taken over and released by a different owner",
                                lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                    else if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        logger.info(
                                "Lease with token {}: Failed to renew. Lease was taken over by owner '{}'",
                                lease.getLeaseToken(),
                                serverLease.getOwner());
                        throw new LeaseLostException(lease);
                    }

                    return serverLease;
                })
            );
    }

    @Override
    public Mono<Lease> updateProperties(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        if (lease.getOwner() != null && !lease.getOwner().equalsIgnoreCase(this.settings.getHostName())) {
            logger.info(
                    "Lease with token {}: Failed to update properties. Lease was taken over by owner '{}' before lease item update",
                    lease.getLeaseToken(),
                    lease.getOwner());
            throw new LeaseLostException(lease);
        }

        return this.leaseUpdater.updateLease(
            lease,
            lease.getId(),
            new PartitionKey(lease.getId()),
            this.requestOptionsFactory.createItemRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    logger.info(
                            "Lease with token {}: Failed to update properties. Lease was taken over by owner '{}'",
                            lease.getLeaseToken(),
                            serverLease.getOwner());
                    throw new LeaseLostException(lease);
                }
                serverLease.setProperties(lease.getProperties());
                return serverLease;
            });
    }

    @Override
    public Mono<Lease> checkpoint(Lease lease, String continuationToken, CancellationToken cancellationToken) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkArgument(!StringUtils.isEmpty(continuationToken), "Argument 'continuationToken' must be a non-empty string");

        if (cancellationToken.isCancellationRequested()) return Mono.error(new TaskCancelledException());

        return this.leaseDocumentClient.readItem(lease.getId(),
                                                 new PartitionKey(lease.getId()),
                                                 this.requestOptionsFactory.createItemRequestOptions(lease),
                                                 InternalObjectNode.class)
            .map(documentResourceResponse -> Lease.builder().buildFromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap(refreshedLease -> {
                if (cancellationToken.isCancellationRequested()) return Mono.error(new TaskCancelledException());

                return this.leaseUpdater.updateLease(
                    refreshedLease,
                    lease.getId(),
                    new PartitionKey(lease.getId()),
                    this.requestOptionsFactory.createItemRequestOptions(lease),
                    serverLease -> {
                        if (serverLease.getOwner() == null) {
                            logger.info(
                                    "Lease with token {}: Failed to checkpoint. Lease was taken over and released by a different owner",
                                    lease.getLeaseToken());
                            throw new LeaseLostException(lease);
                        } else if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                            logger.info(
                                    "Lease with token {}: Failed to checkpoint. Lease was taken over by owner '{}'",
                                    lease.getLeaseToken(),
                                    serverLease.getOwner());
                            throw new LeaseLostException(lease);
                        }
                        serverLease.setContinuationToken(continuationToken);

                        return serverLease;
                    });
            })
            .doOnError(throwable -> {
                logger.info(
                        "Lease with token {}: Failed to checkpoint with lease concurrency token '{}', owner '{}', continuationToken '{}'",
                        lease.getLeaseToken(),
                        lease.getConcurrencyToken(),
                        lease.getOwner(),
                        lease.getContinuationToken());
            });
    }

    @Override
    public Mono<Boolean> isInitialized() {
        return this.leaseStore.isInitialized();
    }

    @Override
    public Mono<Boolean> markInitialized() {
        return this.leaseStore.markInitialized();
    }

    @Override
    public Mono<Boolean> acquireInitializationLock(Duration lockExpirationTime) {
        return this.leaseStore.acquireInitializationLock(lockExpirationTime);
    }

    @Override
    public Mono<Boolean> releaseInitializationLock() {
        return this.leaseStore.releaseInitializationLock();
    }

    private Flux<Lease> listDocuments(String prefix) {
        if (prefix == null || prefix.isEmpty())  {
            throw new IllegalArgumentException("prefix");
        }

        SqlParameter param = new SqlParameter();
        param.setName("@PartitionLeasePrefix");
        param.setValue(prefix);
        SqlQuerySpec querySpec = new SqlQuerySpec(
            "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)",
            Collections.singletonList(param));

        Flux<FeedResponse<InternalObjectNode>> query = this.leaseDocumentClient.queryItems(
            this.settings.getLeaseContainer(),
            querySpec,
            this.requestOptionsFactory.createQueryRequestOptions(),
            InternalObjectNode.class);

        return query.flatMap( documentFeedResponse -> Flux.fromIterable(documentFeedResponse.getResults()))
            .map(document -> Lease.builder().buildFromDocument(document));
    }

    private String getDocumentId(String leaseToken)
    {
        return this.getPartitionLeasePrefix() + leaseToken;
    }

    private String getPartitionLeasePrefix() {
        return this.settings.getContainerNamePrefix() + LEASE_STORE_MANAGER_LEASE_SUFFIX;
    }

}
