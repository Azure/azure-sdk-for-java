// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.KeyVaultJcaPropertyNames;
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
import java.io.UnsupportedEncodingException;
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
import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.getCertificateNameFromCertificateItemId;
import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadCertificatesFromSecretBundleValue;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.API_VERSION_POSTFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.HTTPS_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.addTrailingSlashIfRequired;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.validateUri;
import static java.util.logging.Level.FINE;
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
     * Stores the provided access token string.
     */
    private final String providedAccessToken;

    /**
     * Stores the token.
     */
    private AccessToken accessToken;

    /**
     * Stores a flag indicating if challenge resource verification shall be disabled.
     */
    private final boolean disableChallengeResourceVerification;

    /**
     * Stores a flag indicating whether Authority Information Access (AIA) certificate downloads are disabled.
     */
    private final boolean disableAiaDownload;

    /**
     * Constructor for authentication with user-assigned managed identity.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param managedIdentity The user-assigned managed identity object ID.
     */
    KeyVaultClient(String keyVaultUri, String managedIdentity) {
        this(keyVaultUri, null, null, null, managedIdentity, null, false);
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
        this(keyVaultUri, tenantId, clientId, clientSecret, null, null, false);
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
        this(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, null,
            disableChallengeResourceVerification);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant ID.
     * @param clientId The client ID.
     * @param clientSecret The client secret.
     * @param managedIdentity The user-assigned managed identity object ID.
     * @param providedAccessToken The access token for authentication.
     * @param disableChallengeResourceVerification Indicates if the challenge resource verification should be disabled.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity, String providedAccessToken, boolean disableChallengeResourceVerification) {
        this(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, providedAccessToken,
            disableChallengeResourceVerification, false);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant ID.
     * @param clientId The client ID.
     * @param clientSecret The client secret.
     * @param managedIdentity The user-assigned managed identity object ID.
     * @param providedAccessToken The access token for authentication.
     * @param disableChallengeResourceVerification Indicates if the challenge resource verification should be disabled.
     * @param disableAiaDownload Indicates if AIA certificate downloads should be disabled.
     */
    public KeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity, String providedAccessToken, boolean disableChallengeResourceVerification,
        boolean disableAiaDownload) {

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
        this.providedAccessToken = providedAccessToken;
        this.disableChallengeResourceVerification = disableChallengeResourceVerification;
        this.disableAiaDownload = disableAiaDownload;
    }

    public static KeyVaultClient createKeyVaultClientBySystemProperty() {
        String keyVaultUri = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        String tenantId = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_TENANT_ID);
        String clientId = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_ID);
        String clientSecret = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_SECRET);
        String managedIdentity = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_MANAGED_IDENTITY);
        String accessToken = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_ACCESS_TOKEN);
        boolean disableChallengeResourceVerification = Boolean.parseBoolean(
            System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_DISABLE_CHALLENGE_RESOURCE_VERIFICATION));
        boolean disableAiaDownload
            = Boolean.parseBoolean(System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD));

        return new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, accessToken,
            disableChallengeResourceVerification, disableAiaDownload);
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

        accessToken = obtainAccessToken();
        if (accessToken == null) {
            LOGGER.log(WARNING, "Access token not returned.");
            return null;
        }

        return accessToken.getAccessToken();
    }

    /**
     * Obtain the access token.
     *
     * @return The access token.
     */
    private AccessToken obtainAccessToken() {
        LOGGER.entering("KeyVaultClient", "obtainAccessToken");

        AccessToken result = null;

        try {
            String resource = URLEncoder.encode(keyVaultBaseUri, "UTF-8");

            if (managedIdentity != null) {
                managedIdentity = URLEncoder.encode(managedIdentity, "UTF-8");
            }

            // Priority: 1. Service Principal (Client ID/Secret), 2. Workload Identity, 3. Managed Identity, 4. Provided Access Token
            if (tenantId != null && clientId != null && clientSecret != null) {
                LOGGER.info("Using client credentials (client ID/secret) for authentication");
                String aadAuthenticationUri = getLoginUri(keyVaultUri + "certificates" + API_VERSION_POSTFIX,
                    disableChallengeResourceVerification);
                result
                    = AccessTokenUtil.getAccessToken(resource, aadAuthenticationUri, tenantId, clientId, clientSecret);
            } else if (AccessTokenUtil.isWorkloadIdentityAvailable(clientId, tenantId)) {
                LOGGER.info("Using workload identity for authentication");
                result = AccessTokenUtil.getAccessTokenWithWorkloadIdentity(keyVaultBaseUri, tenantId, clientId);
            } else if (managedIdentity != null) {
                LOGGER.info("Using managed identity for authentication");
                result = AccessTokenUtil.getAccessToken(resource, managedIdentity);
            } else if (providedAccessToken != null && !providedAccessToken.isEmpty()) {
                LOGGER.info("Using provided access token for authentication");
                // Create an AccessToken object from the provided token string
                // Set expiration to a very large value since we cannot refresh provided tokens.
                // When the token actually expires, Azure will return authentication errors,
                // which will inform the user to provide a new token.
                result = new AccessToken(providedAccessToken, Long.MAX_VALUE / 1000);
            } else {
                LOGGER.info("Using managed identity for authentication (default)");
                result = AccessTokenUtil.getAccessToken(resource, null);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(WARNING, "Could not obtain access token to authenticate with.", e);
        }

        LOGGER.exiting("KeyVaultClient", "obtainAccessToken", result);

        return result;
    }

    /**
     * Get the list of aliases.
     *
     * @return The list of aliases.
     */
    public List<String> getAliases() {
        LOGGER.entering("KeyVaultClient", "getAliases");

        ArrayList<String> result = new ArrayList<>();
        HashMap<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyVaultUri + "certificates" + API_VERSION_POSTFIX;

        while (uri != null && !uri.isEmpty()) {
            String response = HttpUtil.get(uri, headers);
            CertificateListResult certificateListResult = null;

            if (response != null) {
                try {
                    certificateListResult = JsonConverterUtil.fromJson(CertificateListResult::fromJson, response);
                } catch (IOException e) {
                    LOGGER.log(WARNING, "Failed to parse certificate list response", e);
                }
            }

            if (certificateListResult != null) {
                uri = certificateListResult.getNextLink();
                for (CertificateItem certificateItem : certificateListResult.getValue()) {
                    String id = certificateItem.getId();
                    String alias = getCertificateNameFromCertificateItemId(id);

                    // Skip certificates that are explicitly disabled in Key Vault. Attempting to load a disabled
                    // certificate's key/secret later would fail with an HTTP 403 and break keystore initialization.
                    if (!certificateItem.isEnabled()) {
                        LOGGER.log(WARNING, "Skipping disabled certificate with alias: {0}", alias);
                        continue;
                    }

                    result.add(alias);
                }
            } else {
                uri = null;
            }
        }

        LOGGER.exiting("KeyVaultClient", "getAliases", result);

        return result;
    }

    /**
     * Get the certificate bundle.
     *
     * @param alias The alias.
     * @return The certificate bundle.
     */
    private CertificateBundle getCertificateBundle(String alias) {
        LOGGER.entering("KeyVaultClient", "getCertificateBundle", alias);

        CertificateBundle result = null;
        HashMap<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyVaultUri + "certificates/" + alias + API_VERSION_POSTFIX;
        String response = HttpUtil.get(uri, headers);

        if (response != null) {
            try {
                result = JsonConverterUtil.fromJson(CertificateBundle::fromJson, response);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse certificate bundle response", e);
            }
        }

        LOGGER.exiting("KeyVaultClient", "getCertificateBundle", result);

        return result;
    }

    /**
     * Resolves the certificate, secret, and key references for one certificate version.
     *
     * @param alias The certificate alias.
     * @return The resolved certificate version, or {@code null} if it could not be resolved.
     */
    public CertificateVersion resolveCertificateVersion(String alias) {
        CertificateBundle certificateBundle = getCertificateBundle(alias);
        if (certificateBundle == null) {
            return null;
        }

        boolean exportable = Optional.ofNullable(certificateBundle.getPolicy())
            .map(CertificatePolicy::getKeyProperties)
            .map(KeyProperties::isExportable)
            .orElse(false);
        String keyType = Optional.ofNullable(certificateBundle.getPolicy())
            .map(CertificatePolicy::getKeyProperties)
            .map(KeyProperties::getKty)
            .orElse(null);

        return new CertificateVersion(alias, certificateBundle.getCer(), certificateBundle.getKid(),
            certificateBundle.getSid(), exportable, keyType);
    }

    /**
     * Gets the certificate from the latest version of an alias.
     *
     * @param alias The alias.
     *
     * @return The certificate, or {@code null} if not found.
     */
    public Certificate getCertificate(String alias) {
        return getCertificateForVersion(resolveCertificateVersion(alias));
    }

    /**
     * Gets the certificate from a resolved certificate version.
     *
     * @param certificateVersion The resolved certificate version.
     * @return The certificate, or {@code null} if not found.
     */
    public Certificate getCertificateForVersion(CertificateVersion certificateVersion) {
        String alias = certificateVersion == null ? null : certificateVersion.getAlias();
        LOGGER.entering("KeyVaultClient", "getCertificateForVersion", alias);
        LOGGER.log(INFO, "Getting certificate for alias: {0}", alias);

        X509Certificate certificate = null;
        String certificateData = certificateVersion == null ? null : certificateVersion.getCertificateData();

        if (certificateData != null) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) certificateFactory
                    .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificateData)));
            } catch (CertificateException exception) {
                LOGGER.log(WARNING, "Unable to decode certificate", exception);
            }
        }

        LOGGER.exiting("KeyVaultClient", "getCertificateForVersion", certificate);

        return certificate;
    }

    /**
     * Gets the certificate chain from the latest version of an alias.
     *
     * @param alias The alias.
     *
    * @return The certificate chain, or an empty array if there is no resolved certificate version, no versioned
    * secret, or no certificate in the decoded secret.
    * @throws IllegalStateException If the certificate chain response cannot be loaded, parsed, or decoded.
     */
    public Certificate[] getCertificateChain(String alias) {
        return getCertificateChainForVersion(resolveCertificateVersion(alias));
    }

    /**
     * Gets the certificate chain from a resolved certificate version.
     *
     * @param certificateVersion The resolved certificate version.
    * @return The certificate chain, or an empty array if there is no resolved certificate version, no versioned
    * secret, or no certificate in the decoded secret.
    * @throws IllegalStateException If the certificate chain response cannot be loaded, parsed, or decoded.
     */
    public Certificate[] getCertificateChainForVersion(CertificateVersion certificateVersion) {
        String alias = certificateVersion == null ? null : certificateVersion.getAlias();
        LOGGER.entering("KeyVaultClient", "getCertificateChainForVersion", alias);

        if (certificateVersion == null) {
            LOGGER.log(FINE, "No resolved certificate version is available for certificate chain.");
            return new Certificate[0];
        }
        LOGGER.log(INFO, "Getting certificate chain for alias: {0}", alias);
        if (certificateVersion.getSecretId() == null) {
            LOGGER.log(FINE, "No certificate chain secret is available for alias: {0}", alias);
            return new Certificate[0];
        }

        HashMap<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String response = HttpUtil.get(certificateVersion.getSecretId() + API_VERSION_POSTFIX, headers);

        if (response == null) {
            throw new IllegalStateException("Failed to load certificate chain response for alias: " + alias);
        }

        SecretBundle secretBundle;

        try {
            secretBundle = JsonConverterUtil.fromJson(SecretBundle::fromJson, response);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse certificate chain response for alias: " + alias,
                exception);
        }

        String secretValue = secretBundle == null ? null : secretBundle.getValue();
        if (secretValue == null || secretValue.trim().isEmpty()) {
            throw new IllegalStateException("Certificate chain response has no secret value for alias: " + alias);
        }

        try {
            Certificate[] certificates = loadCertificatesFromSecretBundleValue(secretValue, disableAiaDownload);
            LOGGER.exiting("KeyVaultClient", "getCertificateChainForVersion", alias);
            return certificates;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException
            | NoSuchProviderException | PKCSException | IllegalArgumentException exception) {
            throw new IllegalStateException("Failed to decode certificate chain for alias: " + alias, exception);
        }
    }

    /**
     * Gets the key from the latest version of an alias.
     *
     * @param alias The alias.
     * @param password The password.
     *
     * @return The key, or {@code null} if not found.
     */
    public Key getKey(String alias, char[] password) {
        return getKeyForVersion(resolveCertificateVersion(alias), password);
    }

    /**
     * Gets the key from a resolved certificate version.
     *
     * @param certificateVersion The resolved certificate version.
     * @param password The password.
     * @return The key, or {@code null} if not found.
     */
    public Key getKeyForVersion(CertificateVersion certificateVersion, char[] password) {
        String alias = certificateVersion == null ? null : certificateVersion.getAlias();
        LOGGER.entering("KeyVaultClient", "getKeyForVersion", new Object[] { alias, password });
        LOGGER.log(INFO, "Getting key for alias: {0}", alias);

        if (certificateVersion == null) {
            return null;
        }

        boolean exportable = certificateVersion.isExportable();
        String keyType = certificateVersion.getKeyType();

        if (!exportable) {
            // Keyless signing uses the versioned key ID instead of exporting private key material.
            String keyAlgorithm = keyType.contains("-HSM") ? keyType.substring(0, keyType.indexOf("-HSM")) : keyType;

            KeyVaultPrivateKey key = Optional.ofNullable(certificateVersion.getKeyId())
                .map(keyId -> new KeyVaultPrivateKey(keyAlgorithm, keyId, this))
                .orElse(null);

            LOGGER.exiting("KeyVaultClient", "getKeyForVersion", key);

            return key;
        }

        String certificateSecretUri = certificateVersion.getSecretId();
        if (certificateSecretUri == null) {
            return null;
        }
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String body = HttpUtil.get(certificateSecretUri + API_VERSION_POSTFIX, headers);

        if (body == null) {
            // If the private key is not available the certificate cannot be used for server side certificates or mTLS.
            // Then we do not know the intent of the usage at this stage we skip this key.
            LOGGER.exiting("KeyVaultClient", "getKeyForVersion", null);

            // We return null because it is really not needed.
            // The private key is only used for identity authentication.
            // If we are unable to obtain the private key, it proves that the client does not own the private key
            // (maybe due to lack of authority or other reasons).
            return null;
        }

        // If the certificate is exportable the private key is available, so we'll store the private key for
        // authentication instead of obtaining a digital signature through the API (without keyless).
        Key key = null;
        SecretBundle secretBundle = null;
        String contentType = null;

        try {
            secretBundle = JsonConverterUtil.fromJson(SecretBundle::fromJson, body);
            contentType = secretBundle.getContentType();
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to parse secret bundle response.", e);
        }

        if ("application/x-pkcs12".equals(contentType)) {
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");

                keyStore.load(new ByteArrayInputStream(Base64.getDecoder().decode(secretBundle.getValue())),
                    "".toCharArray());

                alias = keyStore.aliases().nextElement();
                key = keyStore.getKey(alias, "".toCharArray());
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException
                | CertificateException e) {

                LOGGER.log(WARNING, "Unable to decode key", e);
            }
        } else if ("application/x-pem-file".equals(contentType)) {
            try {
                key = createPrivateKeyFromPem(secretBundle.getValue(), keyType);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
                LOGGER.log(WARNING, "Unable to decode key", e);
            }
        }

        // If the private key is not available the certificate cannot be used for server side certificates or mTLS.
        // Then we do not know the intent of the usage at this stage we skip this key.
        LOGGER.exiting("KeyVaultClient", "getKeyForVersion", key);

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
        LOGGER.entering("KeyVaultClient", "getSignedWithPrivateKey", new Object[] { digestName, digestValue, keyId });

        SignResult result = null;
        String bodyString = String.format("{\"alg\": \"" + digestName + "\", \"value\": \"%s\"}", digestValue);
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + getAccessToken());

        String uri = keyId + "/sign" + API_VERSION_POSTFIX;
        String response = HttpUtil.post(uri, headers, bodyString, "application/json");

        if (response != null) {
            try {
                result = JsonConverterUtil.fromJson(SignResult::fromJson, response);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse sign result response.", e);
            }
        } else {
            LOGGER.log(WARNING,
                "Can not get signature. It can be caused by missing 'sign' permission. To know how to add 'sign' permission, "
                    + "see https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-jca#key-less-certificates.");
        }

        byte[] signature;

        if (result != null) {
            signature = Base64.getUrlDecoder().decode(result.getValue());
        } else {
            signature = new byte[0];
        }

        LOGGER.exiting("KeyVaultClient", "getSignedWithPrivateKey", signature);

        return signature;
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

        // The PEM string holds the private key, so it must stay out of the log.
        LOGGER.entering("KeyVaultClient", "createPrivateKeyFromPem", keyType);

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
        PrivateKey privateKey = factory.generatePrivate(spec);

        LOGGER.exiting("KeyVaultClient", "createPrivateKeyFromPem", privateKey);

        return privateKey;
    }
}
