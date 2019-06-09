/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.changefeed.exceptions.LeaseLostException;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.changefeed.Lease;
import com.azure.data.cosmos.changefeed.LeaseStore;
import com.azure.data.cosmos.changefeed.LeaseStoreManager;
import com.azure.data.cosmos.changefeed.LeaseStoreManagerSettings;
import com.azure.data.cosmos.changefeed.RequestOptionsFactory;
import com.azure.data.cosmos.changefeed.ServiceItemLease;
import com.azure.data.cosmos.changefeed.ServiceItemLeaseUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public LeaseStoreManagerBuilderDefinition withLeaseContextClient(ChangeFeedContextClient leaseContextClient) {
        if (leaseDocumentClient == null) {
            throw new IllegalArgumentException("leaseDocumentClient");
        }

        this.leaseDocumentClient = leaseContextClient;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition withLeasePrefix(String leasePrefix) {
        if (leasePrefix == null) {
            throw new IllegalArgumentException("leasePrefix");
        }

        this.settings.withContainerNamePrefix(leasePrefix);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition withLeaseCollectionLink(CosmosContainer leaseCollectionLink) {
        if (leaseCollectionLink == null) {
            throw new IllegalArgumentException("leaseCollectionLink");
        }

        this.settings.withLeaseCollectionLink(leaseCollectionLink);
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition withRequestOptionsFactory(RequestOptionsFactory requestOptionsFactory) {
        if (requestOptionsFactory == null) {
            throw new IllegalArgumentException("requestOptionsFactory");
        }

        this.requestOptionsFactory = requestOptionsFactory;
        return this;
    }

    @Override
    public LeaseStoreManagerBuilderDefinition withHostName(String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName");
        }

        this.settings.withHostName(hostName);
        return this;
    }

    @Override
    public Mono<LeaseStoreManager> build() {
        if (this.settings == null) throw new IllegalArgumentException("settings");
        if (this.settings.getContainerNamePrefix() == null) throw new IllegalArgumentException("settings.containerNamePrefix");
        if (this.settings.getLeaseCollectionLink() == null) throw new IllegalArgumentException("settings.leaseCollectionLink");
        if (this.settings.getHostName() == null || this.settings.getHostName().isEmpty()) throw new IllegalArgumentException("settings.hostName");
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
                        //Logger.InfoFormat("Some other host created lease for {0}.", leaseToken);
                        return Mono.empty();
                    }
                }

                Mono.error(ex);
                return Mono.empty();
            })
            .map(documentResourceResponse -> {
                if (documentResourceResponse == null) return null;

                CosmosItemProperties document = documentResourceResponse.properties();
                return documentServiceLease
                    .withId(document.id())
                    .withEtag(document.etag())
                    .withTs(document.getString(Constants.Properties.LAST_MODIFIED));
            });
        //Logger.InfoFormat("Created lease for partition {0}.", leaseToken);
    }

    @Override
    public Mono<Void> delete(Lease lease) {
        if (lease == null || lease.getId() == null) throw new IllegalArgumentException("lease");

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

                Mono.error(ex);
                return Mono.empty();
            })
            // return some add-hoc value since we don't actually care about the result.
            .map( documentResourceResponse -> true)
            .then();
    }

    @Override
    public Mono<Lease> acquire(Lease lease) {
        if (lease == null) throw new IllegalArgumentException("lease");

        String oldOwner = lease.getOwner();

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(oldOwner)) {
                    // Logger.InfoFormat("Partition {0} lease was taken over by owner '{1}'", lease.LeaseToken, serverLease.Owner);
                    Mono.error(new LeaseLostException(lease));
                }
                serverLease.setOwner(this.settings.getHostName());
                serverLease.setProperties(lease.getProperties());

                return serverLease;
            });
    }

    @Override
    public Mono<Void> release(Lease lease) {
        if (lease == null) throw new IllegalArgumentException("lease");

        CosmosItem itemForLease = this.createItemForLease(lease.getId());
        LeaseStoreManagerImpl self = this;

        return this.leaseDocumentClient.readItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        // Logger.InfoFormat("Partition {0} failed to renew lease. The lease is gone already.", lease.LeaseToken);
                        Mono.error(new LeaseLostException(lease));
                    }
                }

                Mono.error(ex);
                return Mono.empty();
            })
            .map( documentResourceResponse -> ServiceItemLease.fromDocument(documentResourceResponse.properties()))
            .flatMap( refreshedLease -> self.leaseUpdater.updateLease(
                refreshedLease,
                self.createItemForLease(refreshedLease.getId()),
                self.requestOptionsFactory.createRequestOptions(lease),
                serverLease ->
                {
                    if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        //Logger.InfoFormat("Partition {0} no need to release lease. The lease was already taken by another host '{1}'.", lease.LeaseToken, serverLease.Owner);
                        Mono.error(new LeaseLostException(lease));
                    }

                    serverLease.setOwner(null);

                    return serverLease;
                })
            ).then();
    }

    @Override
    public Mono<Lease> renew(Lease lease) {
        if (lease == null) throw new IllegalArgumentException("lease");

        // Get fresh lease. The assumption here is that check-pointing is done with higher frequency than lease renewal so almost
        // certainly the lease was updated in between.
        CosmosItem itemForLease = this.createItemForLease(lease.getId());
        LeaseStoreManagerImpl self = this;

        return this.leaseDocumentClient.readItem(itemForLease, this.requestOptionsFactory.createRequestOptions(lease))
            .onErrorResume( ex -> {
                if (ex instanceof CosmosClientException) {
                    CosmosClientException e = (CosmosClientException) ex;
                    if (e.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND) {
                        // Logger.InfoFormat("Partition {0} failed to renew lease. The lease is gone already.", lease.LeaseToken);
                        Mono.error(new LeaseLostException(lease));
                    }
                }

                Mono.error(ex);
                return Mono.empty();
            })
            .map( documentResourceResponse -> ServiceItemLease.fromDocument(documentResourceResponse.properties()))
            .flatMap( refreshedLease -> self.leaseUpdater.updateLease(
                refreshedLease,
                self.createItemForLease(refreshedLease.getId()),
                self.requestOptionsFactory.createRequestOptions(lease),
                serverLease ->
                {
                    if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                        // Logger.InfoFormat("Partition {0} lease was taken over by owner '{1}'", lease.LeaseToken, serverLease.Owner);
                        Mono.error(new LeaseLostException(lease));
                    }

                    return serverLease;
                })
            );
    }

    @Override
    public Mono<Lease> updateProperties(Lease lease) {
        if (lease == null) throw new IllegalArgumentException("lease");

        if (!lease.getOwner().equalsIgnoreCase(this.settings.getHostName()))
        {
            // Logger.InfoFormat("Partition '{0}' lease was taken over by owner '{1}' before lease item update", lease.LeaseToken, lease.Owner);
            Mono.error(new LeaseLostException(lease));
        }

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (!serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    // Logger.InfoFormat("Partition '{0}' lease was taken over by owner '{1}'", lease.LeaseToken, serverLease.Owner);
                    Mono.error(new LeaseLostException(lease));
                }
                serverLease.setProperties(lease.getProperties());
                return serverLease;
            });
    }

    @Override
    public Mono<Lease> checkpoint(Lease lease, String continuationToken) {
        if (lease == null) throw new IllegalArgumentException("lease");

        if (continuationToken == null || continuationToken.isEmpty()) {
            throw new IllegalArgumentException("continuationToken must be a non-empty string");
        }

        return this.leaseUpdater.updateLease(
            lease,
            this.createItemForLease(lease.getId()),
            this.requestOptionsFactory.createRequestOptions(lease),
            serverLease -> {
                if (serverLease.getOwner() != null && !serverLease.getOwner().equalsIgnoreCase(lease.getOwner())) {
                    //Logger.InfoFormat("Partition {0} lease was taken over by owner '{1}'", lease.LeaseToken, serverLease.Owner);
                    Mono.error(new LeaseLostException(lease));
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

                Mono.error(ex);
                return Mono.empty();
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
            new SqlParameterCollection(param));

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
