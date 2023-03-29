// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class MacPoolTest {
    @Test(groups = "unit")
    public void clonesMacWheneverPossible() {
        AtomicLong createdMacs = new AtomicLong(0);
        byte[] masterKeyBytes = TestConfigurations.MASTER_KEY.getBytes(StandardCharsets.UTF_8);
        MacPool macPool = new MacPool(() -> createMac(true, masterKeyBytes, createdMacs));

        MacPool.ReUsableMac firstMac = macPool.take();
        assertThat(firstMac).isNotNull();
        MacPool.ReUsableMac secondMac = macPool.take();
        assertThat(secondMac).isNotNull();

        // expect only 1 MAC to be created via the MacProvider - and then this one will be cloned all the time
        assertThat(createdMacs.get()).isEqualTo(1);
    }

    @Test(groups = "unit")
    public void handlesNonCloneableMacSpiGracefully() {
        AtomicLong createdMacs = new AtomicLong(0);
        byte[] masterKeyBytes = TestConfigurations.MASTER_KEY.getBytes(StandardCharsets.UTF_8);
        MacPool macPool = new MacPool(() -> createMac(false, masterKeyBytes, createdMacs));

        MacPool.ReUsableMac firstMac = macPool.take();
        assertThat(firstMac).isNotNull();
        MacPool.ReUsableMac secondMac = macPool.take();
        assertThat(secondMac).isNotNull();

        // expect 3 MACs to be created via the MacProvider - first is teh rootMac
        // it isn't cloneable - so each of teh two calls to take (if no mac has been returned) would instantiate yet
        // another Mac bad for performance - but functionally correct and necessary because not every MacSpi is
        // guaranteed to support cloning
        // - see https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/HowToImplAProvider.html#Step11
        // - see https://docs.oracle.com/en/java/javase/18/docs/api/java.base/javax/crypto/Mac.html#clone()
        assertThat(createdMacs.get()).isEqualTo(3);
    }

    private static Mac createMac(boolean shouldBeCloneable, byte[] masterKeyBytes, AtomicLong macCreatedCount) {

        byte[] masterKeyDecodedBytes = Utils.Base64Decoder.decode(masterKeyBytes);
        SecretKey signingKey = new SecretKeySpec(masterKeyDecodedBytes, "HMACSHA256");

        Mac macInstance = null;
        try {
            macInstance = Mac.getInstance("HMACSHA256");
            macInstance.init(signingKey);
            macCreatedCount.incrementAndGet();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            fail(e.toString());
        }

        if (shouldBeCloneable) {
            return macInstance;
        }

        Mac notCloneableMac = Mockito.spy(macInstance);
        try {
            Mockito
                .doThrow(new CloneNotSupportedException("Dummy mocked CloneNotSupportedException exception"))
                .when(notCloneableMac)
                .clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return notCloneableMac;
    }
}
