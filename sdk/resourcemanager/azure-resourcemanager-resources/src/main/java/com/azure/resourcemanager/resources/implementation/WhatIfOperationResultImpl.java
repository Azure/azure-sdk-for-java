// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.resources.models.WhatIfChange;
import com.azure.resourcemanager.resources.models.WhatIfOperationResult;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluent.models.WhatIfOperationResultInner;

import java.util.List;

/**
 * Implementation for {@link WhatIfOperationResult}.
 */
public class WhatIfOperationResultImpl extends
        WrapperImpl<WhatIfOperationResultInner>
        implements
        WhatIfOperationResult {

    WhatIfOperationResultImpl(WhatIfOperationResultInner inner) {
        super(inner);
    }

    @Override
    public String status() {
        return this.innerModel().status();
    }

    @Override
    public List<WhatIfChange> changes() {
        return this.innerModel().changes();
    }

    @Override
    public ManagementError error() {
        return this.innerModel().error();
    }
}
