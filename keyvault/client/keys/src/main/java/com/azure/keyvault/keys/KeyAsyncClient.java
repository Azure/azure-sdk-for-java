// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys;

import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.RestProxy;
import com.azure.keyvault.keys.implementation.DeletedKeyPage;
import com.azure.keyvault.keys.implementation.KeyBasePage;
import com.azure.keyvault.keys.models.*;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

public final class KeyAsyncClient extends ServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final int DEFAULT_MAX_PAGE_RESULTS = 25;
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private String endpoint;
    private final KeyService service;

    KeyAsyncClient(URL endpoint, HttpPipeline pipeline) {
        super(pipeline);
        // Objects.requireNonNull(endpoint, KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));
        this.endpoint = endpoint.toString();
        this.service = RestProxy.create(KeyService.class, this);
    }

    static KeyAsyncClientBuilder builder() {
        return new KeyAsyncClientBuilder();
    }


    public Mono<Response<Key>> createKey(String name, JsonWebKeyType keyType) {
        KeyRequestParameters parameters = new KeyRequestParameters().kty(keyType);
        return service.createKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Key>> createRSAKey(RSAKeyCreateConfig rsaKeyCreateConfig) {
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(rsaKeyCreateConfig.keyType())
            .keySize(rsaKeyCreateConfig.keySize())
            .keyOps(rsaKeyCreateConfig.keyOperations())
            .keyAttributes(new KeyRequestAttributes(rsaKeyCreateConfig));
        return service.createKey(endpoint, rsaKeyCreateConfig.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Key>> createECKey(ECKeyCreateConfig ecKeyCreateConfig) {
        KeyRequestParameters parameters = new KeyRequestParameters()
            .kty(ecKeyCreateConfig.keyType())
            .curve(ecKeyCreateConfig.curve())
            .keyOps(ecKeyCreateConfig.keyOperations())
            .keyAttributes(new KeyRequestAttributes(ecKeyCreateConfig));
        return service.createKey(endpoint, ecKeyCreateConfig.name(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Key>> getKey(String name, String version) {
        String keyVersion = "";
        if(version != null){
            keyVersion = version;
        }
        return service.getKey(endpoint, name, keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Key>> getKey(String name) {
        return getKey(name, "");
    }


    public Mono<Response<Key>> getKey(KeyBase keyBase) {
        Objects.requireNonNull(keyBase, "The Key Base parameter cannot be null.");
        String keyVersion = "";

        return service.getKey(endpoint, keyBase.name(), keyVersion, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }

    public Mono<Response<Key>> updateKey(Key key) {
        Objects.requireNonNull(key, "The key input parameter cannot be null.");
        KeyRequestParameters parameters = new KeyRequestParameters()
                .tags(key.tags())
                .keyOps(key.keyOperations())
                .keyAttributes(new KeyRequestAttributes(key));

        return service.updateKey(endpoint, key.name(), key.version(), API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE);
    }


    public Mono<Response<DeletedKey>> deleteKey(String name) {
        return service.deleteKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }


    public Mono<Response<DeletedKey>> getDeletedKey(String name) {
        return service.getDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }


    public Mono<VoidResponse> purgeDeletedKey(String name) {
        return service.purgeDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }


    public Mono<Response<Key>> recoverDeletedKey(String name) {
        return service.recoverDeletedKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }


    public Mono<Response<byte[]>> backupKey(String name) {
        return service.backupKey(endpoint, name, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE)
                .flatMap(base64URLResponse ->  Mono.just(new SimpleResponse<byte[]>(base64URLResponse.request(),
                base64URLResponse.statusCode(), base64URLResponse.headers(), base64URLResponse.value().value())));
    }


    public Mono<Response<Key>> restoreKey(byte[] backup) {
        KeyRestoreRequestParameters parameters = new KeyRestoreRequestParameters().keyBackup(backup);
        return service.restoreKey(endpoint, API_VERSION, parameters, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE);
    }


    public Flux<KeyBase> listKeys() {
        return service.getKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys);
    }


    public Flux<DeletedKey> listDeletedKeys() {
        return service.getDeletedKeys(endpoint, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedKeys);
    }


    public Flux<KeyBase> listKeytVersions(String name) {
        return service.getKeyVersions(endpoint, name, DEFAULT_MAX_PAGE_RESULTS, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys);
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listKeys()}.
     *
     * @param nextPageLink The {@link KeyBasePage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<KeyBase> listKeysNext(String nextPageLink) {
        return service.getKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchKeys);
    }

    private Publisher<KeyBase> extractAndFetchKeys(PagedResponse<KeyBase> page) {
        return extractAndFetch(page, this::listKeysNext);
    }

    /**
     * Gets attributes of all the keys given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyAsyncClient#listDeletedKeys()}.
     *
     * @param nextPageLink The {@link DeletedKeyPage#nextLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link KeyBase key} from the next page of results.
     */
    private Flux<DeletedKey> listDeletedKeysNext(String nextPageLink) {
        return service.getDeletedKeys(endpoint, nextPageLink, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE).flatMapMany(this::extractAndFetchDeletedKeys);
    }

    private Publisher<DeletedKey> extractAndFetchDeletedKeys(PagedResponse<DeletedKey> page) {
        return extractAndFetch(page, this::listDeletedKeysNext);
    }

    //TODO: Extract this in azure-core ImplUtils and use from there
    private <T> Publisher<T> extractAndFetch(PagedResponse<T> page, Function<String, Publisher<T>> content) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(content.apply(nextPageLink));
    }
}

