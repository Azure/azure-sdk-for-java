package com.microsoft.azure.management.resources.models.fluent;

import com.microsoft.azure.management.resources.fluentcore.model.Deletable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.fluent.common.GroupableResource;

public interface GenericResource extends
        GroupableResource,
        Refreshable<GenericResource>,
        Wrapper<com.microsoft.azure.management.resources.models.dto.toplevel.GenericResource>,
        Deletable {

    String provider() throws Exception;
    String properties() throws Exception;
    String provisioningState() throws Exception;
}