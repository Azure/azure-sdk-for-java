// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlParameter;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseStore;
import com.azure.data.cosmos.internal.changefeed.LeaseStoreManager;
import com.azure.data.cosmos.internal.changefeed.LeaseStoreManagerSettings;
import com.azure.data.cosmos.internal.changefeed.RequestOptionsFactory;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLease;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLeaseUpdater;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Provides flexible way to build lease manager constructor parameters.
 * For the actual creation of lease manager instance, delegates to lease manager factory.
 */
public class LeaseStoreManagerImpl implements LeaseStoreManager, LeaseStoreManager.LeaseStoreManagerBuilderDefinition {

    private final Logger logger = LoggerFactory.getLogger(LeaseStoreManagerImpl.class);
    private LeaseStoreManagerSettings settings;
    private ChangeFeedContextClient leaseDocumentClient;
    private RequestOptionsFactory requestOptionsFactory;
    private ServiceItemLeaseUpdater leaseUpdater;
    private LeaseStore leaseStore;


    public static LeaseStoreManagerBuilderDefinition Builder() {
        return new LeaseStoreManagerImpl();
    }

    public LeaseStoreManagerImpl() {
        this.settings = new LeaseStoreManagerSettings();
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leaseContextClient(ChangeFeedContextClient leaseContextClient) {
        if (leaseContextClient == null) {
            throw new IllegalArgumentException("leaseContextClient");
        }

        this.leaseDocumentClient = leaseContextClient;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leasePrefix(String leasePrefix) {
        if (leasePrefix == null) {
            throw new IllegalArgumentException("leasePrefix");
        }

        this.settings.withContainerNamePrefix(leasePrefix);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition leaseCollectionLink(CosmosContainer leaseCollectionLink) {
        if (leaseCollectionLink == null) {
            throw new IllegalArgumentException("leaseCollectionLink");
        }

        this.settings.withLeaseCollectionLink(leaseCollectionLink);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition requestOptionsFactory(RequestOptionsFactory requestOptionsFactory) {
        if (requestOptionsFactory == null) {
            throw new IllegalArgumentException("requestOptionsFactory");
        }

        this.requestOptionsFactory = requestOptionsFactory;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition hostName(String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName");
        }

        this.settings.withHostName(hostName);
        return this;
    }

    @Override
    public Mono<LeaseStoreManager> build() {
        if (this.settings == null) throw new IllegalArgumentException("properties");
        if (this.settings.getContainerNamePrefix() == null) throw new IllegalArgumentException("properties.containerNamePrefix");
        if (this.settings.getLeaseCollectionLink() == null) throw new IllegalArgumentException("properties.leaseCollectionLink");
        if (this.settings.getHostName() == null || this.settings.getHostName().isEmpty()) throw new IllegalArgumentException("properties.hostName");
        if (this.leaseDocumentClient == null) throw new IllegalArgumentException("leaseDocumentClient");
        if (this.requestOptionsFactory == null) throw new IllegalArgumentException("requestOptionsFactory");
        if (this.leaseUpdater == null) {
            this.leaseUpdater = new DocumentServiceLeaseUpdaterImpl(leaseDocumentClient);
        }

        this.leaseStore = new DocumentServiceLeaseStore(
            this.leaseDocumentClient,
            this.settings.getContainerNamePrefix(),
            this.settings.getLeaseCollectionLink(),
            this.requestOptionsFactory);

        LeaseStoreManagerImpl self = this;
        if (this.settings.getLeaseCollectionLink() == null)
            throw new IllegalArgumentException("leaseCollectionLink was not specified");
        if (this.requestOptionsFactory == null)
            throw new IllegalArgumentException("requestOptionsFactory was not specified");

        return Mono.just(self);
    }

    @Override
    public Flux<Lease> getAllLeases() {
        return this.listDocuments(this.getPartitionLeasePrefix())
            .map(documentServiceLease -> documentServiceLease);
    }

    @Override
    public Flux<Lease> getOwnedLeases() {
        LeaseStoreManagerImpl self = this;
        return this.getAllLeases()
            .filter(lease -> lease.getOwner() != null && lease.getOwner().equalsIgnoreCase(self.settings.getHostName()));
    }

    @Override
    public Mono<Lease> createLeaseIfNotExist(String leaseToken, String continuationToken) {
        if (leaseToken == null) throw new IllegalArgumentException("leaseToken");

        String leaseDocId = this.getDocumentId(leaseToken);
        ServiceItemLease documentServiceLease = new ServiceItemLease()
            .withId(leaseDocId)
            .withLeaseToken(leaseToken)
            .withContinuationToken(continuationToken);

        return this.leaseDocumentClient.createItem(this.settings.getLeaseCollectionLink(), documentServiceLease, null, false)
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_CONFLICT) {
                        logger.info("Some other host created lease for {}.", leaseToken);
                        return Mono.empty();
                    }
                }

                return Mono.error(ex);
            })
            .map(documentResourceResponse -> {
                if (documentResourceResponse == null) return null;

                CosmosItemProperties document = documentResourceResponse.properties();

                logger.info("Created lease for partition {}.", leaseToken);

                return documentServiceLease
                    .withId(document.id())
                    .withEtag(document.etag())
                    .withTs(document.getString(Constants.Properties.LAST_MODIFIED));
            });
    }

    @Override
    public Mono<Void> delete(Lease lease) {
        if (lease == null || lease.getId() == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        CosmosItem itemForLease = this.createItemForLease(lease.getId());

        return this.leaseDocumentClient
            .deleteItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
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
    public Mono<Lease> acquire(Lease lease) {
        if (lease == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        String oldOwner = lease.getOwner();

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(oldOwner)) {
                    logger.info("Partition {} lease was taken over by owner '{}'", lease.getLeaseToken(), serverLease.getOwner());
                    throw Exceptions.propagate(new LeaseLostException(lease));
                }
                serverLease.setOwner(this.settings.getHostName());
                serverLease.setProperties(lease.getProperties());

                return serverLease;
            });
    }

    @Override
    public Mono<Void> release(Lease lease) {
        if (lease == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        CosmosItem itemForLease = this.createItemForLease(lease.getId());
        LeaseStoreManagerImpl self = this;

        return this.leaseDocumentClient.readItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        logger.info("Partition {} failed to renew lease. The lease is gone already.", lease.getLeaseToken());
                        throw Exceptions.propagate(new LeaseLostException(lease));
                    }
                }

                return Mono.error(ex);
            })
            .map( documentResourceResponse -> ServiceItemLease.fromDocument(documentResourceResponse.properties()))
            .flatMap( refreshedLease -> self.leaseUpdater.updateLease(
                refreshedLease,
                self.createItemForLease(refreshedLease.getId()),
                self.requestOptionsFactory.createRequestOptions(lease),
                serverLease ->
                {
                    if (serverLease.getOwner() != null) {
                        if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                            logger.info("Partition {} no need to release lease. The lease was already taken by another host '{}'.", lease.getLeaseToken(), serverLease.getOwner());
                            throw Exceptions.propagate(new LeaseLostException(lease));
                        }
                    }

                    serverLease.setOwner(null);

                    return serverLease;
                })
            ).then();
    }

    @Override
    public Mono<Lease> renew(Lease lease) {
        if (lease == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        // Get fresh lease. The assumption here is that check-pointing is done with higher frequency than lease renewal so almost
        // certainly the lease was updated in between.
        CosmosItem itemForLease = this.createItemForLease(lease.getId());
        LeaseStoreManagerImpl self = this;

        return this.leaseDocumentClient.readItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        logger.info("Partition {} failed to renew lease. The lease is gone already.", lease.getLeaseToken());
                        throw Exceptions.propagate(new LeaseLostException(lease));
                    }
                }

                return Mono.error(ex);
            })
            .map( documentResourceResponse -> ServiceItemLease.fromDocument(documentResourceResponse.properties()))
            .flatMap( refreshedLease -> self.leaseUpdater.updateLease(
                refreshedLease,
                self.createItemForLease(refreshedLease.getId()),
                self.requestOptionsFactory.createRequestOptions(lease),
                serverLease ->
                {
                    if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        logger.info("Partition {} lease was taken over by owner '{}'", lease.getLeaseToken(), serverLease.getOwner());
                        throw Exceptions.propagate(new LeaseLostException(lease));
                    }

                    return serverLease;
                })
            );
    }

    @Override
    public Mono<Lease> updateProperties(Lease lease) {
        if (lease == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        if (!lease.getOwner().equalsIgnoreCase(this.settings.getHostName()))
        {
            logger.info("Partition '{}' lease was taken over by owner '{}' before lease item update", lease.getLeaseToken(), lease.getOwner());
            throw new LeaseLostException(lease);
        }

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    logger.info("Partition '{}' lease was taken over by owner '{}'", lease.getLeaseToken(), serverLease.getOwner());
                    throw Exceptions.propagate(new LeaseLostException(lease));
                }
                serverLease.setProperties(lease.getProperties());
                return serverLease;
            });
    }

    @Override
    public Mono<Lease> checkpoint(Lease lease, String continuationToken) {
        if (lease == null) throw Exceptions.propagate(new IllegalArgumentException("lease"));

        if (continuationToken == null || continuationToken.isEmpty()) {
            throw new IllegalArgumentException("continuationToken must be a non-empty string");
        }

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    logger.info("Partition {} lease was taken over by owner '{}'", lease.getLeaseToken(), serverLease.getOwner());
                    throw Exceptions.propagate(new LeaseLostException(lease));
                }
                serverLease.setContinuationToken(continuationToken);

                return serverLease;
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

    private Mono<ServiceItemLease> tryGetLease(Lease lease) {
        CosmosItem itemForLease = this.createItemForLease(lease.getId());

        return this.leaseDocumentClient.readItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        return Mono.empty();
                    }
                }

                return Mono.error(ex);
            })
            .map( documentResourceResponse -> {
                if (documentResourceResponse == null) return null;
                return ServiceItemLease.fromDocument(documentResourceResponse.properties());
            });
    }

    private Flux<ServiceItemLease> listDocuments(String prefix) {
        if (prefix == null || prefix.isEmpty())  {
            throw new IllegalArgumentException("prefix");
        }

        SqlParameter param = new SqlParameter();
        param.name("@PartitionLeasePrefix");
        param.value(prefix);
        SqlQuerySpec querySpec = new SqlQuerySpec(
            "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)",
            new SqlParameterList(param));

        Flux<FeedResponse<CosmosItemProperties>> query = this.leaseDocumentClient.queryItems(
            this.settings.getLeaseCollectionLink(),
            querySpec,
            this.requestOptionsFactory.createFeedOptions());

        return query.flatMap( documentFeedResponse -> Flux.fromIterable(documentFeedResponse.results()))
            .map( ServiceItemLease::fromDocument);
    }

    private String getDocumentId(String leaseToken)
    {
        return this.getPartitionLeasePrefix() + leaseToken;
    }

    private String getPartitionLeasePrefix()
    {
        return this.settings.getContainerNamePrefix() + "..";
    }

    private CosmosItem createItemForLease(String leaseId) {
        return this.leaseDocumentClient.getContainerClient().getItem(leaseId, "/id");
    }
}
