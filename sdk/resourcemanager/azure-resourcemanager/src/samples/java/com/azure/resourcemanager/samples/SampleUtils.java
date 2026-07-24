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
     * Generates a random password suitable for samples only.
     *
     * @return a random password
     */
    public static String password() {
        // Sample only. Use a secret store (for example, Azure Key Vault) in production code.
        return "Pa5$" + Long.toHexString(System.nanoTime()) + "aA1!";
    }

    /**
     * Generates an SSH public key in OpenSSH format.
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

    private SampleUtils() {
    }
}
