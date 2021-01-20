package com.azure.security.attestation;

import com.azure.core.util.Base64Url;
import com.nimbusds.jose.*;
import com.nimbusds.jose.util.ArrayUtils;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SecuredPolicyResetToken {
    private JWSSigner signer;
    private Base64 signingCertificate;

    SecuredPolicyResetToken(JWSSigner signer, String signingCertificateBase64) {
        this.signer = signer;
        this.signingCertificate = new Base64(signingCertificateBase64);
    }

    String serialize() throws JOSEException {
        List<Base64> certs = new ArrayList<>();
        certs.add(signingCertificate);
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(certs)
            .build();

        String signedBody = header.toBase64URL() + ".";
        Base64URL signedToken = signer.sign(header, signedBody.getBytes(StandardCharsets.UTF_8));
        return signedBody + "." + signedToken.toString();
    }
}
