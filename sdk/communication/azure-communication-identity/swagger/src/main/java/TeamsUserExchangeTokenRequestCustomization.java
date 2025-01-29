// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;
/**
 * Customizations for ASC Identity CTE swagger code generation.
 */
public class TeamsUserExchangeTokenRequestCustomization extends Customization {

    private static final String MODELS_PACKAGE = "com.azure.communication.identity.models";
    private static final String CLASS_NAME = "GetTokenForTeamsUserOptions";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getPackage(MODELS_PACKAGE).getClass(CLASS_NAME)
            .customizeAst(ast -> {
                // Add additional imports.
                ast.addImport("com.azure.communication.identity.CommunicationIdentityAsyncClient");
                ast.addImport("com.azure.communication.identity.CommunicationIdentityClient");
                ast.addImport("com.azure.core.annotation.Immutable");

                ast.getClassByName(CLASS_NAME).ifPresent(clazz -> {
                    // Customize the class Javadoc.
                    clazz.setJavadocComment("Options class for configuring the "
                        + "{@link CommunicationIdentityAsyncClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} "
                        + "and {@link CommunicationIdentityClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} "
                        + "methods.");

                    // Replace Fluent with Immutable
                    clazz.getAnnotationByName("Fluent").ifPresent(Node::remove);
                    clazz.addMarkerAnnotation("Immutable");

                    // Mutate the constructor to make it immutable.
                    clazz.getDefaultConstructor().ifPresent(ctor -> {
                        ctor.addParameter("String", "teamsUserAadToken");
                        ctor.addParameter("String", "clientId");
                        ctor.addParameter("String", "userObjectId");
                        ctor.setBody(StaticJavaParser.parseBlock("{ this.teamsUserAadToken = teamsUserAadToken;"
                            + "this.clientId = clientId;this.userObjectId = userObjectId; }"));
                        ctor.setJavadocComment(new Javadoc(JavadocDescription.parseText("Constructor of {@link GetTokenForTeamsUserOptions}."))
                            .addBlockTag("param", "teamsUserAadToken", "Azure AD access token of a Teams User.")
                            .addBlockTag("param", "clientId", "Client ID of an Azure AD application to be verified against the appId claim in the Azure AD access token.")
                            .addBlockTag("param", "userObjectId", "Object ID of an Azure AD user (Teams User) to be verified against the OID claim in the Azure AD access token."));
                    });

                    // Add no args constructor for stream-style serialization.
                    clazz.addConstructor(Modifier.Keyword.PRIVATE);

                    // Remove setter methods.
                    clazz.getMethodsByName("setTeamsUserAadToken").forEach(Node::remove);
                    clazz.getMethodsByName("setClientId").forEach(Node::remove);
                    clazz.getMethodsByName("setUserObjectId").forEach(Node::remove);
                });
            });
    }
}
