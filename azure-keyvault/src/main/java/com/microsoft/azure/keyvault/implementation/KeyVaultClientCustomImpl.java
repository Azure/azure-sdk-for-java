package com.microsoft.azure.keyvault.implementation;

import com.google.common.base.Joiner;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.CertificateIdentifier;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.KeyVaultClientCustom;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.*;
import com.microsoft.azure.keyvault.requests.*;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

public class KeyVaultClientCustomImpl extends KeyVaultClientBaseImpl implements KeyVaultClientCustom {

    private KeyVaultClientService service;
    private AzureClient azureClient;

    public KeyVaultClientCustomImpl(ServiceClientCredentials credentials) {
        super(credentials);
    }

    public KeyVaultClientCustomImpl(RestClient restClient) {
        super(restClient);
    }

    public void initializeService() {
        service = restClient().retrofit().create(KeyVaultClientService.class);
    }

    @Override
    public OkHttpClient httpClient() {
        return super.httpClient();
    }

    @Override
    public SerializerAdapter<?> serializerAdapter() {
        return super.serializerAdapter();
    }

    /**
     * The interface defining all the services for KeyVaultClient to be
     * used by Retrofit to perform actually REST calls.
     */
    interface KeyVaultClientService {
        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient createKey"})
        @POST("keys/{key-name}/create")
        Observable<Response<ResponseBody>> createKey(@Path("key-name") String keyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyCreateParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient importKey"})
        @PUT("keys/{key-name}")
        Observable<Response<ResponseBody>> importKey(@Path("key-name") String keyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyImportParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteKey"})
        @HTTP(path = "keys/{key-name}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteKey(@Path("key-name") String keyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateKey"})
        @PATCH("keys/{key-name}/{key-version}")
        Observable<Response<ResponseBody>> updateKey(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyUpdateParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getKey"})
        @GET("keys/{key-name}/{key-version}")
        Observable<Response<ResponseBody>> getKey(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getKeyVersions"})
        @GET("keys/{key-name}/versions")
        Observable<Response<ResponseBody>> getKeyVersions(@Path("key-name") String keyName, @Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getKeys"})
        @GET("keys")
        Observable<Response<ResponseBody>> getKeys(@Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient backupKey"})
        @POST("keys/{key-name}/backup")
        Observable<Response<ResponseBody>> backupKey(@Path("key-name") String keyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient restoreKey"})
        @POST("keys/restore")
        Observable<Response<ResponseBody>> restoreKey(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyRestoreParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient encrypt"})
        @POST("keys/{key-name}/{key-version}/encrypt")
        Observable<Response<ResponseBody>> encrypt(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyOperationsParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient decrypt"})
        @POST("keys/{key-name}/{key-version}/decrypt")
        Observable<Response<ResponseBody>> decrypt(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyOperationsParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient sign"})
        @POST("keys/{key-name}/{key-version}/sign")
        Observable<Response<ResponseBody>> sign(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeySignParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient verify"})
        @POST("keys/{key-name}/{key-version}/verify")
        Observable<Response<ResponseBody>> verify(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyVerifyParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient wrapKey"})
        @POST("keys/{key-name}/{key-version}/wrapkey")
        Observable<Response<ResponseBody>> wrapKey(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyOperationsParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient unwrapKey"})
        @POST("keys/{key-name}/{key-version}/unwrapkey")
        Observable<Response<ResponseBody>> unwrapKey(@Path("key-name") String keyName, @Path("key-version") String keyVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body KeyOperationsParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient setSecret"})
        @PUT("secrets/{secret-name}")
        Observable<Response<ResponseBody>> setSecret(@Path("secret-name") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body SecretSetParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteSecret"})
        @HTTP(path = "secrets/{secret-name}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteSecret(@Path("secret-name") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateSecret"})
        @PATCH("secrets/{secret-name}/{secret-version}")
        Observable<Response<ResponseBody>> updateSecret(@Path("secret-name") String secretName, @Path("secret-version") String secretVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body SecretUpdateParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getSecret"})
        @GET("secrets/{secret-name}/{secret-version}")
        Observable<Response<ResponseBody>> getSecret(@Path("secret-name") String secretName, @Path("secret-version") String secretVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getSecrets"})
        @GET("secrets")
        Observable<Response<ResponseBody>> getSecrets(@Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getSecretVersions"})
        @GET("secrets/{secret-name}/versions")
        Observable<Response<ResponseBody>> getSecretVersions(@Path("secret-name") String secretName, @Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificates"})
        @GET("certificates")
        Observable<Response<ResponseBody>> getCertificates(@Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteCertificate"})
        @HTTP(path = "certificates/{certificate-name}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteCertificate(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient setCertificateContacts"})
        @PUT("certificates/contacts")
        Observable<Response<ResponseBody>> setCertificateContacts(@Body Contacts contacts, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateContacts"})
        @GET("certificates/contacts")
        Observable<Response<ResponseBody>> getCertificateContacts(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteCertificateContacts"})
        @HTTP(path = "certificates/contacts", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteCertificateContacts(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateIssuers"})
        @GET("certificates/issuers")
        Observable<Response<ResponseBody>> getCertificateIssuers(@Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient setCertificateIssuer"})
        @PUT("certificates/issuers/{issuer-name}")
        Observable<Response<ResponseBody>> setCertificateIssuer(@Path("issuer-name") String issuerName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateIssuerSetParameters parameter, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateCertificateIssuer"})
        @PATCH("certificates/issuers/{issuer-name}")
        Observable<Response<ResponseBody>> updateCertificateIssuer(@Path("issuer-name") String issuerName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateIssuerUpdateParameters parameter, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateIssuer"})
        @GET("certificates/issuers/{issuer-name}")
        Observable<Response<ResponseBody>> getCertificateIssuer(@Path("issuer-name") String issuerName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteCertificateIssuer"})
        @HTTP(path = "certificates/issuers/{issuer-name}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteCertificateIssuer(@Path("issuer-name") String issuerName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient createCertificate"})
        @POST("certificates/{certificate-name}/create")
        Observable<Response<ResponseBody>> createCertificate(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateCreateParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient importCertificate"})
        @POST("certificates/{certificate-name}/import")
        Observable<Response<ResponseBody>> importCertificate(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateImportParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateVersions"})
        @GET("certificates/{certificate-name}/versions")
        Observable<Response<ResponseBody>> getCertificateVersions(@Path("certificate-name") String certificateName, @Query("maxresults") Integer maxresults, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificatePolicy"})
        @GET("certificates/{certificate-name}/policy")
        Observable<Response<ResponseBody>> getCertificatePolicy(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateCertificatePolicy"})
        @PATCH("certificates/{certificate-name}/policy")
        Observable<Response<ResponseBody>> updateCertificatePolicy(@Path("certificate-name") String certificateName, @Body CertificatePolicy certificatePolicy, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateCertificate"})
        @PATCH("certificates/{certificate-name}/{certificate-version}")
        Observable<Response<ResponseBody>> updateCertificate(@Path("certificate-name") String certificateName, @Path("certificate-version") String certificateVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateUpdateParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificate"})
        @GET("certificates/{certificate-name}/{certificate-version}")
        Observable<Response<ResponseBody>> getCertificate(@Path("certificate-name") String certificateName, @Path("certificate-version") String certificateVersion, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient updateCertificateOperation"})
        @PATCH("certificates/{certificate-name}/pending")
        Observable<Response<ResponseBody>> updateCertificateOperation(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateOperationUpdateParameter certificateOperation, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateOperation"})
        @GET("certificates/{certificate-name}/pending")
        Observable<Response<ResponseBody>> getCertificateOperation(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient deleteCertificateOperation"})
        @HTTP(path = "certificates/{certificate-name}/pending", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteCertificateOperation(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient mergeCertificate"})
        @POST("certificates/{certificate-name}/pending/merge")
        Observable<Response<ResponseBody>> mergeCertificate(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body CertificateMergeParameters parameters, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getKeyVersionsNext"})
        @GET
        Observable<Response<ResponseBody>> getKeyVersionsNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getKeysNext"})
        @GET
        Observable<Response<ResponseBody>> getKeysNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getSecretsNext"})
        @GET
        Observable<Response<ResponseBody>> getSecretsNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getSecretVersionsNext"})
        @GET
        Observable<Response<ResponseBody>> getSecretVersionsNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificatesNext"})
        @GET
        Observable<Response<ResponseBody>> getCertificatesNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateIssuersNext"})
        @GET
        Observable<Response<ResponseBody>> getCertificateIssuersNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getCertificateVersionsNext"})
        @GET
        Observable<Response<ResponseBody>> getCertificateVersionsNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({"Content-Type: application/json; charset=utf-8", "Accept: application/pkcs10", "x-ms-logging-context: com.microsoft.azure.keyvault.KeyVaultClient getPendingCertificateSigningRequest"})
        @GET("certificates/{certificate-name}/pending")
        Observable<Response<ResponseBody>> getPendingCertificateSigningRequest(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client. The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: Requires the keys/create permission.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * @return the KeyBundle if successful.
     */
    public KeyBundle createKey(CreateKeyRequest createKeyRequest) {

        return createKey(
                createKeyRequest.vaultBaseUrl(),
                createKeyRequest.keyName(),
                createKeyRequest.keyType(),
                createKeyRequest.keySize(),
                createKeyRequest.keyOperations(),
                createKeyRequest.keyAttributes(),
                createKeyRequest.tags(),
                createKeyRequest.curve());
    }

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client. The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: Requires the keys/create permission.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * @param serviceCallback  the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyBundle> createKeyAsync(CreateKeyRequest createKeyRequest, ServiceCallback<KeyBundle> serviceCallback) {
        createKeyRequest.vaultBaseUrl();
        return createKeyAsync(
                createKeyRequest.vaultBaseUrl(),
                createKeyRequest.keyName(),
                createKeyRequest.keyType(),
                createKeyRequest.keySize(),
                createKeyRequest.keyOperations(),
                createKeyRequest.keyAttributes(),
                createKeyRequest.tags(),
                createKeyRequest.curve(),
                serviceCallback);
    }

    /**
     * Imports an externally created key, stores it, and returns key parameters and attributes to the client. The import key operation may be used to import any key type into an Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: requires the keys/import permission.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     * @return the KeyBundle if successful.
     */
    public KeyBundle importKey(ImportKeyRequest importKeyRequest) {
        return importKey(
                importKeyRequest.vaultBaseUrl(),
                importKeyRequest.keyName(),
                importKeyRequest.key(),
                importKeyRequest.isHsm(),
                importKeyRequest.keyAttributes(),
                importKeyRequest.tags());
    }

    /**
     * Imports an externally created key, stores it, and returns key parameters and attributes to the client. The import key operation may be used to import any key type into an Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: requires the keys/import permission.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     * @param serviceCallback  the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyBundle> importKeyAsync(ImportKeyRequest importKeyRequest, final ServiceCallback<KeyBundle> serviceCallback) {
        return importKeyAsync(
                importKeyRequest.vaultBaseUrl(),
                importKeyRequest.keyName(),
                importKeyRequest.key(),
                importKeyRequest.isHsm(),
                importKeyRequest.keyAttributes(),
                importKeyRequest.tags(),
                serviceCallback);
    }


    /**
     * The update key operation changes specified attributes of a stored key and can be applied to any key type and key version stored in Azure Key Vault. The cryptographic material of a key itself cannot be changed. In order to perform this operation, the key must already exist in the Key Vault. Authorization: requires the keys/update permission.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     * @return the KeyBundle if successful.
     */
    public KeyBundle updateKey(UpdateKeyRequest updateKeyRequest) {
        return updateKey(
                updateKeyRequest.vaultBaseUrl(),
                updateKeyRequest.keyName(),
                updateKeyRequest.keyVersion(),
                updateKeyRequest.keyOperations(),
                updateKeyRequest.keyAttributes(),
                updateKeyRequest.tags());
    }

    /**
     * The update key operation changes specified attributes of a stored key and can be applied to any key type and key version stored in Azure Key Vault. The cryptographic material of a key itself cannot be changed. In order to perform this operation, the key must already exist in the Key Vault. Authorization: requires the keys/update permission.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     * @param serviceCallback  the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyBundle> updateKeyAsync(UpdateKeyRequest updateKeyRequest, final ServiceCallback<KeyBundle> serviceCallback) {
        return updateKeyAsync(
                updateKeyRequest.vaultBaseUrl(),
                updateKeyRequest.keyName(),
                updateKeyRequest.keyVersion(),
                updateKeyRequest.keyOperations(),
                updateKeyRequest.keyAttributes(),
                updateKeyRequest.tags(),
                serviceCallback);
    }

    /**
     * Gets the public part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param keyIdentifier The full key identifier
     * @return the KeyBundle if successful.
     */
    public KeyBundle getKey(String keyIdentifier) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return getKey(id.vault(), id.name(), id.version() == null ? "" : id.version());
    }

    /**
     * Gets the public part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param keyIdentifier   The full key identifier
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyBundle> getKeyAsync(String keyIdentifier, final ServiceCallback<KeyBundle> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return getKeyAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), serviceCallback);
    }

    /**
     * Gets the public part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName      The name of the key
     * @return the KeyBundle if successful.
     */
    public KeyBundle getKey(String vaultBaseUrl, String keyName) {
        return getKey(vaultBaseUrl, keyName, "");
    }

    /**
     * Gets the public part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName         The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyBundle> getKeyAsync(String vaultBaseUrl, String keyName, final ServiceCallback<KeyBundle> serviceCallback) {
        return getKeyAsync(vaultBaseUrl, keyName, "", serviceCallback);
    }

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName      The name of the key
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName) {
        return getKeyVersions(vaultBaseUrl, keyName);
    }


    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       algorithm identifier
     * @param value           the key to be wrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyOperationResult> wrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return wrapKeyAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value, serviceCallback);
    }

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     algorithm identifier
     * @param value         the key to be unwrapped
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult unwrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return unwrapKey(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value);
    }

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       algorithm identifier
     * @param value           the key to be unwrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return unwrapKeyAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value, serviceCallback);
    }

    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     algorithm identifier
     * @param value         the key to be wrapped
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult wrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return wrapKey(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value);
    }

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName         The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final ListOperationCallback<KeyItem> serviceCallback) {
        return getKeyVersionsAsync(vaultBaseUrl, keyName, serviceCallback);
    }

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName         The name of the key
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
        return getKeyVersionsAsync(vaultBaseUrl, keyName, maxresults, serviceCallback);
    }


    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName      The name of the key
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName, final Integer maxresults) {
        return getKeyVersions(vaultBaseUrl, keyName, maxresults);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeys(final String vaultBaseUrl) {
        return getKeys(vaultBaseUrl);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final ListOperationCallback<KeyItem> serviceCallback) {
        return getKeysAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
        return getKeysAsync(vaultBaseUrl, maxresults, serviceCallback);
    }


    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeys(final String vaultBaseUrl, final Integer maxresults) {
        return getKeys(vaultBaseUrl, maxresults);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     algorithm identifier
     * @param value         the content to be encrypted
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult encrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return encrypt(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       algorithm identifier
     * @param value           the content to be encrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyOperationResult> encryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return encryptAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value, serviceCallback);
    }

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     algorithm identifier
     * @param value         the content to be decrypted
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult decrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return decrypt(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value);
    }

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       algorithm identifier
     * @param value           the content to be decrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyOperationResult> decryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return decryptAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value, serviceCallback);
    }

    /**
     * Creates a signature from a digest using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     algorithm identifier
     * @param value         the content to be signed
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult sign(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return sign(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value);
    }

    /**
     * Creates a signature from a digest using the specified key.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       algorithm identifier
     * @param value           the content to be signed
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyOperationResult> signAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return signAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, value, serviceCallback);
    }

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm     The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest        The digest used for signing
     * @param signature     The signature to be verified
     * @return the KeyVerifyResult if successful.
     */
    public KeyVerifyResult verify(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return verify(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, digest, signature);
    }

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier   The full key identifier
     * @param algorithm       The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest          The digest used for signing
     * @param signature       The signature to be verified
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyVerifyResult> verifyAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature, final ServiceCallback<KeyVerifyResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return verifyAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), algorithm, digest, signature, serviceCallback);
    }


    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     * @return the SecretBundle if successful.
     */
    public SecretBundle setSecret(SetSecretRequest setSecretRequest) {
        return setSecret(
                setSecretRequest.vaultBaseUrl(),
                setSecretRequest.secretName(),
                setSecretRequest.value(),
                setSecretRequest.tags(),
                setSecretRequest.contentType(),
                setSecretRequest.secretAttributes());
    }

    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     * @param serviceCallback  the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SecretBundle> setSecretAsync(SetSecretRequest setSecretRequest, final ServiceCallback<SecretBundle> serviceCallback) {
        return setSecretAsync(
                setSecretRequest.vaultBaseUrl(),
                setSecretRequest.secretName(),
                setSecretRequest.value(),
                setSecretRequest.tags(),
                setSecretRequest.contentType(),
                setSecretRequest.secretAttributes(),
                serviceCallback);
    }

    /**
     * Updates the attributes associated with a specified secret in a given key vault.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     * @return the SecretBundle if successful.
     */
    public SecretBundle updateSecret(UpdateSecretRequest updateSecretRequest) {
        return updateSecret(
                updateSecretRequest.vaultBaseUrl(),
                updateSecretRequest.secretName(),
                updateSecretRequest.secretVersion(),
                updateSecretRequest.contentType(),
                updateSecretRequest.secretAttributes(),
                updateSecretRequest.tags());
    }

    /**
     * Updates the attributes associated with a specified secret in a given key vault.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     * @param serviceCallback     the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SecretBundle> updateSecretAsync(UpdateSecretRequest updateSecretRequest, final ServiceCallback<SecretBundle> serviceCallback) {
        return updateSecretAsync(
                updateSecretRequest.vaultBaseUrl(),
                updateSecretRequest.secretName(),
                updateSecretRequest.secretVersion(),
                updateSecretRequest.contentType(),
                updateSecretRequest.secretAttributes(),
                updateSecretRequest.tags(),
                serviceCallback);
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * @param secretIdentifier The URL for the secret.
     * @return the SecretBundle if successful.
     */
    public SecretBundle getSecret(String secretIdentifier) {
        SecretIdentifier id = new SecretIdentifier(secretIdentifier);
        return getSecret(id.vault(), id.name(), id.version() == null ? "" : id.version());
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * @param secretIdentifier The URL for the secret.
     * @param serviceCallback  the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SecretBundle> getSecretAsync(String secretIdentifier, final ServiceCallback<SecretBundle> serviceCallback) {
        SecretIdentifier id = new SecretIdentifier(secretIdentifier);
        return getSecretAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), serviceCallback);
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName   The name of the secret in the given vault
     * @return the SecretBundle if successful.
     */
    public SecretBundle getSecret(String vaultBaseUrl, String secretName) {
        return getSecret(vaultBaseUrl, secretName, "");
    }

    /**
     * Get a specified secret from a given key vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName      The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback) {
        return getSecretAsync(vaultBaseUrl, secretName, "", serviceCallback);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecrets(final String vaultBaseUrl) {
        return getSecrets(vaultBaseUrl);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<SecretItem> serviceCallback) {
        return getSecretsAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecrets(final String vaultBaseUrl, final Integer maxresults) {
        return getSecrets(vaultBaseUrl, maxresults);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
        return getSecretsAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName   The name of the secret in the given vault
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName) {
        return getSecretVersions(vaultBaseUrl, secretName);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName      The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final ListOperationCallback<SecretItem> serviceCallback) {
        return getSecretVersionsAsync(vaultBaseUrl, secretName, serviceCallback);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName   The name of the secret in the given vault
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName, final Integer maxresults) {
        return getSecretVersions(vaultBaseUrl, secretName, maxresults);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName      The name of the secret in the given vault
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
        return getSecretVersionsAsync(vaultBaseUrl, secretName, maxresults, serviceCallback);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificates(final String vaultBaseUrl) {
        return getCertificates(vaultBaseUrl);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateItem> serviceCallback) {
        return getCertificatesAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificates(final String vaultBaseUrl, final Integer maxresults) {
        return getCertificates(vaultBaseUrl, maxresults);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
        return getCertificatesAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    public PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl) {
        return getCertificateIssuers(vaultBaseUrl);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return getCertificateIssuersAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults   Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    public PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl, final Integer maxresults) {
        return getCertificateIssuers(vaultBaseUrl, maxresults);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return getCertificateIssuersAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle setCertificateIssuer(SetCertificateIssuerRequest setCertificateIssuerRequest) {
        return setCertificateIssuer(
                setCertificateIssuerRequest.vaultBaseUrl(),
                setCertificateIssuerRequest.issuerName(),
                setCertificateIssuerRequest.provider(),
                setCertificateIssuerRequest.credentials(),
                setCertificateIssuerRequest.organizationDetails(),
                setCertificateIssuerRequest.attributes());
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     * @param serviceCallback             the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<IssuerBundle> setCertificateIssuerAsync(SetCertificateIssuerRequest setCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback) {
        return setCertificateIssuerAsync(
                setCertificateIssuerRequest.vaultBaseUrl(),
                setCertificateIssuerRequest.issuerName(),
                setCertificateIssuerRequest.provider(),
                setCertificateIssuerRequest.credentials(),
                setCertificateIssuerRequest.organizationDetails(),
                setCertificateIssuerRequest.attributes(),
                serviceCallback);
    }

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle updateCertificateIssuer(UpdateCertificateIssuerRequest updateCertificateIssuerRequest) {
        return updateCertificateIssuer(
                updateCertificateIssuerRequest.vaultBaseUrl(),
                updateCertificateIssuerRequest.issuerName(),
                updateCertificateIssuerRequest.provider(),
                updateCertificateIssuerRequest.credentials(),
                updateCertificateIssuerRequest.organizationDetails(),
                updateCertificateIssuerRequest.attributes());
    }

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     * @param serviceCallback                the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     * @throws IllegalArgumentException thrown if callback is null
     */
    public ServiceFuture<IssuerBundle> updateCertificateIssuerAsync(UpdateCertificateIssuerRequest updateCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback) {
        return updateCertificateIssuerAsync(
                updateCertificateIssuerRequest.vaultBaseUrl(),
                updateCertificateIssuerRequest.issuerName(),
                updateCertificateIssuerRequest.provider(),
                updateCertificateIssuerRequest.credentials(),
                updateCertificateIssuerRequest.organizationDetails(),
                updateCertificateIssuerRequest.attributes(),
                serviceCallback);
    }

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation createCertificate(CreateCertificateRequest createCertificateRequest) {
        return createCertificate(
                createCertificateRequest.vaultBaseUrl(),
                createCertificateRequest.certificateName(),
                createCertificateRequest.certificatePolicy(),
                createCertificateRequest.certificateAttributes(),
                createCertificateRequest.tags());
    }

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     * @param serviceCallback          the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateOperation> createCertificateAsync(CreateCertificateRequest createCertificateRequest, final ServiceCallback<CertificateOperation> serviceCallback) {
        return createCertificateAsync(
                createCertificateRequest.vaultBaseUrl(),
                createCertificateRequest.certificateName(),
                createCertificateRequest.certificatePolicy(),
                createCertificateRequest.certificateAttributes(),
                createCertificateRequest.tags(),
                serviceCallback);
    }

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle importCertificate(ImportCertificateRequest importCertificateRequest) {
        return importCertificate(
                importCertificateRequest.vaultBaseUrl(),
                importCertificateRequest.certificateName(),
                importCertificateRequest.base64EncodedCertificate(),
                importCertificateRequest.password(),
                importCertificateRequest.certificatePolicy(),
                importCertificateRequest.certificateAttributes(),
                importCertificateRequest.tags());
    }

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     * @param serviceCallback          the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateBundle> importCertificateAsync(ImportCertificateRequest importCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return importCertificateAsync(
                importCertificateRequest.vaultBaseUrl(),
                importCertificateRequest.certificateName(),
                importCertificateRequest.base64EncodedCertificate(),
                importCertificateRequest.password(),
                importCertificateRequest.certificatePolicy(),
                importCertificateRequest.certificateAttributes(),
                importCertificateRequest.tags(),
                serviceCallback);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName) {
        return getCertificateVersions(vaultBaseUrl, certificateName);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final ListOperationCallback<CertificateItem> serviceCallback) {
        return getCertificateVersionsAsync(vaultBaseUrl, certificateName, serviceCallback);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName, final Integer maxresults) {
        return getCertificateVersions(vaultBaseUrl, certificateName, maxresults);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults      Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
        return getCertificateVersionsAsync(vaultBaseUrl, certificateName, maxresults, serviceCallback);
    }

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     * @return the CertificatePolicy if successful.
     */
    public CertificatePolicy updateCertificatePolicy(UpdateCertificatePolicyRequest updateCertificatePolicyRequest) {
        return updateCertificatePolicy(
                updateCertificatePolicyRequest.vaultBaseUrl(),
                updateCertificatePolicyRequest.certificateName(),
                updateCertificatePolicyRequest.certificatePolicy());
    }

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     * @param serviceCallback                the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificatePolicy> updateCertificatePolicyAsync(UpdateCertificatePolicyRequest updateCertificatePolicyRequest, final ServiceCallback<CertificatePolicy> serviceCallback) {
        return updateCertificatePolicyAsync(
                updateCertificatePolicyRequest.vaultBaseUrl(),
                updateCertificatePolicyRequest.certificateName(),
                updateCertificatePolicyRequest.certificatePolicy(),
                serviceCallback);
    }

    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle updateCertificate(UpdateCertificateRequest updateCertificateRequest) {
        return updateCertificate(
                updateCertificateRequest.vaultBaseUrl(),
                updateCertificateRequest.certificateName(),
                updateCertificateRequest.certificateVersion(),
                updateCertificateRequest.certificatePolicy(),
                updateCertificateRequest.certificateAttributes(),
                updateCertificateRequest.tags());
    }

    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     * @param serviceCallback          the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateBundle> updateCertificateAsync(UpdateCertificateRequest updateCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return updateCertificateAsync(
                updateCertificateRequest.vaultBaseUrl(),
                updateCertificateRequest.certificateName(),
                updateCertificateRequest.certificateVersion(),
                updateCertificateRequest.certificatePolicy(),
                updateCertificateRequest.certificateAttributes(),
                updateCertificateRequest.tags(),
                serviceCallback);
    }

    /**
     * Gets information about a specified certificate.
     *
     * @param certificateIdentifier The certificate identifier
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle getCertificate(String certificateIdentifier) {
        CertificateIdentifier id = new CertificateIdentifier(certificateIdentifier);
        return getCertificate(id.vault(), id.name(), id.version() == null ? "" : id.version());
    }

    /**
     * Gets information about a specified certificate.
     *
     * @param certificateIdentifier The certificate identifier
     * @param serviceCallback       the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateBundle> getCertificateAsync(String certificateIdentifier, final ServiceCallback<CertificateBundle> serviceCallback) {
        CertificateIdentifier id = new CertificateIdentifier(certificateIdentifier);
        return getCertificateAsync(id.vault(), id.name(), id.version() == null ? "" : id.version(), serviceCallback);
    }

    /**
     * Gets information about a specified certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle getCertificate(String vaultBaseUrl, String certificateName) {
        return getCertificate(vaultBaseUrl, certificateName, "");
    }

    /**
     * Gets information about a specified certificate.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateBundle> getCertificateAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateBundle> serviceCallback) {
        return getCertificateAsync(vaultBaseUrl, certificateName, "", serviceCallback);
    }

    /**
     * Updates a certificate operation.
     *
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation updateCertificateOperation(UpdateCertificateOperationRequest updateCertificateOperationRequest) {
        return updateCertificateOperation(
                updateCertificateOperationRequest.vaultBaseUrl(),
                updateCertificateOperationRequest.certificateName(),
                updateCertificateOperationRequest.cancellationRequested());
    }

    /**
     * Updates a certificate operation.
     *
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     * @param serviceCallback                   the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateOperation> updateCertificateOperationAsync(UpdateCertificateOperationRequest updateCertificateOperationRequest, final ServiceCallback<CertificateOperation> serviceCallback) {
        return updateCertificateOperationAsync(
                updateCertificateOperationRequest.vaultBaseUrl(),
                updateCertificateOperationRequest.certificateName(),
                updateCertificateOperationRequest.cancellationRequested(),
                serviceCallback);
    }

    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle mergeCertificate(MergeCertificateRequest mergeCertificateRequest) {
        return mergeCertificate(
                mergeCertificateRequest.vaultBaseUrl(),
                mergeCertificateRequest.certificateName(),
                mergeCertificateRequest.x509Certificates(),
                mergeCertificateRequest.certificateAttributes(),
                mergeCertificateRequest.tags());
    }

    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     * @param serviceCallback         the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CertificateBundle> mergeCertificateAsync(MergeCertificateRequest mergeCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return mergeCertificateAsync(
                mergeCertificateRequest.vaultBaseUrl(),
                mergeCertificateRequest.certificateName(),
                mergeCertificateRequest.x509Certificates(),
                mergeCertificateRequest.certificateAttributes(),
                mergeCertificateRequest.tags(),
                serviceCallback);
    }

    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @return the String if successful.
     */
    public String getPendingCertificateSigningRequest(String vaultBaseUrl, String certificateName) {
        return getPendingCertificateSigningRequestWithServiceResponseAsync(vaultBaseUrl, certificateName).toBlocking().single().body();
    }

    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> getPendingCertificateSigningRequestAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(getPendingCertificateSigningRequestWithServiceResponseAsync(vaultBaseUrl, certificateName), serviceCallback);
    }

    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl    The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @return the observable to the String object
     */
    private Observable<ServiceResponse<String>> getPendingCertificateSigningRequestWithServiceResponseAsync(String vaultBaseUrl, String certificateName) {
        if (vaultBaseUrl == null) {
            throw new IllegalArgumentException("Parameter vaultBaseUrl is required and cannot be null.");
        }
        if (certificateName == null) {
            throw new IllegalArgumentException("Parameter certificateName is required and cannot be null.");
        }
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{vaultBaseUrl}", vaultBaseUrl);
        return service.getPendingCertificateSigningRequest(certificateName, this.apiVersion(), this.acceptLanguage(), parameterizedHost, this.userAgent())
                .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                    @Override
                    public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                        try {
                            ServiceResponse<String> clientResponse = new ServiceResponse<String>(response.body().string(), response);
                            return Observable.just(clientResponse);
                        } catch (Throwable t) {
                            return Observable.error(t);
                        }
                    }
                });
    }

	@Override
	public KeyBundle createKey(String vaultBaseUrl, String keyName, JsonWebKeyType kty, Integer keySize,
			List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags) {
		return createKey(vaultBaseUrl, keyName, kty, keySize, keyOps, keyAttributes, tags, null);
	}

	@Override
	public ServiceFuture<KeyBundle> createKeyAsync(String vaultBaseUrl, String keyName, JsonWebKeyType kty,
			Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags,
			ServiceCallback<KeyBundle> serviceCallback) {
		return createKeyAsync(vaultBaseUrl, keyName, kty, keySize, keyOps, keyAttributes, tags, null, serviceCallback);
	}

	@Override
	public Observable<KeyBundle> createKeyAsync(String vaultBaseUrl, String keyName, JsonWebKeyType kty,
			Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags) {
		return createKeyAsync(vaultBaseUrl, keyName, kty, keySize, keyOps, keyAttributes, tags, (JsonWebKeyCurveName) null);
	}

	@Override
	public Observable<ServiceResponse<KeyBundle>> createKeyWithServiceResponseAsync(String vaultBaseUrl, String keyName,
			JsonWebKeyType kty, Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes,
			Map<String, String> tags) {
		return createKeyWithServiceResponseAsync(vaultBaseUrl, keyName, kty, keySize, keyOps, keyAttributes, tags, null);
	}

	@Override
	public PagedList<CertificateItem> getCertificates(String vaultBaseUrl, Integer maxresults) {
		return getCertificates(vaultBaseUrl, maxresults, false);
	}

	@Override
	public ServiceFuture<List<CertificateItem>> getCertificatesAsync(String vaultBaseUrl, Integer maxresults,
			ListOperationCallback<CertificateItem> serviceCallback) {
		return getCertificatesAsync(vaultBaseUrl, maxresults, false, serviceCallback);
	}

	@Override
	public Observable<Page<CertificateItem>> getCertificatesAsync(String vaultBaseUrl, Integer maxresults) {
		return getCertificatesAsync(vaultBaseUrl, maxresults, false);
	}

	@Override
	public Observable<ServiceResponse<Page<CertificateItem>>> getCertificatesWithServiceResponseAsync(
			String vaultBaseUrl, Integer maxresults) {
		return getCertificatesWithServiceResponseAsync(vaultBaseUrl, maxresults, false);
	}

	@Override
	public PagedList<DeletedCertificateItem> getDeletedCertificates(String vaultBaseUrl, Integer maxresults) {
		return getDeletedCertificates(vaultBaseUrl, maxresults, false);
	}

	@Override
	public ServiceFuture<List<DeletedCertificateItem>> getDeletedCertificatesAsync(String vaultBaseUrl,
			Integer maxresults, ListOperationCallback<DeletedCertificateItem> serviceCallback) {
		return getDeletedCertificatesAsync(vaultBaseUrl, maxresults, false, serviceCallback);
	}

	@Override
	public Observable<Page<DeletedCertificateItem>> getDeletedCertificatesAsync(String vaultBaseUrl,
			Integer maxresults) {
		return getDeletedCertificatesAsync(vaultBaseUrl, maxresults, false);
	}

	@Override
	public Observable<ServiceResponse<Page<DeletedCertificateItem>>> getDeletedCertificatesWithServiceResponseAsync(
			String vaultBaseUrl, Integer maxresults) {
		return getDeletedCertificatesWithServiceResponseAsync(vaultBaseUrl, maxresults, false);
	}


}