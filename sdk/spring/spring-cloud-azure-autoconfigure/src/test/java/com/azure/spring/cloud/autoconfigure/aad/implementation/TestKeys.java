// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public final class TestKeys {

    public static final KeyFactory KF;
    static {
        try {
            KF = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // @formatter:off
    public static final String DEFAULT_RSA_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3FlqJr5TRskIQIgdE3Dd"
            + "7D9lboWdcTUT8a+fJR7MAvQm7XXNoYkm3v7MQL1NYtDvL2l8CAnc0WdSTINU6IRv"
            + "c5Kqo2Q4csNX9SHOmEfzoROjQqahEcve1jBXluoCXdYuYpx4/1tfRgG6ii4Uhxh6"
            + "iI8qNMJQX+fLfqhbfYfxBQVRPywBkAbIP4x1EAsbC6FSNmkhCxiMNqEgxaIpY8C2"
            + "kJdJ/ZIV+WW4noDdzpKqHcwmB8FsrumlVY/DNVvUSDIipiq9PbP4H99TXN1o746o"
            + "RaNa07rq1hoCgMSSy+85SagCoxlmyE+D+of9SsMY8Ol9t0rdzpobBuhyJ/o5dfvj"
            + "KwIDAQAB";
    // @formatter:on

    public static final RSAPublicKey DEFAULT_PUBLIC_KEY;
    static {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(DEFAULT_RSA_PUBLIC_KEY));
        try {
            DEFAULT_PUBLIC_KEY = (RSAPublicKey) KF.generatePublic(spec);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // @formatter:off
    public static final String DEFAULT_RSA_PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDcWWomvlNGyQhA"
            + "iB0TcN3sP2VuhZ1xNRPxr58lHswC9Cbtdc2hiSbe/sxAvU1i0O8vaXwICdzRZ1JM"
            + "g1TohG9zkqqjZDhyw1f1Ic6YR/OhE6NCpqERy97WMFeW6gJd1i5inHj/W19GAbqK"
            + "LhSHGHqIjyo0wlBf58t+qFt9h/EFBVE/LAGQBsg/jHUQCxsLoVI2aSELGIw2oSDF"
            + "oiljwLaQl0n9khX5ZbiegN3OkqodzCYHwWyu6aVVj8M1W9RIMiKmKr09s/gf31Nc"
            + "3WjvjqhFo1rTuurWGgKAxJLL7zlJqAKjGWbIT4P6h/1Kwxjw6X23St3OmhsG6HIn"
            + "+jl1++MrAgMBAAECggEBAMf820wop3pyUOwI3aLcaH7YFx5VZMzvqJdNlvpg1jbE"
            + "E2Sn66b1zPLNfOIxLcBG8x8r9Ody1Bi2Vsqc0/5o3KKfdgHvnxAB3Z3dPh2WCDek"
            + "lCOVClEVoLzziTuuTdGO5/CWJXdWHcVzIjPxmK34eJXioiLaTYqN3XKqKMdpD0ZG"
            + "mtNTGvGf+9fQ4i94t0WqIxpMpGt7NM4RHy3+Onggev0zLiDANC23mWrTsUgect/7"
            + "62TYg8g1bKwLAb9wCBT+BiOuCc2wrArRLOJgUkj/F4/gtrR9ima34SvWUyoUaKA0"
            + "bi4YBX9l8oJwFGHbU9uFGEMnH0T/V0KtIB7qetReywkCgYEA9cFyfBIQrYISV/OA"
            + "+Z0bo3vh2aL0QgKrSXZ924cLt7itQAHNZ2ya+e3JRlTczi5mnWfjPWZ6eJB/8MlH"
            + "Gpn12o/POEkU+XjZZSPe1RWGt5g0S3lWqyx9toCS9ACXcN9tGbaqcFSVI73zVTRA"
            + "8J9grR0fbGn7jaTlTX2tnlOTQ60CgYEA5YjYpEq4L8UUMFkuj+BsS3u0oEBnzuHd"
            + "I9LEHmN+CMPosvabQu5wkJXLuqo2TxRnAznsA8R3pCLkdPGoWMCiWRAsCn979TdY"
            + "QbqO2qvBAD2Q19GtY7lIu6C35/enQWzJUMQE3WW0OvjLzZ0l/9mA2FBRR+3F9A1d"
            + "rBdnmv0c3TcCgYEAi2i+ggVZcqPbtgrLOk5WVGo9F1GqUBvlgNn30WWNTx4zIaEk"
            + "HSxtyaOLTxtq2odV7Kr3LGiKxwPpn/T+Ief+oIp92YcTn+VfJVGw4Z3BezqbR8lA"
            + "Uf/+HF5ZfpMrVXtZD4Igs3I33Duv4sCuqhEvLWTc44pHifVloozNxYfRfU0CgYBN"
            + "HXa7a6cJ1Yp829l62QlJKtx6Ymj95oAnQu5Ez2ROiZMqXRO4nucOjGUP55Orac1a"
            + "FiGm+mC/skFS0MWgW8evaHGDbWU180wheQ35hW6oKAb7myRHtr4q20ouEtQMdQIF"
            + "snV39G1iyqeeAsf7dxWElydXpRi2b68i3BIgzhzebQKBgQCdUQuTsqV9y/JFpu6H"
            + "c5TVvhG/ubfBspI5DhQqIGijnVBzFT//UfIYMSKJo75qqBEyP2EJSmCsunWsAFsM"
            + "TszuiGTkrKcZy9G0wJqPztZZl2F2+bJgnA6nBEV7g5PA4Af+QSmaIhRwqGDAuROR"
            + "47jndeyIaMTNETEmOnms+as17g==";
    // @formatter:on

    public static final RSAPrivateKey DEFAULT_PRIVATE_KEY;
    static {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(DEFAULT_RSA_PRIVATE_KEY));
        try {
            DEFAULT_PRIVATE_KEY = (RSAPrivateKey) KF.generatePrivate(spec);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // @formatter:off
    public static final String DEFAULT_CERTIFICATE_KEY = "-----BEGIN CERTIFICATE-----\n"
        + "MIIDzzCCAregAwIBAgIUEc1Q6X2u3x6iGQ7RjVeDJIjQ1XMwDQYJKoZIhvcNAQEL\n"
        + "BQAwdzELMAkGA1UEBhMCR0IxDzANBgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9u\n"
        + "ZG9uMRgwFgYDVQQKDA9HbG9iYWwgU2VjdXJpdHkxFjAUBgNVBAsMDUlUIERlcGFy\n"
        + "dG1lbnQxFDASBgNVBAMMC2V4YW1wbGUuY29tMB4XDTIyMDUzMDA5NDE1N1oXDTIz\n"
        + "MDUzMDA5NDE1N1owdzELMAkGA1UEBhMCR0IxDzANBgNVBAgMBkxvbmRvbjEPMA0G\n"
        + "A1UEBwwGTG9uZG9uMRgwFgYDVQQKDA9HbG9iYWwgU2VjdXJpdHkxFjAUBgNVBAsM\n"
        + "DUlUIERlcGFydG1lbnQxFDASBgNVBAMMC2V4YW1wbGUuY29tMIIBIjANBgkqhkiG\n"
        + "9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3OG2pj0NhQhpZhajw4i4Viq4ys8rjqMSn8uF\n"
        + "eMlpYflRCSBrWi3/+ll+th/vdQRoE/N2hq6oMIoZ1oAXsXEeCIcw2vJrU+2Fd0N8\n"
        + "D9CFQIHatFUQsJmIhih7bv7DzFTpHOVU7tzzs4WVDnqJfMsMHCGh+oAx1nBEN5LS\n"
        + "1nsQoDBw32fW6mcQnmD+aWByeb9rHpSO2+E1XotRYnSzsMJY2tuLyIqH8647fXq+\n"
        + "9K/olDgtGsha+TDJ1CX6UGS28zQ3BCaj7h54+y7LqOtFdw8ICn6Hoj39mNEtVlxC\n"
        + "4hUFLLvDphMFC7/+WNuYCq9iIr7xJCj6c+1mTLNduZ30UuEk1QIDAQABo1MwUTAd\n"
        + "BgNVHQ4EFgQUjFN264nAPC1KW4zs408p8gizoQ0wHwYDVR0jBBgwFoAUjFN264nA\n"
        + "PC1KW4zs408p8gizoQ0wDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOC\n"
        + "AQEAAX3VT+cMzihRo/2n0z1SHvY3Ozv+l7C4S709EmvdZBc4KaOzm8kJ+MTohmg9\n"
        + "8TmJMtnBLcOiiEyd2qXZkD64vIfuWNsSjGDy/YBEqNNuwX+8LDbKbqJyeb07E+Hb\n"
        + "63c+nogfCGgGXxuuRODMmh1rFprx60owNaNXYAE/K0DljOg8onYqSidVb1gIdmKN\n"
        + "8EUJPDpryEo4Zt95ruAPrnoWcLvjUlKDjrnvjnAOy1wkAtywLcQbmYWXrYQojNuJ\n"
        + "0DGXu1zSGh8dLo96tlvWYRVE4MzPQvRjNDhuShlw3c9+gTEandAODQ5Kj0wL1q0i\n"
        + "F051Y7HdrnfZ/mK58D4VPm+EbQ==\n"
        + "-----END CERTIFICATE-----";
    // @formatter:on

    public static final X509Certificate DEFAULT_CERTIFICATE;
    static {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream cert = new ByteArrayInputStream(DEFAULT_CERTIFICATE_KEY.getBytes(StandardCharsets.UTF_8));
            DEFAULT_CERTIFICATE = (X509Certificate) certFactory.generateCertificate(cert);
        } catch (CertificateException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private TestKeys() {
    }

}
