package com.azure.identitymiserver;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ManagedIdentityTestController {
    @GetMapping("/mitest")
    public String webapp() {

        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String resourceId = configuration.get("IDENTITY_USER_DEFINED_IDENTITY");
        String account1 = configuration.get("IDENTITY_STORAGE_NAME_1");
        String account2 = configuration.get("IDENTITY_STORAGE_NAME_2");

        ManagedIdentityCredential credential1 = new ManagedIdentityCredentialBuilder().build();
        ManagedIdentityCredential credential2 = new ManagedIdentityCredentialBuilder().resourceId(resourceId).build();


        try {
            credential1.getTokenSync(new TokenRequestContext().addScopes("https://management.azure.com/.default"));
            credential2.getTokenSync(new TokenRequestContext().addScopes("https://management.azure.com/.default"));
            return "Successfully acquired a token from ManagedIdentityCredential";
        } catch (Exception ex) {
            return "Failed to acquire a token from ManagedIdentityCredential";
        }
    }
}
