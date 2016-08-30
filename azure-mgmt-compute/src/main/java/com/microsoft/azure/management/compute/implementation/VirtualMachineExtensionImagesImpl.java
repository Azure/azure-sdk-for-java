package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImages;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachinePublishers;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineExtensionImages}.
 */
class VirtualMachineExtensionImagesImpl
        implements VirtualMachineExtensionImages {
    private final VirtualMachinePublishers publishers;

    VirtualMachineExtensionImagesImpl(VirtualMachinePublishers publishers) {
        this.publishers = publishers;
    }

    @Override
    public PagedList<VirtualMachineExtensionImage> listByRegion(Region region) throws CloudException, IOException {
        return listByRegion(region.toString());
    }

    @Override
    public PagedList<VirtualMachineExtensionImage> listByRegion(String regionName) throws CloudException, IOException {
        PagedList<VirtualMachinePublisher> publishers = this.publishers().listByRegion(regionName);

        PagedList<VirtualMachineExtensionImageType> extensionTypes =
                new ChildListFlattener<>(publishers, new ChildListFlattener.ChildListLoader<VirtualMachinePublisher, VirtualMachineExtensionImageType>() {
                    @Override
                    public PagedList<VirtualMachineExtensionImageType> loadList(VirtualMachinePublisher publisher) throws CloudException, IOException  {
                        return publisher.extensionTypes().list();
                    }
                }).flatten();

        PagedList<VirtualMachineExtensionImageVersion> extensionTypeVersions =
                new ChildListFlattener<>(extensionTypes, new ChildListFlattener.ChildListLoader<VirtualMachineExtensionImageType, VirtualMachineExtensionImageVersion>() {
                    @Override
                    public PagedList<VirtualMachineExtensionImageVersion> loadList(VirtualMachineExtensionImageType type) throws CloudException, IOException  {
                        return type.versions().list();
                    }
                }).flatten();

        PagedListConverter<VirtualMachineExtensionImageVersion, VirtualMachineExtensionImage> converter =
                new PagedListConverter<VirtualMachineExtensionImageVersion, VirtualMachineExtensionImage>() {
                    @Override
                    public VirtualMachineExtensionImage typeConvert(VirtualMachineExtensionImageVersion virtualMachineExtensionImageVersion) {
                        try {
                            return virtualMachineExtensionImageVersion.image();
                        } catch (CloudException cloudException) {
                            throw new RuntimeException(cloudException);
                        } catch (IOException ioException) {
                            throw new RuntimeException(ioException);
                        }
                    }
                };

        return converter.convert(extensionTypeVersions);
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }
}