// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedQueryFilteringRntbdTest {
    @Test(groups = "unit")
    public void filteredQueryEncodesPhysicalContinuationAndOrdinaryQueriesRemainUnchanged() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(TestUtils.mockDiagnosticsClientContext(),
            OperationType.Query, "dbs/db/colls/coll", ResourceType.Document);
        request.setContentBytes("{\"query\":\"SELECT * FROM c WHERE c.a = 1\"}".getBytes(StandardCharsets.UTF_8));
        request.getHeaders().put(HttpConstants.HttpHeaders.IF_NONE_MATCH, "\"42\"");
        request.getHeaders().put(HttpConstants.HttpHeaders.IF_MATCH, "\"ordinary-query-match\"");
        request.getHeaders().put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
        RntbdRequestFrame frame = new RntbdRequestFrame(UUID.randomUUID(), OperationType.Query, ResourceType.Document);
        RntbdRequestHeaders filtered = new RntbdRequestHeaders(
            new RntbdRequestArgs(request, new Uri("https://localhost/replica-path")), frame);
        assertThat(frame.getOperationType()).isEqualTo(RntbdConstants.RntbdOperationType.Query);
        assertThat(filtered.get(RntbdConstants.RntbdRequestHeader.Match).getValue()).isEqualTo("\"42\"");
        assertThat(filtered.get(RntbdConstants.RntbdRequestHeader.PayloadPresent).getValue()).isEqualTo((byte) 1);

        request.getHeaders().remove(HttpConstants.HttpHeaders.A_IM);
        RntbdRequestHeaders ordinary = new RntbdRequestHeaders(
            new RntbdRequestArgs(request, new Uri("https://localhost/replica-path")), frame);
        assertThat(ordinary.get(RntbdConstants.RntbdRequestHeader.Match).getValue()).isEqualTo("\"ordinary-query-match\"");
    }
}
