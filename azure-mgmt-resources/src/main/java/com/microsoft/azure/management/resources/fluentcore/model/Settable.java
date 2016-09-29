package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;

/**
 * The base interface for all template interfaces for child resources that support
 * update operations.
 *
 * @param <ParentT> the parent definition {@link Settable#parent()} returns to
 */
@LangDefinition(ContainerName = "ChildResourceActions")
public interface Settable<ParentT> {
    /**
     * Begins an update for a child resource.
     * <p>
     * This is the beginning of the builder pattern used to update child resources
     * The final method completing the update and continue
     * the actual parent resource update process in Azure is {@link Settable#parent()}.
     *
     * @return the stage of  parent resource update
     */
    @Method
    ParentT parent();
}
