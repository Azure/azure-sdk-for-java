package com.microsoft.windowsazure.services.media.authentication;

import java.util.Map;

import com.microsoft.windowsazure.core.Builder;

/**
 * AzureAdTokenProvider's Factory
 *
 * Internal use.
 */
public class AzureAdTokenFactory implements Builder.Factory<AzureAdTokenProvider>  {

    @Override
    public <S> AzureAdTokenProvider create(String profile, Class<S> service, Builder builder,
            Map<String, Object> properties) {
        return (AzureAdTokenProvider) properties.get(profile);
    }

}
