// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.implementation.Constants;
import com.azure.cosmos.encryption.implementation.EncryptionImplementationBridgeHelpers;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.encryption.implementation.EncryptionUtils;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.EncryptionType;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a SQL query with encryption parameters in the Azure Cosmos DB database service.
 */
public final class SqlQuerySpecWithEncryption {
    private final SqlQuerySpec sqlQuerySpec;
    private final HashMap<String, SqlParameter> encryptionParamMap = new HashMap<>();
    private final EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncContainerHelper.CosmosEncryptionAsyncContainerAccessor cosmosEncryptionAsyncContainerAccessor = EncryptionImplementationBridgeHelpers.CosmosEncryptionAsyncContainerHelper.getCosmosEncryptionAsyncContainerAccessor();

    /**
     * Creates a new instance of SQL query spec with encryption.
     *
     * @param sqlQuerySpec the SQL query spec.
     */
    public SqlQuerySpecWithEncryption(SqlQuerySpec sqlQuerySpec) {
        this.sqlQuerySpec = sqlQuerySpec;
    }

    /**
     * Adds an encryption parameter.
     *
     * @param path Path
     * @param sqlParameter SQL parameter
     */
    public void addEncryptionParameter(String path, SqlParameter sqlParameter) {
        encryptionParamMap.put(path, sqlParameter);
    }

    Mono<Void> addEncryptionParameterAsync(String path, SqlParameter sqlParameter,
                                           CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
        if (StringUtils.isEmpty(path) || path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            return Mono.empty();
        }

        List<SqlParameter> parameters = sqlQuerySpec.getParameters();
        if (parameters != null) {
            return cosmosEncryptionAsyncContainerAccessor.getEncryptionProcessor(cosmosEncryptionAsyncContainer)
                .initEncryptionSettingsIfNotInitializedAsync().then(Mono.defer(() -> {
                    String propertyName = path.substring(1);
                    return cosmosEncryptionAsyncContainerAccessor.getEncryptionProcessor(cosmosEncryptionAsyncContainer)
                        .getEncryptionSettings()
                        .getEncryptionSettingForPropertyAsync(propertyName,
                            cosmosEncryptionAsyncContainerAccessor.getEncryptionProcessor(cosmosEncryptionAsyncContainer)).flatMap(encryptionSettings -> {            // encryptionSettings.
                            if (encryptionSettings == null) {
                                // property not encrypted.
                                return Mono.empty();
                            }
                            if (encryptionSettings.getEncryptionType() == EncryptionType.Randomized) {
                                return Mono.error(new IllegalArgumentException(String.format("Path %s cannot be used " +
                                    "in " +
                                    "the " +
                                    "query because of randomized encryption", path)));
                            }
                            try {
                                if (propertyName.equals(Constants.PROPERTY_NAME_ID)) {
                                    if (sqlParameter.getValue(Object.class).getClass() != String.class) {
                                        throw new IllegalArgumentException("Unsupported argument type. The value to escape has to be string " +
                                            "type. Please refer to https://aka.ms/CosmosClientEncryption for more details.");
                                    }
                                }
                                byte[] valueByte =
                                    EncryptionUtils.serializeJsonToByteArray(CosmosItemSerializer.DEFAULT_SERIALIZER,
                                        sqlParameter.getValue(Object.class));
                                JsonNode itemJObj = Utils.parse(valueByte, JsonNode.class, CosmosItemSerializer.DEFAULT_SERIALIZER);
                                Pair<EncryptionProcessor.TypeMarker, byte[]> typeMarkerPair =
                                    EncryptionProcessor.toByteArray(itemJObj);
                                byte[] cipherText =
                                    encryptionSettings.getAeadAes256CbcHmac256EncryptionAlgorithm().encrypt(typeMarkerPair.getRight());
                                byte[] cipherTextWithTypeMarker = new byte[cipherText.length + 1];
                                cipherTextWithTypeMarker[0] = (byte) typeMarkerPair.getLeft().getValue();
                                System.arraycopy(cipherText, 0, cipherTextWithTypeMarker, 1, cipherText.length);

                                SqlParameter encryptedParameter;
                                if (propertyName.equals(Constants.PROPERTY_NAME_ID)) {
                                    // case: id does not support '/','\','?','#'. Convert Base64 string to Uri safe string
                                    String base64UriSafeString =  convertToBase64UriSafeString(cipherTextWithTypeMarker);
                                    encryptedParameter = new SqlParameter(sqlParameter.getName(),
                                        base64UriSafeString.getBytes(StandardCharsets.UTF_8));

                                } else {
                                    encryptedParameter = new SqlParameter(sqlParameter.getName(),
                                        cipherTextWithTypeMarker);
                                }
                                parameters.add(encryptedParameter);

                            } catch (MicrosoftDataEncryptionException ex) {
                                return Mono.error(ex);
                            }
                            return Mono.empty();
                        });

                }));
        }
        return Mono.empty();
    }

    private String convertToBase64UriSafeString(byte[] bytesToProcess) {
        // Base 64 Encoding with URL and Filename Safe Alphabet  https://datatracker.ietf.org/doc/html/rfc4648#section-5
        // https://docs.microsoft.com/en-us/azure/cosmos-db/concepts-limits#per-item-limits, due to base64 conversion and encryption
        // the permissible size of the property will further reduce.
        return Base64.getUrlEncoder().encodeToString(bytesToProcess);
    }

    HashMap<String, SqlParameter> getEncryptionParamMap() {
        return encryptionParamMap;
    }

    SqlQuerySpec getSqlQuerySpec() {
        return sqlQuerySpec;
    }

    static {
        EncryptionImplementationBridgeHelpers.SqlQuerySpecWithEncryptionHelper.setSqlQuerySpecWithEncryptionAccessor(new EncryptionImplementationBridgeHelpers.SqlQuerySpecWithEncryptionHelper.SqlQuerySpecWithEncryptionAccessor() {
            @Override
            public HashMap<String, SqlParameter> getEncryptionParamMap(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption) {
                return sqlQuerySpecWithEncryption.getEncryptionParamMap();
            }

            @Override
            public Mono<Void> addEncryptionParameterAsync(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption,
                                                          String path, SqlParameter sqlParameter,
                                                          CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer) {
                return sqlQuerySpecWithEncryption.addEncryptionParameterAsync(path, sqlParameter, cosmosEncryptionAsyncContainer);
            }

            @Override
            public SqlQuerySpec getSqlQuerySpec(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption) {
                return sqlQuerySpecWithEncryption.getSqlQuerySpec();
            }
        });
    }
}
