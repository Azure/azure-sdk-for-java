// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.model;

import com.azure.core.management.polling.PollResult;
import com.azure.core.util.polling.PollerFlux;

public interface AcceptedCreatable<InnerT, T extends HasInner<InnerT>> {

    T getAcceptedResult();

    PollerFlux<PollResult<InnerT>, InnerT> beginPolling();

    T getFinalResult();
}
