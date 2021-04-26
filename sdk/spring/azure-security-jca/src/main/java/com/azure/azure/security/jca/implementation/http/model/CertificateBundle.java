// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.azure.security.jca.implementation.http.model;

import java.io.Serializable;

public class CertificateBundle implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String cer;
    private String kid;
    private CertificatePolicy policy;
    private String sid;

    public String getCer() {
        return cer;
    }

    public String getKid() {
        return kid;
    }

    public CertificatePolicy getPolicy() {
        return policy;
    }

    public String getSid() {
        return sid;
    }

    public void setCer(String cer) {
        this.cer = cer;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public void setPolicy(CertificatePolicy policy) {
        this.policy = policy;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
