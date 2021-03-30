package com.azure.media.analytics;// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;


public class MethodRequestCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.media.analytics.models"));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        //customizePipelineSetRequest(packageCustomization.getClass("PipelineTopologySetRequest"));
        //customizeMethodRequest(packageCustomization.getClass("MethodRequest"));
    }

    private void customizePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() {\n" +
                "    PipelineTopologySetRequestBody setRequestBody = new PipelineTopologySetRequestBody();\n" +
                "    setRequestBody.setName(this.pipelineTopology.getName());\n" +
                "    ObjectSerializer serializer = JsonSerializerProviders.createInstance();\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
                "    serializer.serialize(outputStream, setRequestBody);\n" +
                "    String payload = outputStream.toString(StandardCharsets.UTF_8);\n" +
                "    return payload;\n" +
                "}"
        );
    }
    private void customizeMethodRequest(ClassCustomization classCustomization) {
        /*classCustomization.addMethod(
            "public String getPayloadAsJson() {\n" +
                "    PipelineTopologySetRequestBody setRequestBody = new PipelineTopologySetRequestBody();\n" +
                "    setRequestBody.setName(this.pipelineTopology.getName());\n" +
                "    ObjectSerializer serializer = JsonSerializerProviders.createInstance();\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
                "    serializer.serialize(outputStream, setRequestBody);\n" +
                "    String payload = outputStream.toString(StandardCharsets.UTF_8);\n" +
                "    return payload;\n" +
                "}"
        );*/
    }


}
