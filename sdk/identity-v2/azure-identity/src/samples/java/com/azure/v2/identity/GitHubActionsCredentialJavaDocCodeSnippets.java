// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenCredential;

class GitHubActionsCredentialJavaDocCodeSnippets {
    void construct() {
        // BEGIN: com.azure.v2.identity.credential.githubactionscredential.construct
        GitHubActionsCredential credential = new GitHubActionsCredentialBuilder().build();
        // END: com.azure.v2.identity.credential.githubactionscredential.construct

        // BEGIN: readme-sample-v2-github-actions-credential
        TokenCredential gitHubCredential = new GitHubActionsCredentialBuilder().build();
        // END: readme-sample-v2-github-actions-credential

        // BEGIN: readme-sample-v2-github-actions-credential-explicit
        TokenCredential explicitCredential
            = new GitHubActionsCredentialBuilder().tenantId("<tenant-id>").clientId("<client-id>").build();
        // END: readme-sample-v2-github-actions-credential-explicit
    }
}
