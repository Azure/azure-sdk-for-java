package com.azure.spring.test;

import org.springframework.util.Assert;

public class EnvironmentVariable {
    public static final String AAD_B2C_CLIENT_ID = System.getenv("AAD_B2C_CLIENT_ID");
    public static final String AAD_B2C_CLIENT_SECRET = System.getenv("AAD_B2C_CLIENT_SECRET");
    public static final String AAD_B2C_PROFILE_EDIT = System.getenv("AAD_B2C_PROFILE_EDIT");
    public static final String AAD_B2C_REPLY_URL = System.getenv("AAD_B2C_REPLY_URL");
    public static final String AAD_B2C_SIGN_UP_OR_SIGN_IN = System.getenv("AAD_B2C_SIGN_UP_OR_SIGN_IN");
    public static final String AAD_B2C_TENANT = System.getenv("AAD_B2C_TENANT");
    public static final String AAD_B2C_USER_EMAIL = System.getenv("AAD_B2C_USER_EMAIL");
    public static final String AAD_B2C_USER_PASSWORD = System.getenv("AAD_B2C_USER_PASSWORD");
    public static final String AAD_MULTI_TENANT_CLIENT_ID = System.getenv("AAD_MULTI_TENANT_CLIENT_ID");
    public static final String AAD_MULTI_TENANT_CLIENT_SECRET = System.getenv("AAD_MULTI_TENANT_CLIENT_SECRET");
    public static final String AAD_SINGLE_TENANT_CLIENT_ID = System.getenv("AAD_SINGLE_TENANT_CLIENT_ID");
    public static final String AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE = System.getenv("AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE");
    public static final String AAD_SINGLE_TENANT_CLIENT_SECRET = System.getenv("AAD_SINGLE_TENANT_CLIENT_SECRET");
    public static final String AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE = System.getenv("AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE");
    public static final String AAD_TENANT_ID_1 = System.getenv("AAD_TENANT_ID_1");
    public static final String AAD_TENANT_ID_2 = System.getenv("AAD_TENANT_ID_2");
    public static final String AAD_USER_NAME_1 = System.getenv("AAD_USER_NAME_1");
    public static final String AAD_USER_NAME_2 = System.getenv("AAD_USER_NAME_2");
    public static final String AAD_USER_PASSWORD_1 = System.getenv("AAD_USER_PASSWORD_1");
    public static final String AAD_USER_PASSWORD_2 = System.getenv("AAD_USER_PASSWORD_2");
    public static final String AZURE_KEYVAULT2_URI = System.getenv("AZURE_KEYVAULT2_URI");
    public static final String AZURE_KEYVAULT_URI = System.getenv("AZURE_KEYVAULT_URI");
    public static final String AZURE_STORAGE_ACCOUNT_KEY = System.getenv("AZURE_STORAGE_ACCOUNT_KEY");
    public static final String AZURE_STORAGE_ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    public static final String AZURE_STORAGE_BLOB = System.getenv("AZURE_STORAGE_BLOB");
    public static final String AZURE_STORAGE_BLOB_ENDPOINT = System.getenv("AZURE_STORAGE_BLOB_ENDPOINT");
    public static final String AZURE_STORAGE_FILE = System.getenv("AZURE_STORAGE_FILE");
    public static final String AZURE_STORAGE_FILE_ENDPOINT = System.getenv("AZURE_STORAGE_FILE_ENDPOINT");
    public static final String KEY_VAULT1_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT1_COMMON_SECRET_VALUE");
    public static final String KEY_VAULT1_SECRET_NAME = System.getenv("KEY_VAULT_SECRET_NAME");
    public static final String KEY_VAULT1_SECRET_VALUE = System.getenv("KEY_VAULT_SECRET_VALUE");
    public static final String KEY_VAULT2_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT2_COMMON_SECRET_VALUE");
    public static final String KEY_VAULT2_SECRET_NAME = System.getenv("KEY_VAULT2_SECRET_NAME");
    public static final String KEY_VAULT2_SECRET_VALUE = System.getenv("KEY_VAULT2_SECRET_VALUE");
    public static final String KEY_VAULT_COMMON_SECRET_NAME = System.getenv("KEY_VAULT_COMMON_SECRET_NAME");
    public static final String KEY_VAULT_SECRET_NAME = System.getenv("KEY_VAULT_SECRET_NAME");
    public static final String KEY_VAULT_SECRET_VALUE = System.getenv("KEY_VAULT_SECRET_VALUE");
    public static final String SPRING_CLIENT_ID = System.getenv("SPRING_CLIENT_ID");
    public static final String SPRING_CLIENT_SECRET = System.getenv("SPRING_CLIENT_SECRET");
    public static final String SPRING_RESOURCE_GROUP = System.getenv("SPRING_RESOURCE_GROUP");
    public static final String SPRING_SUBSCRIPTION_ID = System.getenv("SPRING_SUBSCRIPTION_ID");
    public static final String SPRING_TENANT_ID = System.getenv("SPRING_TENANT_ID");
}
