package com.example;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws NoSuchAlgorithmException
    {
        System.out.println(Arrays.toString(Security.getProviders()));

        Provider provider = Security.getProvider("SunEC");

        System.out.println(provider);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", provider);

        System.out.println(generator);
    }
}
