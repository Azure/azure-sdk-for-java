// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Add these annotations since the default deserializer does not handle these cases correctly.
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.share.implementation.models");
        implementationModels.getClass("FilesAndDirectoriesListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.file.share.implementation.util.FilesAndDirectoriesListSegmentDeserializer.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");
        models.getClass("ShareFileRangeList").addAnnotation("@JsonDeserialize(using = ShareFileRangeListDeserializer.class)");

        // Changes to JacksonXmlRootElement for classes that have been renamed.
        changeJacksonXmlRootElementName(models.getClass("ShareMetrics"), "Metrics");
        changeJacksonXmlRootElementName(models.getClass("ShareRetentionPolicy"), "RetentionPolicy");
        changeJacksonXmlRootElementName(models.getClass("ShareSignedIdentifier"), "SignedIdentifier");
        changeJacksonXmlRootElementName(models.getClass("ShareAccessPolicy"), "AccessPolicy");

        // Replace JacksonXmlRootElement annotations that are causing a semantic breaking change.
        changeJacksonXmlRootElementName(models.getClass("ShareFileHttpHeaders"), "share-file-http-headers");
        changeJacksonXmlRootElementName(models.getClass("SourceModifiedAccessConditions"), "source-modified-access-conditions");

        ClassCustomization shareTokenIntent = models.getClass("ShareTokenIntent");
        shareTokenIntent.getJavadoc().setDescription("The request intent specifies requests that are intended for " +
            "backup/admin type operations, meaning that all file/directory ACLs are bypassed and full permissions are " +
            "granted. User must also have required RBAC permission.");
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
