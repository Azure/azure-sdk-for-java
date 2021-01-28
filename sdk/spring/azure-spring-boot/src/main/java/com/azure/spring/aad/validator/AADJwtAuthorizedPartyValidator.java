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
public class AADJwtAuthorizedPartyValidator implements OAuth2TokenValidator<Jwt> {

    private final AADJwtClaimValidator<String> validator;

    /**
     * Constructs a {@link AADJwtAuthorizedPartyValidator} using the provided parameters
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AADJwtAuthorizedPartyValidator(Set<String> aclLists) {
        if (!aclLists.isEmpty()) {
            this.validator = new AADJwtClaimValidator<>(AADTokenClaim.AZP, azp -> {
                if (azp == null) {
                    return true;
                }
                return aclLists.contains(azp);
            });
        } else {
            this.validator = new AADJwtClaimValidator<>(AADTokenClaim.AZP, azp -> true);
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
