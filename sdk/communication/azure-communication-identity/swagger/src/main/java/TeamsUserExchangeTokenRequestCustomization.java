// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
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
                ast.addImport("com.azure.communication.identity.CommunicationIdentityAsyncClient");
                ast.addImport("com.azure.communication.identity.CommunicationIdentityClient");

                ast.getClassByName(CLASS_NAME).ifPresent(clazz -> {
                    Javadoc javadoc = new Javadoc(JavadocDescription.parseText("Options class for configuring the "
                        + "{@link CommunicationIdentityAsyncClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} "
                        + "and {@link CommunicationIdentityClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} "
                        + "methods."));
                    clazz.getJavadoc().ifPresent(javadocToCopy -> javadoc.getBlockTags().addAll(javadocToCopy.getBlockTags()));

                    clazz.getAnnotationByName("Fluent").ifPresent(Node::remove);
                    clazz.addMarkerAnnotation("Immutable");

                    clazz.getDefaultConstructor().ifPresent(ctor -> {
                        ctor.setJavadocComment(new Javadoc(JavadocDescription.parseText("Constructor of {@link GetTokenForTeamsUserOptions}."))
                            .addBlockTag("param", "teamsUserAadToken", "Azure AD access token of a Teams User.")
                            .addBlockTag("param", "clientId", "Client ID of an Azure AD application to be verified against the appId claim in the Azure AD access token.")
                            .addBlockTag("param", "userObjectId", "Object ID of an Azure AD user (Teams User) to be verified against the OID claim in the Azure AD access token."));

                        ctor.addParameter("String", "teamsUserAadToken");
                        ctor.addParameter("String", "clientId");
                        ctor.addParameter("String", "userObjectId");
                    });

                    clazz.getMethodsByName("setTeamsUserAadToken").forEach(Node::remove);
                    clazz.getMethodsByName("setClientId").forEach(Node::remove);
                    clazz.getMethodsByName("setUserObjectId").forEach(Node::remove);
                });
            });
    }
}
