package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.rx.TestSuiteBase;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static com.azure.cosmos.implementation.directconnectivity.ReflectionUtils.getClassBySimpleName;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailoverTests extends TestSuiteBase {

    private static final String IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY = "COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED";

    private GlobalEndpointManager globalEndpointManagerMock;

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionCircuitBreakerTests.class);
    private final static Pair<URI, String> LocationEastUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us.documents.azure.com"), "eastus");
    private final static Pair<URI, String> LocationEastUs2EndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us-2.documents.azure.com"), "eastus2");
    private final static Pair<URI, String> LocationCentralUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-central-us.documents.azure.com"), "centralus");

    @BeforeClass(groups = {"unit"})
    public void beforeClass() {
        this.globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        Mockito.when(this.globalEndpointManagerMock.getConnectionPolicy()).thenReturn(connectionPolicy);
    }

    @DataProvider(name = "")
    public Object[][] tryMarkEndpointAsUnavailableForPartitionKeyRangeTestArgs() {
        return new Object[][]{
            {OperationType.Create}, {OperationType.Read}
        };
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void tryMarkEndpointAsUnavailableForPartitionKeyRange(OperationType operationType) throws NoSuchFieldException, IllegalAccessException {

        System.setProperty(IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY, "true");

        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
            = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(this.globalEndpointManagerMock, Configs.isPerPartitionAutomaticFailoverEnabled());

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        Class<?>[] enclosedClasses = GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class.getDeclaredClasses();
        Class<?> partitionLevelFailoverInfoClass = getClassBySimpleName(enclosedClasses, "PartitionLevelFailoverInfo");

        assertThat(partitionLevelFailoverInfoClass).isNotNull();

        Field failedLocationsField = partitionLevelFailoverInfoClass.getDeclaredField("failedLocations");

        assertThat(failedLocationsField).isNotNull();

        Field currentField = partitionLevelFailoverInfoClass.getDeclaredField("current");

        assertThat(currentField).isNotNull();

        failedLocationsField.setAccessible(true);
        currentField.setAccessible(true);

        Field partitionKeyRangeToLocationField
            = GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class.getDeclaredField("partitionKeyRangeToLocation");

        partitionKeyRangeToLocationField.setAccessible(true);

        ConcurrentHashMap<PartitionKeyRangeWrapper, ?> partitionKeyRangeToLocation
            = (ConcurrentHashMap<PartitionKeyRangeWrapper, ?>) partitionKeyRangeToLocationField.get(globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            operationType,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            collectionResourceId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        boolean canFailover
            = globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request);

        Object partitionLevelFailoverInfo
            = partitionKeyRangeToLocation.get(new PartitionKeyRangeWrapper(request.requestContext.resolvedPartitionKeyRange, collectionResourceId));

        Set<URI> failedLocations = (Set<URI>) failedLocationsField.get(partitionLevelFailoverInfo);
        URI current = (URI) currentField.get(partitionLevelFailoverInfo);

        System.clearProperty(IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY);
    }

    private RxDocumentServiceRequest constructRxDocumentServiceRequestInstance(
        OperationType operationType,
        ResourceType resourceType,
        String collectionResourceId,
        String partitionKeyRangeId,
        String collectionLink,
        String minInclusive,
        String maxExclusive,
        URI locationEndpointToRoute) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.setResourceId(collectionResourceId);

        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(partitionKeyRangeId, minInclusive, maxExclusive);
        request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = request.requestContext.resolvedPartitionKeyRange;
        request.requestContext.locationEndpointToRoute = locationEndpointToRoute;
        request.requestContext.setExcludeRegions(Collections.emptyList());
        request.requestContext.setPointOperationContext(
            new PointOperationContextForCircuitBreaker(
                new AtomicBoolean(false),
                false,
                collectionLink,
                new SerializationDiagnosticsContext()));

        return request;
    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
