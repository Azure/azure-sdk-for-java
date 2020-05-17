// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A user principal manager to load user info from JWT.
 */
public class UserPrincipalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPrincipalManager.class);

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private final JWKSource<SecurityContext> keySource;
    private final AADAuthenticationProperties aadAuthProps;
    private final Boolean explicitAudienceCheck;
    private final Set<String> validAudiences = new HashSet<>();

    /**ø
     * Creates a new {@link UserPrincipalManager} with a predefined {@link JWKSource}.
     * <p>
     * This is helpful in cases the JWK is not a remote JWKSet or for unit testing.
     *
     * @param keySource - {@link JWKSource} containing at least one key
     */
    public UserPrincipalManager(JWKSource<SecurityContext> keySource) {
        this.keySource = keySource;
        this.explicitAudienceCheck = false;
        this.aadAuthProps = null;
    }

    /**
     * Create a new {@link UserPrincipalManager} based of the {@link ServiceEndpoints#getAadKeyDiscoveryUri()} and
     * {@link AADAuthenticationProperties#getEnvironment()}.
     *
     * @param serviceEndpointsProps -  used to retrieve the JWKS URL
     * @param aadAuthProps          - used to retrieve the environment.
     * @param resourceRetriever     - configures the {@link RemoteJWKSet} call.
     * @param explicitAudienceCheck - explicit audience check
     */
    public UserPrincipalManager(ServiceEndpointsProperties serviceEndpointsProps,
                                AADAuthenticationProperties aadAuthProps,
                                ResourceRetriever resourceRetriever,
                                boolean explicitAudienceCheck) {
        this.aadAuthProps = aadAuthProps;
        this.explicitAudienceCheck = explicitAudienceCheck;
        if (explicitAudienceCheck) {
            // client-id for "normal" check
            this.validAudiences.add(this.aadAuthProps.getClientId());
            // app id uri for client credentials flow (server to server communication)
            this.validAudiences.add(this.aadAuthProps.getAppIdUri());
        }
        try {
            keySource = new RemoteJWKSet<>(new URL(serviceEndpointsProps
                .getServiceEndpoints(aadAuthProps.getEnvironment()).getAadKeyDiscoveryUri()), resourceRetriever);
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to parse active directory key discovery uri.", e);
            throw new IllegalStateException("Failed to parse active directory key discovery uri.", e);
        }
    }

    /**
     * Create a new {@link UserPrincipalManager} based of the {@link ServiceEndpoints#getAadKeyDiscoveryUri()} and
     * {@link AADAuthenticationProperties#getEnvironment()}.
     *
     * @param serviceEndpointsProps - used to retrieve the JWKS URL
     * @param aadAuthProps          - used to retrieve the environment.
     * @param resourceRetriever     - configures the {@link RemoteJWKSet} call.
     * @param jwkSetCache           - used to cache the JWK set for a finite time, default set to 5 minutes
     *                              which matches constructor above if no jwkSetCache is passed in
     * @param explicitAudienceCheck - explicit audience check
     */
    public UserPrincipalManager(ServiceEndpointsProperties serviceEndpointsProps,
                                AADAuthenticationProperties aadAuthProps,
                                ResourceRetriever resourceRetriever,
                                boolean explicitAudienceCheck,
                                JWKSetCache jwkSetCache) {
        this.aadAuthProps = aadAuthProps;
        this.explicitAudienceCheck = explicitAudienceCheck;
        if (explicitAudienceCheck) {
            // client-id for "normal" check
            this.validAudiences.add(this.aadAuthProps.getClientId());
            // app id uri for client credentials flow (server to server communication)
            this.validAudiences.add(this.aadAuthProps.getAppIdUri());
        }
        try {
            keySource = new RemoteJWKSet<>(new URL(serviceEndpointsProps
                .getServiceEndpoints(aadAuthProps.getEnvironment()).getAadKeyDiscoveryUri()),
                resourceRetriever,
                jwkSetCache);
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to parse active directory key discovery uri.", e);
            throw new IllegalStateException("Failed to parse active directory key discovery uri.", e);
        }
    }

    public UserPrincipal buildUserPrincipal(String idToken) throws ParseException, JOSEException, BadJOSEException {
        final JWSObject jwsObject = JWSObject.parse(idToken);
        final ConfigurableJWTProcessor<SecurityContext> validator =
            getAadJwtTokenValidator(jwsObject.getHeader().getAlgorithm());
        final JWTClaimsSet jwtClaimsSet = validator.process(idToken, null);
        final JWTClaimsSetVerifier<SecurityContext> verifier = validator.getJWTClaimsSetVerifier();
        verifier.verify(jwtClaimsSet, null);

        return new UserPrincipal(jwsObject, jwtClaimsSet);
    }

    public boolean isTokenIssuedByAAD(String token) {
        try {
            final JWT jwt = JWTParser.parse(token);
            return isAADIssuer(jwt.getJWTClaimsSet().getIssuer());
        } catch (ParseException e) {
            LOGGER.info("Fail to parse JWT {}, exception {}", token, e);
        }
        return false;
    }

    private static boolean isAADIssuer(String issuer) {
        if (issuer == null) {
            return false;
        }
        return issuer.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER) || issuer.startsWith(STS_WINDOWS_ISSUER)
            || issuer.startsWith(STS_CHINA_CLOUD_API_ISSUER);
    }

    private ConfigurableJWTProcessor<SecurityContext> getAadJwtTokenValidator(JWSAlgorithm jwsAlgorithm) {
        final ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        final JWSKeySelector<SecurityContext> keySelector =
            new JWSVerificationKeySelector<>(jwsAlgorithm, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);

        //TODO: would it make sense to inject it? and make it configurable or even allow to provide own implementation
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<SecurityContext>() {
            @Override
            public void verify(JWTClaimsSet claimsSet, SecurityContext ctx) throws BadJWTException {
                super.verify(claimsSet, ctx);
                final String issuer = claimsSet.getIssuer();
                if (!isAADIssuer(issuer)) {
                    throw new BadJWTException("Invalid token issuer");
                }
                if (explicitAudienceCheck) {
                    final Optional<String> matchedAudience = claimsSet.getAudience().stream()
                        .filter(UserPrincipalManager.this.validAudiences::contains).findFirst();
                    if (matchedAudience.isPresent()) {
                        LOGGER.debug("Matched audience [{}]", matchedAudience.get());
                    } else {
                        throw new BadJWTException("Invalid token audience. Provided value " + claimsSet.getAudience()
                            + "does not match neither client-id nor AppIdUri.");
                    }
                }
            }
        });
        return jwtProcessor;
    }
}
