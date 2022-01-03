// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.intellij;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class IntelliJKdbxMetadata {
    public static final UUID AES_CIPHER = UUID.fromString("31C1F2E6-BF71-4350-BE58-05216AFC5AFF");
    private final ClientLogger logger = new ClientLogger(IntelliJKdbxMetadata.class);

    private UUID cipherUuid;
    private DatabaseCompressionFlags databaseCompressionFlags;
    private byte[] baseSeed;
    private byte[] transformSeed;
    private long transformRounds;
    private byte[] encryptionIv;
    private byte[] encryptionKey;
    private DatabaseEncryptionAlgorithm databaseEncryptionAlgorithm;
    private byte[] initBytes;
    private byte[] headerHash;

    public IntelliJKdbxMetadata() {
        SecureRandom random = new SecureRandom();
        this.cipherUuid = AES_CIPHER;
        this.databaseCompressionFlags = DatabaseCompressionFlags.GZIP;
        this.baseSeed = random.generateSeed(32);
        this.transformSeed = random.generateSeed(32);
        this.transformRounds = 6000L;
        this.encryptionIv = random.generateSeed(16);
        this.encryptionKey = random.generateSeed(32);
        this.initBytes = new byte[32];
        this.databaseEncryptionAlgorithm = DatabaseEncryptionAlgorithm.SALSA_20;
    }

    public InputStream createDecryptedStream(byte[] digest, InputStream inputStream) throws IOException {
        byte[] finalKeyDigest = IntelliJCryptoUtil.createKey(digest, this.getBaseSeed(), this.getTransformSeed(), this.getTransformRounds());
        return IntelliJCryptoUtil.getDecryptedInputStream(inputStream, finalKeyDigest, this.getEncryptionIv());
    }

    public DatabaseCompressionFlags getDatabaseCompressionFlags() {
        return this.databaseCompressionFlags;
    }

    public byte[] getBaseSeed() {
        return this.baseSeed;
    }

    public byte[] getTransformSeed() {
        return this.transformSeed;
    }

    public long getTransformRounds() {
        return this.transformRounds;
    }

    public byte[] getEncryptionIv() {
        return this.encryptionIv;
    }

    public byte[] getEncryptionKey() {
        return this.encryptionKey;
    }

    public byte[] getInitBytes() {
        return this.initBytes;
    }

    public void setCipherUuid(byte[] uuid) {
        ByteBuffer b = ByteBuffer.wrap(uuid);
        UUID incoming = new UUID(b.getLong(), b.getLong(8));
        if (!incoming.equals(AES_CIPHER)) {
            throw logger.logExceptionAsError(new IllegalStateException("Unknown Cipher UUID " + incoming.toString()));
        } else {
            this.cipherUuid = incoming;
        }
    }

    public void setDatabaseCompressionFlags(int flags) {
        this.databaseCompressionFlags = DatabaseCompressionFlags.values()[flags];
    }

    public void setBaseSeed(byte[] baseSeed) {
        this.baseSeed = baseSeed;
    }

    public void setTransformSeed(byte[] transformSeed) {
        this.transformSeed = transformSeed;
    }

    public void setTransformRounds(long transformRounds) {
        this.transformRounds = transformRounds;
    }

    public void setEncryptionIv(byte[] encryptionIv) {
        this.encryptionIv = encryptionIv;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setInitBytes(byte[] initBytes) {
        this.initBytes = initBytes;
    }

    public void setEncryptionAlgorithm(int encryptionAlgorithmId) {
        this.databaseEncryptionAlgorithm = DatabaseEncryptionAlgorithm.values()[encryptionAlgorithmId];
    }

    public void setHeaderHash(byte[] headerHash) {
        this.headerHash = headerHash;
    }

    public enum DatabaseEncryptionAlgorithm {
        NONE,
        ARC_FOUR,
        SALSA_20;

        DatabaseEncryptionAlgorithm() {
        }
    }

    public enum DatabaseCompressionFlags {
        NONE,
        GZIP;

        DatabaseCompressionFlags() {
        }
    }
}
