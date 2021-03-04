// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

public class SearchServiceCustomizations extends Customization {
    private static final String MODELS = "com.azure.search.documents.indexes.models";
    private static final String SEARCH_FIELD_DATA_TYPE = "SearchFieldDataType";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchFieldDataType(packageCustomization.getClass(SEARCH_FIELD_DATA_TYPE));
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {\n" +
            "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));\n" +
            "}")
            .addAnnotation("@JsonCreator")
            .getJavadoc()
            .setDescription("Returns a collection of a specific SearchFieldDataType")
            .setParam("dataType", "the corresponding SearchFieldDataType")
            .setReturn("a Collection of the corresponding SearchFieldDataType");
    }
}
