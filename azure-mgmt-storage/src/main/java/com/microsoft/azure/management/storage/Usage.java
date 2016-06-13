/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.implementation.api.UsageName;
import com.microsoft.azure.management.storage.implementation.api.UsageUnit;

/**
 * An immutable client-side representation of an Azure storage resource usage info object.
 */
public interface Usage extends Wrapper<com.microsoft.azure.management.storage.implementation.api.Usage> {
    /**
     * @return the unit of measurement. Possible values include: 'Count',
     * 'Bytes', 'Seconds', 'Percent', 'CountsPerSecond', 'BytesPerSecond'.
     */
    UsageUnit unit();

    /**
     * @return the current count of the allocated resources in the subscription
     */
    int currentValue();

    /**
     * @return the maximum count of the resources that can be allocated in the
     * subscription
     */
    int limit();

    /**
     * @return the name of the type of usage
     */
    UsageName name();

}