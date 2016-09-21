package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;

/**
 * Represents an external child resource.
 *
 * @param <FluentModelT> fluent type of the external child resource
 * @param <ParentT> parent interface
 */
public interface ExternalChildResource<FluentModelT, ParentT> extends ChildResource<ParentT>, Refreshable<FluentModelT> {
    /**
     * @return the id of the external child resource
     */
    String id();
}
