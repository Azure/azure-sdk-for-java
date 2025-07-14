// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class FaceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.vision.face.administration", "FaceAdministrationClientBuilder")
            .customizeAst(ast -> ast.getClassByName("FaceAdministrationClientBuilder").ifPresent(clazz -> {
                clazz.getAnnotationByName("ServiceClientBuilder").ifPresent(Node::remove);
                clazz.addAnnotation(StaticJavaParser.parseAnnotation("@ServiceClientBuilder(serviceClients = {"
                    + "FaceAdministrationClient.class, FaceAdministrationAsyncClient.class, LargeFaceListClient.class, "
                    + "LargePersonGroupClient.class, LargeFaceListAsyncClient.class, LargePersonGroupAsyncClient.class })"));
            }));
    }
}
