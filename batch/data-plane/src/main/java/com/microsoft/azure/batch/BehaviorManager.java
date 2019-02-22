// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.interceptor.DetailLevelInterceptor;
import com.microsoft.azure.batch.interceptor.RequestInterceptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class BehaviorManager {

    private Collection<BatchClientBehavior> perCallBehaviors;
    private Collection<BatchClientBehavior> baseBehaviors;

    BehaviorManager(Collection<BatchClientBehavior> baseBehaviors, Iterable<BatchClientBehavior> perCallBehaviors) {
        this.baseBehaviors = new LinkedList<>();

        if (null != baseBehaviors) {
            this.baseBehaviors().addAll(baseBehaviors);
        }

        this.perCallBehaviors = new LinkedList<>();

        if (null != perCallBehaviors) {
            for (BatchClientBehavior bh : perCallBehaviors) {
                this.perCallBehaviors().add(bh);
            }
        }
    }

    Collection<BatchClientBehavior> getMasterListOfBehaviors() {
        List<BatchClientBehavior> ml = new LinkedList<>(this.baseBehaviors());

        ml.addAll(this.perCallBehaviors());

        return ml;
    }

    void applyRequestBehaviors(Object request) {
        for (BatchClientBehavior bh : getMasterListOfBehaviors()) {
            if (bh instanceof RequestInterceptor) {
                ((RequestInterceptor) bh).handler().modify(request);
            }
        }
    }

    void appendDetailLevelToPerCallBehaviors(DetailLevel dl) {
        if (dl != null) {
            this.perCallBehaviors().add(new DetailLevelInterceptor(dl));
        }
    }

    private Collection<BatchClientBehavior> baseBehaviors() {
        return baseBehaviors;
    }

    public BehaviorManager withBaseBehaviors(Collection<BatchClientBehavior> baseBehaviors) {
        this.baseBehaviors = baseBehaviors;
        return this;
    }

    private Collection<BatchClientBehavior> perCallBehaviors() {
        return perCallBehaviors;
    }

    public BehaviorManager withPerCallBehaviors(Collection<BatchClientBehavior> perCallBehaviors) {
        this.perCallBehaviors = perCallBehaviors;
        return this;
    }
}
