// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.GalleryImageInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of an Azure gallery image. A gallery image resource is a container for
 * multiple versions of the same image.
 */
@Fluent
public interface GalleryImage
    extends HasInnerModel<GalleryImageInner>,
        Indexable,
        Refreshable<GalleryImage>,
        Updatable<GalleryImage.Update>,
        HasManager<ComputeManager> {
    /** @return the description of the image. */
    String description();

    /** @return the disk types not supported by the image. */
    List<DiskSkuTypes> unsupportedDiskTypes();

    /** @return a description of features not supported by the image. */
    Disallowed disallowed();

    /** @return the date indicating image's end of life. */
    OffsetDateTime endOfLifeDate();

    /** @return the image eula. */
    String eula();

    /** @return the ARM id of the image. */
    String id();

    /** @return an identifier describing publisher, offer and sku of the image. */
    GalleryImageIdentifier identifier();

    /** @return the location of the image. */
    String location();

    /** @return the image name. */
    String name();

    /** @return the OS state of the image. */
    OperatingSystemStateTypes osState();

    /** @return the image OS type. */
    OperatingSystemTypes osType();

    /** @return the uri to image privacy statement. */
    String privacyStatementUri();

    /** @return the provisioningState of image resource. */
    String provisioningState();

    /** @return the purchasePlan of the image. */
    ImagePurchasePlan purchasePlan();

    /** @return the value describing recommended configuration for a virtual machine based on this image. */
    RecommendedMachineConfiguration recommendedVirtualMachineConfiguration();

    /** @return the uri to the image release note. */
    String releaseNoteUri();

    /** @return the tags associated with the image. */
    Map<String, String> tags();

    /** @return the type value. */
    String type();

    /**
     * Retrieves information about an image version.
     *
     * @param versionName The name of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<GalleryImageVersion> getVersionAsync(String versionName);

    /**
     * Retrieves information about an image version.
     *
     * @param versionName The name of the image version.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the image version
     */
    GalleryImageVersion getVersion(String versionName);

    /**
     * List image versions.
     *
     * @return the observable for the request
     */
    PagedFlux<GalleryImageVersion> listVersionsAsync();

    /**
     * List image versions.
     *
     * @return the list of image versions
     */
    PagedIterable<GalleryImageVersion> listVersions();

    /** The entirety of the gallery image definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGallery,
            DefinitionStages.WithLocation,
            DefinitionStages.WithIdentifier,
            DefinitionStages.WithOsTypeAndState,
            DefinitionStages.WithCreate {
    }

    /** Grouping of gallery image definition stages. */
    interface DefinitionStages {
        /** The first stage of a gallery image definition. */
        interface Blank extends WithGallery {
        }

        /** The stage of the gallery image definition allowing to specify parent gallery it belongs to. */
        interface WithGallery {
            /**
             * Specifies the gallery in which this image resides.
             *
             * @param resourceGroupName The name of the resource group
             * @param galleryName The name of the gallery
             * @return the next definition stage
             */
            WithLocation withExistingGallery(String resourceGroupName, String galleryName);

            /**
             * Specifies the gallery in which this image resides.
             *
             * @param gallery the gallery
             * @return the next definition stage
             */
            WithLocation withExistingGallery(Gallery gallery);
        }

        /** The stage of the gallery image definition allowing to specify location of the image. */
        interface WithLocation {
            /**
             * Specifies location.
             *
             * @param location resource location
             * @return the next definition stage
             */
            WithIdentifier withLocation(String location);

            /**
             * Specifies location.
             *
             * @param location resource location
             * @return the next definition stage
             */
            WithIdentifier withLocation(Region location);
        }

        /**
         * The stage of the gallery image definition allowing to specify identifier that identifies publisher, offer and
         * sku of the image.
         */
        interface WithIdentifier {
            /**
             * Specifies identifier (publisher, offer and sku) for the image.
             *
             * @param identifier the identifier parameter value
             * @return the next definition stage
             */
            WithOsTypeAndState withIdentifier(GalleryImageIdentifier identifier);

            /**
             * Specifies an identifier (publisher, offer and sku) for the image.
             *
             * @param publisher image publisher name
             * @param offer image offer name
             * @param sku image sku name
             * @return the next definition stage
             */
            WithOsTypeAndState withIdentifier(String publisher, String offer, String sku);
        }

        interface WithOsTypeAndState {
            /**
             * Specifies that image is a Windows image with OS state as generalized.
             *
             * @return the next definition stage
             */
            WithCreate withGeneralizedWindows();

            /**
             * Specifies that image is a Linux image with OS state as generalized.
             *
             * @return the next definition stage
             */
            WithCreate withGeneralizedLinux();

            /**
             * Specifies that image is a Windows image.
             *
             * @param osState operating system state
             * @return the next definition stage
             */
            WithCreate withWindows(OperatingSystemStateTypes osState);

            /**
             * Specifies that image is a Linux image.
             *
             * @param osState operating system state
             * @return the next definition stage
             */
            WithCreate withLinux(OperatingSystemStateTypes osState);
        }

        /** The stage of the gallery image definition allowing to specify description. */
        interface WithDescription {
            /**
             * Specifies description.
             *
             * @param description the description of the gallery image
             * @return the next definition stage
             */
            WithCreate withDescription(String description);
        }

        /**
         * The stage of the gallery image definition allowing to specify settings disallowed for a virtual machine based
         * on the image.
         */
        interface WithDisallowed {
            /**
             * Specifies the disk type not supported by the image.
             *
             * @param diskType the disk type
             * @return the next definition stage
             */
            WithCreate withUnsupportedDiskType(DiskSkuTypes diskType);

            /**
             * Specifies the disk types not supported by the image.
             *
             * @param diskTypes the disk types
             * @return the next definition stage
             */
            WithCreate withUnsupportedDiskTypes(List<DiskSkuTypes> diskTypes);

            /**
             * Specifies disallowed settings.
             *
             * @param disallowed the disallowed settings
             * @return the next definition stage
             */
            WithCreate withDisallowed(Disallowed disallowed);
        }

        /** The stage of the gallery image definition allowing to specify end of life of the version. */
        interface WithEndOfLifeDate {
            /**
             * Specifies end of life date of the image.
             *
             * @param endOfLifeDate the end of life of the gallery image
             * @return the next definition stage
             */
            WithCreate withEndOfLifeDate(OffsetDateTime endOfLifeDate);
        }

        /** The stage of the gallery image definition allowing to specify eula. */
        interface WithEula {
            /**
             * Specifies eula.
             *
             * @param eula the Eula agreement for the gallery image
             * @return the next definition stage
             */
            WithCreate withEula(String eula);
        }

        /** The stage of the gallery image definition allowing to specify privacy statement uri. */
        interface WithPrivacyStatementUri {
            /**
             * Specifies image privacy statement uri.
             *
             * @param privacyStatementUri The privacy statement uri
             * @return the next definition stage
             */
            WithCreate withPrivacyStatementUri(String privacyStatementUri);
        }

        /** The stage of the gallery image definition allowing to specify purchase plan. */
        interface WithPurchasePlan {
            /**
             * Specifies purchase plan for this image.
             *
             * @param name plan name
             * @param publisher publisher name
             * @param product product name
             * @return the next definition stage
             */
            WithCreate withPurchasePlan(String name, String publisher, String product);

            /**
             * Specifies purchase plan for this image.
             *
             * @param purchasePlan the purchase plan
             * @return the next definition stage
             */
            WithCreate withPurchasePlan(ImagePurchasePlan purchasePlan);
        }

        /**
         * The stage of the gallery image definition allowing to specify recommended configuration for the virtual
         * machine.
         */
        interface WithRecommendedVMConfiguration {
            /**
             * Specifies the recommended minimum number of virtual CUPs for the virtual machine bases on the image.
             *
             * @param minCount the minimum number of virtual CPUs
             * @return the next definition stage
             */
            WithCreate withRecommendedMinimumCPUsCountForVirtualMachine(int minCount);

            /**
             * Specifies the recommended maximum number of virtual CUPs for the virtual machine bases on this image.
             *
             * @param maxCount the maximum number of virtual CPUs
             * @return the next definition stage
             */
            WithCreate withRecommendedMaximumCPUsCountForVirtualMachine(int maxCount);

            /**
             * Specifies the recommended virtual CUPs for the virtual machine bases on the image.
             *
             * @param minCount the minimum number of virtual CPUs
             * @param maxCount the maximum number of virtual CPUs
             * @return the next definition stage
             */
            WithCreate withRecommendedCPUsCountForVirtualMachine(int minCount, int maxCount);

            /**
             * Specifies the recommended minimum memory for the virtual machine bases on the image.
             *
             * @param minMB the minimum memory in MB
             * @return the next definition stage
             */
            WithCreate withRecommendedMinimumMemoryForVirtualMachine(int minMB);

            /**
             * Specifies the recommended maximum memory for the virtual machine bases on the image.
             *
             * @param maxMB the maximum memory in MB
             * @return the next definition stage
             */
            WithCreate withRecommendedMaximumMemoryForVirtualMachine(int maxMB);

            /**
             * Specifies the recommended memory for the virtual machine bases on the image.
             *
             * @param minMB the minimum memory in MB
             * @param maxMB the maximum memory in MB
             * @return the next definition stage
             */
            WithCreate withRecommendedMemoryForVirtualMachine(int minMB, int maxMB);

            /**
             * Specifies recommended configuration for the virtual machine based on the image.
             *
             * @param recommendedConfig the recommended configuration
             * @return the next definition stage
             */
            WithCreate withRecommendedConfigurationForVirtualMachine(RecommendedMachineConfiguration recommendedConfig);
        }

        /** The stage of the gallery image definition allowing to specify uri to release note. */
        interface WithReleaseNoteUri {
            /**
             * Specifies uri to release note.
             *
             * @param releaseNoteUri the release note uri
             * @return the next definition stage
             */
            WithCreate withReleaseNoteUri(String releaseNoteUri);
        }

        /** The stage of the gallery image definition allowing to specify tags. */
        interface WithTags {
            /**
             * Specifies tags.
             *
             * @param tags resource tags
             * @return the next definition stage
             */
            WithCreate withTags(Map<String, String> tags);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<GalleryImage>,
                DefinitionStages.WithDescription,
                DefinitionStages.WithDisallowed,
                DefinitionStages.WithEndOfLifeDate,
                DefinitionStages.WithEula,
                DefinitionStages.WithPrivacyStatementUri,
                DefinitionStages.WithPurchasePlan,
                DefinitionStages.WithRecommendedVMConfiguration,
                DefinitionStages.WithReleaseNoteUri,
                DefinitionStages.WithTags {
        }
    }
    /** The template for a gallery image update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<GalleryImage>,
            UpdateStages.WithDescription,
            UpdateStages.WithDisallowed,
            UpdateStages.WithEndOfLifeDate,
            UpdateStages.WithEula,
            UpdateStages.WithOsState,
            UpdateStages.WithPrivacyStatementUri,
            UpdateStages.WithRecommendedVMConfiguration,
            UpdateStages.WithReleaseNoteUri,
            UpdateStages.WithTags {
    }

    /** Grouping of gallery image update stages. */
    interface UpdateStages {
        /** The stage of the gallery image update allowing to specify description. */
        interface WithDescription {
            /**
             * Specifies description of the gallery image resource.
             *
             * @param description The description
             * @return the next update stage
             */
            Update withDescription(String description);
        }

        /**
         * The stage of the gallery image update allowing to specify settings disallowed for a virtual machine based on
         * the image.
         */
        interface WithDisallowed {
            /**
             * Specifies the disk type not supported by the image.
             *
             * @param diskType the disk type
             * @return the next update stage
             */
            Update withUnsupportedDiskType(DiskSkuTypes diskType);

            /**
             * Specifies the disk types not supported by the image.
             *
             * @param diskTypes the disk types
             * @return the next update stage
             */
            Update withUnsupportedDiskTypes(List<DiskSkuTypes> diskTypes);

            /**
             * Specifies the disk type should be removed from the unsupported disk type.
             *
             * @param diskType the disk type
             * @return the next update stage
             */
            Update withoutUnsupportedDiskType(DiskSkuTypes diskType);

            /**
             * Specifies disallowed settings.
             *
             * @param disallowed the disallowed settings
             * @return the next update stage
             */
            Update withDisallowed(Disallowed disallowed);
        }

        /** The stage of the gallery image update allowing to specify EndOfLifeDate. */
        interface WithEndOfLifeDate {
            /**
             * Specifies end of life date of the image.
             *
             * @param endOfLifeDate the end of life of the gallery image
             * @return the next update stage
             */
            Update withEndOfLifeDate(OffsetDateTime endOfLifeDate);
        }

        /** The stage of the gallery image update allowing to specify Eula. */
        interface WithEula {
            /**
             * Specifies eula.
             *
             * @param eula the Eula agreement for the gallery image
             * @return the next update stage
             */
            Update withEula(String eula);
        }

        /** The stage of the gallery image update allowing to specify OsState. */
        interface WithOsState {
            /**
             * Specifies osState.
             *
             * @param osState the OS State.
             * @return the next update stage
             */
            Update withOsState(OperatingSystemStateTypes osState);
        }

        /** The stage of the gallery image update allowing to specify privacy statement uri. */
        interface WithPrivacyStatementUri {
            /**
             * Specifies image privacy statement uri.
             *
             * @param privacyStatementUri the privacy statement uri
             * @return the next update stage
             */
            Update withPrivacyStatementUri(String privacyStatementUri);
        }

        /**
         * The stage of the gallery image definition allowing to specify recommended configuration for the virtual
         * machine.
         */
        interface WithRecommendedVMConfiguration {
            /**
             * Specifies the recommended minimum number of virtual CUPs for the virtual machine bases on the image.
             *
             * @param minCount the minimum number of virtual CPUs
             * @return the next update stage
             */
            Update withRecommendedMinimumCPUsCountForVirtualMachine(int minCount);

            /**
             * Specifies the recommended maximum number of virtual CUPs for the virtual machine bases on the image.
             *
             * @param maxCount the maximum number of virtual CPUs
             * @return the next update stage
             */
            Update withRecommendedMaximumCPUsCountForVirtualMachine(int maxCount);

            /**
             * Specifies the recommended virtual CUPs for the virtual machine bases on the image.
             *
             * @param minCount the minimum number of virtual CPUs
             * @param maxCount the maximum number of virtual CPUs
             * @return the next update stage
             */
            Update withRecommendedCPUsCountForVirtualMachine(int minCount, int maxCount);

            /**
             * Specifies the recommended minimum memory for the virtual machine bases on the image.
             *
             * @param minMB the minimum memory in MB
             * @return the next update stage
             */
            Update withRecommendedMinimumMemoryForVirtualMachine(int minMB);

            /**
             * Specifies the recommended maximum memory for the virtual machine bases on the image.
             *
             * @param maxMB the maximum memory in MB
             * @return the next update stage
             */
            Update withRecommendedMaximumMemoryForVirtualMachine(int maxMB);

            /**
             * Specifies the recommended virtual CUPs for the virtual machine bases on the image.
             *
             * @param minMB the minimum memory in MB
             * @param maxMB the maximum memory in MB
             * @return the next update stage
             */
            Update withRecommendedMemoryForVirtualMachine(int minMB, int maxMB);

            /**
             * Specifies recommended configuration for the virtual machine based on the image.
             *
             * @param recommendedConfig the recommended configuration
             * @return the next update stage
             */
            Update withRecommendedConfigurationForVirtualMachine(RecommendedMachineConfiguration recommendedConfig);
        }

        /** The stage of the gallery image update allowing to specify uri to release note. */
        interface WithReleaseNoteUri {
            /**
             * Specifies release note uri.
             *
             * @param releaseNoteUri the release note uri
             * @return the next update stage
             */
            Update withReleaseNoteUri(String releaseNoteUri);
        }

        /** The stage of the gallery image update allowing to specify Tags. */
        interface WithTags {
            /**
             * Specifies tags.
             *
             * @param tags resource tags
             * @return the next update stage
             */
            Update withTags(Map<String, String> tags);
        }
    }
}
