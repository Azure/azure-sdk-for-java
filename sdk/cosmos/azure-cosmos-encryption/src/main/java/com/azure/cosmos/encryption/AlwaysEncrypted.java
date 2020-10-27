// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.encryption.DataEncryptionKeyProperties;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class AlwaysEncrypted {

    // TODO: key container should be read as input or config?
    public static final String keyContainerName = "keyContainer";

    private Logger logger = LoggerFactory.getLogger(AlwaysEncrypted.class);

    private final String CosmosContainerWrappedDekProviderType = "CosmosContainer";
    private final CosmosAsyncContainer container;
    private final EncryptionKeyStoreProvider encryptionKeyStoreProvider;
    private ClientEncryptionPolicy clientEncryptionPolicy;
    private CosmosAsyncContainer keyContainer;
    private WrappedDataEncryptionKeyProvider wrappedDekProvider;
    private HashMap<String, Settings> settingsByPath;
    private SerializerDefaultMappings serializerDefaultMappings;

    private AlwaysEncrypted(
        CosmosAsyncContainer container,
        EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        Preconditions.checkNotNull(container, "container");
        Preconditions.checkNotNull(encryptionKeyStoreProvider, "encryptionKeyStoreProvider");

        this.container = container;
        this.encryptionKeyStoreProvider = encryptionKeyStoreProvider;
        this.serializerDefaultMappings = new serializerDefaultMappingsImp();
    }

    // TODO: finalize public API
    /// <summary>
    /// Initializes AlwaysEncrypted.
    /// </summary>
    /// <param name="container">Container that has items which are to be encrypted in wrapped (encrypted) form.</param>
    /// <param name="encryptionKeyStoreProvider">Provider that allows interaction with the master keys.</param>
    /// <param name="cancellationToken">(Optional) Token to cancel the operation.</param>
    /// <returns>Task to await.</returns>
    public static Mono<AlwaysEncrypted> createAsync(
        CosmosAsyncContainer container,
        EncryptionKeyStoreProvider encryptionKeyStoreProvider, ClientEncryptionPolicy clientEncryptionPolicy) {
        AlwaysEncrypted encryption = new AlwaysEncrypted(container, encryptionKeyStoreProvider);
        encryption.setClientEncryptionPolicy(clientEncryptionPolicy);
        encryption.initializeAsync(null);
        // TODO: async vs non async?
        return Mono.just(encryption);
    }

    /// <summary>
    /// Initializes AlwaysEncrypted.
    /// </summary>
    /// <param name="keyContainer">
    /// (Optional) Container that stores Data Encryption Keys in wrapped (encrypted) form.
    /// This is required to be passed when the Data Encryption Keys are stored in a different account
    /// from the container with items being encrypted.
    /// </param>
    /// <param name="cancellationToken">(Optional) Token to cancel the operation.</param>
    /// <returns>Task to await.</returns>
    private Mono<Void> initializeAsync(
        CosmosAsyncContainer keyContainer) {
        if (this.settingsByPath != null) {
            throw new IllegalStateException("Already initialized");
        }

        this.populateClientEncryptionPolicyAsync();

        CosmosDataEncryptionKeyProviderMetadata dekProviderMetadata =
            this.clientEncryptionPolicy.dataEncryptionKeyMetadata;
        if (dekProviderMetadata.type == CosmosContainerWrappedDekProviderType) {
            if (keyContainer == null) {
                // TODO: support this case?
                //                if (dekProviderMetadata.accountEndpoint != this.container.getDatabase().Client
                //                .Endpoint)
                //                {
                //                    throw new ArgumentNullException("Key container needs to be provided when Data
                //                    Encryption Keys are stored in a different Cosmos DB account.", nameof
                //                    (keyContainer));
                //                }
                //                else
                {
                    this.keyContainer = this.container.getDatabase().getContainer(dekProviderMetadata.ContainerName);
                }
            } else if (!StringUtils.equals(keyContainer.getId(), dekProviderMetadata.ContainerName)
                || StringUtils.equals(keyContainer.getDatabase().getId(), dekProviderMetadata.databaseName)
                // TODO: check this || !keyContainer.Database.Client.Endpoint.Equals(dekProviderMetadata
                //  .AccountEndpoint)
            ) {
                throw new IllegalArgumentException("Misconfigured key container [todo: add all params]" + keyContainer.getId());
            } else {
                this.keyContainer = keyContainer;
            }
        } else {
            throw new IllegalStateException(
                Strings.lenientFormat("Unknown data encryption key provider: %s", dekProviderMetadata.type));
        }

        this.wrappedDekProvider = new WrappedDataEncryptionKeyProvider(this.keyContainer);
        this.wrappedDekProvider.InitializeAsync();

        Map<String, Settings> settingsByDekId = new HashMap<>();
        for (String dekId :
            this.clientEncryptionPolicy.includedPaths.stream().map(p -> p.dataEncryptionKeyId).distinct().collect(Collectors.toList())) {

            DataEncryptionKeyProperties dekProperties =
                this.wrappedDekProvider.GetDataEncryptionKeyPropertiesAsync(dekId);
            String masterKeyPath = dekProperties.encryptionKeyWrapMetadata.value;

            // todo: AAP should expose AzureKeyVaultProvider.ProviderName
            if (StringUtils.equals(this.encryptionKeyStoreProvider.providerName, "AZURE_KEY_VAULT") && !StringUtils.equals(dekProperties.encryptionKeyWrapMetadata.type, "akv")) {
                throw new IllegalArgumentException("Key is not suitable for Always Encryption.");
            }

            // todo: is using masterKeyPath for name fine?
            Settings.MasterKey masterKey = new Settings.MasterKey(masterKeyPath, masterKeyPath, this.encryptionKeyStoreProvider);

            byte[] wrappedDek = dekProperties.wrappedDataEncryptionKey;

            Settings settings = new Settings();
            settings.dekId = dekId;
            settings.masterKey = masterKey;
            settings.wrappedDek = wrappedDek;

            // TODO: the logic of unwrapping key with access to master key can move to EncryptionKey class
            // EncryptionKey encryptionKey = new EncryptionKey(dekId, masterKey, wrappedDek);

            // for now we are directly doing it here:
            byte[] key = this.encryptionKeyStoreProvider.unwrapKey(dekProperties.id,
                EncryptionKeyStoreProvider.KeyEncryptionKeyAlgorithm.fromString(dekProperties.encryptionAlgorithm),
                dekProperties.wrappedDataEncryptionKey);

            settings.encryptionKey = DataEncryptionKey.create(key,
                CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED);
            settingsByDekId.put(dekId, settings);

        }

        this.settingsByPath = new HashMap<>();
        for (ClientEncryptionIncludedPath propertyToEncrypt : this.clientEncryptionPolicy.includedPaths) {
            EncryptionType encryptionType = EncryptionType.DETERMINISTIC;
            switch (propertyToEncrypt.encryptionType) {
                case "Deterministic":
                    encryptionType = EncryptionType.DETERMINISTIC;
                    break;
                case "Randomized":
                    encryptionType = EncryptionType.RANDOMIZED;
                    break;
                default:
                    // TODO Debug.Fail(string.Format("Invalid encryption type {0}", propertyToEncrypt.EncryptionType));
                    break;
            }

            Settings settings = settingsByDekId.get(propertyToEncrypt.dataEncryptionKeyId);
            Preconditions.checkNotNull(settings);
            this.settingsByPath.put(propertyToEncrypt.path.substring(1),
                // TODO: validate settingsByDekId.get is not null
                Settings.create(settings, encryptionType));
        }

        return Mono.empty();
    }

    // TODO: this will be removed once service side adds supports for ClientEncryptionPolicy info
    private void setClientEncryptionPolicy(ClientEncryptionPolicy clientEncryptionPolicy) {
        this.clientEncryptionPolicy = clientEncryptionPolicy;
    }

    // todo: add and use ContainerProperties.ClientEncryptionPolicy
    private void populateClientEncryptionPolicyAsync() {
        // TODO: once service side adds supports for ClientEncryptionPolicy info fetch it here
        // CosmosContainerProperties containerProperties =
        //            this.container.read().block().getProperties();
        //
        // TODO: get ClientRetryPolicy from the service side
    }

    public Mono<byte[]> encrypt(byte[] streamPayload) {
        // TODO: if what is passed is Object node we shouldn't do serialization/deserializtion as optimization
        ObjectNode document = Utils.parse(streamPayload, ObjectNode.class);
        try {
            JsonFactory factory = new JsonFactory();
            StringWriter jsonObjectWriter = new StringWriter();
            JsonGenerator generator = factory.createGenerator(jsonObjectWriter);
            // TODO fix code and objectMapper instantiation
            generator.setCodec(new ObjectMapper());
            generator.writeStartObject();
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator =
                document.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                Settings settings = this.settingsByPath.get(entry.getKey());
                if (settings != null) {
                    generator.writeFieldName(entry.getKey());
                    this.writeCipherTextForValue(entry.getValue(), generator, settings);
                } else {
                    generator.writeFieldName(entry.getKey());
                    generator.writeTree(entry.getValue());
                }

            }
            generator.writeEndObject();
            generator.flush();

            // TODO: optimize to byte array
            return Mono.just(Utils.getUTF8Bytes(jsonObjectWriter.toString()));

        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    public Mono<byte[]> decrypt(byte[] encryptedData) {
        JsonNode jsonNode = Utils.parse(encryptedData, JsonNode.class);
        ObjectNode document = Utils.as(jsonNode, ObjectNode.class);
        if (document == null) {
            return Mono.just(encryptedData);
        }

        try {
            JsonFactory factory = new JsonFactory();
            StringWriter jsonObjectWriter = new StringWriter();
            JsonGenerator generator = factory.createGenerator(jsonObjectWriter);
            // TODO fix code and objectMapper instantiation
            generator.setCodec(new ObjectMapper());
            generator.writeStartObject();

            Iterator<Map.Entry<String, JsonNode>> fieldsIterator =
                document.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                Settings settings = this.settingsByPath.get(entry.getKey());
                if (settings != null) {
                    generator.writeFieldName(entry.getKey());
                    this.writePlainTextForValue(entry.getValue(), generator, settings);
                } else {
                    generator.writeFieldName(entry.getKey());
                    generator.writeTree(entry.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();

            // TODO: optimize to write to byte[] directly
            return Mono.just(Utils.getUTF8Bytes(jsonObjectWriter.toString()));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private void writePlainTextForValue(
        JsonNode value,
        JsonGenerator writer,
        Settings settings) throws IOException {
        // nulls are not encrypted

        if (value.isNull()) {
            writer.writeNull();
        } else if (value.isArray()) {
            writer.writeStartArray();
            Iterator<JsonNode> elementIterator = ((ArrayNode) value).elements();
            while (elementIterator.hasNext()) {
                JsonNode element = elementIterator.next();
                this.writePlainTextForValue(element, writer, settings);
            }

            writer.writeEndArray();
        } else if (value.isObject()) {
            writer.writeStartObject();
            Iterator<Map.Entry<String, JsonNode>> fieldIterator = value.fields();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldIterator.next();
                writer.writeFieldName(entry.getKey());
                this.writePlainTextForValue(entry.getValue(), writer, settings);
            }

            writer.writeEndObject();
        } else {
            // primitive
            this.writerPlainTextForPrimitiveValue(value, writer, settings);
        }
    }

    private void writerPlainTextForPrimitiveValue(
        JsonNode value,
        JsonGenerator writer,
        Settings settings) throws IOException {
        if (!value.isTextual()) {
            new IllegalArgumentException("Unexpected encrypted value of Json ValueKind " + value.getNodeType());
        }

        // todo: see if there is a better way to get the UTF8 bytes directly
        byte[] cipherTextWithTypeMarker = value.binaryValue();

        byte[] cipherText = new byte[cipherTextWithTypeMarker.length - 1];
        System.arraycopy(cipherTextWithTypeMarker, 1, cipherText, 0, cipherTextWithTypeMarker.length - 1);
        byte[] plainText = settings.algorithm.decryptData(cipherText);
        TypeMarker typeMarker = TypeMarker.fromByte(cipherTextWithTypeMarker[0]);
        if (typeMarker == null) {
            logger.error("Unexpected type marker {}", cipherTextWithTypeMarker[0]);
        }

        this.deserializeAndWritePropertyValue(
            TypeMarker.fromByte(cipherTextWithTypeMarker[0]),
            plainText,
            writer);
    }

    private void deserializeAndWritePropertyValue(
        TypeMarker typeMarker,
        byte[] serializedBytes,
        JsonGenerator writer) throws IOException {
        switch (typeMarker) {
            case BOOLEAN:
                writer.writeBoolean(serializerDefaultMappings.deserializeAsBoolean(serializedBytes));
                break;
            case NUMBER:
                writer.writeNumber(serializerDefaultMappings.deserializeAsDouble(serializedBytes));
                break;
            case STRING:
                writer.writeString(serializerDefaultMappings.deserializeAsString(serializedBytes));
                break;
            default:
                // TODO: should we throw or just no-op?
                break;
        }
    }

    private void writeCipherTextForValue(JsonNode value, JsonGenerator writer, Settings settings) throws IOException {
        // nulls are not encrypted
        if (value.isNull()) {
            writer.writeNull();
        } else if (value.isArray()) {
            writer.writeStartArray();
            Iterator<JsonNode> elementIterator = ((ArrayNode) value).elements();
            while (elementIterator.hasNext()) {
                JsonNode element = elementIterator.next();
                writeCipherTextForValue(element, writer, settings);
            }

            writer.writeEndArray();
        } else if (value.isObject()) {
            writer.writeStartObject();
            Iterator<Map.Entry<String, JsonNode>> fieldIterator = value.fields();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldIterator.next();
                writer.writeFieldName(entry.getKey());
                writeCipherTextForValue(entry.getValue(), writer, settings);
            }

            writer.writeEndObject();
        } else {
            // primitive
            this.writerCipherTextForPrimitiveValue(value, writer, settings);
        }
    }

    private void writerCipherTextForPrimitiveValue(JsonNode value, JsonGenerator writer, Settings settings) throws IOException {
        Pair<TypeMarker, byte[]> serializationResult = this.serialize(value);

        byte[] plainText = serializationResult.getRight();
        TypeMarker typeMarker = serializationResult.getLeft();
        byte[] cipherText = settings.algorithm.encryptData(plainText);

        byte[] cipherTextWithTypeMarker = new byte[cipherText.length + 1];
        cipherTextWithTypeMarker[0] = typeMarker.value;
        System.arraycopy(cipherText, 0, cipherTextWithTypeMarker, 1, cipherText.length);
        // todo write string?
        writer.writeBinary(cipherTextWithTypeMarker);
    }

    // todo: take settings.Serializer for custom serialization
    private Pair<TypeMarker, byte[]> serialize(JsonNode value) {
        byte[] plainText;
        TypeMarker typeMarker;
        if (value.isBoolean()) {
            plainText = serializerDefaultMappings.serializeBoolean(value.booleanValue());
            typeMarker = TypeMarker.BOOLEAN;
        } else if (value.isNumber()) {
            // todo: int64 vs double separation?
            plainText = serializerDefaultMappings.serializeDouble(value.asDouble());
            typeMarker = TypeMarker.NUMBER;
        } else if (value.isTextual()) {
            plainText = serializerDefaultMappings.serializeString(value.textValue());
            typeMarker = TypeMarker.STRING;
        } else {
            // unexpected value
            // TODO: validate? assertion
            typeMarker = null;
            plainText = null;
        }

        return Pair.of(typeMarker, plainText);

    }

    enum TypeMarker {
        BOOLEAN(1),
        NUMBER(2),
        STRING(3);

        TypeMarker(int val) {
            this.value = (byte) val;
        }

        static TypeMarker fromByte(byte val) {
            if (val == BOOLEAN.value) {
                return BOOLEAN;
            }

            if (val == NUMBER.value) {
                return NUMBER;
            }

            if (val == STRING.value) {
                return STRING;
            }

            return null;
        }

        final byte value;
    }

    static private class Settings {
        public static class MasterKey {
            public MasterKey(String masterKeyPath, String masterKeyPat1, EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
            }
        }


        public String dekId;
        public byte[] wrappedDek;
        public MasterKey masterKey;
        public DataEncryptionKey encryptionKey;
        public EncryptionType encryptionType;
        public EncryptionAlgorithm algorithm;

        public Settings() {
        }

        public static Settings create(Settings settingsForKey, EncryptionType encryptionType) {
            Settings settings = new Settings();
            settings.dekId = settingsForKey.dekId;
            settings.wrappedDek = settingsForKey.wrappedDek;
            settings.masterKey = settingsForKey.masterKey;
            settings.encryptionKey = settingsForKey.encryptionKey;
            settings.encryptionType = encryptionType;
            settings.algorithm = new EncryptionAlgorithm(settingsForKey.encryptionKey, encryptionType);

            return settings;
        }
    }
}
