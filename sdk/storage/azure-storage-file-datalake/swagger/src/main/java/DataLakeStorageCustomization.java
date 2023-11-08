// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File DataLake Storage.
 */
public class DataLakeStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.datalake.implementation.models");
        implementationModels.getClass("BlobHierarchyListSegment")
            .addAnnotation("@JsonDeserialize(using = com.azure.storage.file.datalake.implementation.util.CustomHierarchicalListingDeserializer.class)");
        changeJsonPropertyValue(implementationModels.getClass("FileSystemList"), "filesystems", "filesystems");
        changeJsonPropertyValue(implementationModels.getClass("PathList"), "paths", "paths");
        changeJsonPropertyValue(implementationModels.getClass("SetAccessControlRecursiveResponse"), "failedEntries", "failedEntries");
    }
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void changeJsonPropertyValue(ClassCustomization classCustomization, String fieldName, String jsonPropertyValue) {
        classCustomization.customizeAst(ast -> {
            AnnotationExpr annotationExpr = ast.getClassByName(classCustomization.getClassName())
                .get()
                .getFieldByName(fieldName)
                .get()
                .getAnnotationByName("JsonProperty")
                .get();

            if (annotationExpr instanceof NormalAnnotationExpr) {
                ((NormalAnnotationExpr) annotationExpr)
                    .setPairs(new NodeList<>(new MemberValuePair("value", new StringLiteralExpr(jsonPropertyValue))));
            } else {
                ((SingleMemberAnnotationExpr) annotationExpr).setMemberValue(new StringLiteralExpr(jsonPropertyValue));
            }
        });
    }
}
