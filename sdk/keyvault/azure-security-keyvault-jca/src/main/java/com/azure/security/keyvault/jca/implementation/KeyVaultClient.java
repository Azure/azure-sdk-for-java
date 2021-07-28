// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.model.CertificatePolicy;
import com.azure.security.keyvault.jca.implementation.model.KeyProperties;
import com.azure.security.keyvault.jca.implementation.model.SecretBundle;
import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The REST client specific to Azure Key Vault.
 */
public class KeyVaultClient {

    public static final String KEY_VAULT_BASE_URI_GLOBAL = "https://vault.azure.net";
    public static final String KEY_VAULT_BASE_URI_CN = "https://vault.azure.cn";
    public static final String KEY_VAULT_BASE_URI_US = "https://vault.usgovcloudapi.net";
    public static final String KEY_VAULT_BASE_URI_DE = "https://vault.microsoftazure.de";
    public static final String AAD_LOGIN_URI_GLOBAL = "https://login.microsoftonline.com/";
    public static final String AAD_LOGIN_URI_CN = "https://login.partner.microsoftonline.cn/";
    public static final String AAD_LOGIN_URI_US = "https://login.microsoftonline.us/";
    public static final String AAD_LOGIN_URI_DE = "https://login.microsoftonline.de/";

    private static final Logger LOGGER = Logger.getLogger(KeyVaultClient.class.getName());
    private static final String HTTPS_PREFIX = "https://";
    private static final String API_VERSION_POSTFIX = "?api-version=7.1";

    public static String getAADLoginURIByKeyVaultBaseUri(String keyVaultBaseUri) {
        String aadAuthenticationUrl;
        switch (keyVaultBaseUri) {
            case KEY_VAULT_BASE_URI_GLOBAL :
                aadAuthenticationUrl = AAD_LOGIN_URI_GLOBAL;
                break;
            case KEY_VAULT_BASE_URI_CN :
                aadAuthenticationUrl = AAD_LOGIN_URI_CN;
                break;
            case KEY_VAULT_BASE_URI_US :
                aadAuthenticationUrl = AAD_LOGIN_URI_US;
                break;
            case KEY_VAULT_BASE_URI_DE:
                aadAuthenticationUrl = AAD_LOGIN_URI_DE;
                break;
            default:
                throw new IllegalArgumentException("Property of azure.keyvault.uri is illegal.");
        }
        return aadAuthenticationUrl;
    }

    /**
     * Stores the Key Vault cloud URI.
     */
    private final String keyVaultBaseUri;

    /**
     * Stores the Azure Key Vault URL.
     */
    private final String keyVaultUrl;

    /**
     * Stores the AAD authentication URL (or null to default to Azure Public Cloud).
     */
    private final String aadAuthenticationUrl;

    /**
     * Stores the tenant ID.
     */
    private final String tenantId;

    /**
     * Stores the client ID.
     */
    private final String clientId;

    /**
     * Stores the client secret.
     */
    private final String clientSecret;

    /**
     * Stores the managed identity (either the user-assigned managed identity object ID or null if system-assigned)
     */
    private String managedIdentity;

    /**
     * Constructor for authentication with user-assigned managed identity.
     *
     * @param keyVaultUri the Azure Key Vault URI.
     * @param managedIdentity the user-assigned managed identity object ID.
     */
    KeyVaultClient(String keyVaultUri, String managedIdentity) {
        this(keyVaultUri, null, null, null, managedIdentity);
    }

    /**
     * Constructor for authentication with service principal.
     *
     * @param keyVaultUri the Azure Key Vault URI.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret) {
        this(keyVaultUri, tenantId, clientId, clientSecret, null);
    }


    /**
     * Constructor.
     *
     * @param keyVaultUri the Azure Key Vault URI.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     * @param managedIdentity the user-assigned managed identity object ID.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret, String managedIdentity) {
        LOGGER.log(INFO, "Using Azure Key Vault: {0}", keyVaultUri);
        if (!keyVaultUri.endsWith("/")) {
            keyVaultUri = keyVaultUri + "/";
        }
        this.keyVaultUrl = keyVaultUri;
        // Base Uri shouldn't end with a slash.
        String domainNameSuffix = Optional.of(keyVaultUri)
                                          .map(uri -> uri.split("\\.", 2)[1])
                                          .map(suffix -> suffix.substring(0, suffix.length() - 1))
                                          .orElse(null);
        keyVaultBaseUri = HTTPS_PREFIX + domainNameSuffix;
        aadAuthenticationUrl = getAADLoginURIByKeyVaultBaseUri(keyVaultBaseUri);

        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentity = managedIdentity;
    }

    /**
     * Get the access token.
     *
     * @return the access token.
     */
    private String getAccessToken() {
        LOGGER.entering("KeyVaultClient", "getAccessToken");
        String accessToken = null;
        try {
            String resource = URLEncoder.encode(keyVaultBaseUri, "UTF-8");
            if (managedIdentity != null) {
                managedIdentity = URLEncoder.encode(managedIdentity, "UTF-8");
            }

            if (tenantId != null && clientId != null && clientSecret != null) {
                accessToken = AccessTokenUtil.getAccessToken(resource, aadAuthenticationUrl, tenantId, clientId,
                    clientSecret);
            } else {
                accessToken = AccessTokenUtil.getAccessToken(resource, managedIdentity);
            }
        } catch (Throwable throwable) {
            LOGGER.log(WARNING, "Unsupported encoding or missing Httpclient", throwable);
        }
        LOGGER.exiting("KeyVaultClient", "getAccessToken", accessToken);
        return accessToken;
    }

    /**
     * Get the list of aliases.
     *
     * @return the list of aliases.
     */
    public List<String> getAliases() {
        ArrayList<String> result = new ArrayList<>();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getAccessToken());
        String url = String.format("%scertificates%s", keyVaultUrl, API_VERSION_POSTFIX);

        while (url != null && url.length() != 0) {
            String response = HttpUtil.get(url, headers);
            CertificateListResult certificateListResult = null;
            if (response != null) {
                certificateListResult = (CertificateListResult) JsonConverterUtil.fromJson(response,
                    CertificateListResult.class);
            } else {
                url = null;
            }
            if (certificateListResult != null && certificateListResult.getValue().size() > 0) {
                for (CertificateItem certificateItem : certificateListResult.getValue()) {
                    String id = certificateItem.getId();
                    String alias = id.substring(id.indexOf("certificates") + "certificates".length() + 1);
                    result.add(alias);
                }
                url = certificateListResult.getNextLink();
            }

        }
        return result;
    }

    /**
     * Get the certificate bundle.
     *
     * @param alias the alias.
     * @return the certificate bundle.
     */
    private CertificateBundle getCertificateBundle(String alias) {
        CertificateBundle result = null;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getAccessToken());
        String url = String.format("%scertificates/%s%s", keyVaultUrl, alias, API_VERSION_POSTFIX);
        String response = HttpUtil.get(url, headers);
        if (response != null) {
            result = (CertificateBundle) JsonConverterUtil.fromJson(response, CertificateBundle.class);
        }
        return result;
    }

    /**
     * Get the certificate.
     *
     * @param alias the alias.
     * @return the certificate, or null if not found.
     */
    public Certificate getCertificate(String alias) {
        LOGGER.entering("KeyVaultClient", "getCertificate", alias);
        LOGGER.log(INFO, "Getting certificate for alias: {0}", alias);
        X509Certificate certificate = null;
        CertificateBundle certificateBundle = getCertificateBundle(alias);
        if (certificateBundle != null) {
            String certificateString = certificateBundle.getCer();
            if (certificateString != null) {
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    certificate = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(Base64.getDecoder().decode(certificateString))
                    );
                } catch (CertificateException ce) {
                    LOGGER.log(WARNING, "Certificate error", ce);
                }
            }
        }
        LOGGER.exiting("KeyVaultClient", "getCertificate", certificate);
        return certificate;
    }

    /**
     * Get the key.
     *
     * @param alias the alias.
     * @param password the password.
     * @return the key.
     */
    public Key getKey(String alias, char[] password) {
        LOGGER.entering("KeyVaultClient", "getKey", new Object[] { alias, password });
        LOGGER.log(INFO, "Getting key for alias: {0}", alias);
        Key key = null;
        CertificateBundle certificateBundle = getCertificateBundle(alias);
        boolean isExportable = Optional.ofNullable(certificateBundle)
                                       .map(CertificateBundle::getPolicy)
                                       .map(CertificatePolicy::getKeyProperties)
                                       .map(KeyProperties::isExportable)
                                       .orElse(false);
        if (isExportable) {
            //
            // Because the certificate is exportable the private key is
            // available. So we'll use the Azure Key Vault Secrets API to get
            // the private key.
            //
            String certificateSecretUri = certificateBundle.getSid();
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + getAccessToken());
            String body = HttpUtil.get(certificateSecretUri + API_VERSION_POSTFIX, headers);
            if (body != null) {
                SecretBundle secretBundle = (SecretBundle) JsonConverterUtil.fromJson(body, SecretBundle.class);
                if (secretBundle.getContentType().equals("application/x-pkcs12")) {
                    try {
                        KeyStore keyStore = KeyStore.getInstance("PKCS12");
                        keyStore.load(
                            new ByteArrayInputStream(Base64.getDecoder().decode(secretBundle.getValue())),
                            "".toCharArray()
                        );
                        alias = keyStore.aliases().nextElement();
                        key = keyStore.getKey(alias, "".toCharArray());
                    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex) {
                        LOGGER.log(WARNING, "Unable to decode key", ex);
                    }
                }
                if (secretBundle.getContentType().equals("application/x-pem-file")) {
                    try {
                        key = createPrivateKeyFromPem(secretBundle.getValue());
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException ex) {
                        LOGGER.log(WARNING, "Unable to decode key", ex);
                    }
                }
            }
        }

        //
        // If the private key is not available the certificate cannot be
        // used for server side certificates or mTLS. Then we do not know
        // the intent of the usage at this stage we skip this key.
        //
        LOGGER.exiting("KeyVaultClient", "getKey", key);
        return key;
    }

    /**
     * Get the private key from the PEM string.
     *
     * @param pemString the PEM file in string format.
     * @return the private key
     * @throws IOException when an I/O error occurs.
     * @throws NoSuchAlgorithmException when algorithm is unavailable.
     * @throws InvalidKeySpecException when the private key cannot be generated.
     */
    private PrivateKey createPrivateKeyFromPem(String pemString)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(pemString))) {
            String line = reader.readLine();
            if (line == null || !line.contains("BEGIN PRIVATE KEY")) {
                throw new IllegalArgumentException("No PRIVATE KEY found");
            }
            line = "";
            while (line != null) {
                if (line.contains("END PRIVATE KEY")) {
                    break;
                }
                builder.append(line);
                line = reader.readLine();
            }
        }
        byte[] bytes = Base64.getDecoder().decode(builder.toString());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    String getKeyVaultBaseUri() {
        return keyVaultBaseUri;
    }

    String getAadAuthenticationUrl() {
        return aadAuthenticationUrl;
    }
}
