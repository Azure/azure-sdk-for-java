package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;

/**
 * Represents an external child resource.
 *
 * @param <T> fluent type of the external child resource
 */
public interface ExternalChildResource<T> extends ChildResource, Refreshable<T> {
    /**
     * @return the id of the external child resource
     */
    String id();
}
