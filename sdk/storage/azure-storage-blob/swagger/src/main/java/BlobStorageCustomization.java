// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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

        // Models customizations
        PackageCustomization models = customization.getPackage("com.azure.storage.blob.models");

        models.getClass("PageList").customizeAst(ast -> {
            ast.addImport("com.fasterxml.jackson.databind.annotation.JsonDeserialize")
                .addImport("com.azure.storage.blob.implementation.models.PageListHelper");

            ClassOrInterfaceDeclaration clazz = ast.getClassByName("PageList").get();

            clazz.addAnnotation(StaticJavaParser.parseAnnotation("@JsonDeserialize(using = PageListDeserializer.class)"));

            clazz.getMethodsByName("getNextMarker").get(0).setModifiers(com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
            clazz.getMethodsByName("setNextMarker").get(0).setModifiers(com.github.javaparser.ast.Modifier.Keyword.PRIVATE);

            // Add Accessor to PageList
            clazz.setMembers(clazz.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(String.join("\n",
                "static {",
                "    PageListHelper.setAccessor(new PageListHelper.PageListAccessor() {",
                "        @Override",
                "        public String getNextMarker(PageList pageList) {",
                "            return pageList.getNextMarker();",
                "        }",
                "",
                "        @Override",
                "        public PageList setNextMarker(PageList pageList, String marker) {",
                "            return pageList.setNextMarker(marker);",
                "        }",
                "    });",
                "}"
            ))));
        });

        ClassCustomization blobContainerEncryptionScope = models.getClass("BlobContainerEncryptionScope");
        blobContainerEncryptionScope.getMethod("isEncryptionScopeOverridePrevented")
            .setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);

        // Changes to JacksonXmlRootElement for classes that aren't serialized to maintain backwards compatibility.
        changeJacksonXmlRootElementName(models.getClass("BlobHttpHeaders"), "blob-http-headers");
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
