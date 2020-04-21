package com.azure.messaging.signalr;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AuthenticationPolicy implements HttpPipelinePolicy {
    private final AzureKeyCredential credential;

    public AuthenticationPolicy(final String key) {
        this.credential = new AzureKeyCredential(key);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        byte[] keyBytes = credential.getKey().getBytes(StandardCharsets.UTF_8);

        System.out.println(context.getHttpRequest().getUrl().toString());

        Key key = Keys.hmacShaKeyFor(keyBytes);
        String token = Jwts.builder()
            .setIssuer(null)
            .setHeaderParam("typ", "jwt")
            .setAudience(context.getHttpRequest().getUrl().toString())
            .setExpiration(Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant()))
            .signWith(key)
            .compact();

        context.getHttpRequest().setHeader("Authorization", "Bearer " + token);
        return next.process();
    }
}
