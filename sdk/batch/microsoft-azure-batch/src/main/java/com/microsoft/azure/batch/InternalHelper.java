// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal helper functions
 */
class InternalHelper {
    /**
     * Inherit the BatchClientBehavior classes from parent object
     *
     * @param inheritingObject the inherit object
     * @param baseBehaviors base class behavior list
     */
    public static void inheritClientBehaviorsAndSetPublicProperty(IInheritedBehaviors inheritingObject, Iterable<BatchClientBehavior> baseBehaviors) {
        // implement inheritance of behaviors
        List<BatchClientBehavior> customBehaviors = new ArrayList<>();

        // if there were any behaviors, pre-populate the collection (ie: inherit)
        if (null != baseBehaviors) {
            for (BatchClientBehavior be : baseBehaviors) {
                customBehaviors.add(be);
            }
        }

        // set the public property
        inheritingObject.withCustomBehaviors(customBehaviors);
    }
}
