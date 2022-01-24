// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link CopyAuthorization} instance.
 */
public final class CopyAuthorizationHelper {
    private static CopyAuthorizationAccessor accessor;

    private CopyAuthorizationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link CopyAuthorization} instance.
     */
    public interface CopyAuthorizationAccessor {

        void setTargetResourceId(CopyAuthorization copyAuthorization, String targetResourceId);

        void setTargetResourceRegion(CopyAuthorization copyAuthorization, String targetResourceRegion);

        void setTargetModelId(CopyAuthorization copyAuthorization, String targetModelId);

        void setTargetModelLocation(CopyAuthorization copyAuthorization, String targetModelLocation);

        void setAccessToken(CopyAuthorization copyAuthorization, String accessToken);

        void setExpirationDateTime(CopyAuthorization copyAuthorization, OffsetDateTime expirationDateTime);
    }

    /**
     * The method called from {@link CopyAuthorization} to set it's accessor.
     *
     * @param copyAuthorizationAccessor The accessor.
     */
    public static void setAccessor(final CopyAuthorizationAccessor copyAuthorizationAccessor) {
        accessor = copyAuthorizationAccessor;
    }

    static void setTargetResourceId(CopyAuthorization copyAuthorization, String targetResourceId) {
        accessor.setTargetResourceId(copyAuthorization, targetResourceId);
    }

    static void setTargetResourceRegion(CopyAuthorization copyAuthorization, String targetResourceRegion) {
        accessor.setTargetResourceRegion(copyAuthorization, targetResourceRegion);
    }

    static void setTargetModelId(CopyAuthorization copyAuthorization, String targetModelId) {
        accessor.setTargetModelId(copyAuthorization, targetModelId);
    }

    static void setTargetModelLocation(CopyAuthorization copyAuthorization, String targetModelLocation) {
        accessor.setTargetModelLocation(copyAuthorization, targetModelLocation);
    }

    static void setAccessToken(CopyAuthorization copyAuthorization, String accessToken) {
        accessor.setAccessToken(copyAuthorization, accessToken);
    }

    static void setExpirationDateTime(CopyAuthorization copyAuthorization, OffsetDateTime expirationDateTime) {
        accessor.setExpirationDateTime(copyAuthorization, expirationDateTime);
    }
}
