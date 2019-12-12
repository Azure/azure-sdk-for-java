package com.azure.identity;

public class CliCredentialBuilder extends AadCredentialBuilderBase<CliCredentialBuilder> {

    public CliCredential build() {
        return new CliCredential(identityClientOptions);
    }
}