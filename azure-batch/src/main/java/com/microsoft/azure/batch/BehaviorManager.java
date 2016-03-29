package com.microsoft.azure.batch;

import com.microsoft.azure.batch.interceptor.DetailLevelInterceptor;
import com.microsoft.azure.batch.interceptor.RequestInterceptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xingwu on 1/29/2016.
 */
class BehaviorManager {

    private Collection<BatchClientBehavior> perCallBehaviors;
    private Collection<BatchClientBehavior> baseBehaviors;

    BehaviorManager(Collection<BatchClientBehavior> baseBehaviors, Iterable<BatchClientBehavior> perCallBehaviors) {
        this.baseBehaviors = new LinkedList<BatchClientBehavior>();

        if (null != baseBehaviors) {
            this.getBaseBehaviors().addAll(baseBehaviors);
        }

        this.perCallBehaviors = new LinkedList<BatchClientBehavior>();

        if (null != perCallBehaviors) {
            for (BatchClientBehavior bh : perCallBehaviors) {
                this.getPerCallBehaviors().add(bh);
            }
        }
    }

    Collection<BatchClientBehavior> getMasterListOfBehaviors() {
        List<BatchClientBehavior> ml = new LinkedList<BatchClientBehavior>(this.getBaseBehaviors());

        ml.addAll(this.getPerCallBehaviors());

        return ml;
    }

    void applyRequestBehaviors(Object request) {
        for (BatchClientBehavior bh : getMasterListOfBehaviors()) {
            if (bh instanceof RequestInterceptor) {
                ((RequestInterceptor) bh).getHandler().modify(request);
            }
        }
    }

    void appendDetailLevelToPerCallBehaviors(DetailLevel dl) {
        if (dl != null) {
            this.getPerCallBehaviors().add(new DetailLevelInterceptor(dl));
        }
    }

    public Collection<BatchClientBehavior> getBaseBehaviors() {
        return baseBehaviors;
    }

    public void setBaseBehaviors(Collection<BatchClientBehavior> baseBehaviors) {
        this.baseBehaviors = baseBehaviors;
    }

    public Collection<BatchClientBehavior> getPerCallBehaviors() {
        return perCallBehaviors;
    }

    public void setPerCallBehaviors(Collection<BatchClientBehavior> perCallBehaviors) {
        this.perCallBehaviors = perCallBehaviors;
    }
}
