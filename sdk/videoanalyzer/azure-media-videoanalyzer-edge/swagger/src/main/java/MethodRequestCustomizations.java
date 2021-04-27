// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.ClassCustomization;

public class MethodRequestCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.media.video.analyzer.edge.models"));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeMethodRequest(packageCustomization.getClass("MethodRequest"));
        customizePipelineSetRequest(packageCustomization.getClass("PipelineTopologySetRequest"));
        customizeLivePipelineSetRequest(packageCustomization.getClass("LivePipelineSetRequest"));
    }

    private void customizePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() {\n" +
                "    PipelineTopologySetRequestBody setRequestBody = new PipelineTopologySetRequestBody(this.pipelineTopology.getName());\n" +
                "    setRequestBody.setSystemData(this.pipelineTopology.getSystemData());\n" +
                "    setRequestBody.setProperties(this.pipelineTopology.getProperties());\n" +
                "    return setRequestBody.getPayloadAsJson();\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");
    }
    private void customizeLivePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() {\n" +
                "    LivePipelineSetRequestBody setRequestBody = new LivePipelineSetRequestBody(this.livePipeline.getName());\n" +
                "    setRequestBody.setSystemData(this.livePipeline.getSystemData());\n" +
                "    setRequestBody.setProperties(this.livePipeline.getProperties());\n" +
                "    return setRequestBody.getPayloadAsJson();\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");
    }
    private void customizeMethodRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() {\n" +
                "    ObjectSerializer serializer = JsonSerializerProviders.createInstance();\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
                "    serializer.serialize(outputStream, this);\n" +
                "    String payload = outputStream.toString();\n" +
                "    return payload;\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").addAnnotation("@JsonIgnore");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");


    }

}
