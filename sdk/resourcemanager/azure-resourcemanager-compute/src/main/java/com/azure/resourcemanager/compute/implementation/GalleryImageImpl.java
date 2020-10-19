// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.Disallowed;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.DiskStorageAccountTypes;
import com.azure.resourcemanager.compute.models.Gallery;
import com.azure.resourcemanager.compute.models.GalleryImage;
import com.azure.resourcemanager.compute.models.GalleryImageIdentifier;
import com.azure.resourcemanager.compute.models.GalleryImageVersion;
import com.azure.resourcemanager.compute.models.ImagePurchasePlan;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.RecommendedMachineConfiguration;
import com.azure.resourcemanager.compute.models.ResourceRange;
import com.azure.resourcemanager.compute.fluent.models.GalleryImageInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** The implementation for GalleryImage and its create and update interfaces. */
class GalleryImageImpl extends CreatableUpdatableImpl<GalleryImage, GalleryImageInner, GalleryImageImpl>
    implements GalleryImage, GalleryImage.Definition, GalleryImage.Update {
    private final ComputeManager manager;
    private String resourceGroupName;
    private String galleryName;
    private String galleryImageName;

    GalleryImageImpl(String name, ComputeManager manager) {
        super(name, new GalleryImageInner());
        this.manager = manager;
        // Set resource name
        this.galleryImageName = name;
        //
    }

    GalleryImageImpl(GalleryImageInner inner, ComputeManager manager) {
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.galleryImageName = inner.name();
        // resource ancestor names
        this.resourceGroupName = getValueFromIdByName(inner.id(), "resourceGroups");
        this.galleryName = getValueFromIdByName(inner.id(), "galleries");
        this.galleryImageName = getValueFromIdByName(inner.id(), "images");
        //
    }

    @Override
    public Mono<GalleryImageVersion> getVersionAsync(String versionName) {
        return this
            .manager()
            .galleryImageVersions()
            .getByGalleryImageAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, versionName);
    }

    @Override
    public GalleryImageVersion getVersion(String versionName) {
        return this
            .manager()
            .galleryImageVersions()
            .getByGalleryImage(this.resourceGroupName, this.galleryName, this.galleryImageName, versionName);
    }

    @Override
    public PagedFlux<GalleryImageVersion> listVersionsAsync() {
        return this
            .manager()
            .galleryImageVersions()
            .listByGalleryImageAsync(this.resourceGroupName, this.galleryName, this.galleryImageName);
    }

    @Override
    public PagedIterable<GalleryImageVersion> listVersions() {
        return this
            .manager()
            .galleryImageVersions()
            .listByGalleryImage(this.resourceGroupName, this.galleryName, this.galleryImageName);
    }

    @Override
    public ComputeManager manager() {
        return this.manager;
    }

    @Override
    public Mono<GalleryImage> createResourceAsync() {
        return manager()
            .serviceClient()
            .getGalleryImages()
            .createOrUpdateAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<GalleryImage> updateResourceAsync() {
        return manager()
            .serviceClient()
            .getGalleryImages()
            .createOrUpdateAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<GalleryImageInner> getInnerAsync() {
        return manager()
            .serviceClient()
            .getGalleryImages()
            .getAsync(this.resourceGroupName, this.galleryName, this.galleryImageName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public String description() {
        return this.innerModel().description();
    }

    @Override
    public List<DiskSkuTypes> unsupportedDiskTypes() {
        if (this.innerModel().disallowed() == null || this.innerModel().disallowed().diskTypes() == null) {
            return Collections.unmodifiableList(new ArrayList<DiskSkuTypes>());
        } else {
            List<DiskSkuTypes> diskTypes = new ArrayList<DiskSkuTypes>();
            for (String diskTypeStr : this.innerModel().disallowed().diskTypes()) {
                diskTypes.add(DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(diskTypeStr)));
            }
            return Collections.unmodifiableList(diskTypes);
        }
    }

    @Override
    public Disallowed disallowed() {
        return this.innerModel().disallowed();
    }

    @Override
    public OffsetDateTime endOfLifeDate() {
        return this.innerModel().endOfLifeDate();
    }

    @Override
    public String eula() {
        return this.innerModel().eula();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public GalleryImageIdentifier identifier() {
        return this.innerModel().identifier();
    }

    @Override
    public String location() {
        return this.innerModel().location();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public OperatingSystemStateTypes osState() {
        return this.innerModel().osState();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.innerModel().osType();
    }

    @Override
    public String privacyStatementUri() {
        return this.innerModel().privacyStatementUri();
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState().toString();
    }

    @Override
    public ImagePurchasePlan purchasePlan() {
        return this.innerModel().purchasePlan();
    }

    @Override
    public RecommendedMachineConfiguration recommendedVirtualMachineConfiguration() {
        return this.innerModel().recommended();
    }

    @Override
    public String releaseNoteUri() {
        return this.innerModel().releaseNoteUri();
    }

    @Override
    public Map<String, String> tags() {
        return this.innerModel().tags();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public GalleryImageImpl withExistingGallery(String resourceGroupName, String galleryName) {
        this.resourceGroupName = resourceGroupName;
        this.galleryName = galleryName;
        return this;
    }

    @Override
    public GalleryImageImpl withExistingGallery(Gallery gallery) {
        this.resourceGroupName = gallery.resourceGroupName();
        this.galleryName = gallery.name();
        return this;
    }

    @Override
    public GalleryImageImpl withLocation(String location) {
        this.innerModel().withLocation(location);
        return this;
    }

    @Override
    public GalleryImageImpl withLocation(Region location) {
        this.innerModel().withLocation(location.toString());
        return this;
    }

    @Override
    public GalleryImageImpl withIdentifier(GalleryImageIdentifier identifier) {
        this.innerModel().withIdentifier(identifier);
        return this;
    }

    @Override
    public GalleryImageImpl withIdentifier(String publisher, String offer, String sku) {
        this
            .innerModel()
            .withIdentifier(new GalleryImageIdentifier().withPublisher(publisher).withOffer(offer).withSku(sku));
        return this;
    }

    @Override
    public GalleryImageImpl withGeneralizedWindows() {
        return this.withWindows(OperatingSystemStateTypes.GENERALIZED);
    }

    @Override
    public GalleryImageImpl withGeneralizedLinux() {
        return this.withLinux(OperatingSystemStateTypes.GENERALIZED);
    }

    @Override
    public GalleryImageImpl withWindows(OperatingSystemStateTypes osState) {
        this.innerModel().withOsType(OperatingSystemTypes.WINDOWS).withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withLinux(OperatingSystemStateTypes osState) {
        this.innerModel().withOsType(OperatingSystemTypes.LINUX).withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withDescription(String description) {
        this.innerModel().withDescription(description);
        return this;
    }

    @Override
    public GalleryImageImpl withUnsupportedDiskType(DiskSkuTypes diskType) {
        if (this.innerModel().disallowed() == null) {
            this.innerModel().withDisallowed(new Disallowed());
        }
        if (this.innerModel().disallowed().diskTypes() == null) {
            this.innerModel().disallowed().withDiskTypes(new ArrayList<String>());
        }
        boolean found = false;
        String newDiskTypeStr = diskType.toString();
        for (String diskTypeStr : this.innerModel().disallowed().diskTypes()) {
            if (diskTypeStr.equalsIgnoreCase(newDiskTypeStr)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.innerModel().disallowed().diskTypes().add(diskType.toString());
        }
        return this;
    }

    @Override
    public GalleryImageImpl withUnsupportedDiskTypes(List<DiskSkuTypes> diskTypes) {
        if (this.innerModel().disallowed() == null) {
            this.innerModel().withDisallowed(new Disallowed());
        }
        this.innerModel().disallowed().withDiskTypes(new ArrayList<String>());
        for (DiskSkuTypes diskType : diskTypes) {
            this.innerModel().disallowed().diskTypes().add(diskType.toString());
        }
        return this;
    }

    @Override
    public GalleryImageImpl withoutUnsupportedDiskType(DiskSkuTypes diskType) {
        if (this.innerModel().disallowed() != null && this.innerModel().disallowed().diskTypes() != null) {
            int foundIndex = -1;
            int i = 0;
            String diskTypeToRemove = diskType.toString();
            for (String diskTypeStr : this.innerModel().disallowed().diskTypes()) {
                if (diskTypeStr.equalsIgnoreCase(diskTypeToRemove)) {
                    foundIndex = i;
                    break;
                }
                i++;
            }
            if (foundIndex != -1) {
                this.innerModel().disallowed().diskTypes().remove(foundIndex);
            }
        }
        return this;
    }

    @Override
    public GalleryImageImpl withDisallowed(Disallowed disallowed) {
        this.innerModel().withDisallowed(disallowed);
        return this;
    }

    @Override
    public GalleryImageImpl withEndOfLifeDate(OffsetDateTime endOfLifeDate) {
        this.innerModel().withEndOfLifeDate(endOfLifeDate);
        return this;
    }

    @Override
    public GalleryImageImpl withEula(String eula) {
        this.innerModel().withEula(eula);
        return this;
    }

    @Override
    public GalleryImageImpl withOsState(OperatingSystemStateTypes osState) {
        this.innerModel().withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withPrivacyStatementUri(String privacyStatementUri) {
        this.innerModel().withPrivacyStatementUri(privacyStatementUri);
        return this;
    }

    @Override
    public GalleryImageImpl withPurchasePlan(String name, String publisher, String product) {
        return this
            .withPurchasePlan(new ImagePurchasePlan().withName(name).withPublisher(publisher).withProduct(product));
    }

    @Override
    public GalleryImageImpl withPurchasePlan(ImagePurchasePlan purchasePlan) {
        this.innerModel().withPurchasePlan(purchasePlan);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMinimumCPUsCountForVirtualMachine(int minCount) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.innerModel().recommended().vCPUs() == null) {
            this.innerModel().recommended().withVCPUs(new ResourceRange());
        }
        this.innerModel().recommended().vCPUs().withMin(minCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMaximumCPUsCountForVirtualMachine(int maxCount) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.innerModel().recommended().vCPUs() == null) {
            this.innerModel().recommended().withVCPUs(new ResourceRange());
        }
        this.innerModel().recommended().vCPUs().withMax(maxCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedCPUsCountForVirtualMachine(int minCount, int maxCount) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        this.innerModel().recommended().withVCPUs(new ResourceRange());
        this.innerModel().recommended().vCPUs().withMin(minCount);
        this.innerModel().recommended().vCPUs().withMax(maxCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMinimumMemoryForVirtualMachine(int minMB) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.innerModel().recommended().memory() == null) {
            this.innerModel().recommended().withMemory(new ResourceRange());
        }
        this.innerModel().recommended().memory().withMin(minMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMaximumMemoryForVirtualMachine(int maxMB) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.innerModel().recommended().memory() == null) {
            this.innerModel().recommended().withMemory(new ResourceRange());
        }
        this.innerModel().recommended().memory().withMax(maxMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMemoryForVirtualMachine(int minMB, int maxMB) {
        if (this.innerModel().recommended() == null) {
            this.innerModel().withRecommended(new RecommendedMachineConfiguration());
        }
        this.innerModel().recommended().withMemory(new ResourceRange());
        this.innerModel().recommended().memory().withMin(minMB);
        this.innerModel().recommended().memory().withMax(maxMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedConfigurationForVirtualMachine(
        RecommendedMachineConfiguration recommendedConfig) {
        this.innerModel().withRecommended(recommendedConfig);
        return this;
    }

    @Override
    public GalleryImageImpl withReleaseNoteUri(String releaseNoteUri) {
        this.innerModel().withReleaseNoteUri(releaseNoteUri);
        return this;
    }

    @Override
    public GalleryImageImpl withTags(Map<String, String> tags) {
        this.innerModel().withTags(tags);
        return this;
    }

    private static String getValueFromIdByName(String id, String name) {
        if (id == null) {
            return null;
        }
        Iterable<String> iterable = Arrays.asList(id.split("/"));
        Iterator<String> itr = iterable.iterator();
        while (itr.hasNext()) {
            String part = itr.next();
            if (part != null && !part.trim().isEmpty()) {
                if (part.equalsIgnoreCase(name)) {
                    if (itr.hasNext()) {
                        return itr.next();
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
