// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;
/**
 * Customizations for ASC Identity CTE swagger code generation.
 */
public class TeamsUserExchangeTokenRequestCustomization extends Customization {

    private static final String MODELS_PACKAGE = "com.azure.communication.identity.models";
    private static final String CLASS_NAME = "GetTokenForTeamsUserOptions";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization classCustomization = libraryCustomization.getPackage(MODELS_PACKAGE)
                .getClass("TeamsUserExchangeTokenRequest");
        classCustomization.rename(CLASS_NAME);
        classCustomization = libraryCustomization.getPackage(MODELS_PACKAGE)
                .getClass(CLASS_NAME);
        customizeClassDesc(classCustomization);
        addConstructor(classCustomization);
        customizeTokenVariable(classCustomization);
        customizeAppIdVariable(classCustomization);
        customizeUserIdVariable(classCustomization);
    }

    private void customizeClassDesc(ClassCustomization classCustomization){
         classCustomization.addImports(
                         "com.azure.communication.identity.CommunicationIdentityAsyncClient",
                         "com.azure.communication.identity.CommunicationIdentityClient")
                 .getJavadoc()
                 .setDescription("Options class for configuring the {@link CommunicationIdentityAsyncClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} and {@link CommunicationIdentityClient#getTokenForTeamsUser(GetTokenForTeamsUserOptions)} methods.");
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }

    private void addConstructor(ClassCustomization classCustomization) {
        classCustomization.addConstructor(joinWithNewline(
                        "public GetTokenForTeamsUserOptions(String teamsUserAadToken, String clientId, String userObjectId) {",
                        "    this.teamsUserAadToken = teamsUserAadToken;",
                        "    this.clientId = clientId;",
                        "    this.userObjectId = userObjectId;",
                        "}"))
                .getJavadoc()
                .setDescription("Constructor of {@link GetTokenForTeamsUserOptions}.")
                .setParam("teamsUserAadToken", "Azure AD access token of a Teams User.")
                .setParam("clientId", "Client ID of an Azure AD application to be verified against the appId claim in the Azure AD access token.")
                .setParam("userObjectId", "Object ID of an Azure AD user (Teams User) to be verified against the OID claim in the Azure AD access token.");
    }

    private void customizeTokenVariable(ClassCustomization classCustomization) {
        renameVariableName(classCustomization, "token", "teamsUserAadToken");
        customizeGetter(classCustomization,
                "getTeamsUserAadToken",
                "Gets the Azure AD access token of a Teams User.",
                "the Azure AD access token of a Teams User.");
        classCustomization.removeMethod("setTeamsUserAadToken");
    }

    private void customizeAppIdVariable(ClassCustomization classCustomization) {
        renameVariableName(classCustomization, "appId", "clientId");
        customizeGetter(classCustomization,
                "getClientId",
                "Gets the Client ID of an Azure AD application.",
                "the Client ID of an Azure AD application.");
        classCustomization.removeMethod("setClientId");
    }

    private void customizeUserIdVariable(ClassCustomization classCustomization){
        renameVariableName(classCustomization, "userId", "userObjectId");
        customizeGetter(classCustomization,
                "getUserObjectId",
                "Gets the Object ID of an Azure AD user (Teams User).",
                "the Object ID of an Azure AD user (Teams User).");
        classCustomization.removeMethod("setUserObjectId");
    }

    private void renameVariableName(ClassCustomization classCustomization, String oldName, String newName){
        classCustomization.getProperty(oldName)
                .rename(newName);
    }

    private void customizeGetter(ClassCustomization classCustomization, String getterName, String desc, String returnDesc){
        classCustomization.getMethod(getterName)
                .getJavadoc()
                .setDescription(desc)
                .setReturn(returnDesc);
    }

}
