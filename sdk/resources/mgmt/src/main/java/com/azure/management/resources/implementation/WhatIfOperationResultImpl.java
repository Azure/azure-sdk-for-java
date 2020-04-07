// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.implementation;

import com.azure.management.resources.ErrorResponse;
import com.azure.management.resources.WhatIfOperationResult;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.resources.models.WhatIfOperationResultInner;

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
    public ErrorResponse error() {
        return this.inner().error();
    }
}
