// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for Digital Twins.
 */
public class DigitalTwinsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization implModels = customization.getPackage("com.azure.digitaltwins.core.implementation.models");

        customizeQuerySpecification(implModels.getClass("QuerySpecification"));
    }

    /**
     * Customization for Digital Twins {@code QuerySpecification}.
     * <p>
     * Digital Twins designed continuation tokens to use a JSON stringified value, rather than a full object type. So,
     * QuerySpecification's handling of its {@code continuationToken} field in JSON serialization needs to write the
     * value as raw JSON rather than a JSON string. If written as a JSON string it would strigify the continuation token
     * a second time, resulting in incorrect JSON being sent to the service, resulting in the operation failing.
     */
    private static void customizeQuerySpecification(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz ->
            clazz.getMethodsByName("toJson").forEach(toJson -> toJson.getBody().ifPresent(body -> {
                // Replace 'jsonWriter.writeStringField("continuationToken", this.continuationToken)' with
                // if (this.continuationToken != null) { jsonWriter.writeRawField("continuationToken", this.continuationToken); }
                // to have the continuationToken written as raw JSON as needed by the service to prevent double
                // stringify.
                String bodyString = body.toString()
                    .replace("jsonWriter.writeStringField(\"continuationToken\", this.continuationToken);",
                        "if (this.continuationToken != null) {jsonWriter.writeRawField(\"continuationToken\", this.continuationToken);}");
                toJson.setBody(StaticJavaParser.parseBlock(bodyString));
            }))));
    }
}
