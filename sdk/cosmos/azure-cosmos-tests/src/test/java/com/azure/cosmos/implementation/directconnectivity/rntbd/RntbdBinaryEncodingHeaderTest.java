// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ContentSerializationFormat;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public final class RntbdBinaryEncodingHeaderTest {
    private static final String BINARY_ENCODING_PROPERTY = "azure.cosmos.binaryEncodingEnabled";

    @Test(groups = "unit")
    public void mapsCosmosBinaryHeaderToRntbdToken() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            null, OperationType.Read, ResourceType.Document);
        request.getHeaders().put(
            HttpConstants.HttpHeaders.CONTENT_SERIALIZATION_FORMAT,
            ContentSerializationFormat.CosmosBinary.toString());
        RntbdRequestFrame frame = new RntbdRequestFrame(
            UUID.randomUUID(),
            RntbdConstants.RntbdOperationType.Read,
            RntbdConstants.RntbdResourceType.Document);

        RntbdRequestHeaders headers = new RntbdRequestHeaders(
            new RntbdRequestArgs(request, new Uri("rntbd://localhost:10255")), frame);
        RntbdToken token = headers.get(RntbdConstants.RntbdRequestHeader.ContentSerializationFormat);

        assertThat(token.isPresent()).isTrue();
        assertThat(token.getValue(Byte.class))
            .isEqualTo(RntbdConstants.RntbdContentSerializationFormat.CosmosBinary.id());
    }

    @Test(groups = "unit")
    public void negotiatesBinaryQueryResponsesWithDotNetToken() {
        System.setProperty(BINARY_ENCODING_PROPERTY, "true");
        try {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                null, OperationType.Query, ResourceType.Document);

            RntbdToken token = createHeaders(request)
                .get(RntbdConstants.RntbdRequestHeader.SupportedSerializationFormats);

            assertThat(RntbdConstants.RntbdRequestHeader.SupportedSerializationFormats.id())
                .isEqualTo((short) 0x00C4);
            assertThat(token.getTokenType()).isEqualTo(RntbdTokenType.Byte);
            assertThat(token.isPresent()).isTrue();
            assertThat(token.getValue(Byte.class)).isEqualTo((byte) 0x03);
        } finally {
            System.clearProperty(BINARY_ENCODING_PROPERTY);
        }
    }

    @Test(groups = "unit")
    public void selectsBinaryForChangeFeedButNotGenericReadFeed() {
        System.setProperty(BINARY_ENCODING_PROPERTY, "true");
        try {
            RxDocumentServiceRequest changeFeed = RxDocumentServiceRequest.create(
                null, OperationType.ReadFeed, ResourceType.Document);
            changeFeed.getHeaders().put(
                HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
            RxDocumentServiceRequest readFeed = RxDocumentServiceRequest.create(
                null, OperationType.ReadFeed, ResourceType.Document);

            RntbdToken changeFeedFormat = createHeaders(changeFeed)
                .get(RntbdConstants.RntbdRequestHeader.ContentSerializationFormat);
            assertThat(changeFeedFormat.isPresent()).isTrue();
            assertThat(changeFeedFormat.getValue(Byte.class))
                .isEqualTo(RntbdConstants.RntbdContentSerializationFormat.CosmosBinary.id());
            assertThat(createHeaders(readFeed)
                .get(RntbdConstants.RntbdRequestHeader.ContentSerializationFormat).isPresent()).isFalse();
        } finally {
            System.clearProperty(BINARY_ENCODING_PROPERTY);
        }
    }

    @Test(groups = "unit")
    public void doesNotNegotiateBinaryQueryForReadFeedOrThinClient() {
        System.setProperty(BINARY_ENCODING_PROPERTY, "true");
        try {
            RxDocumentServiceRequest readFeed = RxDocumentServiceRequest.create(
                null, OperationType.ReadFeed, ResourceType.Document);
            RxDocumentServiceRequest thinQuery = RxDocumentServiceRequest.create(
                null, OperationType.Query, ResourceType.Document);
            thinQuery.useThinClientMode = true;

            assertThat(createHeaders(readFeed)
                .get(RntbdConstants.RntbdRequestHeader.SupportedSerializationFormats).isPresent()).isFalse();
            assertThat(createHeaders(thinQuery)
                .get(RntbdConstants.RntbdRequestHeader.SupportedSerializationFormats).isPresent()).isFalse();
        } finally {
            System.clearProperty(BINARY_ENCODING_PROPERTY);
        }
    }

    private static RntbdRequestHeaders createHeaders(RxDocumentServiceRequest request) {
        RntbdRequestFrame frame = new RntbdRequestFrame(
            UUID.randomUUID(),
            RntbdConstants.RntbdOperationType.Query,
            RntbdConstants.RntbdResourceType.Document);
        return new RntbdRequestHeaders(
            new RntbdRequestArgs(request, new Uri("rntbd://localhost:10255")), frame);
    }
}
