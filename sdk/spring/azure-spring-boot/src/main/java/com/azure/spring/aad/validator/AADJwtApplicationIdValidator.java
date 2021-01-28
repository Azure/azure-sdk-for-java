package com.azure.spring.aad.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

/**
 * Validates the "azp" claim in a {@link Jwt}, that is matches a configured value.
 */
public class AADJwtApplicationIdValidator implements OAuth2TokenValidator<Jwt> {

    private final AADJwtClaimValidator<String> validator;

    /**
     * Constructs a {@link AADJwtApplicationIdValidator} using the provided parameters
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AADJwtApplicationIdValidator(Set<String> aclLists) {
        if (!aclLists.isEmpty()) {
            this.validator = new AADJwtClaimValidator<>(AADTokenClaim.APP_ID, appId -> {
                if (appId == null) {
                    return true;
                }
                return aclLists.contains(appId);
            });
        } else {
            this.validator = new AADJwtClaimValidator<>(AADTokenClaim.APP_ID, appId -> true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }
}
