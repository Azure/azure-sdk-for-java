/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.models.VirtualMachineScaleSetVMsInner;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 *  Entry point to virtual machine scale set instance management API.
 */
@Fluent
public interface VirtualMachineScaleSetVMs extends
        SupportsListing<VirtualMachineScaleSetVM>,
    HasInner<VirtualMachineScaleSetVMsInner> {
    /**
     * Deletes the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be deleted
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> deleteInstancesAsync(Collection<String> instanceIds);

    /**
     * Deletes the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be deleted
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> deleteInstancesAsync(String... instanceIds);

    /**
     * Deletes the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be deleted
     */
    void deleteInstances(String... instanceIds);

    /**
     * Get the specified virtual machine instance from the scale set.
     * @param instanceId instance ID of the virtual machine scale set instance to be fetched
     * @return the virtual machine scale set instance.
     */
    VirtualMachineScaleSetVM getInstance(String instanceId);

    /**
     * Get the specified virtual machine instance from the scale set.
     * @param instanceId instance ID of the virtual machine scale set instance to be fetched.
     * @return the virtual machine scale set instance.
     */
    Mono<VirtualMachineScaleSetVM> getInstanceAsync(String instanceId);

    /**
     * Updates the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be updated
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> updateInstancesAsync(Collection<String> instanceIds);

    /**
     * Updates the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be updated
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> updateInstancesAsync(String... instanceIds);

    /**
     * Updates the specified virtual machine instances from the scale set.
     *
     * @param instanceIds instance IDs of the virtual machine scale set instances to be updated
     */
    void updateInstances(String... instanceIds);
}
