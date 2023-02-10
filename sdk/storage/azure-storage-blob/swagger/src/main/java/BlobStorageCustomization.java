// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
        changeJacksonXmlRootElementName(blobHttpHeaders, "blob-http-headers");
        changeJacksonXmlRootElementName(blobContainerEncryptionScope, "blob-container-encryption-scope");
        changeJacksonXmlRootElementName(models.getClass("CpkInfo"), "cpk-info");

        // Changes to JacksonXmlRootElement for classes that have been renamed.
        changeJacksonXmlRootElementName(models.getClass("BlobMetrics"), "Metrics");
        changeJacksonXmlRootElementName(models.getClass("BlobAnalyticsLogging"), "Logging");
        changeJacksonXmlRootElementName(models.getClass("BlobRetentionPolicy"), "RetentionPolicy");
        changeJacksonXmlRootElementName(models.getClass("BlobServiceStatistics"), "StorageServiceStats");
        changeJacksonXmlRootElementName(models.getClass("BlobSignedIdentifier"), "SignedIdentifier");
        changeJacksonXmlRootElementName(models.getClass("BlobAccessPolicy"), "AccessPolicy");

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

        // BlobErrorCode
        // Fix typo
        String blobErrorCodeFile = "src/main/java/com/azure/storage/blob/models/BlobErrorCode.java";
        String blobErrorCodeFileContent = customization.getRawEditor().getFileContent(blobErrorCodeFile);
        blobErrorCodeFileContent = blobErrorCodeFileContent.replaceAll("SnaphotOperationRateExceeded", "SnapshotOperationRateExceeded");
        customization.getRawEditor().replaceFile(blobErrorCodeFile, blobErrorCodeFileContent);
        // deprecate
        ClassCustomization blobErrorCode = models.getClass("BlobErrorCode");
        blobErrorCode.getConstant("SNAPHOT_OPERATION_RATE_EXCEEDED")
            .addAnnotation("@Deprecated")
            .getJavadoc()
            .setDeprecated("Please use {@link BlobErrorCode#SNAPSHOT_OPERATION_RATE_EXCEEDED}");

        blobErrorCode.getConstant("INCREMENTAL_COPY_OF_ERALIER_VERSION_SNAPSHOT_NOT_ALLOWED")
            .addAnnotation("@Deprecated")
            .getJavadoc()
            .setDeprecated("Please use {@link BlobErrorCode#INCREMENTAL_COPY_OF_EARLIER_VERSION_SNAPSHOT_NOT_ALLOWED}");

    }

    /*
     * Uses ClassCustomization.customizeAst to replace the 'localName' value of the JacksonXmlRootElement instead of
     * the previous implementation which removed the JacksonXmlRootElement then added it back with the updated
     * 'localName'. The previous implementation would occasionally run into an issue where the JacksonXmlRootElement
     * import wouldn't be added back, causing a failure in CI when validating that code generation was up-to-date.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void changeJacksonXmlRootElementName(ClassCustomization classCustomization, String rootElementName) {
        classCustomization.customizeAst(ast -> ast.getClassByName(classCustomization.getClassName()).get()
            .getAnnotationByName("JacksonXmlRootElement").get()
            .asNormalAnnotationExpr()
            .setPairs(new NodeList<>(new MemberValuePair("localName", new StringLiteralExpr(rootElementName)))));
    }
}
