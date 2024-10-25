// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.sharing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.azure.analytics.purview.sharing.models.InPlaceSentShare;
import com.azure.analytics.purview.sharing.models.SentShare;
import com.azure.analytics.purview.sharing.models.ShareResource;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

public class ShareResourceClientTest extends PurviewShareTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    void listShareResourcesTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        SentShare sentShare = super.createSentShare(sentShareId);

        PagedIterable<BinaryData> shareResources = super.shareResourcesClient.listShareResources(new RequestOptions());

        String sentShareResource
            = ((InPlaceSentShare) sentShare).getProperties().getArtifact().getStoreReference().getReferenceName();

        assertTrue(shareResources.stream().findAny().isPresent());
        assertTrue(shareResources.stream()
            .map(binaryData -> binaryData.toObject(ShareResource.class))
            .anyMatch(shareResource -> shareResource.getStoreReference()
                .getReferenceName()
                .equalsIgnoreCase(sentShareResource)));
    }
}
