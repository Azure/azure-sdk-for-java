package com.azure.spring.aad.resource.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

public class AzureJwtAudienceValidator implements OAuth2TokenValidator<Jwt> {


    private final JwtClaimValidator<List<String>> validator;

    public AzureJwtAudienceValidator(List<String> audiences) {
        Assert.notNull(audiences, "audiences cannot be null");
        this.validator = new JwtClaimValidator(AADTokenClaim.AUD, aud -> audiences.containsAll((List<String>) aud));
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
