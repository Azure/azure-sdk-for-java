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

// TODO: Access conditions, leases, etc from the download call that need to be applied to this request
// TODO: If we do a download on a big file, every single download chunk is going to repeat this process. How to avoid that?
// Current thinking is override anything that manages large downloads and do the getProps up front and then pass it through
// the context for this policy to skip
// What about pathological download chunk sizes of like 4mb+1, where we grab almost an entire extra region on every download?
// Maybe we just respond to customers when they complain and tell them to adjust a bit.
class FetchEncryptionVersionPolicy implements HttpPipelinePolicy {

    private final BlobAsyncClient blobClient;
    private final boolean requiresEncryption;

    FetchEncryptionVersionPolicy(BlobAsyncClient blobClient, boolean requiresEncryption) {
        this.blobClient = blobClient;
        this.requiresEncryption = requiresEncryption;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy nextPolicy) {
        /*
         * If there's a range header, we'll need to know how much to expand the range in the decryption policy, which
         * requires fetching the encryption version first.
         * If there's no range, it's either not a download request, in which case we don't need the properties, or it's
         * a full blob download in which case we'll get the entire blob anyway. Therefore, we won't need to expand the
         * range at all, and we'll get the properties/EncryptionData for free with the download request.
         * Assumption: Download is the only API on an encrypted client that sets x-ms-range.
         */
        if (context.getHttpRequest().getHeaders().getValue(CryptographyConstants.RANGE_HEADER) == null) {
            return nextPolicy.process();
        } else {
            return this.blobClient.getProperties().map(props -> {
                    EncryptionData encryptionData = EncryptionData.getAndValidateEncryptionData(
                        props.getMetadata().get(CryptographyConstants.ENCRYPTION_DATA_KEY), requiresEncryption);

                    if (encryptionData != null) {
                        context.setData(CryptographyConstants.ENCRYPTION_DATA_KEY, encryptionData);
                    }

                    return props;
                })
                .then(Mono.defer(nextPolicy::process));
        }
    }
}
