// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.implementation.keyprovider.EncryptionKeyStoreProviderImpl;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.encryption.implementation
 **/
public class EncryptionImplementationBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(EncryptionImplementationBridgeHelpers.class);

    public static final class SqlQuerySpecWithEncryptionHelper {
        static {
            ensureClassLoaded(SqlQuerySpecWithEncryption.class);
        }

        private static SqlQuerySpecWithEncryptionAccessor accessor;

        private SqlQuerySpecWithEncryptionHelper() {
        }

        public static void setSqlQuerySpecWithEncryptionAccessor(final SqlQuerySpecWithEncryptionAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("SqlQuerySpecWithEncryption accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static SqlQuerySpecWithEncryptionAccessor getSqlQuerySpecWithEncryptionAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("SqlQuerySpecWithEncryption accessor is not initialized!");
            }

            return accessor;
        }

        public interface SqlQuerySpecWithEncryptionAccessor {
            HashMap<String, SqlParameter> getEncryptionParamMap(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption);

            Mono<Void> addEncryptionParameterAsync(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption, String path
                , SqlParameter sqlParameter, CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer);

            SqlQuerySpec getSqlQuerySpec(SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption);
        }
    }

    public static final class CosmosEncryptionAsyncClientHelper {
        static {
            ensureClassLoaded(CosmosEncryptionAsyncClient.class);
        }

        private static CosmosEncryptionAsyncClientAccessor accessor;

        private CosmosEncryptionAsyncClientHelper() {
        }

        public static void seCosmosEncryptionAsyncClientAccessor(final CosmosEncryptionAsyncClientAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosEncryptionAsyncClient accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosEncryptionAsyncClientAccessor getCosmosEncryptionAsyncClientAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosEncryptionAsyncClient accessor is not initialized!");
            }

            return accessor;
        }

        public interface CosmosEncryptionAsyncClientAccessor {
            Mono<CosmosClientEncryptionKeyProperties> getClientEncryptionPropertiesAsync(
                CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient,
                String clientEncryptionKeyId,
                String databaseRid,
                CosmosAsyncContainer cosmosAsyncContainer,
                boolean shouldForceRefresh,
                String ifNoneMatchEtag,
                boolean shouldForceRefreshGateway);

            Mono<CosmosContainerProperties> getContainerPropertiesAsync(
                CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient,
                CosmosAsyncContainer container,
                boolean shouldForceRefresh);

            EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl(CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient);
        }
    }

    public static final class CosmosEncryptionAsyncContainerHelper {
        static {
            ensureClassLoaded(CosmosEncryptionAsyncContainer.class);
        }

        private static CosmosEncryptionAsyncContainerAccessor accessor;

        private CosmosEncryptionAsyncContainerHelper() {
        }

        public static void setCosmosEncryptionAsyncContainerAccessor(final CosmosEncryptionAsyncContainerAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosEncryptionAsyncContainer accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosEncryptionAsyncContainerAccessor getCosmosEncryptionAsyncContainerAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosEncryptionAsyncContainer accessor is not initialized!");
            }

            return accessor;
        }

        public interface CosmosEncryptionAsyncContainerAccessor {
            EncryptionProcessor getEncryptionProcessor(CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer);
        }
    }

    private static <T> void ensureClassLoaded(Class<T> classType) {
        try {
            // ensures the class is loaded
            Class.forName(classType.getName());
        } catch (ClassNotFoundException e) {
            logger.error("cannot load class {}", classType.getName());
            throw new RuntimeException(e);
        }
    }
}
