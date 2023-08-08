// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.GalleryArtifactVersionSource;
import com.azure.resourcemanager.compute.models.GalleryImageVersion;
import com.azure.resourcemanager.compute.models.GalleryImageVersionPublishingProfile;
import com.azure.resourcemanager.compute.models.GalleryImageVersionStorageProfile;
import com.azure.resourcemanager.compute.models.ReplicationStatus;
import com.azure.resourcemanager.compute.models.TargetRegion;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.fluent.models.GalleryImageVersionInner;
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

/** The implementation for GalleryImageVersion and its create and update interfaces. */
class GalleryImageVersionImpl
    extends CreatableUpdatableImpl<GalleryImageVersion, GalleryImageVersionInner, GalleryImageVersionImpl>
    implements GalleryImageVersion, GalleryImageVersion.Definition, GalleryImageVersion.Update {
    private final ComputeManager manager;
    private String resourceGroupName;
    private String galleryName;
    private String galleryImageName;
    private String galleryImageVersionName;

    GalleryImageVersionImpl(String name, ComputeManager manager) {
        super(name, new GalleryImageVersionInner());
        this.manager = manager;
        // Set resource name
        this.galleryImageVersionName = name;
        //
    }

    GalleryImageVersionImpl(GalleryImageVersionInner inner, ComputeManager manager) {
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.galleryImageVersionName = inner.name();
        // resource ancestor names
        this.resourceGroupName = getValueFromIdByName(inner.id(), "resourceGroups");
        this.galleryName = getValueFromIdByName(inner.id(), "galleries");
        this.galleryImageName = getValueFromIdByName(inner.id(), "images");
        this.galleryImageVersionName = getValueFromIdByName(inner.id(), "versions");
        //
    }

    @Override
    public ComputeManager manager() {
        return this.manager;
    }

    @Override
    public Mono<GalleryImageVersion> createResourceAsync() {
        return manager()
            .serviceClient()
            .getGalleryImageVersions()
            .createOrUpdateAsync(
                this.resourceGroupName,
                this.galleryName,
                this.galleryImageName,
                this.galleryImageVersionName,
                this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<GalleryImageVersion> updateResourceAsync() {
        return manager()
            .serviceClient()
            .getGalleryImageVersions()
            .createOrUpdateAsync(
                this.resourceGroupName,
                this.galleryName,
                this.galleryImageName,
                this.galleryImageVersionName,
                this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<GalleryImageVersionInner> getInnerAsync() {
        return manager()
            .serviceClient()
            .getGalleryImageVersions()
            .getAsync(this.resourceGroupName, this.galleryName, this.galleryImageName, this.galleryImageVersionName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public String id() {
        return this.innerModel().id();
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
    public String provisioningState() {
        return this.innerModel().provisioningState().toString();
    }

    @Override
    public GalleryImageVersionPublishingProfile publishingProfile() {
        return this.innerModel().publishingProfile();
    }

    @Override
    public List<TargetRegion> availableRegions() {
        List<TargetRegion> regions = new ArrayList<TargetRegion>();
        if (this.innerModel().publishingProfile() != null
            && this.innerModel().publishingProfile().targetRegions() != null) {
            for (TargetRegion targetRegion : this.innerModel().publishingProfile().targetRegions()) {
                regions
                    .add(
                        new TargetRegion()
                            .withName(targetRegion.name())
                            .withRegionalReplicaCount(targetRegion.regionalReplicaCount()));
            }
        }
        return Collections.unmodifiableList(regions);
    }

    @Override
    public OffsetDateTime endOfLifeDate() {
        if (this.innerModel().publishingProfile() != null) {
            return this.innerModel().publishingProfile().endOfLifeDate();
        } else {
            return null;
        }
    }

    @Override
    public Boolean isExcludedFromLatest() {
        if (this.innerModel().publishingProfile() != null) {
            return this.innerModel().publishingProfile().excludeFromLatest();
        } else {
            return false;
        }
    }

    @Override
    public ReplicationStatus replicationStatus() {
        return this.innerModel().replicationStatus();
    }

    @Override
    public GalleryImageVersionStorageProfile storageProfile() {
        return this.innerModel().storageProfile();
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
    public GalleryImageVersionImpl withExistingImage(
        String resourceGroupName, String galleryName, String galleryImageName) {
        this.resourceGroupName = resourceGroupName;
        this.galleryName = galleryName;
        this.galleryImageName = galleryImageName;
        return this;
    }

    @Override
    public GalleryImageVersionImpl withLocation(String location) {
        this.innerModel().withLocation(location);
        return this;
    }

    @Override
    public DefinitionStages.WithSource withLocation(Region location) {
        this.innerModel().withLocation(location.toString());
        return this;
    }

    @Override
    public GalleryImageVersionImpl withSourceCustomImage(String customImageId) {
        if (this.innerModel().storageProfile() == null) {
            this.innerModel().withStorageProfile(new GalleryImageVersionStorageProfile());
        }
        if (this.innerModel().storageProfile().source() == null) {
            this.innerModel().storageProfile().withSource(new GalleryArtifactVersionSource());
        }
        this.innerModel().storageProfile().source().withId(customImageId);
        return this;
    }

    @Override
    public GalleryImageVersionImpl withSourceCustomImage(VirtualMachineCustomImage customImage) {
        return this.withSourceCustomImage(customImage.id());
    }

    @Override
    public GalleryImageVersionImpl withRegionAvailability(Region region, int replicaCount) {
        if (this.innerModel().publishingProfile() == null) {
            this.innerModel().withPublishingProfile(new GalleryImageVersionPublishingProfile());
        }
        if (this.innerModel().publishingProfile().targetRegions() == null) {
            this.innerModel().publishingProfile().withTargetRegions(new ArrayList<>());
        }

        boolean found = false;
        String newRegionName = region.toString();
        String newRegionNameTrimmed = newRegionName.replaceAll("\\s", "");
        for (TargetRegion targetRegion : this.innerModel().publishingProfile().targetRegions()) {
            String regionStr = targetRegion.name();
            String regionStrTrimmed = regionStr.replaceAll("\\s", "");
            if (regionStrTrimmed.equalsIgnoreCase(newRegionNameTrimmed)) {
                targetRegion.withRegionalReplicaCount(replicaCount); // Update existing
                found = true;
                break;
            }
        }
        if (!found) {
            TargetRegion targetRegion =
                new TargetRegion().withName(newRegionName).withRegionalReplicaCount(replicaCount);
            this.innerModel().publishingProfile().targetRegions().add(targetRegion);
        }
        //
        // Gallery image version publishing profile regions list must contain the location of image version.
        //
        found = false;
        String locationTrimmed = this.location().replaceAll("\\s", "");
        for (TargetRegion targetRegion : this.innerModel().publishingProfile().targetRegions()) {
            String regionStr = targetRegion.name();
            String regionStrTrimmed = regionStr.replaceAll("\\s", "");
            if (regionStrTrimmed.equalsIgnoreCase(locationTrimmed)) {
                found = true;
                break;
            }
        }
        if (!found) {
            TargetRegion defaultTargetRegion =
                new TargetRegion()
                    .withName(this.location())
                    .withRegionalReplicaCount(null); // null means default where service default to 1 replica
            this.innerModel().publishingProfile().targetRegions().add(defaultTargetRegion);
        }
        //
        return this;
    }

    @Override
    public GalleryImageVersionImpl withRegionAvailability(List<TargetRegion> targetRegions) {
        if (this.innerModel().publishingProfile() == null) {
            this.innerModel().withPublishingProfile(new GalleryImageVersionPublishingProfile());
        }
        this.innerModel().publishingProfile().withTargetRegions(targetRegions);
        //
        // Gallery image version publishing profile regions list must contain the location of image version.
        //
        boolean found = false;
        String locationTrimmed = this.location().replaceAll("\\s", "");
        for (TargetRegion targetRegion : this.innerModel().publishingProfile().targetRegions()) {
            String regionStr = targetRegion.name();
            String regionStrTrimmed = regionStr.replaceAll("\\s", "");
            if (regionStrTrimmed.equalsIgnoreCase(locationTrimmed)) {
                found = true;
                break;
            }
        }
        if (!found) {
            TargetRegion defaultTargetRegion =
                new TargetRegion()
                    .withName(this.location())
                    .withRegionalReplicaCount(null); // null means default where service default to 1 replica
            this.innerModel().publishingProfile().targetRegions().add(defaultTargetRegion);
        }
        //
        return this;
    }

    @Override
    public Update withoutRegionAvailability(Region region) {
        if (this.innerModel().publishingProfile() != null
            && this.innerModel().publishingProfile().targetRegions() != null) {
            int foundIndex = -1;
            int i = 0;
            String regionNameToRemove = region.toString();
            String regionNameToRemoveTrimmed = regionNameToRemove.replaceAll("\\s", "");

            for (TargetRegion targetRegion : this.innerModel().publishingProfile().targetRegions()) {
                String regionStr = targetRegion.name();
                String regionStrTrimmed = regionStr.replaceAll("\\s", "");
                if (regionStrTrimmed.equalsIgnoreCase(regionNameToRemoveTrimmed)) {
                    foundIndex = i;
                    break;
                }
                i++;
            }
            if (foundIndex != -1) {
                this.innerModel().publishingProfile().targetRegions().remove(foundIndex);
            }
        }
        return this;
    }

    @Override
    public GalleryImageVersionImpl withEndOfLifeDate(OffsetDateTime endOfLifeDate) {
        if (this.innerModel().publishingProfile() == null) {
            this.innerModel().withPublishingProfile(new GalleryImageVersionPublishingProfile());
        }
        this.innerModel().publishingProfile().withEndOfLifeDate(endOfLifeDate);
        return this;
    }

    @Override
    public GalleryImageVersionImpl withExcludedFromLatest() {
        if (this.innerModel().publishingProfile() == null) {
            this.innerModel().withPublishingProfile(new GalleryImageVersionPublishingProfile());
        }
        this.innerModel().publishingProfile().withExcludeFromLatest(true);
        return this;
    }

    @Override
    public GalleryImageVersionImpl withoutExcludedFromLatest() {
        if (this.innerModel().publishingProfile() == null) {
            this.innerModel().withPublishingProfile(new GalleryImageVersionPublishingProfile());
        }
        this.innerModel().publishingProfile().withExcludeFromLatest(false);
        return this;
    }

    @Override
    public GalleryImageVersionImpl withTags(Map<String, String> tags) {
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
