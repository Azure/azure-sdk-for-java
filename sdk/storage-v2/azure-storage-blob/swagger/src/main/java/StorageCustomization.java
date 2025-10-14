// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Blob Storage.
 */
public class StorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.v2.storage.blob.models");

        //QueryFormat
        customizeQueryFormat(implementationModels.getClass("QueryFormat"));

        ClassCustomization serviceClient = customization.getClass("com.azure.v2.storage.blob", "ServiceClient");
        serviceClient.customizeAst(cu -> {
            cu.getClassByName("ServiceClient").ifPresent(serviceClientClass -> {
                serviceClientClass.getConstructors().forEach(constructor -> {
                    constructor.setName("StorageServiceClient");
                });
            });
        });
        String replace = serviceClient.getEditor().getFileContent(serviceClient.getFileName())
            .replace("public final class ServiceClient", "public final class StorageServiceClient");
        customization.getRawEditor().addFile(serviceClient.getFileName().replace("ServiceClient", "StorageServiceClient"), replace);
        customization.getRawEditor().removeFile(serviceClient.getFileName());

        ClassCustomization builderClass = customization.getClass("com.azure.v2.storage.blob", "AzureBlobStorageBuilder");
        String updatedBuilder = builderClass.getEditor().getFileContent(builderClass.getFileName())
            .replace(" ServiceClient", " StorageServiceClient")
            .replace("ServiceClient.class", "StorageServiceClient.class");

        customization.getRawEditor().replaceFile(builderClass.getFileName(), updatedBuilder);

    }

    private static void customizeQueryFormat(ClassCustomization classCustomization) {
        String fileContent = classCustomization.getEditor().getFileContent(classCustomization.getFileName());
        fileContent = fileContent.replace("xmlWriter.nullElement(\"ParquetTextConfiguration\", this.parquetTextConfiguration);",
                "xmlWriter.writeStartElement(\"ParquetTextConfiguration\").writeEndElement();");
        fileContent = fileContent.replace("deserializedQueryFormat.parquetTextConfiguration = reader.null;",
                "deserializedQueryFormat.parquetTextConfiguration = new Object();\nxmlReader.skipElement();");
        classCustomization.getEditor().replaceFile(classCustomization.getFileName(), fileContent);
    }
}
