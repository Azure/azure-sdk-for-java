/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import java.util.ArrayList;
import java.util.List;

class InternalHelper {
    public static void InheritClientBehaviorsAndSetPublicProperty(IInheritedBehaviors inheritingObject, Iterable<BatchClientBehavior> baseBehaviors) {
        // implement inheritance of behaviors
        List<BatchClientBehavior> customBehaviors = new ArrayList<BatchClientBehavior>();

        // if there were any behaviors, pre-populate the collection (ie: inherit)
        if (null != baseBehaviors)
        {
            for (BatchClientBehavior be : baseBehaviors)
            customBehaviors.add(be);
        }

        // set the public property
        inheritingObject.withCustomBehaviors(customBehaviors);
    }
}
