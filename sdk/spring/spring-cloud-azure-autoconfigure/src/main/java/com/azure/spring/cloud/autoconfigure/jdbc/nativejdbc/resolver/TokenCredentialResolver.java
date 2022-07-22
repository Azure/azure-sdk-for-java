package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver;

import com.azure.core.credential.TokenCredential;

import java.util.Map;

public interface TokenCredentialResolver {

    TokenCredential resolve(Map<String,String> map);
}
