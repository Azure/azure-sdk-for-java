package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.microsoft.windowsazure.core.utils.Base64;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public final class TokenRestrictionTemplateSerializer {

    private TokenRestrictionTemplateSerializer() {

    }

    public static String serialize(TokenRestrictionTemplate template) throws JAXBException {
    	
    	validateTokenRestrictionTemplate(template);
    	
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(TokenRestrictionTemplate.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String[] getPreDeclaredNamespaceUris() {
                return new String[] { XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI };
            }

            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {
                    return "i";
                }
                return suggestion;
            }
        });
        m.marshal(template, writer);
        return writer.toString();
    }

    private static void validateTokenRestrictionTemplate(TokenRestrictionTemplate template) {
    	if (template.getPrimaryVerificationKey() == null && template.getOpenIdConnectDiscoveryDocument() == null) {
            throw new IllegalArgumentException(
                    ErrorMessages.PRIMARY_VERIFICATIONKEY_AND_OPENIDCONNECTDISCOVERYDOCUMENT_ARE_NULL);
        }

        if (template.getOpenIdConnectDiscoveryDocument() != null) {
            if (template.getOpenIdConnectDiscoveryDocument().getOpenIdDiscoveryUri() == null
                    || template.getOpenIdConnectDiscoveryDocument().getOpenIdDiscoveryUri().isEmpty()) {
                throw new IllegalArgumentException(ErrorMessages.OPENIDDISCOVERYURI_STRING_IS_NULL_OR_EMPTY);
            }

            boolean openIdDiscoveryUrlValid = true;
            try {
                new URL(template.getOpenIdConnectDiscoveryDocument().getOpenIdDiscoveryUri());
            } catch (MalformedURLException e) {
                openIdDiscoveryUrlValid = false;
            }

            if (!openIdDiscoveryUrlValid) {
                throw new IllegalArgumentException(ErrorMessages.OPENIDDISCOVERYURI_STRING_IS_NOT_ABSOLUTE_URI);
            }
        }		
	}

	public static TokenRestrictionTemplate deserialize(String xml) throws JAXBException {
        try {
            return deserialize(xml, null);
        } catch (SAXException e) {
            // never reached.
            return null;
        }
    }

    public static TokenRestrictionTemplate deserialize(String xml, String validationSchemaFileName)
            throws JAXBException, SAXException {
        JAXBContext context = JAXBContext.newInstance(TokenRestrictionTemplate.class);
        Unmarshaller u = context.createUnmarshaller();
        if (validationSchemaFileName != null) {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File(validationSchemaFileName));
            u.setSchema(schema);
        }
        TokenRestrictionTemplate template = (TokenRestrictionTemplate) u.unmarshal(new StringReader(xml));
    	validateTokenRestrictionTemplate(template);
        return template;
    }

    private static String generateTokenExpiry(Date expiry) {
        return Long.toString(expiry.getTime() / 1000L);
    }

    @SuppressWarnings("deprecation")
    private static String urlEncode(String toEncode) {
        StringBuilder encoded = new StringBuilder(URLEncoder.encode(toEncode));
        // This code provides uppercase url encoding in order to
        // get generateTestToken test working.
        for (int i = 0; i < encoded.length() - 2; i++) {
            if (encoded.charAt(i) == '%') {
                encoded.setCharAt(i + 1, Character.toLowerCase(encoded.charAt(i + 1)));
                encoded.setCharAt(i + 2, Character.toLowerCase(encoded.charAt(i + 2)));
            }
        }
        return encoded.toString();
    }

    public static String generateTestToken(TokenRestrictionTemplate tokenTemplate, TokenVerificationKey signingKeyToUse,
            UUID keyIdForContentKeyIdentifierClaim, Date tokenExpiration, Date notBefore) {

        if (tokenTemplate == null) {
            throw new NullPointerException("tokenTemplate");
        }

        if (signingKeyToUse == null) {
            signingKeyToUse = tokenTemplate.getPrimaryVerificationKey();
        }

        if (tokenExpiration == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, 10);
            tokenExpiration = cal.getTime();
        }

        if (notBefore == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, -5);
            notBefore = cal.getTime();
        }

        if (tokenTemplate.getTokenType().equals(TokenType.SWT)) {
            return generateTestTokenSWT(tokenTemplate, signingKeyToUse, keyIdForContentKeyIdentifierClaim,
                    tokenExpiration);
        } else {
            return generateTestTokenJWT(tokenTemplate, signingKeyToUse, keyIdForContentKeyIdentifierClaim,
                    tokenExpiration, notBefore);
        }
    }

    public static String generateTestTokenJWT(TokenRestrictionTemplate tokenTemplate,
            TokenVerificationKey signingKeyToUse, UUID keyIdForContentKeyIdentifierClaim, Date tokenExpiration,
            Date notBefore) {

        SymmetricVerificationKey signingKey = (SymmetricVerificationKey) signingKeyToUse;
        SecretKeySpec secretKey = new SecretKeySpec(signingKey.getKeyValue(), "HmacSHA256");

        // Mapping Claims.
        Map<String, Object> claims = new HashMap<String, Object>();
        for (TokenClaim claim : tokenTemplate.getRequiredClaims()) {
            String claimValue = claim.getClaimValue();
            if (claimValue == null && claim.getClaimType().equals(TokenClaim.getContentKeyIdentifierClaimType())) {
                if (keyIdForContentKeyIdentifierClaim == null) {
                    throw new IllegalArgumentException(String.format(
                            "The 'keyIdForContentKeyIdentifierClaim' parameter cannot be null when the token template contains a required '%s' claim type.",
                            TokenClaim.getContentKeyIdentifierClaimType()));
                }
                claimValue = keyIdForContentKeyIdentifierClaim.toString();
            }
            claims.put(claim.getClaimType(), claimValue);
        }

        return Jwts.builder().setHeaderParam("typ", "JWT").setClaims(claims)
                .setIssuer(tokenTemplate.getIssuer().toString()).setAudience(tokenTemplate.getAudience().toString())
                .setIssuedAt(notBefore).setExpiration(tokenExpiration).signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public static String generateTestTokenSWT(TokenRestrictionTemplate tokenTemplate,
            TokenVerificationKey signingKeyToUse, UUID keyIdForContentKeyIdentifierClaim, Date tokenExpiration) {

        StringBuilder builder = new StringBuilder();

        for (TokenClaim claim : tokenTemplate.getRequiredClaims()) {
            String claimValue = claim.getClaimValue();
            if (claim.getClaimType().equals(TokenClaim.getContentKeyIdentifierClaimType())) {
                if (keyIdForContentKeyIdentifierClaim == null) {
                    throw new IllegalArgumentException(String.format(
                            "The 'keyIdForContentKeyIdentifierClaim' parameter cannot be null when the token template contains a required '%s' claim type.",
                            TokenClaim.getContentKeyIdentifierClaimType()));
                }
                claimValue = keyIdForContentKeyIdentifierClaim.toString();
            }
            builder.append(String.format("%s=%s&", urlEncode(claim.getClaimType()), urlEncode(claimValue)));
        }

        builder.append(String.format("Audience=%s&", urlEncode(tokenTemplate.getAudience().toString())));
        builder.append(String.format("ExpiresOn=%s&", generateTokenExpiry(tokenExpiration)));
        builder.append(String.format("Issuer=%s", urlEncode(tokenTemplate.getIssuer().toString())));

        SymmetricVerificationKey signingKey = (SymmetricVerificationKey) signingKeyToUse;
        SecretKeySpec secretKey = new SecretKeySpec(signingKey.getKeyValue(), "HmacSHA256");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] unsignedTokenAsBytes = builder.toString().getBytes();
        byte[] signatureBytes = mac.doFinal(unsignedTokenAsBytes);
        String encoded = new String(Base64.encode(signatureBytes));

        builder.append(String.format("&HMACSHA256=%s", urlEncode(encoded)));

        return builder.toString();
    }

}
