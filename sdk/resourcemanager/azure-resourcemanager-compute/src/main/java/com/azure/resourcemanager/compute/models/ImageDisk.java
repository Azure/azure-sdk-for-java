// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.SubResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Describes a image disk.
 */
@Fluent
public class ImageDisk implements JsonSerializable<ImageDisk> {
    /*
     * The snapshot.
     */
    private SubResource snapshot;

    /*
     * The managedDisk.
     */
    private SubResource managedDisk;

    /*
     * The Virtual Hard Disk.
     */
    private String blobUri;

    /*
     * Specifies the caching requirements. Possible values are: **None,** **ReadOnly,** **ReadWrite.** The default
     * values are: **None for Standard storage. ReadOnly for Premium storage.**
     */
    private CachingTypes caching;

    /*
     * Specifies the size of empty data disks in gigabytes. This element can be used to overwrite the name of the disk
     * in a virtual machine image. This value cannot be larger than 1023 GB.
     */
    private Integer diskSizeGB;

    /*
     * Specifies the storage account type for the managed disk. NOTE: UltraSSD_LRS can only be used with data disks, it
     * cannot be used with OS Disk.
     */
    private StorageAccountTypes storageAccountType;

    /*
     * Specifies the customer managed disk encryption set resource id for the managed image disk.
     */
    private DiskEncryptionSetParameters diskEncryptionSet;

    /**
     * Creates an instance of ImageDisk class.
     */
    public ImageDisk() {
    }

    /**
     * Get the snapshot property: The snapshot.
     * 
     * @return the snapshot value.
     */
    public SubResource snapshot() {
        return this.snapshot;
    }

    /**
     * Set the snapshot property: The snapshot.
     * 
     * @param snapshot the snapshot value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withSnapshot(SubResource snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Get the managedDisk property: The managedDisk.
     * 
     * @return the managedDisk value.
     */
    public SubResource managedDisk() {
        return this.managedDisk;
    }

    /**
     * Set the managedDisk property: The managedDisk.
     * 
     * @param managedDisk the managedDisk value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withManagedDisk(SubResource managedDisk) {
        this.managedDisk = managedDisk;
        return this;
    }

    /**
     * Get the blobUri property: The Virtual Hard Disk.
     * 
     * @return the blobUri value.
     */
    public String blobUri() {
        return this.blobUri;
    }

    /**
     * Set the blobUri property: The Virtual Hard Disk.
     * 
     * @param blobUri the blobUri value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withBlobUri(String blobUri) {
        this.blobUri = blobUri;
        return this;
    }

    /**
     * Get the caching property: Specifies the caching requirements. Possible values are: **None,** **ReadOnly,**
     * **ReadWrite.** The default values are: **None for Standard storage. ReadOnly for Premium storage.**.
     * 
     * @return the caching value.
     */
    public CachingTypes caching() {
        return this.caching;
    }

    /**
     * Set the caching property: Specifies the caching requirements. Possible values are: **None,** **ReadOnly,**
     * **ReadWrite.** The default values are: **None for Standard storage. ReadOnly for Premium storage.**.
     * 
     * @param caching the caching value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withCaching(CachingTypes caching) {
        this.caching = caching;
        return this;
    }

    /**
     * Get the diskSizeGB property: Specifies the size of empty data disks in gigabytes. This element can be used to
     * overwrite the name of the disk in a virtual machine image. This value cannot be larger than 1023 GB.
     * 
     * @return the diskSizeGB value.
     */
    public Integer diskSizeGB() {
        return this.diskSizeGB;
    }

    /**
     * Set the diskSizeGB property: Specifies the size of empty data disks in gigabytes. This element can be used to
     * overwrite the name of the disk in a virtual machine image. This value cannot be larger than 1023 GB.
     * 
     * @param diskSizeGB the diskSizeGB value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withDiskSizeGB(Integer diskSizeGB) {
        this.diskSizeGB = diskSizeGB;
        return this;
    }

    /**
     * Get the storageAccountType property: Specifies the storage account type for the managed disk. NOTE: UltraSSD_LRS
     * can only be used with data disks, it cannot be used with OS Disk.
     * 
     * @return the storageAccountType value.
     */
    public StorageAccountTypes storageAccountType() {
        return this.storageAccountType;
    }

    /**
     * Set the storageAccountType property: Specifies the storage account type for the managed disk. NOTE: UltraSSD_LRS
     * can only be used with data disks, it cannot be used with OS Disk.
     * 
     * @param storageAccountType the storageAccountType value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withStorageAccountType(StorageAccountTypes storageAccountType) {
        this.storageAccountType = storageAccountType;
        return this;
    }

    /**
     * Get the diskEncryptionSet property: Specifies the customer managed disk encryption set resource id for the
     * managed image disk.
     * 
     * @return the diskEncryptionSet value.
     */
    public DiskEncryptionSetParameters diskEncryptionSet() {
        return this.diskEncryptionSet;
    }

    /**
     * Set the diskEncryptionSet property: Specifies the customer managed disk encryption set resource id for the
     * managed image disk.
     * 
     * @param diskEncryptionSet the diskEncryptionSet value to set.
     * @return the ImageDisk object itself.
     */
    public ImageDisk withDiskEncryptionSet(DiskEncryptionSetParameters diskEncryptionSet) {
        this.diskEncryptionSet = diskEncryptionSet;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (diskEncryptionSet() != null) {
            diskEncryptionSet().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("snapshot", this.snapshot);
        jsonWriter.writeJsonField("managedDisk", this.managedDisk);
        jsonWriter.writeStringField("blobUri", this.blobUri);
        jsonWriter.writeStringField("caching", this.caching == null ? null : this.caching.toString());
        jsonWriter.writeNumberField("diskSizeGB", this.diskSizeGB);
        jsonWriter.writeStringField("storageAccountType",
            this.storageAccountType == null ? null : this.storageAccountType.toString());
        jsonWriter.writeJsonField("diskEncryptionSet", this.diskEncryptionSet);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ImageDisk from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ImageDisk if the JsonReader was pointing to an instance of it, or null if it was pointing
     * to JSON null.
     * @throws IOException If an error occurs while reading the ImageDisk.
     */
    public static ImageDisk fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ImageDisk deserializedImageDisk = new ImageDisk();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("snapshot".equals(fieldName)) {
                    deserializedImageDisk.snapshot = SubResource.fromJson(reader);
                } else if ("managedDisk".equals(fieldName)) {
                    deserializedImageDisk.managedDisk = SubResource.fromJson(reader);
                } else if ("blobUri".equals(fieldName)) {
                    deserializedImageDisk.blobUri = reader.getString();
                } else if ("caching".equals(fieldName)) {
                    deserializedImageDisk.caching = CachingTypes.fromString(reader.getString());
                } else if ("diskSizeGB".equals(fieldName)) {
                    deserializedImageDisk.diskSizeGB = reader.getNullable(JsonReader::getInt);
                } else if ("storageAccountType".equals(fieldName)) {
                    deserializedImageDisk.storageAccountType = StorageAccountTypes.fromString(reader.getString());
                } else if ("diskEncryptionSet".equals(fieldName)) {
                    deserializedImageDisk.diskEncryptionSet = DiskEncryptionSetParameters.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedImageDisk;
        });
    }
}
