// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.RootCause;
import com.azure.ai.metricsadvisor.implementation.models.RootCauseList;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IncidentRootCauseTransforms {
    public static PagedResponse<IncidentRootCause> fromInnerResponse(Response<RootCauseList> innerResponse) {
        final RootCauseList innerRootCauseList = innerResponse.getValue();
        if (innerRootCauseList == null || innerRootCauseList.getValue() == null) {
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                new ArrayList<>(),
                null,
                null);
        } else {
            final List<IncidentRootCause> rootCauseList = innerRootCauseList.getValue()
                .stream()
                .map(inner -> IncidentRootCauseTransforms.fromInner(inner))
                .collect(Collectors.toList());
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                rootCauseList,
                null,
                null);
        }
    }

    public static IncidentRootCause fromInner(RootCause inner) {
        final IncidentRootCause incidentRootCause = new IncidentRootCause();
        IncidentRootCauseHelper.setSeriesKey(incidentRootCause, new DimensionKey(inner.getRootCause().getDimension()));
        IncidentRootCauseHelper.setPaths(incidentRootCause, inner.getPath());
        IncidentRootCauseHelper.setContributionScore(incidentRootCause, inner.getScore());
        IncidentRootCauseHelper.setDescription(incidentRootCause, inner.getDescription());
        return incidentRootCause;
    }
}
