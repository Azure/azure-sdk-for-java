import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.AccessTokenType;

import java.time.Duration;
import java.time.OffsetDateTime;

public class AccessTokenApis {
    public static void main(String... args) {

        AccessToken accessToken1 = new AccessToken("token", OffsetDateTime.MAX);
        AccessToken accessToken2 = new AccessToken("token", OffsetDateTime.MAX, OffsetDateTime.now());
        AccessToken accessToken3 = new AccessToken("token", OffsetDateTime.MAX, OffsetDateTime.now(), AccessTokenType.fromString("text"));

        String tokenValue = accessToken1.getToken();
        OffsetDateTime expiresAt = accessToken1.getExpiresAt();
        Boolean isExpired = accessToken1.isExpired();
        OffsetDateTime refreshAt = accessToken2.getRefreshAt();
        String tokenType = accessToken3.getTokenType().getValue();
        Duration duration = Duration.between(OffsetDateTime.now(), accessToken3.getExpiresAt());

    }
}
