package com.microsoft.azure.sevicebus.security;

import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.SASUtil;

public class SharedAccessSignatureTokenProvider extends TokenProvider
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SharedAccessSignatureTokenProvider.class);
    
    private String sasKeyName;
    private String sasKey;
    private int tokenValidityInSeconds;
    private String sasToken;
    private Instant sasTokenValidUntil;
    
    public SharedAccessSignatureTokenProvider(String sasKeyName, String sasKey, int tokenValidityInSeconds)
    {
        this.sasKeyName = sasKeyName;
        this.sasKey = sasKey;
        this.tokenValidityInSeconds = tokenValidityInSeconds;
        this.sasToken = null;
    }
    
    public SharedAccessSignatureTokenProvider(String sasToken, Instant sasTokenValidUntil)
    {
        this.sasToken = sasToken;
        this.sasTokenValidUntil= sasTokenValidUntil;
    }
    
    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        if(this.sasToken != null)
        {
            SecurityToken securityToken = new SecurityToken(SecurityTokenType.SAS, audience, this.sasToken, Instant.now(), this.sasTokenValidUntil);
            return CompletableFuture.completedFuture(securityToken);
        }
        else
        {
            CompletableFuture<SecurityToken> tokenGeneratingFuture = new CompletableFuture<>();
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    String genereatedSASToken = SASUtil.generateSharedAccessSignatureToken(this.sasKeyName, this.sasKey, audience, this.tokenValidityInSeconds);
                    tokenGeneratingFuture.complete(new SecurityToken(SecurityTokenType.SAS, audience, genereatedSASToken, Instant.now(), Instant.now().plus(Duration.ofSeconds(this.tokenValidityInSeconds))));
                } catch (InvalidKeyException e) {
                    TRACE_LOGGER.error("SharedAccessSignature token generation failed.", e);
                    tokenGeneratingFuture.completeExceptionally(e);
                }
            });
            
            return tokenGeneratingFuture;
        }
    }

}
