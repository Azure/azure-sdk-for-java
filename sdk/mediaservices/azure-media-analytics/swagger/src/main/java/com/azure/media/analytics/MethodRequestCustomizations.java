package com.azure.media.analytics;// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;


public class MethodRequestCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.media.analytics.models"));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizePipelineSetRequest(packageCustomization.getClass("PipelineTopologySetRequest"));
    }

    private void customizePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "/**\n" +
                "public String getPayloadAsJson() {\n" +
                "    PipelineTopologySetRequestBody setRequestBody = new PipelineTopologySetRequestBody();\n" +
                "    setRequestBody.setName(this.pipelineTopology.getName());\n" +
                "    setRequestBody.setSystemData(this.systemData);\n" +
                "    setRequestBody.setProperties(this.properties);\n" +
                "    JsonSerializer serializer = JsonSerializerProviders.createInstance();\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
                "    serializer.serialize(outputStream, setRequestBody);\n" +
                "    String payload = outputStream.toString(StandardCharsets.UTF_8);\n" +
                "    return payload;\n" +
                "}"
        );
    }
}
