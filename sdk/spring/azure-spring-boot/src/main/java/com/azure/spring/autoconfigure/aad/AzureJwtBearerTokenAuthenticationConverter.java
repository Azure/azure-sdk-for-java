package com.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 *  A {@link Converter} that takes a {@link Jwt} and converts it into a {@link PreAuthenticatedAuthenticationToken}.
 */
public class AzureJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    public AzureJwtBearerTokenAuthenticationConverter() {
    }

    public AbstractAuthenticationToken convert(Jwt jwt) {
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        Collection<GrantedAuthority> authorities = token.getAuthorities();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(),
            jwt.getExpiresAt());
        Map<String, Object> attributes = jwt.getClaims();
        JWTClaimsSet.Builder builder = new Builder();
        for (Entry<String, Object> entry : attributes.entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(accessToken.getTokenValue());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        UserPrincipal userPrincipal = new UserPrincipal(accessToken.getTokenValue(), jwsObject, builder.build());
        return new PreAuthenticatedAuthenticationToken(userPrincipal, null, authorities);
    }
}
