package com.microsoft.azure.services.blob;

/*
 * TODO: Add convenience methods to create/augment a configuration with
 * strongly typed settings
 *
 * TODO: Support for connection strings?
 *
 * TODO: Built-in support for dev fabric?
 */
public class BlobConfiguration {
    public final static String ACCOUNT_NAME = "blob.accountName";
    public final static String ACCOUNT_KEY = "blob.accountKey";
    public final static String URL = "blob.url";
    public final static String TIMEOUT = "blob.timeout";
}
