// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionTokenMismatchRetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

import static com.azure.cosmos.implementation.Utils.ValueHolder;

/*
 ConsistencyLevel      Replication Mode         Desired ReadMode
 -------------------   --------------------     ---------------------------------------------------------------------------
 STRONG                  Synchronous              READ from READ Quorum
                         Asynchronous             Not supported

 Bounded Staleness       Synchronous              READ from READ Quorum
                         Asynchronous             READ from READ Quorum. Performing read barrier on Primary is unsupported.

 SESSION                 Sync/Async               READ Any (With LSN Cookie)
                                                  DEFAULT to Primary as last resort (which should succeed always)

 EVENTUAL                Sync/Async               READ Any

 Client does validation of unsupported combinations.


 Preliminaries
 =============
 1. We do primary copy/single master replication.
 2. We do sync or async replication depending on the value of DefaultConsistencyLevel on a database account.
 If the database account is configured with DefaultConsistencyLevel = STRONG, we do sync replication. By default, for all other values of DefaultConsistencyLevel, we do asynchronous replication.

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
 W = Write Quorum = NUMBER of replicas which acknowledge a write before the primary can ack the client. It is majority set i.e. N/2 + 1
 R = READ Quorum = Set of replicas such that there is non-empty intersection between W and R that constitute N i.e. R = N -W + 1

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

 STRONG Consistency
 ==================
 STRONG READ requires following guarantees.
 * READ value is the latest that has been written. If a write operation finished. Any subsequent reads should see that value.
 * Monotonic guarantee. Any read that starts after a previous read operation, should see atleast return equal or higher version of the value.

 To perform strong read we require that atleast R i.e. READ Quorum number of replicas have the value committed. To acheve that such read :
 * READ R replicas. If they have the same LSN, use the read result
 * If they don't have the same LSN, we will either return the result with the highest LSN observed from those R replicas, after ensuring that LSN
   becomes available with R replicas.
 * Secondary replicas are always preferred for reading. If R secondaries have returned the result but cannot agree on the resulting LSN, we can include Primary to satisfy read quorum.
 * If we only have R replicas (i.e. N==R), we include primary in reading the result and validate N==R.

 Bounded Staleness
 =================
 Sync Replication:
 Bounded staleness uses the same logic as STRONG for cases where the server is using sync replication.

 Async Replication:
 For async replication, we make sure that we do not use the Primary as barrier for read quorum. This is because Primary is always going to run ahead (async replication uses W=1 on Primary).
 Using primary would voilate the monotonic read guarantees when we fall back to reading from secondary in the subsequent reads as they are always running slower as compared to Primary.

 SESSION
 =======
 We read from secondaries one by one until we find a match for the client's session token (LSN-C).
 We go to primary as a last resort which should satisfy LSN-C.

 Availability for Bounded Staleness (for NMax = 4 and NMin = 2):
 When there is a partition, the minority quorum can remain available for read as long as N >= 1
 When there is a partition, the minority quorum can remain available for writes as long as N >= 2

 EVENTUAL
 ========
 We can read from any replicas.

 Availability for Bounded Staleness (for NMax = 4 and NMin = 2):
 When there is a partition, the minority quorum can remain available for read as long as N >= 1
 When there is a partition, the minority quorum can remain available for writes as long as N >= 2

 READ Retry logic
 -----------------
 For Any NonQuorum Reads(A.K.A ReadAny); AddressCache is refreshed for following condition.
  1) No Secondary Address is found in Address Cache.
  2) Chosen Secondary Returned GoneException/EndpointNotFoundException.

 For Quorum READ address cache is refreshed on following condition.
  1) We found only R secondary where R < RMAX.
  2) We got GoneException/EndpointNotFoundException on all the secondary we contacted.

 */
/**
 * ConsistencyReader has a dependency on both StoreReader and QuorumReader. For Bounded Staleness and STRONG Consistency, it uses the Quorum Reader
 * to converge on a read from read quorum number of replicas.
 * For SESSION and EVENTUAL Consistency, it directly uses the store reader.
 */
public class ConsistencyReader {
    private final static Logger logger = LoggerFactory.getLogger(ConsistencyReader.class);

    private final DiagnosticsClientContext diagnosticsClientContext;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final StoreReader storeReader;
    private final QuorumReader quorumReader;
    private final Configs configs;

    public ConsistencyReader(
        DiagnosticsClientContext diagnosticsClientContext,
        Configs configs,
        AddressSelector addressSelector,
        ISessionContainer sessionContainer,
        TransportClient transportClient,
        GatewayServiceConfigurationReader serviceConfigReader,
        IAuthorizationTokenProvider authorizationTokenProvider) {
        this.diagnosticsClientContext = diagnosticsClientContext;
        this.configs = configs;
        this.serviceConfigReader = serviceConfigReader;
        this.storeReader = createStoreReader(transportClient, addressSelector, sessionContainer);
        this.quorumReader = createQuorumReader(transportClient, addressSelector, this.storeReader, serviceConfigReader, authorizationTokenProvider);
    }

    public Mono<StoreResponse> readAsync(RxDocumentServiceRequest entity,
                                         TimeoutHelper timeout,
                                         boolean isInRetry,
                                         boolean forceRefresh) {
        if (!isInRetry) {
            if (timeout.isElapsed()) {
                return Mono.error(new RequestTimeoutException());
            }

        } else {
            if (timeout.isElapsed()) {
                return Mono.error(new GoneException());
            }
        }

        entity.requestContext.timeoutHelper = timeout;

        if (entity.requestContext.requestChargeTracker == null) {
            entity.requestContext.requestChargeTracker = new RequestChargeTracker();
        }

        if(entity.requestContext.cosmosDiagnostics == null) {
            entity.requestContext.cosmosDiagnostics = entity.createCosmosDiagnostics();
        }

        entity.requestContext.forceRefreshAddressCache = forceRefresh;

        ValueHolder<ConsistencyLevel> targetConsistencyLevel = ValueHolder.initialize(null);
        ValueHolder<Boolean> useSessionToken = ValueHolder.initialize(null);
        ReadMode desiredReadMode;
        try {
            desiredReadMode = this.deduceReadMode(entity, targetConsistencyLevel, useSessionToken);
        } catch (CosmosException e) {
            return Mono.error(e);
        }
        int maxReplicaCount = this.getMaxReplicaSetSize(entity);
        int readQuorumValue = maxReplicaCount - (maxReplicaCount / 2);

        switch (desiredReadMode) {
            case Primary:
                return this.readPrimaryAsync(entity, useSessionToken.v);

            case Strong:
                entity.requestContext.performLocalRefreshOnGoneException = true;
                return this.quorumReader.readStrongAsync(this.diagnosticsClientContext, entity, readQuorumValue, desiredReadMode);

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
                return this.quorumReader.readStrongAsync(this.diagnosticsClientContext, entity, readQuorumValue, desiredReadMode);

            case Any:
                if (targetConsistencyLevel.v == ConsistencyLevel.SESSION) {
                    return BackoffRetryUtility.executeRetry(
                        () -> this.readSessionAsync(entity, desiredReadMode),
                        new SessionTokenMismatchRetryPolicy(BridgeInternal.getRetryContext(entity.requestContext.cosmosDiagnostics)));
                } else {
                    return this.readAnyAsync(entity, desiredReadMode);
                }

            default:
                throw new IllegalStateException("invalid operation " + desiredReadMode);
        }
    }

    private Mono<StoreResponse> readPrimaryAsync(RxDocumentServiceRequest entity,
                                                 boolean useSessionToken) {

        Mono<StoreResult> responseObs = this.storeReader.readPrimaryAsync(
            entity,
            false /*required valid LSN*/,
            useSessionToken);
        return responseObs.flatMap(response -> {
            try {
                return Mono.just(response.toResponse());
            } catch (CosmosException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<StoreResponse> readAnyAsync(RxDocumentServiceRequest entity,
                                             ReadMode readMode) {
        Mono<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
            entity,
            /* includePrimary */ true,
            /* replicaCountToRead */ 1,
            /* requiresValidLSN*/ false,
            /* useSessionToken */ false,
            /* readMode */ readMode);

        return responsesObs.flatMap(
                responses -> {
            if (responses.size() == 0) {
                        return Mono.error(new GoneException(RMResources.Gone));
            }

            try {
                        return Mono.just(responses.get(0).toResponse());
            } catch (CosmosException e) {
                return Mono.error(e);
            }
                }
        );
    }

    private Mono<StoreResponse> readSessionAsync(RxDocumentServiceRequest entity,
                                                 ReadMode readMode) {

        if (entity.requestContext.timeoutHelper.isElapsed()) {
            return Mono.error(new GoneException());
        }

        Mono<List<StoreResult>> responsesObs = this.storeReader.readMultipleReplicaAsync(
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
                    return Mono.just(responses.get(0).toResponse(entity.requestContext.requestChargeTracker));
                } catch (NotFoundException notFoundException) {
                    try {
                        if (entity.requestContext.sessionToken != null
                            && responses.get(0).sessionToken != null
                            && !entity.requestContext.sessionToken.isValid(responses.get(0).sessionToken)) {
                            logger.warn("Convert to session read exception, request {} SESSION Lsn {}, responseLSN {}", entity.getResourceAddress(), entity.requestContext.sessionToken.convertToString(), responses.get(0).lsn);
                            notFoundException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
                        }
                        return Mono.error(notFoundException);
                    } catch (CosmosException e) {
                        return Mono.error(e);
                    }
                } catch (CosmosException dce) {
                    return Mono.error(dce);
                }

            }

            // else
            HashMap<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));
            ISessionToken requestSessionToken = entity.requestContext.sessionToken;
            logger.warn("Fail the session read {}, request session token {}", entity.getResourceAddress(), requestSessionToken == null ? "<empty>" : requestSessionToken.convertToString());
            return Mono.error(new NotFoundException(RMResources.ReadSessionNotAvailable, responseHeaders, null));
        });
    }

    ReadMode deduceReadMode(RxDocumentServiceRequest request,
                            ValueHolder<ConsistencyLevel> targetConsistencyLevel,
                            ValueHolder<Boolean> useSessionToken) {
        targetConsistencyLevel.v = RequestHelper.getConsistencyLevelToUse(this.serviceConfigReader, request);
        useSessionToken.v = (targetConsistencyLevel.v == ConsistencyLevel.SESSION);

        if (request.getDefaultReplicaIndex() != null) {
            // Don't use session token - this is used by internal scenarios which technically don't intend session read when they target
            // request to specific replica.
            useSessionToken.v = false;
            return ReadMode.Primary;  //Let the addressResolver decides which replica to connect to.
        }

        switch (targetConsistencyLevel.v) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
            case SESSION:
                return ReadMode.Any;

            case BOUNDED_STALENESS:
                return ReadMode.BoundedStaleness;

            case STRONG:
                return ReadMode.Strong;

            default:
                throw new IllegalStateException("INVALID Consistency Level " + targetConsistencyLevel.v);
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

    public StoreReader createStoreReader(TransportClient transportClient,
                                  AddressSelector addressSelector,
                                  ISessionContainer sessionContainer) {
        return new StoreReader(transportClient,
            addressSelector,
            sessionContainer);
    }

    public QuorumReader createQuorumReader(TransportClient transportClient,
                                    AddressSelector addressSelector,
                                    StoreReader storeReader,
                                    GatewayServiceConfigurationReader serviceConfigurationReader,
                                    IAuthorizationTokenProvider authorizationTokenProvider) {
        return new QuorumReader(this.diagnosticsClientContext,
            transportClient,
            addressSelector,
            storeReader,
            serviceConfigurationReader,
            authorizationTokenProvider,
            configs);
    }
}
