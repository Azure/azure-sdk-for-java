// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.inner.ImageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import java.util.Map;

/** An immutable client-side representation of an Azure virtual machine custom image. */
@Fluent
public interface VirtualMachineCustomImage
    extends GroupableResource<ComputeManager, ImageInner>, Refreshable<VirtualMachineCustomImage> {
    /** @return true if this image was created by capturing a virtual machine */
    boolean isCreatedFromVirtualMachine();

    /** @return the hyper v Generation */
    HyperVGenerationTypes hyperVGeneration();

    /** @return ID of the virtual machine if this image was created by capturing that virtual machine */
    String sourceVirtualMachineId();

    /** @return operating system disk image in this image */
    ImageOSDisk osDiskImage();

    /** @return data disk images in this image, indexed by the disk LUN */
    Map<Integer, ImageDataDisk> dataDiskImages();

    /** The entirety of the image definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithHyperVGeneration,
            DefinitionStages.WithOSDiskImageSourceAltVirtualMachineSource,
            DefinitionStages.WithOSDiskImageSource,
            DefinitionStages.WithSourceVirtualMachine,
            DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings {
    }

    /** Grouping of image definition stages. */
    interface DefinitionStages {
        /** The first stage of a image definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the image definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithHyperVGeneration> {
        }

        /**
         * The stage of the image definition that allows choosing between using a virtual machine as the source for OS
         * and the data disk images or beginning an OS disk image definition.
         */
        interface WithOSDiskImageSourceAltVirtualMachineSource extends WithOSDiskImageSource, WithSourceVirtualMachine {
        }

        /**
         * The stage of the image definition that allows us to choose a hyper V generation. Default, if this stage is
         * not added will be hyperV gen 1.
         */
        interface WithHyperVGeneration extends WithOSDiskImageSourceAltVirtualMachineSource {
            WithOSDiskImageSourceAltVirtualMachineSource withHyperVGeneration(HyperVGenerationTypes hyperVGeneration);
        }

        /** The stage of the image definition allowing to choose an OS source and an OS state for the OS image. */
        interface WithOSDiskImageSource {
            /**
             * Specifies the Windows source native VHD for the OS disk image.
             *
             * @param sourceVhdUrl source Windows virtual hard disk URL
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withWindowsFromVhd(
                String sourceVhdUrl, OperatingSystemStateTypes osState);

            /**
             * Specifies the Linux source native VHD for the OS disk image.
             *
             * @param sourceVhdUrl source Linux virtual hard disk URL
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withLinuxFromVhd(
                String sourceVhdUrl, OperatingSystemStateTypes osState);

            /**
             * Specifies the Windows source snapshot for the OS disk image.
             *
             * @param sourceSnapshot source snapshot resource
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withWindowsFromSnapshot(
                Snapshot sourceSnapshot, OperatingSystemStateTypes osState);

            /**
             * Specifies the Linux source snapshot for the OS disk image.
             *
             * @param sourceSnapshot source snapshot resource
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withLinuxFromSnapshot(
                Snapshot sourceSnapshot, OperatingSystemStateTypes osState);

            /**
             * Specifies the Windows source snapshot for the OS disk image.
             *
             * @param sourceSnapshotId source snapshot resource ID
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withWindowsFromSnapshot(
                String sourceSnapshotId, OperatingSystemStateTypes osState);

            /**
             * Specifies the Linux source snapshot for the OS disk image.
             *
             * @param sourceSnapshotId source snapshot resource ID
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withLinuxFromSnapshot(
                String sourceSnapshotId, OperatingSystemStateTypes osState);

            /**
             * Specifies the Windows source managed disk for the OS disk image.
             *
             * @param sourceManagedDiskId source managed disk resource ID
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withWindowsFromDisk(
                String sourceManagedDiskId, OperatingSystemStateTypes osState);

            /**
             * Specifies the Linux source managed disk for the OS disk image.
             *
             * @param sourceManagedDiskId source managed disk resource ID
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withLinuxFromDisk(
                String sourceManagedDiskId, OperatingSystemStateTypes osState);

            /**
             * Specifies the Windows source managed disk for the OS disk image.
             *
             * @param sourceManagedDisk source managed disk
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withWindowsFromDisk(
                Disk sourceManagedDisk, OperatingSystemStateTypes osState);

            /**
             * Specifies the Linux source managed disk for the OS disk image.
             *
             * @param sourceManagedDisk source managed disk
             * @param osState operating system state
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withLinuxFromDisk(
                Disk sourceManagedDisk, OperatingSystemStateTypes osState);
        }

        /** The stage of the image definition allowing to choose source virtual machine. */
        interface WithSourceVirtualMachine {
            /**
             * Uses the virtual machine's OS disk and data disks as the source for OS disk image and data disk images of
             * this image.
             *
             * @param virtualMachineId source virtual machine resource ID
             * @return the next stage of the definition
             */
            WithCreate fromVirtualMachine(String virtualMachineId);

            /**
             * Uses the virtual machine's OS and data disks as the sources for OS disk image and data disk images of
             * this image.
             *
             * @param virtualMachine source virtual machine
             * @return the next stage of the definition
             */
            WithCreate fromVirtualMachine(VirtualMachine virtualMachine);
        }

        /**
         * The stage of an image definition allowing to create the image or add optional data disk images and configure
         * OS disk settings.
         */
        interface WithCreateAndDataDiskImageOSDiskSettings extends WithCreate, WithOSDiskSettings, WithDataDiskImage {
        }

        /**
         * The stage of an image definition allowing to specify configurations for the OS disk when it is created from
         * the image's OS disk image.
         */
        interface WithOSDiskSettings {
            /**
             * Specifies the size in GB for OS disk.
             *
             * @param diskSizeGB the disk size in GB
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withOSDiskSizeInGB(int diskSizeGB);

            /**
             * Specifies the caching type for OS disk.
             *
             * @param cachingType the disk caching type
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withOSDiskCaching(CachingTypes cachingType);
        }

        /** The stage of an image definition allowing to add a data disk image. */
        interface WithDataDiskImage {
            /**
             * Adds a data disk image with a virtual hard disk as the source.
             *
             * @param sourceVhdUrl source virtual hard disk URL
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withDataDiskImageFromVhd(String sourceVhdUrl);

            /**
             * Adds a data disk image with an existing snapshot as the source.
             *
             * @param sourceSnapshotId source snapshot resource ID
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withDataDiskImageFromSnapshot(String sourceSnapshotId);

            /**
             * Adds a data disk image with an existing managed disk as the source.
             *
             * @param sourceManagedDiskId source managed disk resource ID
             * @return the next stage of the definition
             */
            WithCreateAndDataDiskImageOSDiskSettings withDataDiskImageFromManagedDisk(String sourceManagedDiskId);

            /**
             * Begins the definition of a new data disk image to add to the image.
             *
             * @return the first stage of the new data disk image definition
             */
            CustomImageDataDisk.DefinitionStages.Blank<WithCreateAndDataDiskImageOSDiskSettings> defineDataDiskImage();
        }

        /** The stage of an image definition allowing to enable zone resiliency. */
        interface WithZoneResilient {
            /**
             * Specifies that zone resiliency should be enabled for the image.
             *
             * @return the next stage of the definition
             */
            WithCreate withZoneResilient();
        }

        /**
         * The stage of an image definition containing all the required inputs for the resource to be created, but also
         * allowing for any other optional settings to be specified.
         */
        interface WithCreate
            extends DefinitionStages.WithZoneResilient,
                Creatable<VirtualMachineCustomImage>,
                Resource.DefinitionWithTags<VirtualMachineCustomImage.DefinitionStages.WithCreate> {
        }
    }

    /** An immutable client-side representation of a data disk image in an image resource. */
    @Fluent
    interface CustomImageDataDisk extends HasInner<ImageDataDisk>, ChildResource<VirtualMachineCustomImage> {
        /** Grouping of data disk image definition stages. */
        interface DefinitionStages {
            /**
             * The first stage of the data disk image definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface Blank<ParentT> extends WithDiskLun<ParentT> {
            }

            /**
             * The stage of the image definition allowing to specify the LUN for the disk image.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithDiskLun<ParentT> {
                /**
                 * Specifies the LUN for the data disk to be created from the disk image.
                 *
                 * @param lun the unique LUN for the data disk
                 * @return the next stage of the definition
                 */
                WithImageSource<ParentT> withLun(int lun);
            }

            /**
             * The stage of the image definition allowing to choose the source of the data disk image.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithImageSource<ParentT> {
                /**
                 * Specifies the source VHD for the data disk image.
                 *
                 * @param sourceVhdUrl source virtual hard disk URL
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> fromVhd(String sourceVhdUrl);

                /**
                 * Specifies the source snapshot for the data disk image.
                 *
                 * @param sourceSnapshotId source snapshot resource ID
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> fromSnapshot(String sourceSnapshotId);

                /**
                 * Specifies the source managed disk for the data disk image.
                 *
                 * @param sourceManagedDiskId source managed disk resource ID
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> fromManagedDisk(String sourceManagedDiskId);

                /**
                 * Specifies the source managed disk for the data disk image.
                 *
                 * @param sourceManagedDisk source managed disk
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> fromManagedDisk(Disk sourceManagedDisk);
            }

            /**
             * The stage of data disk image definition allowing to specify configurations for the data disk when it is
             * created from the same data disk image.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithDiskSettings<ParentT> {
                /**
                 * Specifies the size in GB for data disk.
                 *
                 * @param diskSizeGB the disk size in GB
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withDiskSizeInGB(int diskSizeGB);

                /**
                 * Specifies the caching type for data disk.
                 *
                 * @param cachingType the disk caching type
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withDiskCaching(CachingTypes cachingType);
            }

            /**
             * The final stage of the data disk image definition.
             *
             * <p>At this stage, any remaining optional settings can be specified, or the data disk definition can be
             * attached to the parent image definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithDiskSettings<ParentT> {
            }
        }

        /**
         * The entirety of a data disk image definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Definition<ParentT>
            extends DefinitionStages.Blank<ParentT>,
                DefinitionStages.WithDiskLun<ParentT>,
                DefinitionStages.WithImageSource<ParentT>,
                DefinitionStages.WithDiskSettings<ParentT>,
                DefinitionStages.WithAttach<ParentT> {
        }
    }
}
