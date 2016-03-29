/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import java.util.Collection;

public interface IInheritedBehaviors {

    Collection<BatchClientBehavior> getCustomBehaviors();

    void setCustomBehaviors(Collection<BatchClientBehavior> behaviors);

}
