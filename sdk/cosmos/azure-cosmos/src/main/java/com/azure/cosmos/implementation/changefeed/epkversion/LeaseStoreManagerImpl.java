// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManagerSettings;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.ServiceItemLeaseUpdater;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedHelper;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides flexible way to buildAsyncClient lease manager constructor parameters.
 * For the actual creation of lease manager instance, delegates to lease manager factory.
 */
class LeaseStoreManagerImpl implements LeaseStoreManager, LeaseStoreManager.LeaseStoreManagerBuilderDefinition {
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
    public LeaseStoreManagerBuilderDefinition leaseCollectionLink(CosmosAsyncContainer leaseCollectionLink) {
        checkNotNull(leaseCollectionLink, "Argument 'leaseCollectionLink' can not be null");

        this.settings.withLeaseCollectionLink(leaseCollectionLink);
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
    public LeaseStoreManager build() {
        checkNotNull(this.settings, "settings can not be null");
        checkNotNull(this.settings.getContainerNamePrefix(), "settings.containerNamePrefix can not be null");
        checkNotNull(this.settings.getLeaseCollectionLink(), "settings.leaseCollectionLink can not be null");
        checkArgument(StringUtils.isNotEmpty(this.settings.getHostName()), "settings.getHostName can not be null nor empty");
        checkNotNull(this.leaseDocumentClient, "leaseDocumentClient can not be null");
        checkNotNull(this.requestOptionsFactory, "requestOptionsFactory can not be null");

        if (this.leaseUpdater == null) {
            this.leaseUpdater = new DocumentServiceLeaseUpdaterImpl(leaseDocumentClient);
        }

        this.leaseStore = new LeaseStoreImpl(
            this.leaseDocumentClient,
            this.settings.getContainerNamePrefix(),
            this.settings.getLeaseCollectionLink(),
            this.requestOptionsFactory);

        return this;
    }

    @Override
    public Flux<Lease> getAllLeases() {
        return this.listDocuments(this.getPartitionLeasePrefix())
            .map(documentServiceLease -> documentServiceLease);
    }

    @Override
    public Flux<Lease> getTopLeases(int topCount) {
        return this.listDocuments(this.getPartitionLeasePrefix(), topCount)
            .map(documentServiceLease -> documentServiceLease);
    }

    @Override
    public Flux<Lease> getOwnedLeases() {
        return this.getAllLeases()
            .filter(lease -> lease.getOwner() != null && lease.getOwner().equalsIgnoreCase(this.settings.getHostName()));
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(String leaseToken, String continuationToken) {
        return this.createLeaseIfNotExist(leaseToken, continuationToken, null);
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(String leaseToken, String continuationToken, Map<String, String> properties) {
        throw new UnsupportedOperationException("partition key based leases are not supported for Change Feed V1 wire format");
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(FeedRangeEpkImpl feedRange, String continuationToken) {
        return this.createLeaseIfNotExist(feedRange, continuationToken, null);
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(FeedRangeEpkImpl feedRange, String continuationToken, Map<String, String> properties) {
        checkNotNull(feedRange, "Argument 'feedRanges' should not be null");

        String leaseToken = feedRange.getRange().getMin() + "-" + feedRange.getRange().getMax();
        String leaseDocId = this.getDocumentId(leaseToken);

        ServiceItemLeaseV1 documentServiceLease = new ServiceItemLeaseV1()
            .withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE)
            .withId(leaseDocId)
            .withLeaseToken(leaseToken)
            .withFeedRange(feedRange)
            .withContinuationToken(continuationToken)
            .withProperties(properties);

        return this.leaseDocumentClient.createItem(
            this.settings.getLeaseCollectionLink(),
                documentServiceLease,
                this.requestOptionsFactory.createItemRequestOptions(documentServiceLease),
                false)
            .onErrorResume( ex -> {
                if (ex instanceof CosmosException) {
                    CosmosException e = (CosmosException) ex;
                    if (Exceptions.isConflict(e)) {
                        logger.info("Some other host created lease for {}.", leaseToken);
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

                return documentServiceLease
                    .withId(document.getId())
                    .withETag(document.getETag())
                    .withTs(document.getString(Constants.Properties.LAST_MODIFIED));
            });
    }

    @Override
    public Mono<Void> delete(Lease lease) {
        if (lease == null || lease.getId() == null) {
            throw new IllegalArgumentException("lease");
        }

        return this.leaseDocumentClient
            .deleteItem(
                lease.getId(),
                new PartitionKey(lease.getId()),
                this.requestOptionsFactory.createItemRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosException) {
                    CosmosException e = (CosmosException) ex;
                    if (Exceptions.isNotFound(e)) {
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

    @Override
    public Mono<Void> deleteAll(List<Lease> leases) {
        checkNotNull(leases, "Argument 'leases' can not be null");

        logger.info("Deleting all leases");
        Map<String, CosmosItemIdentity> cosmosIdentityMap = new HashMap<>();
        for (Lease lease : leases) {
            cosmosIdentityMap.put(lease.getId(), new CosmosItemIdentity(new PartitionKey(lease.getId()), lease.getId()));
        }

        return Mono.defer(() -> Mono.just(cosmosIdentityMap))
            .flatMapMany(itemIdentities ->
                this.leaseDocumentClient.deleteAllItems(cosmosIdentityMap.values().stream().collect(Collectors.toList())))
            .flatMap(itemResponse -> {
                if (itemResponse.getResponse() != null && itemResponse.getResponse().isSuccessStatusCode()) {
                    cosmosIdentityMap.remove(itemResponse.getOperation().getId());
                } else {
                    // should ignore 404/0 for delete, will retry on other cases
                    int effectiveStatusCode = 0;
                    int effectiveSubStatusCode = 0;
                    if (itemResponse.getResponse() != null) {
                        effectiveStatusCode = itemResponse.getResponse().getStatusCode();
                        effectiveSubStatusCode = itemResponse.getResponse().getStatusCode();
                    } else if (itemResponse.getException() != null && itemResponse.getException() instanceof CosmosException) {
                        CosmosException cosmosException = (CosmosException) itemResponse.getException();
                        effectiveStatusCode = cosmosException.getStatusCode();
                        effectiveSubStatusCode = cosmosException.getSubStatusCode();
                    }

                    if (effectiveStatusCode == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND &&
                        effectiveSubStatusCode == 0) {
                        cosmosIdentityMap.remove(itemResponse.getOperation().getId());
                    }
                }

                return Mono.empty();
            })
            .repeat(() -> cosmosIdentityMap.size() != 0)
            .then();
    }

    @Override
    public Mono<Lease> acquire(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        String oldOwner = lease.getOwner();

        return this.leaseUpdater.updateLease(
            lease,
            lease.getId(),
            new PartitionKey(lease.getId()),
            this.requestOptionsFactory.createItemRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(oldOwner)) {
                    logger.info("Lease with token {} : lease was acquired already by owner '{}'", lease.getLeaseToken(), serverLease.getOwner());
                    throw new LeaseLostException(lease);
                }
                serverLease.setOwner(this.settings.getHostName());
                serverLease.setProperties(lease.getProperties());

                return serverLease;
            });
    }

    @Override
    public Mono<Void> release(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        return this.leaseDocumentClient.readItem(lease.getId(),
                                                 new PartitionKey(lease.getId()),
                                                 this.requestOptionsFactory.createItemRequestOptions(lease),
                                                 InternalObjectNode.class)
            .onErrorResume( ex -> {
                if (ex instanceof CosmosException) {
                    CosmosException e = (CosmosException) ex;
                    if (Exceptions.isNotFound(e)) {
                        logger.info("Lease with token {} : failed to renew lease. The lease is gone already.",
                            lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                }

                return Mono.error(ex);
            })
            .map( documentResourceResponse -> ServiceItemLeaseV1.fromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap( refreshedLease -> this.leaseUpdater.updateLease(
                refreshedLease,
                lease.getId(),
                new PartitionKey(lease.getId()),
                this.requestOptionsFactory.createItemRequestOptions(lease),
                serverLease ->
                {
                    if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        logger.info("Lease with token {} : no need to release lease. The lease was already taken by another host '{}'.",
                            lease.getLeaseToken(), serverLease.getOwner());
                        throw new LeaseLostException(lease);
                    }
                    serverLease.setOwner(null);

                    return serverLease;
                })
            ).then();
    }

    @Override
    public Mono<Lease> renew(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        // Get fresh lease. The assumption here is that check-pointing is done with higher frequency than lease renewal so almost
        // certainly the lease was updated in between.

        return this.leaseDocumentClient.readItem(lease.getId(),
                                                 new PartitionKey(lease.getId()),
                                                 this.requestOptionsFactory.createItemRequestOptions(lease),
                                                 InternalObjectNode.class)
            .onErrorResume( ex -> {
                if (ex instanceof CosmosException) {
                    CosmosException e = (CosmosException) ex;
                    if (Exceptions.isNotFound(e)) {
                        logger.info("Lease with token {} : failed to renew lease. The lease is gone already.",
                            lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                }

                return Mono.error(ex);
            })
            .map( documentResourceResponse -> ServiceItemLeaseV1.fromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap( refreshedLease -> this.leaseUpdater.updateLease(
                refreshedLease,
                lease.getId(),
                new PartitionKey(lease.getId()),
                this.requestOptionsFactory.createItemRequestOptions(lease),
                serverLease ->
                {
                    if (serverLease.getOwner() == null) {
                        logger.info("Lease with token {} : lease was taken over and released by a different owner",
                            lease.getLeaseToken());
                        throw new LeaseLostException(lease);
                    }
                    else if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        logger.info("Lease with token {} : lease was taken over by owner '{}'",
                            lease.getLeaseToken(), serverLease.getOwner());
                        throw new LeaseLostException(lease);
                    }

                    return serverLease;
                })
            );
    }

    @Override
    public Mono<Lease> updateProperties(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        if (lease.getOwner() != null && !lease.getOwner().equalsIgnoreCase(this.settings.getHostName())) {
            logger.info("Lease with token '{}' : lease was taken over by owner '{}' before lease item update",
                lease.getLeaseToken(), lease.getOwner());
            throw new LeaseLostException(lease);
        }

        return this.leaseUpdater.updateLease(
            lease,
            lease.getId(),
            new PartitionKey(lease.getId()),
            this.requestOptionsFactory.createItemRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    logger.info("Lease with token '{}' : lease was taken over by owner '{}'",
                        lease.getLeaseToken(), serverLease.getOwner());
                    throw new LeaseLostException(lease);
                }
                serverLease.setProperties(lease.getProperties());
                return serverLease;
            });
    }

    @Override
    public Mono<Lease> checkpoint(Lease lease, String continuationToken, CancellationToken cancellationToken) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        if (continuationToken == null || continuationToken.isEmpty()) {
            throw new IllegalArgumentException("continuationToken must be a non-empty string");
        }

        if (cancellationToken.isCancellationRequested()) return Mono.error(new TaskCancelledException());

        return this.leaseDocumentClient.readItem(lease.getId(),
                                                 new PartitionKey(lease.getId()),
                                                 this.requestOptionsFactory.createItemRequestOptions(lease),
                                                 InternalObjectNode.class)
            .map(documentResourceResponse -> ServiceItemLeaseV1.fromDocument(BridgeInternal.getProperties(documentResourceResponse)))
            .flatMap(refreshedLease -> {
                if (cancellationToken.isCancellationRequested()) return Mono.error(new TaskCancelledException());

                return this.leaseUpdater.updateLease(
                    refreshedLease,
                    lease.getId(), new PartitionKey(lease.getId()),
                    this.requestOptionsFactory.createItemRequestOptions(lease),
                    serverLease -> {
                        if (serverLease.getOwner() == null) {
                            logger.info("Lease with token {} : lease was taken over and released by a different owner",
                                lease.getLeaseToken());
                            throw new LeaseLostException(lease);
                        } else if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                            logger.info("Lease with token {} : lease was taken over by owner '{}'",
                                lease.getLeaseToken(), serverLease.getOwner());
                            throw new LeaseLostException(lease);
                        }
                        serverLease.setContinuationToken(continuationToken);

                        return serverLease;
                    });
            })
            .doOnError(throwable -> {
                logger.info("Lease with token {}  : lease with token '{}' failed to checkpoint for owner '{}' with continuation token '{}'",
                    lease.getLeaseToken(), lease.getConcurrencyToken(), lease.getOwner(), lease.getReadableContinuationToken());
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

    private Flux<ServiceItemLeaseV1> listDocuments(String prefix) {
        return listDocuments(prefix, null);
    }

    private Flux<ServiceItemLeaseV1> listDocuments(String prefix, Integer top) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("prefix cannot be null or empty!");
        }

        SqlQuerySpec querySpec;

        if (top == null) {
            SqlParameter param = new SqlParameter();
            param.setName("@PartitionLeasePrefix");
            param.setValue(prefix);
            querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)",
                Collections.singletonList(param));
        } else {
            SqlParameter topParam = new SqlParameter();
            topParam.setName("@Top");
            topParam.setValue(top);

            SqlParameter param = new SqlParameter();
            param.setName("@PartitionLeasePrefix");
            param.setValue(prefix);
            querySpec = new SqlQuerySpec(
                "SELECT TOP @Top * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix) AND c.ContinuationToken <> null",
                Arrays.asList(topParam, param));
        }

        Flux<FeedResponse<InternalObjectNode>> query = this.leaseDocumentClient.queryItems(
            this.settings.getLeaseCollectionLink(),
            querySpec,
            this.requestOptionsFactory.createQueryRequestOptions(),
            InternalObjectNode.class);

        return query.flatMap( documentFeedResponse -> Flux.fromIterable(documentFeedResponse.getResults()))
            .map(ServiceItemLeaseV1::fromDocument);
    }

    private String getDocumentId(String leaseToken)
    {
        return this.getPartitionLeasePrefix() + leaseToken;
    }

    private String getPartitionLeasePrefix() {
        return this.settings.getContainerNamePrefix() + LEASE_STORE_MANAGER_LEASE_SUFFIX;
    }

}
