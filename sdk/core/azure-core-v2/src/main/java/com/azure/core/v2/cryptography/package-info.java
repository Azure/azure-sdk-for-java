// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>This package contains cryptography interfaces for Azure SDK client libraries. These interfaces allow client
 * libraries to perform cryptographic operations using asymmetric and symmetric keys, such as encrypting, decrypting,
 * signing, verifying, wrapping, and unwrapping keys. The package also provides classes that can resolve key
 * encryption keys from a given key identifier.</p>
 *
 * <p>Some of the key concepts and features of the cryptography package are:</p>
 *
 * <ul>
 *     <li><strong>Async Key Encryption Key and Key Encryption Key interfaces</strong>: These interfaces define the
 *     methods for encrypting and decrypting keys, also known as key wrapping and unwrapping. They also support signing
 *     and verifying data using the configured key.</li>
 *
 *     <li><strong>Async Key Encryption Key Resolver and Key Encryption Key Resolver interfaces</strong>: These
 *     interfaces define the methods for resolving key encryption keys from a given key identifier. They can be used
 *     to create instances of CryptographyClient.</li>
 * </ul>
 *
 * @see com.azure.core.cryptography.KeyEncryptionKey
 * @see com.azure.core.cryptography.AsyncKeyEncryptionKey
 * @see com.azure.core.cryptography.KeyEncryptionKeyResolver
 * @see com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver
 */
package com.azure.core.v2.cryptography;
