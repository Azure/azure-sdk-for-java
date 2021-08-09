# Azure Java Autorest config file

> see https://aka.ms/autorest

## Configuration

```yaml
title: Microsoft Azure SDK for Azure Video Analyzer on IoT Edge - edge client library for Java
description: This package contains the edge client library for Azure Video Analyzer on IoT Edge.
generate-metadata: false
license-header: MICROSOFT_MIT_SMALL
output-folder: ../
source-code-folder-path: ./src/generated
java: true
require: https://github.com/Azure/azure-rest-api-specs/blob/55b3e2d075398ec62f9322829494ff6a4323e299/specification/videoanalyzer/data-plane/readme.md
add-credentials: false
namespace: com.azure.media.videoanalyzer.edge
sync-methods: none
add-context-parameter: true
models-subpackage: models
custom-types-subpacakge: models
context-client-method-parameter: true
use: '@autorest/java@4.0.24'
model-override-setter-from-superclass: true
required-fields-as-ctor-args: true
customization-class: MethodRequestCustomizations
```

### discriminator vs default enum
```yaml
directive:
- from: AzureVideoAnalyzerSdkDefinitions.json
  where: $.definitions
  transform: >
    let definitionKeys = Object.keys($);
    for(let i = 0; i < definitionKeys.length; i++) {
      if(definitionKeys[i] === "MethodRequest") {
        $[definitionKeys[i]].required = ["@apiVersion"];
        delete $[definitionKeys[i]].properties.methodName;
        delete $[definitionKeys[i]].discriminator;
      }
      else {
        if($[definitionKeys[i]]["x-ms-discriminator-value"]) {
          let definition = $[definitionKeys[i]];
          let value = definition["x-ms-discriminator-value"];
          delete definition["x-ms-discriminator-value"];
          if(!definition.properties) {
            definition.properties = {};
          }
          definition.properties.methodName = {
            "type": "string",
            "description": "method name",
            "readOnly": true,
            "enum": [value]
          };
          if(definition.required){
            definition.required.push("methodName");
          }
          else {
            definition.required = ["methodName"];
          }
        }
      }
    }    
```

### Customization

```java
import org.slf4j.Logger;

public class MethodRequestCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.media.videoanalyzer.edge.models"));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeMethodRequest(packageCustomization.getClass("MethodRequest"));
        customizePipelineSetRequest(packageCustomization.getClass("PipelineTopologySetRequest"));
        customizeLivePipelineSetRequest(packageCustomization.getClass("LivePipelineSetRequest"));
    }

    private void customizePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() throws UnsupportedEncodingException {\n" +
                "    PipelineTopologySetRequestBody setRequestBody = new PipelineTopologySetRequestBody(this.pipelineTopology.getName());\n" +
                "    setRequestBody.setSystemData(this.pipelineTopology.getSystemData());\n" +
                "    setRequestBody.setProperties(this.pipelineTopology.getProperties());\n" +
                "    return setRequestBody.getPayloadAsJson();\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().addThrows("UnsupportedEncodingException", "UnsupportedEncodingException");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");
    }
    private void customizeLivePipelineSetRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() throws UnsupportedEncodingException {\n" +
                "    LivePipelineSetRequestBody setRequestBody = new LivePipelineSetRequestBody(this.livePipeline.getName());\n" +
                "    setRequestBody.setSystemData(this.livePipeline.getSystemData());\n" +
                "    setRequestBody.setProperties(this.livePipeline.getProperties());\n" +
                "    return setRequestBody.getPayloadAsJson();\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().addThrows("UnsupportedEncodingException", "UnsupportedEncodingException");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");
    }
    private void customizeMethodRequest(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public String getPayloadAsJson() throws UnsupportedEncodingException {\n" +
                "    ObjectSerializer serializer = JsonSerializerProviders.createInstance();\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
                "    serializer.serialize(outputStream, this);\n" +
                "    String payload = outputStream.toString(\"UTF-8\");\n" +
                "    return payload;\n" +
                "}"
        );
        classCustomization.getMethod("getPayloadAsJson").addAnnotation("@JsonIgnore");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setDescription("Get the payload as JSON: the serialized form of the request body");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().addThrows("UnsupportedEncodingException", "UnsupportedEncodingException");
        classCustomization.getMethod("getPayloadAsJson").getJavadoc().setReturn("the payload as JSON");
    }

}
```

