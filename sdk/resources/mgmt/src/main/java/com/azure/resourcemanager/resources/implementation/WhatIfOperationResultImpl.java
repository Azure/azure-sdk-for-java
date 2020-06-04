// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.ErrorResponse;
import com.azure.resourcemanager.resources.WhatIfChange;
import com.azure.resourcemanager.resources.WhatIfOperationResult;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.models.WhatIfOperationResultInner;

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
        return this.inner().status();
    }

    @Override
    public List<WhatIfChange> changes() {
        return this.inner().changes();
    }

    @Override
    public ErrorResponse error() {
        return this.inner().error();
    }
}
