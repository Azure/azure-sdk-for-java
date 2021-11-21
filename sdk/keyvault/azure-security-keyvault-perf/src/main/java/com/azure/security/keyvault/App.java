// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.security.keyvault.certificates.perf.GetCertificateTest;
import com.azure.security.keyvault.keys.perf.DecryptTest;
import com.azure.security.keyvault.keys.perf.GetKeyTest;
import com.azure.security.keyvault.keys.perf.SignTest;
import com.azure.security.keyvault.keys.perf.UnwrapTest;
import com.azure.security.keyvault.secrets.perf.GetSecretTest;
import com.azure.security.keyvault.secrets.perf.ListSecretsTest;

/**
 * Runs the Azure Security Key Vault performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        PerfStressProgram.run(new Class<?>[]{
            GetCertificateTest.class,
            DecryptTest.class,
            GetKeyTest.class,
            SignTest.class,
            UnwrapTest.class,
            GetSecretTest.class,
            ListSecretsTest.class
        }, args);
    }
}
