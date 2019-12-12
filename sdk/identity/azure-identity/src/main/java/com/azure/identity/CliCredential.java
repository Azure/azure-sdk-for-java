package com.azure.identity;

import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.ScopeUtil;
import com.azure.identity.implementation.IdentityClientOptions;


import reactor.core.publisher.Mono;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CliCredential implements TokenCredential {
    private final static String azCommand = "az account get-access-token --resource ";

    public CliCredential(IdentityClientOptions identityClientOptions) {
        super();
    }

	@Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {

        StringBuilder command = new StringBuilder();
        command.append(azCommand);
        String scopes = ScopeUtil.scopesToResource(request.getScopes());
        command.append(scopes);

        AccessToken token = null;
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command.toString());
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                output.append(line);
            }
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            Map<String,String> objectMap = jacksonAdapter.deserialize(output.toString(), Map.class, SerializerEncoding.JSON);
            String accessToken = objectMap.get("accessToken");
            String time = objectMap.get("expiresOn");
            String a = time.substring(0,time.indexOf("."));
            String t = String.join("T", a.split(" "));
            OffsetDateTime expiresOn = LocalDateTime.parse(t,DateTimeFormatter.ISO_LOCAL_DATE_TIME).atOffset(ZoneOffset.UTC);
            token = new AccessToken(accessToken, expiresOn);
        } catch (Exception e) {
            return Mono.error(e);
        }
        return Mono.just(token);
    }
}
