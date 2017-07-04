/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.VirtualMachineCaptureResultInner;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Type representing the result of a virtual machine capture operation.
 */
public interface VirtualMachineCaptureResult extends HasInner<VirtualMachineCaptureResultInner> {
    /**
     * @return the capture result as Azure template
     */
    String template();
}
