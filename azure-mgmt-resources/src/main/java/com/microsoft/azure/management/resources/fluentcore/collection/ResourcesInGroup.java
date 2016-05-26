package com.microsoft.azure.management.resources.fluentcore.collection;

public interface ResourcesInGroup<T, DefinitionT> extends
    SupportsListing<T>,
    SupportsCreating<DefinitionT>,
    SupportsDeleting {}
