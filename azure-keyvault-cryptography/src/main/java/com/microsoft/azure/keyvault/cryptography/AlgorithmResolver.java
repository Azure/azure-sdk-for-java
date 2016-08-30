/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128CbcHmacSha256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192CbcHmacSha384;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes256Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes256CbcHmacSha512;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw128;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw192;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rsa15;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;

public class AlgorithmResolver {

    public static final AlgorithmResolver Default = new AlgorithmResolver();

    static {
        Default.put(Aes128CbcHmacSha256.AlgorithmName, new Aes128CbcHmacSha256());
        Default.put(Aes192CbcHmacSha384.AlgorithmName, new Aes192CbcHmacSha384());
        Default.put(Aes256CbcHmacSha512.AlgorithmName, new Aes256CbcHmacSha512());

        Default.put(Aes128Cbc.AlgorithmName, new Aes128Cbc());
        Default.put(Aes192Cbc.AlgorithmName, new Aes192Cbc());
        Default.put(Aes256Cbc.AlgorithmName, new Aes256Cbc());

        Default.put(AesKw128.AlgorithmName, new AesKw128());
        Default.put(AesKw192.AlgorithmName, new AesKw192());
        Default.put(AesKw256.AlgorithmName, new AesKw256());

        Default.put(Rsa15.AlgorithmName, new Rsa15());
        Default.put(RsaOaep.AlgorithmName, new RsaOaep());

        // Default.put( Rs256.AlgorithmName, new Rs256() );
        // Default.put( RsNull.AlgorithmName, new RsNull() );
    }

    private final ConcurrentMap<String, Algorithm> _algorithms = new ConcurrentHashMap<String, Algorithm>();

    /// <summary>
    /// Returns the implementation for an algorithm name
    /// </summary>
    /// <param name="algorithmName">The algorithm name</param>
    /// <returns></returns>
    public Algorithm get(String algorithmName) {
        return _algorithms.get(algorithmName);
    }

    public void put(String algorithmName, Algorithm provider) {
        _algorithms.put(algorithmName, provider);
    }

    /// <summary>
    /// Removes an algorithm from the resolver
    /// </summary>
    /// <param name="algorithmName">The algorithm name</param>
    public void remove(String algorithmName) {
        _algorithms.remove(algorithmName);
    }

}
