// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionTokenHelperTests {
    @DataProvider
    public Object[][] explicitTokenSelections() {
        return new Object[][] {
            {"0:1#10#1=10,1:1#20#1=20", null, "1#20#1=20"},
            {"1:1#20#1=20", Collections.emptyList(), "1#20#1=20"},
            {"0:1#10#1=10", Collections.singletonList("0"), "1#10#1=10"},
            {"0:1#10#1=10,1:1#20#1=20", Collections.singletonList("0"), "1#20#1=20"},
            {"1:1#20#1=20,1:1#30#1=30", null, "1#30#1=30"},
            {"0:1#100#1=10#2=20,2:1#120#1=15#2=10", Arrays.asList("0", "2"), "1#120#1=15#2=20"},
            {"9:1#999#1=999,1:1#20#1=20", null, "1#20#1=20"},
            {"0:1#10#1=10,2:1#20#1=20", null, null}
        };
    }

    @Test(groups = "unit", dataProvider = "explicitTokenSelections")
    public void selectsExplicitTargetAndParentProgress(String compoundToken, List<String> parents, String expected) {
        ISessionToken selected = SessionTokenHelper.getLocalSessionToken(compoundToken, "1", parents);

        if (expected == null) {
            assertThat(selected).isNull();
        } else {
            assertThat(selected).isEqualTo(SessionTokenHelper.parse(expected));
        }
    }

    @DataProvider
    public Object[][] malformedTokens() {
        return new Object[][] {
            {"malformed,1:1#20#1=20"},
            {"1:1#20#1=20,malformed"},
            {"9:not-a-vector,1:1#20#1=20"},
            {"1:1#20#1=20,9:not-a-vector"},
            {"1:1#20#1=20:extra"}
        };
    }

    @Test(groups = "unit", dataProvider = "malformedTokens")
    public void validatesAllSegmentsIncludingNonmatchingOnes(String token) {
        assertThatThrownBy(() -> SessionTokenHelper.getLocalSessionToken(token, "1", Collections.emptyList()))
            .isInstanceOf(RuntimeException.class);
    }

    @Test(groups = "unit")
    public void propagatesIncompatibleRegionProgress() {
        assertThatThrownBy(() -> SessionTokenHelper.getLocalSessionToken(
            "1:1#20#1=20,1:1#30#2=30", "1", Collections.emptyList()))
            .isInstanceOfSatisfying(CosmosException.class, error -> {
                assertThat(error.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
                assertThat(error.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.INVALID_REGIONS_IN_SESSION_TOKEN);
            });
    }

    @Test(groups = "unit")
    public void setterAppliesExplicitProgressWithoutConsultingAmbientState() {
        RxDocumentServiceRequest request = request();
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(
            "1", "", "FF", Collections.singletonList("0"));
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, "0:1#10#1=10,9:1#999#1=999");
        ISessionContainer ambient = Mockito.mock(ISessionContainer.class);

        SessionTokenHelper.setPartitionLocalSessionToken(request, ambient);

        assertThat(request.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#10#1=10"));
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo("1:1#10#1=10");
        Mockito.verifyNoInteractions(ambient);
    }

    @Test(groups = "unit")
    public void setterRecordsNoMatchAndClearsProgressWithoutAmbientSubstitution() {
        RxDocumentServiceRequest request = request();
        request.requestContext.resolvedCollectionRid = "rid";
        request.requestContext.sessionToken = SessionTokenHelper.parse("1#999#1=999");
        String explicitToken = "0:1#10#1=10,2:1#20#1=20";
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, explicitToken);
        ISessionContainer ambient = Mockito.mock(ISessionContainer.class);

        SessionTokenHelper.setPartitionLocalSessionToken(request, "1", ambient);

        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1)
            .allSatisfy(result -> assertThat(result).contains(explicitToken, "pkRangeId : 1", "collectionRid : rid"));
        Mockito.verifyNoInteractions(ambient);
    }

    @DataProvider
    public Object[][] absentTokens() {
        return new Object[][] {{null}, {""}};
    }

    @Test(groups = "unit", dataProvider = "absentTokens")
    public void setterUsesAmbientProgressOnlyWhenExplicitTokenIsAbsent(String explicitToken) {
        RxDocumentServiceRequest request = request();
        if (explicitToken != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, explicitToken);
        }
        ISessionContainer ambient = Mockito.mock(ISessionContainer.class);
        ISessionToken progress = SessionTokenHelper.parse("1#30#1=30");
        Mockito.when(ambient.resolvePartitionLocalSessionToken(request, "1")).thenReturn(progress);

        SessionTokenHelper.setPartitionLocalSessionToken(request, "1", ambient);

        assertThat(request.requestContext.sessionToken).isSameAs(progress);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo("1:1#30#1=30");
        Mockito.verify(ambient).resolvePartitionLocalSessionToken(request, "1");
        Mockito.verifyNoMoreInteractions(ambient);
    }

    @Test(groups = "unit")
    public void setterClearsPreviousProgressWhenAmbientHasNoToken() {
        RxDocumentServiceRequest request = request();
        request.requestContext.sessionToken = SessionTokenHelper.parse("1#30#1=30");
        ISessionContainer ambient = Mockito.mock(ISessionContainer.class);

        SessionTokenHelper.setPartitionLocalSessionToken(request, "1", ambient);

        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);
    }

    @Test(groups = "unit")
    public void setterDoesNotMutateProgressOnSelectionFailure() {
        RxDocumentServiceRequest request = request();
        ISessionToken previous = SessionTokenHelper.parse("1#30#1=30");
        request.requestContext.sessionToken = previous;
        String invalidToken = "1:1#20#1=20,9:not-a-vector";
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, invalidToken);
        ISessionContainer ambient = Mockito.mock(ISessionContainer.class);

        assertThatThrownBy(() -> SessionTokenHelper.setPartitionLocalSessionToken(request, "1", ambient))
            .isInstanceOf(RuntimeException.class);

        assertThat(request.requestContext.sessionToken).isSameAs(previous);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(invalidToken);
        Mockito.verifyNoInteractions(ambient);
    }

    @Test(groups = "unit")
    public void validateAndRemoveStillValidatesBeforeRemovingTheHeader() {
        RxDocumentServiceRequest request = request();
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, "1:1#20#1=20,2:1#30#1=30");
        SessionTokenHelper.validateAndRemoveSessionToken(request);
        assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);

        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, "invalid");
        assertThatThrownBy(() -> SessionTokenHelper.validateAndRemoveSessionToken(request))
            .isInstanceOf(BadRequestException.class);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo("invalid");
    }

    @Test(groups = "unit")
    public void missingPartitionKeyRangeIdInContext() {
        try {
            ISessionContainer mockedSessionContainer = Mockito.mock(ISessionContainer.class);

            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                    "/docs/",
                    new Database(),
                    new HashMap<>());

            SessionTokenHelper.setPartitionLocalSessionToken(req, null, mockedSessionContainer);
            Assert.fail("It should fail with internalServerError due to missing pkRangeId");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.MISSING_PARTITION_KEY_RANGE_ID_IN_CONTEXT);
            assertThat(cosmosException.getMessage()).contains(RMResources.PartitionKeyRangeIdAbsentInContext);
        }
    }

    private static RxDocumentServiceRequest request() {
        return RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), OperationType.Read,
            "dbs/db/colls/coll/docs/item", ResourceType.Document);
    }
}
