// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.cosmos.implementation;

import java.nio.file.Path;
import java.nio.file.Files;

final class NativeDriverSmoke {
    private static native void verifyAndCreate(String expectedVersion);

    public static void main(String[] args) throws Exception {
        Path library = Path.of(args[0]).toAbsolutePath();
        Path invalid = Files.createTempFile(library.getParent(), "invalid-", ".library");
        try {
            NativeLibraryLoader.INSTANCE.load(invalid);
            throw new AssertionError("An invalid native library was accepted");
        } catch (UnsatisfiedLinkError expected) {
            // A failed load must not prevent a subsequent valid load.
        } finally {
            Files.delete(invalid);
        }
        NativeLibraryLoader.INSTANCE.load(library);
        NativeLibraryLoader.INSTANCE.load(library);
        Path alternate = Files.copy(library, library.resolveSibling("alternate-" + library.getFileName()));
        try {
            NativeLibraryLoader.INSTANCE.load(alternate);
            throw new AssertionError("Replacing an already-loaded JNI library was accepted");
        } catch (IllegalStateException expected) {
            if (!"A different Cosmos JNI library is already loaded".equals(expected.getMessage())) {
                throw expected;
            }
        } finally {
            Files.delete(alternate);
        }
        try {
            verifyAndCreate("deliberately-incompatible");
            throw new AssertionError("The native ABI version guard accepted a mismatched pin");
        } catch (IllegalStateException expected) {
            if (!"Cosmos native header/library/pin version mismatch".equals(expected.getMessage())) {
                throw expected;
            }
        }
        for (int i = 0; i < 3; i++) {
            verifyAndCreate(args[1]);
        }
        System.out.println("Native driver " + args[1] + ": ABI guard and runtime create/free passed");
    }
}
