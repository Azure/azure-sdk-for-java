// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devtestlabs.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Tells source of creation of lab virtual machine. Output property only.
 */
public final class VirtualMachineCreationSource extends ExpandableStringEnum<VirtualMachineCreationSource> {
    /**
     * Static value FromCustomImage for VirtualMachineCreationSource.
     */
    public static final VirtualMachineCreationSource FROM_CUSTOM_IMAGE = fromString("FromCustomImage");

    /**
     * Static value FromGalleryImage for VirtualMachineCreationSource.
     */
    public static final VirtualMachineCreationSource FROM_GALLERY_IMAGE = fromString("FromGalleryImage");

    /**
     * Static value FromSharedGalleryImage for VirtualMachineCreationSource.
     */
    public static final VirtualMachineCreationSource FROM_SHARED_GALLERY_IMAGE = fromString("FromSharedGalleryImage");

    /**
     * Creates a new instance of VirtualMachineCreationSource value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public VirtualMachineCreationSource() {
    }

    /**
     * Creates or finds a VirtualMachineCreationSource from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding VirtualMachineCreationSource.
     */
    public static VirtualMachineCreationSource fromString(String name) {
        return fromString(name, VirtualMachineCreationSource.class);
    }

    /**
     * Gets known VirtualMachineCreationSource values.
     * 
     * @return known VirtualMachineCreationSource values.
     */
    public static Collection<VirtualMachineCreationSource> values() {
        return values(VirtualMachineCreationSource.class);
    }
}
