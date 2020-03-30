/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.models.VirtualMachineExtensionImageInner;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of an Azure virtual machine extension image version.
 */
@Fluent
public interface VirtualMachineExtensionImageVersion extends
        HasInner<VirtualMachineExtensionImageInner>,
        HasName {
    /**
     * @return the resource ID of the extension image version
     */
    String id();

    /**
     * @return the region in which virtual machine extension image version is available
     */
    String regionName();

    /**
     * @return the virtual machine extension image type this version belongs to
     */
    VirtualMachineExtensionImageType type();

    /**
     * @return virtual machine extension image this version represents
     */
    VirtualMachineExtensionImage getImage();


    /**
     * @return an observable upon subscription emits the image
     */
    Mono<VirtualMachineExtensionImage> getImageAsync();
}