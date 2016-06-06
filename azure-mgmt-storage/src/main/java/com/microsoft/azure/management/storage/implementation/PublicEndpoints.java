/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.storage.implementation.api.Endpoints;

/**
 * An instance of this class stores the The URIs associated with a storage
 * account that are used to perform a retrieval of a public blob, queue or
 * table object.
 */
public class PublicEndpoints {
    private Endpoints primary;
    private Endpoints secondary;

    /**
     * Creates an instance of PublicEndpoints with two access endpoints.
     * @param primary the primary endpoint
     * @param secondary the secondary endpoint
     */
    public PublicEndpoints(Endpoints primary, Endpoints secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * @return the URLs that are used to perform a retrieval of a public blob,
     * queue or table object.Note that StandardZRS and PremiumLRS accounts
     * only return the blob endpoint.
     */
    public Endpoints primary() {
        return primary;
    }

    /**
     * @return the URLs that are used to perform a retrieval of a public blob,
     * queue or table object from the secondary location of the storage
     * account. Only available if the accountType is StandardRAGRS.
     */
    public Endpoints secondary() {
        return secondary;
    }
}
