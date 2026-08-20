// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage profile for a certification package.
 */
@Fluent
public final class CertificationPackageStorageProfile
    implements JsonSerializable<CertificationPackageStorageProfile> {

    private CertificationPackageDiskImage osDiskImage;
    private List<CertificationPackageDiskImage> dataDiskImages = new ArrayList<>();

    /**
     * Gets the operating system disk image.
     *
     * @return the operating system disk image.
     */
    public CertificationPackageDiskImage osDiskImage() {
        return osDiskImage;
    }

    /**
     * Sets the operating system disk image.
     *
     * @param osDiskImage the operating system disk image.
     * @return this storage profile.
     */
    public CertificationPackageStorageProfile withOsDiskImage(CertificationPackageDiskImage osDiskImage) {
        this.osDiskImage = osDiskImage;
        return this;
    }

    /**
     * Gets the data disk images.
     *
     * @return the data disk images.
     */
    public List<CertificationPackageDiskImage> dataDiskImages() {
        return dataDiskImages;
    }

    /**
     * Sets the data disk images.
     *
     * @param dataDiskImages the data disk images.
     * @return this storage profile.
     */
    public CertificationPackageStorageProfile
        withDataDiskImages(List<CertificationPackageDiskImage> dataDiskImages) {
        this.dataDiskImages = dataDiskImages;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        validate();

        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("osDiskImage", osDiskImage);
        jsonWriter.writeArrayField("dataDiskImages", dataDiskImages, (writer, disk) -> writer.writeJson(disk));
        return jsonWriter.writeEndObject();
    }

    void validate() {
        if (osDiskImage == null) {
            throw new IllegalStateException(
                "parameters.certificationPackageReference.storageProfile.osDiskImage is required.");
        }
        osDiskImage.validate("parameters.certificationPackageReference.storageProfile.osDiskImage");
        if (dataDiskImages == null) {
            throw new IllegalStateException(
                "parameters.certificationPackageReference.storageProfile.dataDiskImages cannot be null.");
        }
        for (CertificationPackageDiskImage diskImage : dataDiskImages) {
            if (diskImage == null) {
                throw new IllegalStateException(
                    "parameters.certificationPackageReference.storageProfile.dataDiskImages cannot contain null.");
            }
            diskImage.validate("parameters.certificationPackageReference.storageProfile.dataDiskImages[]");
        }
    }
}
