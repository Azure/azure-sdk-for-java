// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;

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
        models.getClass("PageList").getMethod("getNextMarker").setModifier(Modifier.PRIVATE);
        models.getClass("PageList").getMethod("setNextMarker").setModifier(Modifier.PRIVATE);

        // Add Accessor to PageList
        String pageListFileName = "src/main/java/com/azure/storage/blob/models/PageList.java";

        String fileContent = customization.getRawEditor().getFileContent(pageListFileName);
        int startImportIndex = fileContent.indexOf("import com.azure.core.annotation.Fluent;") + 40;
        int startStaticIndex = fileContent.indexOf("class PageList {") + 16;
        String updatedFileContent = fileContent.substring(0, startImportIndex)
            + "import com.azure.storage.blob.implementation.models.PageListHelper;"
            + fileContent.substring(startImportIndex, startStaticIndex)
            + "static {\n"
            + "        PageListHelper.setAccessor(new PageListHelper.PageListAccessor() {\n"
            + "            @Override\n"
            + "            public String getNextMarker(PageList pageList) {\n"
            + "                return pageList.getNextMarker();\n"
            + "            }\n"
            + "\n"
            + "            @Override\n"
            + "            public PageList setNextMarker(PageList pageList, String marker) {\n"
            + "                return pageList.setNextMarker(marker);\n"
            + "            }\n"
            + "        });\n"
            + "    } "
            + fileContent.substring(startStaticIndex);

        customization.getRawEditor().removeFile(pageListFileName);
        customization.getRawEditor().addFile(pageListFileName, updatedFileContent);

        models.getClass("BlobCopySourceTags").rename("BlobCopySourceTagsMode");


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
        blobContainerEncryptionScope.getMethod("isEncryptionScopeOverridePrevented")
            .setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);

        // Changes to JacksonXmlRootElement for classes that aren't serialized to maintain backwards compatibility.
        blobHttpHeaders.removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"blob-http-headers\")");

        blobContainerEncryptionScope.removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"blob-container-encryption-scope\")");

        models.getClass("CpkInfo").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"cpk-info\")");


        // Changes to JacksonXmlRootElement for classes that have been renamed.
        models.getClass("BlobMetrics").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"Metrics\")");

        models.getClass("BlobAnalyticsLogging").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"Logging\")");

        models.getClass("BlobRetentionPolicy").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"RetentionPolicy\")");

        models.getClass("BlobServiceStatistics").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"StorageServiceStats\")");

        models.getClass("BlobSignedIdentifier").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"SignedIdentifier\")");

        models.getClass("BlobAccessPolicy").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"AccessPolicy\")");

        ClassCustomization blobContainerItemProperties = models.getClass("BlobContainerItemProperties");
        blobContainerItemProperties.getMethod("isEncryptionScopeOverridePrevented")
            .setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);
        blobContainerItemProperties.getMethod("setIsImmutableStorageWithVersioningEnabled")
            .rename("setImmutableStorageWithVersioningEnabled");
        blobContainerItemProperties.getMethod("setEncryptionScopeOverridePrevented")
            .replaceParameters("boolean encryptionScopeOverridePrevented");

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

        ClassCustomization listBlobsIncludeItem = models.getClass("ListBlobsIncludeItem");
        listBlobsIncludeItem.renameEnumMember("IMMUTABILITYPOLICY", "IMMUTABILITY_POLICY")
            .renameEnumMember("LEGALHOLD", "LEGAL_HOLD")
            .renameEnumMember("DELETEDWITHVERSIONS", "DELETED_WITH_VERSIONS");
    }
}
