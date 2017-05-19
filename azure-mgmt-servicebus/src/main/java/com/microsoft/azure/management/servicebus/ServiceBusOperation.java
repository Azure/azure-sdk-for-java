/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.OperationInner;

/**
 * An immutable client-side representation of an Azure service bus operation description object.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_1_0)
public interface ServiceBusOperation extends HasInner<OperationInner> {
    /**
     * @return the operation name
     */
    String name();
    /**
     * @return the description of the operation
     */
    OperationDisplay displayInformation();
}
