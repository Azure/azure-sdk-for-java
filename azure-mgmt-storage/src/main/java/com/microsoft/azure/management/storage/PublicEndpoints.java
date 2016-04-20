package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.storage.implementation.api.Endpoints;

public class PublicEndpoints {
    private Endpoints primary;
    private Endpoints secondary;

    public PublicEndpoints(Endpoints primary, Endpoints secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * Gets the URLs that are used to perform a retrieval of a public blob,
     * queue or table object.Note that StandardZRS and PremiumLRS accounts
     * only return the blob endpoint.
     */
    public Endpoints primary() {
        return primary;
    }

    /**
     * Gets the URLs that are used to perform a retrieval of a public blob,
     * queue or table object from the secondary location of the storage
     * account. Only available if the accountType is StandardRAGRS.
     */
    public Endpoints secondary() {
        return secondary;
    }
}
