package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

public class VsCodeCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {

    /**
     * Creates a new {@link DeviceCodeCredential} with the current configurations.
     *
     * @return a {@link DeviceCodeCredential} with the current configurations.
     */
    public VsCodeCredential build() {
        return new VsCodeCredential(identityClientOptions);
    }
}
