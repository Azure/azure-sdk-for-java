// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.encryption.implementation.EncryptionProcessor;
import com.azure.cosmos.encryption.implementation.EncryptionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.data.encryption.cryptography.EncryptionType;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class EncryptionSqlQuerySpec {
    private SqlQuerySpec sqlQuerySpec;
    private EncryptionCosmosAsyncContainer encryptionCosmosAsyncContainer;

    public EncryptionSqlQuerySpec(SqlQuerySpec sqlQuerySpec,
                                  EncryptionCosmosAsyncContainer encryptionCosmosAsyncContainer) {
        this.sqlQuerySpec = sqlQuerySpec;
        this.encryptionCosmosAsyncContainer = encryptionCosmosAsyncContainer;
    }

    public Mono<Void> addEncryptionParameterAsync(SqlParameter sqlParameter, String path) {
        if (StringUtils.isEmpty(path) || path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            return Mono.empty();
        }
        List<SqlParameter> parameters = sqlQuerySpec.getParameters();
        if (parameters != null) {
            List<SqlParameter> newParameters = new ArrayList<>();
            for (SqlParameter parameter : parameters) {
                if (!parameter.getName().equals(sqlParameter.getName())) {
                    newParameters.add(parameter);
                }
            }

            return this.encryptionCosmosAsyncContainer.getEncryptionProcessor().initEncryptionSettingsIfNotInitializedAsync().then(Mono.defer(() -> {

                return this.encryptionCosmosAsyncContainer.getEncryptionProcessor().getEncryptionSettings().getEncryptionSettingForPropertyAsync(sqlParameter.getName().substring(1),
                    this.encryptionCosmosAsyncContainer.getEncryptionProcessor()).flatMap(encryptionSettings -> {            // encryptionSettings.
                    if (encryptionSettings == null) {
                        // property not encrypted.
                        return Mono.empty();
                    }
                    if (encryptionSettings.getEncryptionType() == EncryptionType.Randomized) {
                        throw new IllegalArgumentException("Unsupported argument with Path: {path} for query. For" +
                            " executing queries on encrypted path requires the use of an encryption - enabled " +
                            "client. Please refer to https://aka.ms/CosmosClientEncryption for more details. ");
                    }

                    try {
                        byte[] valueByte = EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(),
                            sqlParameter.getValue(Object.class));
                        JsonNode itemJObj = Utils.parse(valueByte, JsonNode.class);
                        Pair<EncryptionProcessor.TypeMarker, byte[]> typeMarkerPair =
                            EncryptionProcessor.toByteArray(itemJObj);
                        byte[] cipherText =
                            encryptionSettings.getAeadAes256CbcHmac256EncryptionAlgorithm().encrypt(typeMarkerPair.getRight());
                        byte[] cipherTextWithTypeMarker = new byte[cipherText.length + 1];
                        cipherTextWithTypeMarker[0] = (byte) typeMarkerPair.getLeft().getValue();
                        System.arraycopy(cipherText, 0, cipherTextWithTypeMarker, 1, cipherText.length);
                        SqlParameter encryptedParameter = new SqlParameter(sqlParameter.getName(),
                            cipherTextWithTypeMarker);
                        newParameters.add(encryptedParameter);
                        this.sqlQuerySpec.setParameters(newParameters);
                    } catch (MicrosoftDataEncryptionException ex) {
                        return Mono.error(ex);
                    }
                    return Mono.empty();
                });

            }));
        }
        return Mono.empty();
    }

    public SqlQuerySpec getSqlQuerySpec() {
        return sqlQuerySpec;
    }
}
