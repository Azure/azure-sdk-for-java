// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class QueueStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");

        // Changes to JacksonXmlRootElement for classes that have been renamed.
        changeJacksonXmlRootElementName(models.getClass("QueueMetrics"), "Metrics");
        changeJacksonXmlRootElementName(models.getClass("QueueAnalyticsLogging"), "Logging");
        changeJacksonXmlRootElementName(models.getClass("QueueRetentionPolicy"), "RetentionPolicy");
        changeJacksonXmlRootElementName(models.getClass("QueueServiceStatistics"), "StorageServiceStats");
        changeJacksonXmlRootElementName(models.getClass("QueueSignedIdentifier"), "SignedIdentifier");
        changeJacksonXmlRootElementName(models.getClass("QueueAccessPolicy"), "AccessPolicy");
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
