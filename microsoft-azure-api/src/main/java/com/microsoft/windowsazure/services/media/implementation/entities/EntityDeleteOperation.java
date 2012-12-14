package com.microsoft.windowsazure.services.media.implementation.entities;

public interface EntityDeleteOperation {

    /**
     * Supplies the current proxy information to the action.
     * 
     * @param proxyData
     */
    void setProxyData(EntityProxyData proxyData);

    /**
     * Get the URI to use to delete an entity
     * 
     * @return The uri
     */
    String getUri();
}
