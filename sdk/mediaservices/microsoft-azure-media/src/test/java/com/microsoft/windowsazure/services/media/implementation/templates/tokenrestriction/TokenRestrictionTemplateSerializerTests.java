package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TokenRestrictionTemplateSerializerTests {

    private final String schemaFile = TokenRestrictionTemplateSerializerTests.class.getClassLoader().getResource("")
            .getPath() + "schemas/TokenRestrictionTemplate.xsd";
    private final String _sampleIssuer = "http://sampleIssuerUrl";
    private final String _sampleAudience = "http://sampleAudience";

    @Test
    public void RoundTripTest() throws JAXBException, URISyntaxException {
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.SWT);

        template.setPrimaryVerificationKey(new SymmetricVerificationKey());
        template.getAlternateVerificationKeys().add(new SymmetricVerificationKey());
        template.setAudience(new URI(_sampleAudience));
        template.setIssuer(new URI(_sampleIssuer));
        template.getRequiredClaims().add(TokenClaim.getContentKeyIdentifierClaim());
        template.getRequiredClaims().add(new TokenClaim("Rental", "true"));

        String serializedTemplate = TokenRestrictionTemplateSerializer.serialize(template);
        assertTrue(serializedTemplate != null && serializedTemplate.length() > 0);

        TokenRestrictionTemplate template2 = TokenRestrictionTemplateSerializer.deserialize(serializedTemplate);
        assertNotNull(template2);
        assertEquals(template.getIssuer(), template2.getIssuer());
        assertEquals(template.getAudience(), template2.getAudience());
        assertEquals(template.getTokenType(), TokenType.SWT);
        SymmetricVerificationKey fromTemplate = (SymmetricVerificationKey) template.getPrimaryVerificationKey();
        SymmetricVerificationKey fromTemplate2 = (SymmetricVerificationKey) template2.getPrimaryVerificationKey();

        assertArrayEquals(fromTemplate.getKeyValue(), fromTemplate2.getKeyValue());
    }

    @Test
    public void KnownGoodInputForSwtOnlyScheme() throws JAXBException {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Audience>http://sampleaudience/</Audience><Issuer>http://sampleissuerurl/</Issuer><PrimaryVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></PrimaryVerificationKey><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims></TokenRestrictionTemplate>";

        TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate);
        assertNotNull(template);
        assertEquals(TokenType.SWT, template.getTokenType());
    }

    @Test
    public void KnownGoodInputForJWT() throws JAXBException {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys /><Audience>http://sampleissuerurl/</Audience><Issuer>http://sampleaudience/</Issuer><PrimaryVerificationKey i:type=\"X509CertTokenVerificationKey\"><RawBody>MIIDAzCCAeugAwIBAgIQ2cl0q8oGkaFG+ZTZYsilhDANBgkqhkiG9w0BAQ0FADARMQ8wDQYDVQQDEwZDQVJvb3QwHhcNMTQxMjAxMTg0NzI5WhcNMzkxMjMxMjM1OTU5WjARMQ8wDQYDVQQDEwZDQVJvb3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDjgMbtZcLtKNdJXHSGQ7l6xJBtNCVhjF4+BLZq+D2RmubKTAnGXhNGY4FO2LrPjfkWumdnv5DOlFuwHy2qrsZu1TFZxxQzU9/Yp3VAD1Afk7ShUOxniPpIO9vfkUH+FEX1Taq4ncR/TkiwnIZLy+bBa0DlF2MsPGC62KbiN4xJqvSIuecxQvcN8MZ78NDejtj1/XHF7VBmVjWi5B79GpTvY9ap39BU8nM0Q8vWb9DwmpWLz8j7hm25f+8laHIE6U8CpeeD/OrZT8ncCD0hbhR3ZGGoFqJbyv2CLPVGeaIhIxBH41zgrBYR53NjkRLTB4IEUCgeTGvSzweqlb+4totdAgMBAAGjVzBVMA8GA1UdEwEB/wQFMAMBAf8wQgYDVR0BBDswOYAQSHiCUWtQlUe79thqsTDbbqETMBExDzANBgNVBAMTBkNBUm9vdIIQ2cl0q8oGkaFG+ZTZYsilhDANBgkqhkiG9w0BAQ0FAAOCAQEABa/2D+Rxo6tp63sDFRViikNkDa5GFZscQLn4Rm35NmUt35Wc/AugLaTJ7iP5zJTYIBUI9DDhHbgFqmYpW0p14NebJlBzrRFIaoHBOsHhy4VYrxIB8Q/OvSGPgbI2c39ni/odyTYKVtJacxPrIt+MqeiFMjJ19cJSOkKT2AFoPMa/L0++znMcEObSAHYMy1U51J1njpQvNJ+MQiR8y2gvmMbGEcMgicIJxbLB2imqJWCQkFUlsrxwuuzSvNaLkdd/HyhsR1JXc+kOREO8gWjhT6MAdgGKC9+neamR7sqwJHPNfcLYTDFOhi6cJH10z74mU1Xa5uLsX+aZp2YYHUFw4Q==</RawBody></PrimaryVerificationKey><RequiredClaims /><TokenType>JWT</TokenType></TokenRestrictionTemplate>";
        TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate);
        assertNotNull(template);
        assertEquals(TokenType.JWT, template.getTokenType());
    }

    @Test
    public void KnownGoodInputForSWT() throws JAXBException {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys /><Audience>http://sampleissuerurl/</Audience><Issuer>http://sampleaudience/</Issuer><PrimaryVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></PrimaryVerificationKey><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims><TokenType>SWT</TokenType></TokenRestrictionTemplate>";
        TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate);
        assertNotNull(template);
        assertEquals(TokenType.SWT, template.getTokenType());
    }

    @Test
    public void InputMissingIssuerShouldThrow() {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>RmFrZVRlc3RLZXk=</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Audience>http://sampleaudience/</Audience><PrimaryVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>RmFrZVRlc3RLZXk=</KeyValue></PrimaryVerificationKey><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims></TokenRestrictionTemplate>";

        try {
            @SuppressWarnings("unused")
            TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate,
                    schemaFile);
            fail("Should throw");
        } catch (JAXBException e) {
            assertTrue(e.getLinkedException().getMessage().contains("Issuer"));
        } catch (SAXException e) {
            fail("Invalid Schema");
        }
    }

    @Test
    public void InputMissingAudienceShouldThrow() {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>RmFrZVRlc3RLZXk=</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Issuer>http://sampleissuerurl/</Issuer><PrimaryVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>RmFrZVRlc3RLZXk=</KeyValue></PrimaryVerificationKey><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims></TokenRestrictionTemplate>";

        try {
            @SuppressWarnings("unused")
            TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate,
                    schemaFile);
            fail("Should throw");
        } catch (JAXBException e) {
            assertTrue(e.getLinkedException().getMessage().contains("Audience"));
        } catch (SAXException e) {
            fail("Invalid Schema");
        }
    }

    @Test
    public void InputMissingPrimaryKeyShouldThrow() {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>RmFrZVRlc3RLZXk=</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Audience>http://sampleaudience/</Audience><Issuer>http://sampleissuerurl/</Issuer><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims></TokenRestrictionTemplate>";

        try {
            @SuppressWarnings("unused")
            TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate,
                    schemaFile);
            fail("Should throw");
        } catch (JAXBException e) {
            assertTrue(e.getLinkedException().getMessage().contains("PrimaryVerificationKey"));
        } catch (SAXException e) {
            fail("Invalid Schema");
        }
    }

    @Test
    public void InputMissingRequiredClaimsOkay() throws JAXBException {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Audience>http://sampleaudience/</Audience><Issuer>http://sampleissuerurl/</Issuer><PrimaryVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></PrimaryVerificationKey></TokenRestrictionTemplate>";

        TokenRestrictionTemplate template = TokenRestrictionTemplateSerializer.deserialize(tokenTemplate);
        assertNotNull(template);
        assertEquals(template.getTokenType(), TokenType.SWT);
    }

    @Test
    public void knownGoodGenerateTestTokenSWT() throws Exception {
        // Arrange
        String expectedToken = "urn%3amicrosoft%3aazure%3amediaservices%3acontentkeyidentifier=24734598-f050-4cbb-8b98-2dad6eaa260a&Audience=http%3a%2f%2faudience.com&ExpiresOn=1451606400&Issuer=http%3a%2f%2fissuer.com&HMACSHA256=2XrNjMo1EIZflJOovHxt9dekEhb2DhqG9fU5MjQy9vI%3d";
        byte[] knownSymetricKey = "64bytes6RNhi8EsxcYsdYQ9zpFuNR1Ks9milykbxYWGILaK0LKzd5dCtYonsr456".getBytes();
        UUID knownGuid = UUID.fromString("24734598-f050-4cbb-8b98-2dad6eaa260a");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date knownExpireOn = sdf.parse("2016-01-01");
        String knownAudience = "http://audience.com";
        String knownIssuer = "http://issuer.com";
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.SWT);
        template.setPrimaryVerificationKey(new SymmetricVerificationKey(knownSymetricKey));
        template.setAudience(new URI(knownAudience));
        template.setIssuer(new URI(knownIssuer));
        template.getRequiredClaims().add(TokenClaim.getContentKeyIdentifierClaim());

        // Act
        String resultsToken = TokenRestrictionTemplateSerializer.generateTestToken(template,
                template.getPrimaryVerificationKey(), knownGuid, knownExpireOn, null);

        // Assert
        assertEquals(expectedToken, resultsToken);
    }

    @Test
    public void knownGoodGenerateTestTokenJWT() throws Exception {
        // Arrange
        String expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ1cm46Y29udG9zbyIsInVybjptaWNyb3NvZnQ6YXp1cmU6bWVkaWFzZXJ2aWNlczpjb250ZW50a2V5aWRlbnRpZmllciI6IjA5MTQ0MzVkLTE1MDAtODBjNC02YzJiLWYxZTUyZmRhNDdhZSIsImlzcyI6Imh0dHBzOi8vdHN0LmNvbnRvc28uY29tIiwiZXhwIjoxNDUxNjA2NDAwLCJpYXQiOjE0MjAwNzA0MDB9.Lv3YphKPyakYwcX3CAcA--VKOrvBG0CuAARejz3DDLM";
        byte[] knownSymetricKey = "64bytes6RNhi8EsxcYsdYQ9zpFuNR1Ks9milykbxYWGILaK0LKzd5dCtYonsr456".getBytes();
        String strUuid = "0914435d-1500-80c4-6c2b-f1e52fda47ae";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date notBefore = sdf.parse("2015-01-01");
        Date expires = sdf.parse("2016-01-01");
        String knownAudience = "urn:contoso";
        String knownIssuer = "https://tst.contoso.com";
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.JWT);
        template.setPrimaryVerificationKey(new SymmetricVerificationKey(knownSymetricKey));
        template.setAudience(new URI(knownAudience));
        template.setIssuer(new URI(knownIssuer));
        template.getRequiredClaims().add(new TokenClaim(TokenClaim.getContentKeyIdentifierClaimType(), strUuid));

        // Act
        String resultsToken = TokenRestrictionTemplateSerializer.generateTestToken(template, null, null, expires, notBefore);

        // Assert
        assertEquals(expectedToken, resultsToken);
    }

    @Test
    public void NullContentKeyIdentifierClaimShouldThrownSWT() throws Exception {
        byte[] knownSymetricKey = "64bytes6RNhi8EsxcYsdYQ9zpFuNR1Ks9milykbxYWGILaK0LKzd5dCtYonsr456".getBytes();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date knownExpireOn = sdf.parse("2016-01-01");
        String knownAudience = "http://audience.com";
        String knownIssuer = "http://issuer.com";
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.SWT);
        template.setPrimaryVerificationKey(new SymmetricVerificationKey(knownSymetricKey));
        template.setAudience(new URI(knownAudience));
        template.setIssuer(new URI(knownIssuer));
        template.getRequiredClaims().add(TokenClaim.getContentKeyIdentifierClaim());

        // Act
        try {
            TokenRestrictionTemplateSerializer.generateTestToken(template,
                    template.getPrimaryVerificationKey(), null, knownExpireOn, null);
            fail("Null ContentKeyIdentifier Claim Should thrown.");
        } catch(IllegalArgumentException e) {
            // Assert
            assertTrue(e.getMessage().contains("keyIdForContentKeyIdentifierClaim"));
        }
    }

    @Test
    public void NullContentKeyIdentifierClaimShouldThrownJWT() throws Exception {
        byte[] knownSymetricKey = "64bytes6RNhi8EsxcYsdYQ9zpFuNR1Ks9milykbxYWGILaK0LKzd5dCtYonsr456".getBytes();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date knownExpireOn = sdf.parse("2016-01-01");
        String knownAudience = "http://audience.com";
        String knownIssuer = "http://issuer.com";
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.JWT);
        template.setPrimaryVerificationKey(new SymmetricVerificationKey(knownSymetricKey));
        template.setAudience(new URI(knownAudience));
        template.setIssuer(new URI(knownIssuer));
        template.getRequiredClaims().add(TokenClaim.getContentKeyIdentifierClaim());

        // Act
        try {
            TokenRestrictionTemplateSerializer.generateTestToken(template,
                    template.getPrimaryVerificationKey(), null, knownExpireOn, null);
            fail("Null ContentKeyIdentifier Claim Should thrown.");
        } catch(IllegalArgumentException e) {
            // Assert
            assertTrue(e.getMessage().contains("keyIdForContentKeyIdentifierClaim"));
        }
    }

    @Test
    public void OpenIdDocumentAsVerificationKeyRoundTrip() throws JAXBException, URISyntaxException
    {
        String openConnectId = "https://openconnectIddiscoveryUri";
        String expectedElement =
            "<OpenIdDiscoveryUri>https://openconnectIddiscoveryUri</OpenIdDiscoveryUri>";

        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.JWT);
        template.setAudience(new URI(_sampleAudience));
        template.setIssuer(new URI(_sampleIssuer));
        OpenIdConnectDiscoveryDocument openId = new OpenIdConnectDiscoveryDocument();
        openId.setOpenIdDiscoveryUri(openConnectId);
        template.setOpenIdConnectDiscoveryDocument(openId);
        String templateAsString = TokenRestrictionTemplateSerializer.serialize(template);
        assertTrue(templateAsString.contains("<PrimaryVerificationKey i:nil=\"true\"/>"));
        assertTrue(templateAsString.contains(expectedElement));
        TokenRestrictionTemplate output = TokenRestrictionTemplateSerializer.deserialize(templateAsString);
        assertNotNull(output);
        assertNotNull(output.getOpenIdConnectDiscoveryDocument());
        assertNull(output.getPrimaryVerificationKey());
        assertTrue(output.getAlternateVerificationKeys().isEmpty());
        assertEquals(output.getOpenIdConnectDiscoveryDocument().getOpenIdDiscoveryUri(), openConnectId);

    }

    @Test
    public void TokenRestrictionTemplateSerializeNotPrimaryKeyAndNoOpenConnectIdDocument() throws URISyntaxException
    {
        TokenRestrictionTemplate template = new TokenRestrictionTemplate(TokenType.JWT);
        template.setAudience(new URI(_sampleAudience));
        template.setIssuer(new URI(_sampleIssuer));
        try {
            TokenRestrictionTemplateSerializer.serialize(template);
            fail();
        }
        catch (Exception ex) {
            assertEquals("Both PrimaryVerificationKey and OpenIdConnectDiscoveryDocument are null.", ex.getMessage());
        }
    }

    @Test
    public void InputMissingPrimaryKeyShouldNotThrow()
    {
        String tokenTemplate = "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\"><AlternateVerificationKeys><TokenVerificationKey i:type=\"SymmetricVerificationKey\"><KeyValue>FakeTestKey</KeyValue></TokenVerificationKey></AlternateVerificationKeys><Audience>http://sampleaudience/</Audience><Issuer>http://sampleissuerurl/</Issuer><RequiredClaims><TokenClaim><ClaimType>urn:microsoft:azure:mediaservices:contentkeyidentifier</ClaimType><ClaimValue i:nil=\"true\" /></TokenClaim><TokenClaim><ClaimType>urn:myservice:claims:rental</ClaimType><ClaimValue>true</ClaimValue></TokenClaim></RequiredClaims></TokenRestrictionTemplate>";
        try {
			TokenRestrictionTemplateSerializer.deserialize(tokenTemplate);
			fail();
		} catch (Exception ex) {
            assertEquals("Both PrimaryVerificationKey and OpenIdConnectDiscoveryDocument are null.", ex.getMessage());
		}
    }

    @Test
    public void TokenRestrictionTemplateDeserializeNotAbsoluteDiscoveryUri()
    {
        String body =
            "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\" ><AlternateVerificationKeys /><Audience>http://sampleissuerurl/</Audience><Issuer>http://sampleaudience/</Issuer><OpenIdConnectDiscoveryDocument ><OpenIdDiscoveryUri >RelativeUri</OpenIdDiscoveryUri></OpenIdConnectDiscoveryDocument></TokenRestrictionTemplate>";

        try
        {
            TokenRestrictionTemplateSerializer.deserialize(body);
            fail();
        }
        catch (Exception ex)
        {
        	assertEquals("String representation of OpenIdConnectDiscoveryDocument.OpenIdDiscoveryUri is not valid absolute Uri.", ex.getMessage());
        }
    }

    @Test
    public void TokenRestrictionTemplateDeserializeNilOpenConnectIdDocumentUriNoPrimaryKey()
    {
        String body =
            "<TokenRestrictionTemplate xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/Azure/MediaServices/KeyDelivery/TokenRestrictionTemplate/v1\" ><AlternateVerificationKeys /><Audience>http://sampleissuerurl/</Audience><Issuer>http://sampleaudience/</Issuer><OpenIdConnectDiscoveryDocument ><OpenIdDiscoveryUri i:nil=\"true\"></OpenIdDiscoveryUri></OpenIdConnectDiscoveryDocument></TokenRestrictionTemplate>";
        try
        {
            TokenRestrictionTemplateSerializer.deserialize(body);
            fail();
        }
        catch (Exception ex)
        {
        	assertEquals("OpenIdConnectDiscoveryDocument.OpenIdDiscoveryUri string value is null or empty.", ex.getMessage());
        }
    }
}
