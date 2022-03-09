// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Represents Azure Tools for IntelliJ IDE Plugin's authentication method details.
 */
public class IntelliJAuthMethodDetails {

    private String accountEmail;
    private String credFilePath;
    private String authMethod;
    private String azureEnv;

    /**
     * Get the account email.
     *
     * @return the account email.
     */
    public String getAccountEmail() {
        return accountEmail;
    }

    /**
     * Get the Service Principal cred file path.
     * @return the cred file path.
     */
    public String getCredFilePath() {
        return credFilePath;
    }

    /**
     * Get the auth method used by Azure Tools for IntelliJ plugin.
     *
     * @return the auth method used.
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Get the Azure env used.
     *
     * @return the Azure env used.
     */
    public String getAzureEnv() {
        return azureEnv;
    }

    /**
     * Creates an {@link IntelliJAuthMethodDetails} from the passed {@link JsonParser}.
     * <p>
     * Unknown properties are ignored and the {@code parser} isn't closed by this method.
     *
     * @param parser The {@link JsonParser} cont
     * @return A new instance of {@link IntelliJAuthMethodDetails}.
     * @throws IOException If an error occurs during deserialization.
     */
    public static IntelliJAuthMethodDetails fromParser(JsonParser parser) throws IOException {
        IntelliJAuthMethodDetails methodDetails = new IntelliJAuthMethodDetails();

        if (parser.currentToken() == null) {
            parser.nextToken();
        }

        String fieldName;
        while ((fieldName = parser.nextFieldName()) != null) {
            JsonToken field = parser.nextToken();

            // Skip sub-Objects and sub-Arrays.
            if (field.isStructStart()) {
                parser.skipChildren();
            }

            switch (fieldName) {
                case "accountEmail":
                    methodDetails.accountEmail = parser.getText();
                    break;

                case "credFilePath":
                    methodDetails.credFilePath = parser.getText();
                    break;

                case "authMethod":
                    methodDetails.authMethod = parser.getText();
                    break;

                case "azureEnv":
                    methodDetails.azureEnv = parser.getText();
                    break;

                default:
                    break;
            }
        }

        return methodDetails;
    }
}
