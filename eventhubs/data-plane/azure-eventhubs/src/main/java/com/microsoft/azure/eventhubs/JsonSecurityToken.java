package com.microsoft.azure.eventhubs;

import java.text.ParseException;
import java.util.Date;

import com.microsoft.azure.eventhubs.impl.ClientConstants;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

public class JsonSecurityToken extends SecurityToken {
	public JsonSecurityToken(final String rawToken, final String audience) throws ParseException {
		super(rawToken, GetExpirationDateTimeUtcFromToken(rawToken), audience, ClientConstants.JWT_TOKEN_TYPE);
	}
	
	static Date GetExpirationDateTimeUtcFromToken(final String token) throws ParseException {
		JWT jwt = JWTParser.parse(token);
		JWTClaimsSet claims = jwt.getJWTClaimsSet();
		return claims.getExpirationTime();
	}
}
