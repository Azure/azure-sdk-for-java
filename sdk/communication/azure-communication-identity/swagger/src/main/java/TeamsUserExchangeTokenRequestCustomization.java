// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PropertyCustomization;
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
        customizeTokenVariable(classCustomization);
        customizeAppIdVariable(classCustomization);
        customizeUserIdVariable(classCustomization);
        addConstructor(classCustomization);
    }

    private void customizeClassDesc(ClassCustomization classCustomization){
         classCustomization.getJavadoc()
                 .setDescription("Options used to exchange an AAD access token of a Teams user for a new Communication Identity access token.");
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }

    private void addConstructor(ClassCustomization classCustomization) {
        /*classCustomization.addConstructor(joinWithNewline(
                        "public GetTokenForTeamsUserOptions1(String teamsUserAadToken, String clientId, String userObjectId) {",
                        "    this.teamsUserAadToken = teamsUserAadToken;",
                        "    this.clientId = clientId;",
                        "    this.userObjectId = userObjectId;",
                        "}")); */
    }

    private void customizeTokenVariable(ClassCustomization classCustomization) {
        renameVariableName(classCustomization, "token", "teamsUserAadToken");
        customizeGetter(classCustomization,
                "getTeamsUserAadToken",
                "Gets the Azure AD access token of a Teams User.",
                "the Azure AD access token of a Teams User.");
        customizeSetter(classCustomization,
                "teamsUserAadToken",
                "token",
                "Sets the Azure AD access token of a Teams User.",
                "the Azure AD access token of a Teams User.",
                "the {@link GetTokenForTeamsUserOptions}."
                );
    }

    private void customizeAppIdVariable(ClassCustomization classCustomization) {
        renameVariableName(classCustomization, "appId", "clientId");
        customizeGetter(classCustomization,
                "getClientId",
                "Gets the Client ID of an Azure AD application.",
                "the Client ID of an Azure AD application.");
        customizeSetter(classCustomization,
                "clientId",
                "appId",
                "Sets the Client ID of an Azure AD application to be verified against the appId claim in the Azure AD access token.",
                "the Client ID of an Azure AD application.",
                "the {@link GetTokenForTeamsUserOptions}."
        );
    }

    private void customizeUserIdVariable(ClassCustomization classCustomization){
        renameVariableName(classCustomization, "userId", "userObjectId");
        customizeGetter(classCustomization,
                "getUserObjectId",
                "Gets the Object ID of an Azure AD user (Teams User).",
                "the Object ID of an Azure AD user (Teams User).");
        customizeSetter(classCustomization,
                "userObjectId",
                "userId",
                "Sets the Object ID of an Azure AD user (Teams User) to be verified against the OID claim in the Azure AD access token.",
                "the Object ID of an Azure AD user (Teams User).",
                "the {@link GetTokenForTeamsUserOptions}."
        );
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

    private void customizeSetter(ClassCustomization classCustomization, String paramName, String oldParamName, String desc, String paramDesc, String returnDesc){
        classCustomization.getMethod("set" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1))
                .replaceParameters("String " + paramName)
                .replaceBody(joinWithNewline(
                        "this." + paramName + " = " + paramName + ";",
                        "return this;"
                ))
                .getJavadoc()
                .setDescription(desc)
                .removeParam(oldParamName)
                .setParam(paramName, paramDesc)
                .setReturn(returnDesc);
    }

}
