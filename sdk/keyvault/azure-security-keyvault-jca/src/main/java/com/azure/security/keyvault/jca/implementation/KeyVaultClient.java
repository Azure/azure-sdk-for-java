// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.model.CertificatePolicy;
import com.azure.security.keyvault.jca.implementation.model.KeyProperties;
import com.azure.security.keyvault.jca.implementation.model.SecretBundle;
import com.azure.security.keyvault.jca.implementation.model.SignResult;
import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.bouncycastle.pkcs.PKCSException;

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
import java.security.NoSuchProviderException;
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
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil.getLoginUri;
import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadCertificatesFromSecretBundleValue;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.API_VERSION_POSTFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.HTTPS_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.addTrailingSlashIfRequired;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.validateUri;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * The REST client specific to Azure Key Vault.
 */
public class KeyVaultClient {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultClient.class.getName());

    /**
     * Stores the Key Vault cloud URI.
     */
    private final String keyVaultBaseUri;

    /**
     * Stores the Azure Key Vault URI.
     */
    private final String keyVaultUri;

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
     * Stores the managed identity (either the user-assigned managed identity object ID or null if system-assigned).
     */
    private String managedIdentity;

    /**
     * Stores the token.
     */
    private AccessToken accessToken;

    /**
     * Stores a flag indicating if challenge resource verification shall be disabled.
     */
    private final boolean disableChallengeResourceVerification;


    /**
     * Constructor for authentication with user-assigned managed identity.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param managedIdentity The user-assigned managed identity object ID.
     */
    KeyVaultClient(String keyVaultUri, String managedIdentity) {
        this(keyVaultUri, null, null, null, managedIdentity, false);
    }

    /**
     * Constructor for authentication with service principal.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant ID.
     * @param clientId The client ID.
     * @param clientSecret The client secret.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret) {
        this(keyVaultUri, tenantId, clientId, clientSecret, null, false);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant ID.
     * @param clientId The client ID.
     * @param clientSecret The client secret.
     * @param managedIdentity The user-assigned managed identity object ID.
     * @param disableChallengeResourceVerification Indicates if the challenge resource verification should be disabled.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity, boolean disableChallengeResourceVerification) {

        LOGGER.log(INFO, "Using Azure Key Vault: {0}", keyVaultUri);

        this.keyVaultUri = addTrailingSlashIfRequired(validateUri(keyVaultUri, "Azure Key Vault URI"));
        // Base URI shouldn't end with a slash.
        String domainNameSuffix = Optional.of(this.keyVaultUri)
            .map(uri -> uri.split("\\.", 2)[1])
            .map(suffix -> suffix.substring(0, suffix.length() - 1))
            .orElse(null);
        this.keyVaultBaseUri = HTTPS_PREFIX + domainNameSuffix;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentity = managedIdentity;
        this.disableChallengeResourceVerification = disableChallengeResourceVerification;
    }

    public static KeyVaultClient createKeyVaultClientBySystemProperty() {
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");
        boolean disableChallengeResourceVerification =
            Boolean.parseBoolean(System.getProperty("azure.keyvault.disable-challenge-resource-verification"));

        return new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity,
            disableChallengeResourceVerification);
    }

    /**
     * Get the access token.
     *
     * @return The access token.
     */
    private String getAccessToken() {
        if (accessToken != null && !accessToken.isExpired()) {
            return accessToken.getAccessToken();
        }

        accessToken = getAccessTokenByHttpRequest();

        return accessToken.getAccessToken();
    }

    /**
     * Get the access token.
     *
     * @return The access token.
     */
    private AccessToken getAccessTokenByHttpRequest() {
        LOGGER.entering("KeyVaultClient", "getAccessTokenByHttpRequest");

        AccessToken accessToken = null;

        try {
            String resource = URLEncoder.encode(keyVaultBaseUri, "UTF-8");

            if (managedIdentity != null) {
                managedIdentity = URLEncoder.encode(managedIdentity, "UTF-8");
            }

            if (tenantId != null && clientId != null && clientSecret != null) {
                String aadAuthenticationUri = getLoginUri(keyVaultUri + "certificates" + API_VERSION_POSTFIX,
                    disableChallengeResourceVerification);
                accessToken =
                    AccessTokenUtil.getAccessToken(resource, aadAuthenticationUri, tenantId, clientId, clientSecret);
            } else {
                accessToken = AccessTokenUtil.getAccessToken(resource, managedIdentity);
            }
        } catch (Throwable t) {
            LOGGER.log(WARNING, "Could not obtain access token to authenticate with.", t);
        }

        LOGGER.exiting("KeyVaultClient", "getAccessTokenByHttpRequest", accessToken);

        return accessToken;
    }

    /**
     * Get the list of aliases.
     *
     * @return The list of aliases.
     */
    public List<String> getAliases() {
        ArrayList<String> result = new ArrayList<>();
        HashMap<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyVaultUri + "certificates" + API_VERSION_POSTFIX;

        while (uri != null && !uri.isEmpty()) {
            String response = HttpUtil.get(uri, headers);
            CertificateListResult certificateListResult = null;

            if (response != null) {
                certificateListResult =
                    (CertificateListResult) JsonConverterUtil.fromJson(response, CertificateListResult.class);
            }

            if (certificateListResult != null) {
                uri = certificateListResult.getNextLink();

                for (CertificateItem certificateItem : certificateListResult.getValue()) {
                    String id = certificateItem.getId();
                    String alias = id.substring(id.indexOf("certificates") + "certificates".length() + 1);

                    result.add(alias);
                }
            } else {
                uri = null;
            }
        }

        return result;
    }

    /**
     * Get the certificate bundle.
     *
     * @param alias The alias.
     * @return The certificate bundle.
     */
    private CertificateBundle getCertificateBundle(String alias) {
        CertificateBundle result = null;
        HashMap<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyVaultUri + "certificates/" + alias + API_VERSION_POSTFIX;
        String response = HttpUtil.get(uri, headers);

        if (response != null) {
            result = (CertificateBundle) JsonConverterUtil.fromJson(response, CertificateBundle.class);
        }

        return result;
    }

    /**
     * Get the certificate.
     *
     * @param alias The alias.
     *
     * @return The certificate, or null if not found.
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
                        new ByteArrayInputStream(Base64.getDecoder().decode(certificateString)));
                } catch (CertificateException ce) {
                    LOGGER.log(WARNING, "Certificate error", ce);
                }
            }
        }

        LOGGER.exiting("KeyVaultClient", "getCertificate", certificate);

        return certificate;
    }

    /**
     * Get the certificate chain.
     *
     * @param alias The alias.
     *
     * @return The certificate chain, or null if not found.
     */
    public Certificate[] getCertificateChain(String alias) {
        LOGGER.entering("KeyVaultClient", "getCertificateChain", alias);
        LOGGER.log(INFO, "Getting certificate chain for alias: {0}", alias);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getAccessToken());
        String uri = keyVaultUri + "secrets/" + alias + API_VERSION_POSTFIX;
        String response = HttpUtil.get(uri, headers);
        SecretBundle secretBundle = (SecretBundle) JsonConverterUtil.fromJson(response, SecretBundle.class);

        Certificate[] certificates = new Certificate[0];
        try {
            certificates = loadCertificatesFromSecretBundleValue(secretBundle.getValue());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                 | NoSuchProviderException | PKCSException e) {
            LOGGER.log(WARNING, "Unable to decode certificate chain", e);
        }
        LOGGER.exiting("KeyVaultClient", "getCertificate", alias);
        return certificates;
    }

    /**
     * Get the key.
     *
     * @param alias The alias.
     * @param password The password.
     *
     * @return The key.
     */
    public Key getKey(String alias, char[] password) {
        LOGGER.entering("KeyVaultClient", "getKey", new Object[] { alias, password });
        LOGGER.log(INFO, "Getting key for alias: {0}", alias);

        CertificateBundle certificateBundle = getCertificateBundle(alias);
        boolean isExportable = Optional.ofNullable(certificateBundle)
            .map(CertificateBundle::getPolicy)
            .map(CertificatePolicy::getKeyProperties)
            .map(KeyProperties::isExportable)
            .orElse(false);
        String keyType = Optional.ofNullable(certificateBundle)
            .map(CertificateBundle::getPolicy)
            .map(CertificatePolicy::getKeyProperties)
            .map(KeyProperties::getKty)
            .orElse(null);

        if (!isExportable) {
            // Return KeyVaultPrivateKey if certificate is not exportable because if the service needs to obtain the
            // private key for authentication, and we can't access private key(which is not exportable), we will use
            // the Azure Key Vault Secrets API to obtain the private key (keyless).
            LOGGER.exiting("KeyVaultClient", "getKey", null);

            String keyType2 = keyType.contains("-HSM") ? keyType.substring(0, keyType.indexOf("-HSM")) : keyType;

            return Optional.ofNullable(certificateBundle)
                .map(CertificateBundle::getKid)
                .map(kid -> new KeyVaultPrivateKey(keyType2, kid, this))
                .orElse(null);
        }

        String certificateSecretUri = certificateBundle.getSid();
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String body = HttpUtil.get(certificateSecretUri + API_VERSION_POSTFIX, headers);

        if (body == null) {
            // If the private key is not available the certificate cannot be used for server side certificates or mTLS.
            // Then we do not know the intent of the usage at this stage we skip this key.
            LOGGER.exiting("KeyVaultClient", "getKey", null);

            // We return null because it is really not needed.
            // The private key is only used for identity authentication.
            // If we are unable to obtain the private key, it proves that the client does not own the private key
            // (maybe due to lack of authority or other reasons).
            return null;
        }

        // If the certificate is exportable the private key is available, so we'll store the private key for
        // authentication instead of obtaining a digital signature through the API (without keyless).
        Key key = null;
        SecretBundle secretBundle = (SecretBundle) JsonConverterUtil.fromJson(body, SecretBundle.class);
        String contentType = secretBundle.getContentType();

        if ("application/x-pkcs12".equals(contentType)) {
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");

                keyStore.load(
                    new ByteArrayInputStream(Base64.getDecoder().decode(secretBundle.getValue())), "".toCharArray());

                alias = keyStore.aliases().nextElement();
                key = keyStore.getKey(alias, "".toCharArray());
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException
                | CertificateException e) {

                LOGGER.log(WARNING, "Unable to decode key", e);
            }
        } else if ("application/x-pem-file".equals(contentType)) {
            try {
                key = createPrivateKeyFromPem(secretBundle.getValue(), keyType);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException ex) {
                LOGGER.log(WARNING, "Unable to decode key", ex);
            }
        }

        // If the private key is not available the certificate cannot be
        // used for server side certificates or mTLS. Then we do not know
        // the intent of the usage at this stage we skip this key.
        LOGGER.exiting("KeyVaultClient", "getKey", key);

        return key;
    }

    /**
     * Get signature by Key Vault.
     *
     * @param digestName Digest name.
     * @param digestValue Digest value.
     * @param keyId The key ID.
     *
     * @return Signature.
     */
    public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
        SignResult result = null;
        String bodyString = String.format("{\"alg\": \"" + digestName + "\", \"value\": \"%s\"}", digestValue);
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyId + "/sign" + API_VERSION_POSTFIX;
        String response = HttpUtil.post(uri, headers, bodyString, "application/json");

        if (response != null) {
            result = (SignResult) JsonConverterUtil.fromJson(response, SignResult.class);
        }

        if (result != null) {
            return Base64.getUrlDecoder().decode(result.getValue());
        }

        return new byte[0];
    }

    /**
     * Get the private key from the PEM string.
     *
     * @param pemString The PEM file in string format.
     * @param keyType The private key type in string format.
     *
     * @return The private key.
     *
     * @throws IOException when an I/O error occurs.
     * @throws NoSuchAlgorithmException when algorithm is unavailable.
     * @throws InvalidKeySpecException when the private key cannot be generated.
     */
    private PrivateKey createPrivateKeyFromPem(String pemString, String keyType)
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
        KeyFactory factory = KeyFactory.getInstance(keyType);

        return factory.generatePrivate(spec);
    }
}
