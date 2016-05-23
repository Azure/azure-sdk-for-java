package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * Base interface used by child resources
 */
public interface ChildResource extends Indexable {
    /**
     * @return the name of the child resource
     */
	String name();
}
