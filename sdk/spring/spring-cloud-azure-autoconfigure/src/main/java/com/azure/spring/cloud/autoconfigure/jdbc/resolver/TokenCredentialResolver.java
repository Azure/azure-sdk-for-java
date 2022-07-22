package com.azure.spring.cloud.autoconfigure.jdbc.resolver;

import com.azure.core.credential.TokenCredential;

import java.util.Map;

public interface TokenCredentialResolver {

    TokenCredential resolve(Map<String,String> map);
}
