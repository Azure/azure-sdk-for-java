package com.microsoft.azure.management.resources.fluentcore.model;

/**
 * The base interface for all template interfaces for child resources that support
 * update operations.
 *
 * @param <ParentT> the parent definition {@link Settable#set()} returns to
 */
public interface Settable<ParentT> {
    /**
     * Begins an update for a child resource.
     * <p>
     * This is the beginning of the builder pattern used to update child resources
     * The final method completing the update and continue
     * the actual parent resource update process in Azure is {@link Settable#set()}.
     *
     * @return the stage of  parent resource update
     * @throws Exception exceptions thrown from Azure
     */
    ParentT set() throws Exception;
}
