// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AlertResult;
import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnomalyAlertTransforms {
    public static PagedResponse<AnomalyAlert> fromInnerResponse(PagedResponse<AlertResult> alertResultPagedResponse) {
        final List<AlertResult> innerAlertResultList = alertResultPagedResponse.getValue();
        if (CoreUtils.isNullOrEmpty(innerAlertResultList)) {
            return new PagedResponseBase<>(
                alertResultPagedResponse.getRequest(),
                alertResultPagedResponse.getStatusCode(),
                alertResultPagedResponse.getHeaders(),
                new ArrayList<>(),
                null,
                null);
        } else {
            final List<AnomalyAlert> anomalyAlertList = innerAlertResultList
                .stream()
                .map(inner -> fromInner(inner))
                .collect(Collectors.toList());
            return new PagedResponseBase<>(
                alertResultPagedResponse.getRequest(),
                alertResultPagedResponse.getStatusCode(),
                alertResultPagedResponse.getHeaders(),
                anomalyAlertList,
                null,
                null);
        }
    }

    private static AnomalyAlert fromInner(AlertResult innerAlertResult) {
        final AnomalyAlert anomalyAlert = new AnomalyAlert();
        AnomalyAlertHelper.setId(anomalyAlert, innerAlertResult.getAlertId());
        AnomalyAlertHelper.setCreatedTime(anomalyAlert, innerAlertResult.getCreatedTime());
        AnomalyAlertHelper.setModifiedTime(anomalyAlert, innerAlertResult.getModifiedTime());
        return anomalyAlert;
    }
}
