// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.AzureResourceManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;

/**
 * Minimal set of helpers shared by the resource manager samples.
 * <p>
 * These helpers intentionally avoid the verbose "print resource" utilities so that each sample stays focused on the
 * management operations it demonstrates.
 */
public final class SampleUtils {

    /**
     * Generates a random resource name with the given prefix.
     *
     * @param azure the {@link AzureResourceManager} instance
     * @param prefix the desired name prefix
     * @param maxLen the maximum length of the generated name
     * @return a random resource name
     */
    public static String randomResourceName(AzureResourceManager azure, String prefix, int maxLen) {
        return azure.resourceGroups().manager().internalContext().randomResourceName(prefix, maxLen);
    }

    /**
     * Generates a random UUID that is stable across test record/playback.
     *
     * @param azure the {@link AzureResourceManager} instance
     * @return a random UUID string
     */
    public static String randomUuid(AzureResourceManager azure) {
        return azure.resourceGroups().manager().internalContext().randomUuid();
    }

    /**
     * Generates a strong password that satisfies Microsoft Entra ID complexity requirements.
     *
     * @return a strong password
     */
    public static String password() {
        return "P@0" + Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes(
            StandardCharsets.US_ASCII)).substring(0, 12);
    }

    /**
     * Generates an SSH public key in OpenSSH format.
     * <p>
     * Uses 2048-bit RSA so the sample stays compatible with the Java 8 baseline. If your JDK is 15 or later, prefer
     * Ed25519 (shorter and stronger): delete this method body's RSA logic and use {@code sshPublicKeyEd25519()} below
     * (uncomment it first). No third-party dependency is required on JDK 15+.
     *
     * @return an SSH public key
     */
    public static String sshPublicKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            RSAPublicKey rsaPublicKey = (RSAPublicKey) pair.getPublic();

            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes(StandardCharsets.US_ASCII).length);
            dos.write("ssh-rsa".getBytes(StandardCharsets.US_ASCII));
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
            return "ssh-rsa " + Base64.getEncoder().encodeToString(byteOs.toByteArray());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Ed25519 alternative for JDK 15+. Uncomment (and add the imports below) to switch, then call it from the samples
    // in place of sshPublicKey(). It compiles only on JDK 15+ because EdECPublicKey / EdECPoint were added then.
    //
    //   import java.security.interfaces.EdECPublicKey;
    //   import java.security.spec.EdECPoint;
    //
    // public static String sshPublicKeyEd25519() {
    //     try {
    //         KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    //         EdECPoint point = ((EdECPublicKey) pair.getPublic()).getPoint();
    //         // OpenSSH encodes the 32-byte little-endian y coordinate with the x sign bit in the top bit.
    //         byte[] y = point.getY().toByteArray(); // big-endian
    //         byte[] key = new byte[32];
    //         for (int i = 0; i < y.length && i < 32; i++) {
    //             key[i] = y[y.length - 1 - i];
    //         }
    //         if (point.isXOdd()) {
    //             key[31] |= (byte) 0x80;
    //         }
    //         ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    //         DataOutputStream dos = new DataOutputStream(byteOs);
    //         dos.writeInt("ssh-ed25519".getBytes(StandardCharsets.US_ASCII).length);
    //         dos.write("ssh-ed25519".getBytes(StandardCharsets.US_ASCII));
    //         dos.writeInt(key.length);
    //         dos.write(key);
    //         return "ssh-ed25519 " + Base64.getEncoder().encodeToString(byteOs.toByteArray());
    //     } catch (NoSuchAlgorithmException | IOException e) {
    //         throw new RuntimeException(e);
    //     }
    // }

    private SampleUtils() {
    }
}
