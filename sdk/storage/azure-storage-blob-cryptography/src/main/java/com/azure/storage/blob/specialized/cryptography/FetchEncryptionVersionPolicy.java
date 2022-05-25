// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class FetchEncryptionVersionPolicy implements HttpPipelinePolicy {

    private static final ClientLogger LOGGER = new ClientLogger(FetchEncryptionVersionPolicy.class);

    private final BlobAsyncClient blobClient;
    private final boolean requiresEncryption;

    FetchEncryptionVersionPolicy(BlobAsyncClient blobClient, boolean requiresEncryption) {
        this.blobClient = blobClient;
        this.requiresEncryption = requiresEncryption;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy nextPolicy) {
        // Assumption: Download is the only API on an encrypted client that sets x-ms-range.
        // If not a download call, this policy is a no-op.
        // TODO: Find another way to make this a no-op if it's not a download. May have to override download methods to add a context
        // Or if no range is specified, it's the whole blob anyway, so we'll have everything we need already
        // So maybe we just only expand the range if it's present. And we'd only have to do a getProperties if we see there's a range header
        // Otherwise decryption can take care of it
        if (context.getHttpRequest().getHeaders().getValue(CryptographyConstants.RANGE_HEADER) == null) {
            return nextPolicy.process();
        } else {
            return this.blobClient.getProperties().map(props -> {
                    EncryptionData encryptionData = getAndValidateEncryptionData(
                        props.getMetadata().get(CryptographyConstants.ENCRYPTION_DATA_KEY));

                    context.setData(CryptographyConstants.ENCRYPTION_DATA_KEY, encryptionData);

                    return props;
                    // if the blob is not encrypted and no encryption data is present, we'll skip parsing in the next policy
                })
                .then(Mono.defer(nextPolicy::process));
        }

    }

    /**
     * Gets and validates {@link EncryptionDataV1} from a Blob's metadata
     *
     * @param encryptionDataString {@code String} of encrypted metadata
     * @return {@link EncryptionData}
     */
    private EncryptionData getAndValidateEncryptionData(String encryptionDataString) {
        if (encryptionDataString == null) {
            if (requiresEncryption) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("'requiresEncryption' set to true but "
                    + "downloaded data is not encrypted."));
            }
            return null;
        }

        try {
            EncryptionData encryptionData;
            if (encryptionDataString.contains("\"Protocol\":\"1.0\",")) {
                    encryptionData = EncryptionData.fromJsonString(encryptionDataString, EncryptionDataV1.class);
                Objects.requireNonNull(((EncryptionDataV1) encryptionData).getContentEncryptionIV(),
                    "contentEncryptionIV in encryptionData cannot be null");
                Objects.requireNonNull(encryptionData.getWrappedContentKey().getEncryptedKey(), "encryptedKey in "
                    + "encryptionData.wrappedContentKey cannot be null");
            } else if (encryptionDataString.contains("\"Protocol\":\"2.0\",")) {
                    encryptionData = EncryptionData.fromJsonString(encryptionDataString, EncryptionDataV2.class);
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                    "Invalid Encryption Agent. This version of the client library does not understand the "
                        + "Encryption Agent set on the blob message: %s",
                    encryptionDataString)));
            }

            return encryptionData;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
