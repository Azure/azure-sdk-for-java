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
import com.azure.resourcemanager.compute.fluent.inner.GalleryImageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
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
            .inner()
            .getGalleryImages()
            .createOrUpdateAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<GalleryImage> updateResourceAsync() {
        return manager()
            .inner()
            .getGalleryImages()
            .createOrUpdateAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<GalleryImageInner> getInnerAsync() {
        return manager()
            .inner()
            .getGalleryImages()
            .getAsync(this.resourceGroupName, this.galleryName, this.galleryImageName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    public List<DiskSkuTypes> unsupportedDiskTypes() {
        if (this.inner().disallowed() == null || this.inner().disallowed().diskTypes() == null) {
            return Collections.unmodifiableList(new ArrayList<DiskSkuTypes>());
        } else {
            List<DiskSkuTypes> diskTypes = new ArrayList<DiskSkuTypes>();
            for (String diskTypeStr : this.inner().disallowed().diskTypes()) {
                diskTypes.add(DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(diskTypeStr)));
            }
            return Collections.unmodifiableList(diskTypes);
        }
    }

    @Override
    public Disallowed disallowed() {
        return this.inner().disallowed();
    }

    @Override
    public OffsetDateTime endOfLifeDate() {
        return this.inner().endOfLifeDate();
    }

    @Override
    public String eula() {
        return this.inner().eula();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public GalleryImageIdentifier identifier() {
        return this.inner().identifier();
    }

    @Override
    public String location() {
        return this.inner().location();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public OperatingSystemStateTypes osState() {
        return this.inner().osState();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().osType();
    }

    @Override
    public String privacyStatementUri() {
        return this.inner().privacyStatementUri();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState().toString();
    }

    @Override
    public ImagePurchasePlan purchasePlan() {
        return this.inner().purchasePlan();
    }

    @Override
    public RecommendedMachineConfiguration recommendedVirtualMachineConfiguration() {
        return this.inner().recommended();
    }

    @Override
    public String releaseNoteUri() {
        return this.inner().releaseNoteUri();
    }

    @Override
    public Map<String, String> tags() {
        return this.inner().tags();
    }

    @Override
    public String type() {
        return this.inner().type();
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
        this.inner().withLocation(location);
        return this;
    }

    @Override
    public GalleryImageImpl withLocation(Region location) {
        this.inner().withLocation(location.toString());
        return this;
    }

    @Override
    public GalleryImageImpl withIdentifier(GalleryImageIdentifier identifier) {
        this.inner().withIdentifier(identifier);
        return this;
    }

    @Override
    public GalleryImageImpl withIdentifier(String publisher, String offer, String sku) {
        this
            .inner()
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
        this.inner().withOsType(OperatingSystemTypes.WINDOWS).withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withLinux(OperatingSystemStateTypes osState) {
        this.inner().withOsType(OperatingSystemTypes.LINUX).withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withDescription(String description) {
        this.inner().withDescription(description);
        return this;
    }

    @Override
    public GalleryImageImpl withUnsupportedDiskType(DiskSkuTypes diskType) {
        if (this.inner().disallowed() == null) {
            this.inner().withDisallowed(new Disallowed());
        }
        if (this.inner().disallowed().diskTypes() == null) {
            this.inner().disallowed().withDiskTypes(new ArrayList<String>());
        }
        boolean found = false;
        String newDiskTypeStr = diskType.toString();
        for (String diskTypeStr : this.inner().disallowed().diskTypes()) {
            if (diskTypeStr.equalsIgnoreCase(newDiskTypeStr)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.inner().disallowed().diskTypes().add(diskType.toString());
        }
        return this;
    }

    @Override
    public GalleryImageImpl withUnsupportedDiskTypes(List<DiskSkuTypes> diskTypes) {
        if (this.inner().disallowed() == null) {
            this.inner().withDisallowed(new Disallowed());
        }
        this.inner().disallowed().withDiskTypes(new ArrayList<String>());
        for (DiskSkuTypes diskType : diskTypes) {
            this.inner().disallowed().diskTypes().add(diskType.toString());
        }
        return this;
    }

    @Override
    public GalleryImageImpl withoutUnsupportedDiskType(DiskSkuTypes diskType) {
        if (this.inner().disallowed() != null && this.inner().disallowed().diskTypes() != null) {
            int foundIndex = -1;
            int i = 0;
            String diskTypeToRemove = diskType.toString();
            for (String diskTypeStr : this.inner().disallowed().diskTypes()) {
                if (diskTypeStr.equalsIgnoreCase(diskTypeToRemove)) {
                    foundIndex = i;
                    break;
                }
                i++;
            }
            if (foundIndex != -1) {
                this.inner().disallowed().diskTypes().remove(foundIndex);
            }
        }
        return this;
    }

    @Override
    public GalleryImageImpl withDisallowed(Disallowed disallowed) {
        this.inner().withDisallowed(disallowed);
        return this;
    }

    @Override
    public GalleryImageImpl withEndOfLifeDate(OffsetDateTime endOfLifeDate) {
        this.inner().withEndOfLifeDate(endOfLifeDate);
        return this;
    }

    @Override
    public GalleryImageImpl withEula(String eula) {
        this.inner().withEula(eula);
        return this;
    }

    @Override
    public GalleryImageImpl withOsState(OperatingSystemStateTypes osState) {
        this.inner().withOsState(osState);
        return this;
    }

    @Override
    public GalleryImageImpl withPrivacyStatementUri(String privacyStatementUri) {
        this.inner().withPrivacyStatementUri(privacyStatementUri);
        return this;
    }

    @Override
    public GalleryImageImpl withPurchasePlan(String name, String publisher, String product) {
        return this
            .withPurchasePlan(new ImagePurchasePlan().withName(name).withPublisher(publisher).withProduct(product));
    }

    @Override
    public GalleryImageImpl withPurchasePlan(ImagePurchasePlan purchasePlan) {
        this.inner().withPurchasePlan(purchasePlan);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMinimumCPUsCountForVirtualMachine(int minCount) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.inner().recommended().vCPUs() == null) {
            this.inner().recommended().withVCPUs(new ResourceRange());
        }
        this.inner().recommended().vCPUs().withMin(minCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMaximumCPUsCountForVirtualMachine(int maxCount) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.inner().recommended().vCPUs() == null) {
            this.inner().recommended().withVCPUs(new ResourceRange());
        }
        this.inner().recommended().vCPUs().withMax(maxCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedCPUsCountForVirtualMachine(int minCount, int maxCount) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        this.inner().recommended().withVCPUs(new ResourceRange());
        this.inner().recommended().vCPUs().withMin(minCount);
        this.inner().recommended().vCPUs().withMax(maxCount);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMinimumMemoryForVirtualMachine(int minMB) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.inner().recommended().memory() == null) {
            this.inner().recommended().withMemory(new ResourceRange());
        }
        this.inner().recommended().memory().withMin(minMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMaximumMemoryForVirtualMachine(int maxMB) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        if (this.inner().recommended().memory() == null) {
            this.inner().recommended().withMemory(new ResourceRange());
        }
        this.inner().recommended().memory().withMax(maxMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedMemoryForVirtualMachine(int minMB, int maxMB) {
        if (this.inner().recommended() == null) {
            this.inner().withRecommended(new RecommendedMachineConfiguration());
        }
        this.inner().recommended().withMemory(new ResourceRange());
        this.inner().recommended().memory().withMin(minMB);
        this.inner().recommended().memory().withMax(maxMB);
        return this;
    }

    @Override
    public GalleryImageImpl withRecommendedConfigurationForVirtualMachine(
        RecommendedMachineConfiguration recommendedConfig) {
        this.inner().withRecommended(recommendedConfig);
        return this;
    }

    @Override
    public GalleryImageImpl withReleaseNoteUri(String releaseNoteUri) {
        this.inner().withReleaseNoteUri(releaseNoteUri);
        return this;
    }

    @Override
    public GalleryImageImpl withTags(Map<String, String> tags) {
        this.inner().withTags(tags);
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
