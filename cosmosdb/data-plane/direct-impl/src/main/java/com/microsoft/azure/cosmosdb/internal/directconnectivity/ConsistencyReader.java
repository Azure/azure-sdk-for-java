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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.cosmosdb.ClientSideRequestStatistics;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.ISessionToken;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import static com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

/*
 ConsistencyLevel      Replication Mode         Desired ReadMode
 -------------------   --------------------     ---------------------------------------------------------------------------
 Strong                  Synchronous              Read from Read Quorum
                         Asynchronous             Not supported

 Bounded Staleness       Synchronous              Read from Read Quorum
                         Asynchronous             Read from Read Quorum. Performing read barrier on Primary is unsupported.

 Session                 Sync/Async               Read Any (With LSN Cookie)
                                                  Default to Primary as last resort (which should succeed always)

 Eventual                Sync/Async               Read Any

 Client does validation of unsupported combinations.


 Preliminaries
 =============
 1. We do primary copy/single master replication.
 2. We do sync or async replication depending on the value of DefaultConsistencyLevel on a database account.
 If the database account is configured with DefaultConsistencyLevel = Strong, we do sync replication. By default, for all other values of DefaultConsistencyLevel, we do asynchronous replication.

 Replica set
 ===========
 We define N as the current number of replicas protecting a partition.
 At any given point, the value of N can fluctuate between NMax and NMin.
 NMax is called the target replica set size and NMin is called the minimum write availability set size.
 NMin and NMax are statically defined whereas N is dynamic.
 Dynamic replica set is great for dealing with successive failures.
 Since N fluctuates between NMax and NMin, the value of N at the time of calculation of W may not be the same when R is calculated.
 This is a side effect of dynamic quorum and requires careful consideration.

 NMin = 2, NMax >= 3

 Simultaneous Failures
 =====================
 In general N replicas imply 2f+1 simultaneous failures
 N = 5 allows for 2  simultaneous failures
 N = 4 allows for 1 failure
 N = 3 allows for 1 failure
 N < 3 allows for 0 failures

 Quorums
 =======
 W = Write Quorum = Number of replicas which acknowledge a write before the primary can ack the client. It is majority set i.e. N/2 + 1
 R = Read Quorum = Set of replicas such that there is non-empty intersection between W and R that constitute N i.e. R = N -W + 1

 For sync replication, W is used as a majority quorum.
 For async replication, W = 1. We have two LSNs, one is quorum acknowledged LSN (LSN-Q) and another is what is visible to the client (LSN-C).
 LSN-Q is the stable LSN which corresponds to the write quorum of Windows Fabric. LSN-C is unstable and corresponds to W=1.

 Assumptions
 ===========
 Nmin <= N <= Nmax
 W >= N/2 + 1
 R = N -W + 1

 N from read standpoint means number of address from BE which is returning successful response.
 Successful reponse: Any BE response containing LSN response header is considered successful reponse. Typically every response other than 410 is treated as succesful response.

 Strong Consistency
 ==================
 Strong Read requires following guarantees.
 * Read value is the latest that has been written. If a write operation finished. Any subsequent reads should see that value.
 * Monotonic guarantee. Any read that starts after a previous read operation, should see atleast return equal or higher version of the value.

 To perform strong read we require that atleast R i.e. Read Quorum number of replicas have the value committed. To acheve that such read :
 * Read R replicas. If they have the same LSN, use the read result
 * If they don't have the same LSN, we will either return the result with the highest LSN observed from those R replicas, after ensuring that LSN
   becomes available with R replicas.
 * Secondary replicas are always preferred for reading. If R secondaries have returned the result but cannot agree on the resulting LSN, we can include Primary to satisfy read quorum.
 * If we only have R replicas (i.e. N==R), we include primary in reading the result and validate N==R.

 Bounded Staleness
 =================
 Sync Replication:
 Bounded staleness uses the same logic as Strong for cases where the server is using sync replication.

 Async Replication:
 For async replication, we make sure that we do not use the Primary as barrier for read quorum. This is because Primary is always going to run ahead (async replication uses W=1 on Primary).
 Using primary would voilate the monotonic read guarantees when we fall back to reading from secondary in the subsequent reads as they are always running slower as compared to Primary.

 Session
 =======
 We read from secondaries one by one until we find a match for the client's session token (LSN-C).
 We go to primary as a last resort which should satisfy LSN-C.

 Availability for Bounded Staleness (for NMax = 4 and NMin = 2):
 When there is a partition, the minority quorum can remain available for read as long as N >= 1
 When there is a partition, the minority quorum can remain available for writes as long as N >= 2

 Eventual
 ========
 We can read from any replicas.

 Availability for Bounded Staleness (for NMax = 4 and NMin = 2):
 When there is a partition, the minority quorum can remain available for read as long as N >= 1
 When there is a partition, the minority quorum can remain available for writes as long as N >= 2

 Read Retry logic
 -----------------
 For Any NonQuorum Reads(A.K.A ReadAny); AddressCache is refreshed for following condition.
  1) No Secondary Address is found in Address Cache.
  2) Chosen Secondary Returned GoneException/EndpointNotFoundException.

 For Quorum Read address cache is refreshed on following condition.
  1) We found only R secondary where R < RMAX.
  2) We got GoneException/EndpointNotFoundException on all the secondary we contacted.

 */
/**
 * ConsistencyReader has a dependency on both StoreReader and QuorumReader. For Bounded Staleness and Strong Consistency, it uses the Quorum Reader
 * to converge on a read from read quorum number of replicas.
 * For Session and Eventual Consistency, it directly uses the store reader.
 */
public class ConsistencyReader {
    private final static int MAX_NUMBER_OF_SECONDARY_READ_RETRIES = 3;
    private final static Logger logger = LoggerFactory.getLogger(ConsistencyReader.class);

    private final AddressSelector addressSelector;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final IAuthorizationTokenProvider authorizationTokenProvider;
    private final StoreReader storeReader;
    private final QuorumReader quorumReader;
    private final Configs configs;

    public ConsistencyReader(
            Configs configs,
            AddressSelector addressSelector,
            ISessionContainer sessionContainer,
            TransportClient transportClient,
            GatewayServiceConfigurationReader serviceConfigReader,
            IAuthorizationTokenProvider authorizationTokenProvider) {
        this.configs = configs;
        this.addressSelector = addressSelector;
        this.serviceConfigReader = serviceConfigReader;
        this.authorizationTokenProvider = authorizationTokenProvider;
        this.storeReader = createStoreReader(transportClient, addressSelector, sessionContainer);
        this.quorumReader = createQuorumReader(transportClient, addressSelector, this.storeReader, serviceConfigReader, authorizationTokenProvider);
    }

    public Single<StoreResponse> readAsync(RxDocumentServiceRequest entity,
                                           TimeoutHelper timeout,
                                           boolean isInRetry,
                                           boolean forceRefresh) {
        if (!isInRetry) {
            if (timeout.isElapsed()) {
                return Single.error(new RequestTimeoutException());
            }

        } else {
            if (timeout.isElapsed()) {
                return Single.error(new GoneException());
            }
        }

        entity.requestContext.timeoutHelper = timeout;

        if (entity.requestContext.requestChargeTracker == null) {
            entity.requestContext.requestChargeTracker = new RequestChargeTracker();
        }

        if(entity.requestContext.clientSideRequestStatistics == null) {
            entity.requestContext.clientSideRequestStatistics = new ClientSideRequestStatistics();
        }

        entity.requestContext.forceRefreshAddressCache = forceRefresh;

        ValueHolder<ConsistencyLevel> targetConsistencyLevel = ValueHolder.initialize(null);
        ValueHolder<Boolean> useSessionToken = ValueHolder.initialize(null);
        ReadMode desiredReadMode;
        try {
            desiredReadMode = this.deduceReadMode(entity, targetConsistencyLevel, useSessionToken);
        } catch (DocumentClientException e) {
            return Single.error(e);
        }
        int maxReplicaCount = this.getMaxReplicaSetSize(entity);
        int readQuorumValue = maxReplicaCount - (maxReplicaCount / 2);

        switch (desiredReadMode) {
            case Primary:
                return this.readPrimaryAsync(entity, useSessionToken.v);

            case Strong:
                entity.requestContext.performLocalRefreshOnGoneException = true;
                return this.quorumReader.readStrongAsync(entity, readQuorumValue, desiredReadMode);

            case BoundedStaleness:
                entity.requestContext.performLocalRefreshOnGoneException = true;

                // for bounded staleness, we are defaulting to read strong for local region reads.
                // this can be done since we are always running with majority quorum w = 3 (or 2 during quorum downshift).
                // This means that the primary will always be part of the write quorum, and
                // therefore can be included for barrier reads.

                // NOTE: this assumes that we are running with SYNC replication (i.e. majority quorum).
                // When we run on a minority write quorum(w=2), to ensure monotonic read guarantees
                // we always contact two secondary replicas and exclude primary.
                // However, this model significantly reduces availability and available throughput for serving reads for bounded staleness during reconfiguration.
                // Therefore, to ensure monotonic read guarantee from any replica set we will just use regular quorum read(R=2) since our write quorum is always majority(W=3)
                return this.quorumReader.readStrongAsync(entity, readQuorumValue, desiredReadMode);

            case Any:
                if (targetConsistencyLevel.v == ConsistencyLevel.Session) {
                    return this.readSessionAsync(entity, desiredReadMode);
                } else {
                    return this.readAnyAsync(entity, desiredReadMode);
                }

            default:
                throw new IllegalStateException("invalid operation " + desiredReadMode);
        }
    }

    private Single<StoreResponse> readPrimaryAsync(RxDocumentServiceRequest entity,
                                                   boolean useSessionToken) {

        Single<StoreResult> responseObs = this.storeReader.readPrimaryAsync(
                entity,
                false /*required valid LSN*/,
                useSessionToken);
        return responseObs.flatMap(response -> {
            try {
                return Single.just(response.toResponse());
            } catch (DocumentClientException e) {
                // TODO: RxJava1 due to design flaw doesn't allow throwing checked exception
                // RxJava2 has fixed this design flaw,
                // once we switched to RxJava2 we can get rid of unnecessary catch block
                // also we can switch to Observable.map(.)
                return Single.error(e);
            }
        });
    }

    private Single<StoreResponse> readAnyAsync(RxDocumentServiceRequest entity,
                                               ReadMode readMode) {
        Single<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
                entity,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN*/ false,
                /* useSessionToken */ false,
                /* readMode */ readMode);

        return responsesObs.flatMap(
                responses -> {
                    if (responses.size() == 0) {
                        return Single.error(new GoneException(RMResources.Gone));
                    }

                    try {
                        return Single.just(responses.get(0).toResponse());
                    } catch (DocumentClientException e) {
                        // TODO: RxJava1 due to design flaw doesn't allow throwing checked exception
                        // RxJava2 has fixed this design flaw,
                        // once we switched to RxJava2 we can get rid of unnecessary catch block
                        // also we can switch to Observable.map(.)
                        return Single.error(e);
                    }
                }
        );
    }

    private Single<StoreResponse> readSessionAsync(RxDocumentServiceRequest entity,
                                                   ReadMode readMode) {

        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Single.error(new GoneException());
        }

        Single<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
                entity,
                /* includePrimary */ true,
                /* replicaCountToRead */ 1,
                /* requiresValidLSN */ true,
                /* useSessionToken */ true,
                /* readMode */ readMode,
                /* checkMinLsn */ true,
                /* forceReadAll */ false);

        return responsesObs.flatMap(responses -> {

            if (responses.size() > 0) {
                try {
                    return Single.just(responses.get(0).toResponse(entity.requestContext.requestChargeTracker));
                } catch (NotFoundException notFoundException) {
                    try {
                        if (entity.requestContext.sessionToken != null
                                && responses.get(0).sessionToken != null
                                && !entity.requestContext.sessionToken.isValid(responses.get(0).sessionToken)) {
                            logger.warn("Convert to session read exception, request {} Session Lsn {}, responseLSN {}", entity.getResourceAddress(), entity.requestContext.sessionToken.convertToString(), responses.get(0).lsn);
                            notFoundException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
                        }
                        return Single.error(notFoundException);
                    } catch (DocumentClientException e) {
                        // TODO: RxJava1 due to design flaw doesn't allow throwing checked exception
                        // so we have to catch and return
                        // once we move to RxJava2 we can fix this.
                        return Single.error(e);
                    }
                } catch (DocumentClientException dce) {
                    // TODO: RxJava1 due to design flaw doesn't allow throwing checked exception
                    // so we have to catch and return
                    // once we move to RxJava2 we can fix this.
                    return Single.error(dce);
                }

            }

            // else
            HashMap<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
            ISessionToken requestSessionToken = entity.requestContext.sessionToken;
            logger.warn("Fail the session read {}, request session token {}", entity.getResourceAddress(), requestSessionToken == null ? "<empty>" : requestSessionToken.convertToString());
            return Single.error(new NotFoundException(RMResources.ReadSessionNotAvailable, responseHeaders, null));
        });
    }

    ReadMode deduceReadMode(RxDocumentServiceRequest request,
                            ValueHolder<ConsistencyLevel> targetConsistencyLevel,
                            ValueHolder<Boolean> useSessionToken) throws DocumentClientException {
        targetConsistencyLevel.v = RequestHelper.GetConsistencyLevelToUse(this.serviceConfigReader, request);
        useSessionToken.v = (targetConsistencyLevel.v == ConsistencyLevel.Session);

        if (request.getDefaultReplicaIndex() != null) {
            // Don't use session token - this is used by internal scenarios which technically don't intend session read when they target
            // request to specific replica.
            useSessionToken.v = false;
            return ReadMode.Primary;  //Let the addressResolver decides which replica to connect to.
        }

        switch (targetConsistencyLevel.v) {
            case Eventual:
                return ReadMode.Any;

            case ConsistentPrefix:
                return ReadMode.Any;

            case Session:
                return ReadMode.Any;

            case BoundedStaleness:
                return ReadMode.BoundedStaleness;

            case Strong:
                return ReadMode.Strong;

            default:
                throw new IllegalStateException("Invalid Consistency Level " + targetConsistencyLevel.v);
        }
    }

    public int getMaxReplicaSetSize(RxDocumentServiceRequest entity) {
        boolean isMasterResource = ReplicatedResourceClient.isReadingFromMaster(entity.getResourceType(), entity.getOperationType());
        if (isMasterResource) {
            return this.serviceConfigReader.getSystemReplicationPolicy().getMaxReplicaSetSize();
        } else {
            return this.serviceConfigReader.getUserReplicationPolicy().getMaxReplicaSetSize();
        }
    }

    public int getMinReplicaSetSize(RxDocumentServiceRequest entity) {
        boolean isMasterResource = ReplicatedResourceClient.isReadingFromMaster(entity.getResourceType(), entity.getOperationType());
        if (isMasterResource) {
            return this.serviceConfigReader.getSystemReplicationPolicy().getMinReplicaSetSize();
        } else {
            return this.serviceConfigReader.getUserReplicationPolicy().getMinReplicaSetSize();
        }
    }

    StoreReader createStoreReader(TransportClient transportClient,
                                  AddressSelector addressSelector,
                                  ISessionContainer sessionContainer) {
        return new StoreReader(transportClient,
                               addressSelector,
                               sessionContainer);
    }

    QuorumReader createQuorumReader(TransportClient transportClient,
                                    AddressSelector addressSelector,
                                    StoreReader storeReader,
                                    GatewayServiceConfigurationReader serviceConfigurationReader,
                                    IAuthorizationTokenProvider authorizationTokenProvider) {
        return new QuorumReader(transportClient,
                                addressSelector,
                                storeReader,
                                serviceConfigurationReader,
                                authorizationTokenProvider,
                                configs);
    }
}
