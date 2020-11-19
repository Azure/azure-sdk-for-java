// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;


/**
 * Pojo file to store the service urls for different azure services.
 */
public class ServiceEndpoints {
    private String aadSigninUri;
    private String aadGraphApiUri;
    private String aadKeyDiscoveryUri;
    private String aadMembershipRestUri;

    public String getAadSigninUri() {
        return aadSigninUri;
    }

    public void setAadSigninUri(String aadSigninUri) {
        this.aadSigninUri = aadSigninUri;
    }

    public String getAadGraphApiUri() {
        return aadGraphApiUri;
    }

    public void setAadGraphApiUri(String aadGraphApiUri) {
        this.aadGraphApiUri = aadGraphApiUri;
    }

    public String getAadKeyDiscoveryUri() {
        return aadKeyDiscoveryUri;
    }

    public void setAadKeyDiscoveryUri(String aadKeyDiscoveryUri) {
        this.aadKeyDiscoveryUri = aadKeyDiscoveryUri;
    }

    public String getAadMembershipRestUri() {
        return aadMembershipRestUri;
    }

    public void setAadMembershipRestUri(String aadMembershipRestUri) {
        this.aadMembershipRestUri = aadMembershipRestUri;
    }
}
