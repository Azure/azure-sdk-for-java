// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

/**
 * Anonymous credentials are to be used with with HTTP(S) requests that read blobs from public containers or requests
 * that use a Shared Access Signature (SAS). This is because Anonymous credentials will not set an Authorization header.
 * Pass an instance of this class as the credentials parameter when creating a new pipeline (typically with
 * {@link StorageURL}).
 */
public final class AnonymousCredentials implements ICredentials {

    /**
     * Returns an empty instance of {@code AnonymousCredentials}.
     */
    public AnonymousCredentials() {
    }

    /**
     * Creates a new {@code AnonymousCredentialsPolicy}.
     *
     * @param nextRequestPolicy
     *         The next {@code RequestPolicy} in the pipeline which will be called after this policy completes.
     * @param options
     *         Unused.
     *
     * @return A {@code RequestPolicy} object to be inserted into the {@link HttpPipeline}.
     */
    @Override
    public RequestPolicy create(RequestPolicy nextRequestPolicy, RequestPolicyOptions options) {
        return new AnonymousCredentialsPolicy(nextRequestPolicy);
    }

    /**
     * This policy will perform an a no-op on the Authorization header. Typically constructing a pipeline will even
     * ignore constructing this policy if is recognized. Please refer to either {@link AccountSASSignatureValues},
     * {@link ServiceSASSignatureValues} for more information on SAS requests. Please refer to the following for more
     * information on anonymous requests:
     * <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-manage-access-to-resources>Manage Access to Storage Resources</a>
     * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/set-container-acl>Set Container Permissions</a>
     */
    private static final class AnonymousCredentialsPolicy implements RequestPolicy {
        final RequestPolicy nextPolicy;

        AnonymousCredentialsPolicy(RequestPolicy nextPolicy) {
            this.nextPolicy = nextPolicy;
        }

        /**
         * For anonymous credentials, this is effectively a no-op.
         *
         * @param request
         *         An {@link HttpRequest} object representing the storage request.
         *
         * @return A Single containing the {@link HttpResponse} if successful.
         */
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            return nextPolicy.sendAsync(request);
        }
    }
}
