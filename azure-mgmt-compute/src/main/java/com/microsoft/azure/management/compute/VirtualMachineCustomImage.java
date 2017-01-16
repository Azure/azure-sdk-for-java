/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure virtual machine custom image.
 */
@Fluent
public interface VirtualMachineCustomImage extends
        GroupableResource,
        Refreshable<VirtualMachineCustomImage>,
        Wrapper<ImageInner> {
    /**
     * @return true if this image is created by capturing a virtual machine
     */
    boolean isCreatedFromVirtualMachine();

    /**
     * @return id of the virtual machine if this image is created by capturing that virtual machine
     */
    String sourceVirtualMachineId();

    /**
     * @return operating system disk image in this image
     */
    ImageOSDisk osDiskImage();

    /**
     * @return data disk images in this image, indexed by the disk lun
     */
    Map<Integer, ImageDataDisk> dataDiskImages();

    /**
     * The entirety of the image definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithOsDiskImageAltSourceVirtualMachine,
            DefinitionStages.WithOsDiskImageSource,
            DefinitionStages.WithCreateAndDataDiskImageOsDiskSettings {
    }

    /**
     * Grouping of image definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a image definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the image definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<WithOsDiskImageAltSourceVirtualMachine> {
        }

        /**
         * The stage of the image definition that allows choosing between using a virtual machine as
         * source for OS and data disk images or beginning OS disk image definition.
         */
        interface WithOsDiskImageAltSourceVirtualMachine
                extends WithOsDiskImageStateAndType, WithSourceVirtualMachine {
        }

        /**
         * The stage of the image definition allowing to choose os type and os state for the OS image.
         */
        interface WithOsDiskImageStateAndType {
            /**
             * Specifies that OS disk image is a generalized Windows image.
             *
             * @return the next stage of the image definition
             */
            WithOsDiskImageSource withGeneralizedWindowsOsDiskImage();

            /**
             * Specifies that OS disk image is a specialized Windows image.
             *
             * @return the next stage of the image definition
             */
            WithOsDiskImageSource withSpecializedWindowsOsDiskImage();

            /**
             * Specifies that OS disk image is a generalized Linux image.
             *
             * @return the next stage of the image definition
             */
            WithOsDiskImageSource withGeneralizedLinuxOsDiskImage();

            /**
             * Specifies that OS disk image is a specialized Linux image.
             *
             * @return the next stage of the image definition
             */
            WithOsDiskImageSource withSpecializedLinuxOsDiskImage();
        }

        /**
         * The stage of the image definition allowing to choose source virtual machine.
         */
        interface WithSourceVirtualMachine {
            /**
             * Uses the virtual machine's OS disk and data disks as the source for OS disk image and
             * data disk images of this image.
             *
             * @param virtualMachineId source virtual machine resource id
             * @return the next stage of the image definition
             */
            WithCreate fromVirtualMachine(String virtualMachineId);

            /**
             * Uses the virtual machine's OS and data disks as the sources for OS disk image and data
             * disk images of this image.
             *
             * @param virtualMachine source virtual machine
             * @return the next stage of the image definition
             */
            WithCreate fromVirtualMachine(VirtualMachine virtualMachine);
        }

        /**
         * The stage of the image definition allowing to choose the source of the OS disk image.
         */
        interface WithOsDiskImageSource {
            /**
             * Specifies the source VHD for the OS disk image.
             *
             * @param sourceVhdUrl source virtual hard disk url
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings fromVhd(String sourceVhdUrl);

            /**
             * Specifies the source snapshot for the OS disk image.
             *
             * @param sourceSnapshotId source snapshot resource id
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings fromSnapshot(String sourceSnapshotId);

            /**
             * Specifies the source managed disk for the OS disk image.
             *
             * @param sourceManagedDiskId source managed disk resource id
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings fromManagedDisk(String sourceManagedDiskId);

            /**
             * Specifies the source managed disk for the OS disk image.
             *
             * @param sourceManagedDisk source managed disk
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings fromManagedDisk(Disk sourceManagedDisk);
        }

        /**
         * The stage of an image definition allowing to create the image or add optional data disk images
         * and configure OS disk settings.
         */
        interface WithCreateAndDataDiskImageOsDiskSettings
                extends WithCreate, WithOsDiskSettings, WithDataDiskImage  {
        }

        /**
         * The stage of an image definition allowing to specify configurations for the OS disk when it
         * is created from the image's  OS disk image.
         */
        interface WithOsDiskSettings {
            /**
             * Specifies the size in GB for OS disk.
             *
             * @param diskSizeGB the disk size in GB
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings withOsDiskSizeInGB(int diskSizeGB);

            /**
             * Specifies the caching type for OS disk.
             *
             * @param cachingType the disk caching type
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings withOsDiskCaching(CachingTypes cachingType);
        }

        /**
         * The stage of an image definition allowing to add data disk image.
         */
        interface WithDataDiskImage {
            /**
             * Adds a data disk image with source as a virtual hard disk.
             *
             * @param sourceVhdUrl source virtual hard disk url
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings withDataDiskImageFromVhd(String sourceVhdUrl);

            /**
             * Adds a data disk image with source as snapshot.
             *
             * @param sourceSnapshotId source snapshot resource id
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings withDataDiskImageFromSnapshot(String sourceSnapshotId);

            /**
             * Adds a data disk image with source as managed disk.
             *
             * @param sourceManagedDiskId source managed disk resource id
             * @return the next stage of the image definition
             */
            WithCreateAndDataDiskImageOsDiskSettings withDataDiskImageFromManagedDisk(String sourceManagedDiskId);

            /**
             * Begins the definition of a new data disk image to add to the image.
             * <p>
             * The definition must be completed with a call to {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}
             * @param lun the unique lun for the data disk
             * @return the first stage of the new data disk image definition
             */
            CustomImageDataDisk.DefinitionStages.Blank<WithCreateAndDataDiskImageOsDiskSettings> defineDataDiskImage(int lun);
        }

        /**
         * The stage of an image definition containing all the required inputs for
         * the resource to be created (via {@link VirtualMachineCustomImage.DefinitionStages.WithCreate#create()}), but also allowing
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualMachineCustomImage>,
                Resource.DefinitionWithTags<VirtualMachineCustomImage.DefinitionStages.WithCreate> {
        }
    }

    /**
     * An immutable client-side representation of a data disk image in an image resource.
     */
    @Fluent
    interface CustomImageDataDisk
            extends Wrapper<ImageDataDisk>, ChildResource<VirtualMachineCustomImage> {
        /**
         * Grouping of data disk image definition stages.
         */
        interface DefinitionStages {
            /**
             * The first stage of the data disk image definition.
             * @param <ParentT> the return type of the final {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}
             */
            interface Blank<ParentT> extends WithImageSource<ParentT> {
            }

            /**
             * The stage of the image definition allowing to choose the source of the data disk image.
             *
             * @param <ParentT> the return type of the final {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}
             */
            interface WithImageSource<ParentT> {
                /**
                 * Specifies the source VHD for the data disk image.
                 *
                 * @param sourceVhdUrl source virtual hard disk url
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> fromVhd(String sourceVhdUrl);

                /**
                 * Specifies the source snapshot for the data disk image.
                 *
                 * @param sourceSnapshotId source snapshot resource id
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> fromSnapshot(String sourceSnapshotId);

                /**
                 * Specifies the source managed disk for the data disk image.
                 *
                 * @param sourceManagedDiskId source managed disk resource id
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> fromManagedDisk(String sourceManagedDiskId);

                /**
                 * Specifies the source managed disk for the data disk image.
                 *
                 * @param sourceManagedDisk source managed disk
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> fromManagedDisk(Disk sourceManagedDisk);
            }

            /**
             * The stage of data disk image definition allowing to specify configurations for the data disk when it
             * is created from the same data disk image.
             *
             *  @param <ParentT> the return type of the final {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}
             */
            interface WithDiskSettings<ParentT> {
                /**
                 * Specifies the size in GB for data disk.
                 *
                 * @param diskSizeGB the disk size in GB
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> withDiskSizeInGB(int diskSizeGB);

                /**
                 * Specifies the caching type for data disk.
                 *
                 * @param cachingType the disk caching type
                 * @return the next stage of the data disk image definition
                 */
                WithAttach<ParentT> withDiskCaching(CachingTypes cachingType);
            }

            /**
             * The final stage of the data disk image definition.
             * <p>
             * At this stage, any remaining optional settings can be specified, or the data disk definition
             * can be attached to the parent image definition using {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}.
             * @param <ParentT> the return type of {@link CustomImageDataDisk.DefinitionStages.WithAttach#attach()}
             */
            interface WithAttach<ParentT> extends
                    Attachable.InDefinition<ParentT>,
                    WithDiskSettings<ParentT> {
            }
        }

        /** The entirety of a data disk image definition.
         * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
         */
        interface Definition<ParentT> extends
                DefinitionStages.Blank<ParentT>,
                DefinitionStages.WithImageSource<ParentT>,
                DefinitionStages.WithDiskSettings<ParentT>,
                DefinitionStages.WithAttach<ParentT> {
        }
    }
}
