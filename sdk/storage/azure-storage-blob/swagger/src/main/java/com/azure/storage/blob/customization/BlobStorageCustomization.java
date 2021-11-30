// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Blob Storage.
 */
public class BlobStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.blob.implementation.models");
        implementationModels.getClass("BlobHierarchyListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.blob.implementation.util.CustomHierarchicalListingDeserializer.class)");
        implementationModels.getClass("BlobPrefix").rename("BlobPrefixInternal");

        // Models customizations
        PackageCustomization models = customization.getPackage("com.azure.storage.blob.models");
        models.getClass("PageList").addAnnotation("@JsonDeserialize(using = PageListDeserializer.class)");

        ClassCustomization blobHttpHeaders = models.getClass("BlobHttpHeaders");
        blobHttpHeaders.getMethod("getContentMd5").getJavadoc().setDescription("Get the contentMd5 property: " +
            "Optional. An MD5 hash of the blob content. Note that this hash is not validated, as the hashes for " +
            "the individual blocks were validated when each was uploaded. The value does not need to be base64 " +
            "encoded as the SDK will perform the encoding.");
        blobHttpHeaders.getMethod("setContentMd5").getJavadoc().setDescription("Set the contentMd5 property: " +
            "Optional. An MD5 hash of the blob content. Note that this hash is not validated, as the hashes for " +
            "the individual blocks were validated when each was uploaded. The value does not need to be base64 " +
            "encoded as the SDK will perform the encoding.");

        ClassCustomization blobContainerEncryptionScope = models.getClass("BlobContainerEncryptionScope");
        PropertyCustomization defaultEncryptionScope = blobContainerEncryptionScope.getProperty("defaultEncryptionScope");
        defaultEncryptionScope.removeAnnotation("@JsonProperty(value = \"DefaultEncryptionScope\")")
            .addAnnotation("@JsonProperty(value = \"defaultEncryptionScope\")");
        PropertyCustomization encryptionScopeOverridePrevented = blobContainerEncryptionScope.getProperty("encryptionScopeOverridePrevented");
        encryptionScopeOverridePrevented.removeAnnotation("@JsonProperty(value = \"EncryptionScopeOverridePrevented\")")
            .addAnnotation("@JsonProperty(value = \"encryptionScopeOverridePrevented\")");
        blobContainerEncryptionScope.getMethod("isEncryptionScopeOverridePrevented").setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);

        ClassCustomization blobContainerItemProperties = models.getClass("BlobContainerItemProperties");
        blobContainerItemProperties.getMethod("isEncryptionScopeOverridePrevented").setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);
        blobContainerItemProperties.getMethod("setIsImmutableStorageWithVersioningEnabled").rename("setImmutableStorageWithVersioningEnabled");

        // Block - Generator
        ClassCustomization block = models.getClass("Block");

        block.getMethod("getSizeInt")
            .rename("getSize")
            .addAnnotation("@Deprecated")
            .setReturnType("int", "return (int) this.sizeLong; // return %s;", true)
            .getJavadoc()
            .setDeprecated("Use {@link #getSizeLong()}");

        block.getMethod("setSizeInt")
            .rename("setSize")
            .addAnnotation("@Deprecated")
            .setReturnType("Block", "return %s.setSizeLong((long) sizeInt);", true)
            .getJavadoc()
            .setDeprecated("Use {@link #setSizeLong(long)}");

        String fileName = "src/main/java/com/azure/storage/blob/models/BlobContainerItemProperties.java";
        String updatedCode = customization.getRawEditor()
            .getFileContent(fileName)
            .replace("setEncryptionScopeOverridePrevented(Boolean encryptionScopeOverridePrevented)", "setEncryptionScopeOverridePrevented(boolean encryptionScopeOverridePrevented)");

        customization.getRawEditor().removeFile(fileName);
        customization.getRawEditor().addFile(fileName, updatedCode);

        ClassCustomization listBlobsIncludeItem = models.getClass("ListBlobsIncludeItem");
        listBlobsIncludeItem.renameEnumMember("IMMUTABILITYPOLICY", "IMMUTABILITY_POLICY")
            .renameEnumMember("LEGALHOLD", "LEGAL_HOLD")
            .renameEnumMember("DELETEDWITHVERSIONS", "DELETED_WITH_VERSIONS");
    }
}
