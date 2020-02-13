/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "ServiceEndpoints{" +
            "aadSigninUri='" + aadSigninUri + '\'' +
            ", aadGraphApiUri='" + aadGraphApiUri + '\'' +
            ", aadKeyDiscoveryUri='" + aadKeyDiscoveryUri + '\'' +
            ", aadMembershipRestUri='" + aadMembershipRestUri + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEndpoints that = (ServiceEndpoints) o;
        return Objects.equals(aadSigninUri, that.aadSigninUri) &&
            Objects.equals(aadGraphApiUri, that.aadGraphApiUri) &&
            Objects.equals(aadKeyDiscoveryUri, that.aadKeyDiscoveryUri) &&
            Objects.equals(aadMembershipRestUri, that.aadMembershipRestUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aadSigninUri, aadGraphApiUri, aadKeyDiscoveryUri, aadMembershipRestUri);
    }
}
