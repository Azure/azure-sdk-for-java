/*
 * 
 * The author contributes this code to the public domain,
 * retaining no rights and incurring no responsibilities for its use in whole or in part.
 */
package com.microsoft.windowsazure.services.management;

import java.io.FileInputStream;

public class Example {
    public static void main(String[] args) throws Exception {
        ConnectionCredential cred = new ConnectionCredential(
        // the .jks file (or other jks stream) containing your management cert bytes
                new FileInputStream("../test.jks"),
                // the password to the cert file and the private key inside it
                "I won't tell you", KeyStoreType.jks);
        APICall call = new APICall("my-subscription", cred);
        System.out.println(call.get());
        //or... call.post(someBodyText);

        cred = new ConnectionCredential(
        // the .pfx file (or other pfx stream) containing your management cert bytes
                new FileInputStream("../test.pfx"),
                // the password to the cert file and the private key inside it
                "I won't tell you", KeyStoreType.pkcs12);
        call = new APICall("my-subscription", cred);
        System.out.println(call.get());

    }
}
