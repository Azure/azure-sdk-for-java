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

package com.microsoft.azure.keyvault;

public final class CertificateIdentifier extends ObjectIdentifier {
	
    public static boolean isCertificateIdentifier(String identifier) {
        return ObjectIdentifier.isObjectIdentifier("certificates", identifier);
    }

    public CertificateIdentifier(String vault, String name) {
        this(vault, name, "");
    }

    public CertificateIdentifier(String vault, String name, String version) {
        super(vault, "certificates", name, version);
    }

    public CertificateIdentifier(String identifier) {
        super("certificates", identifier);
    }
}
