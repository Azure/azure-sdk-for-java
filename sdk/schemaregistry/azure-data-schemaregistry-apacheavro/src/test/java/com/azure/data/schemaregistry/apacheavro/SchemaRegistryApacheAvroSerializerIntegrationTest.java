// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.HandOfCards;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests end to end experience of the schema registry class.
 */
public class SchemaRegistryApacheAvroSerializerIntegrationTest extends TestBase {
    static final String SCHEMA_REGISTRY_ENDPOINT = "SCHEMA_REGISTRY_ENDPOINT";
    static final String SCHEMA_REGISTRY_GROUP = "SCHEMA_REGISTRY_GROUP";

    // When we regenerate recordings, make sure that the schema group matches what we are persisting.
    static final String PLAYBACK_TEST_GROUP = "mygroup";

    private String schemaGroup;
    private SchemaRegistryClientBuilder builder;

    @Override
    protected void beforeTest() {
        TokenCredential tokenCredential;
        String endpoint;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = mock(TokenCredential.class);
            schemaGroup = PLAYBACK_TEST_GROUP;

            // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
            when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
                return Mono.fromCallable(() -> {
                    return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
                });
            });

            endpoint = "https://foo.servicebus.windows.net";
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SCHEMA_REGISTRY_ENDPOINT);
            schemaGroup = System.getenv(SCHEMA_REGISTRY_GROUP);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
            assertNotNull(schemaGroup, "'schemaGroup' cannot be null in LIVE/RECORD mode.");
        }

        builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(new RetryPolicy())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy());
        }
    }

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetSchema() throws IOException {
        // Arrange
        final SchemaRegistryClient registryClient = builder.buildClient();
        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(schemaGroup)
            .schemaRegistryAsyncClient(builder.buildAsyncClient())
            .avroSpecificReader(true)
            .buildSerializer();

        final PlayingCard playingCard = PlayingCard.newBuilder()
            .setCardValue(1)
            .setPlayingCardSuit(PlayingCardSuit.SPADES)
            .setIsFaceCard(false)
            .build();
        final PlayingCard playingCard2 = PlayingCard.newBuilder()
            .setCardValue(11)
            .setIsFaceCard(true)
            .setPlayingCardSuit(PlayingCardSuit.DIAMONDS)
            .build();
        final ArrayList<PlayingCard> allCards = new ArrayList<>();
        allCards.add(playingCard);
        allCards.add(playingCard2);

        final HandOfCards cards = HandOfCards.newBuilder()
            .setCards(allCards)
            .build();

        // Register a schema first.
        final Schema handOfCardsSchema = HandOfCards.SCHEMA$;
        final SchemaProperties schemaProperties = registryClient.registerSchema(schemaGroup,
            handOfCardsSchema.getFullName(), handOfCardsSchema.toString(), SchemaFormat.AVRO);

        assertNotNull(schemaProperties);

        // Act & Assert
        byte[] outputArray;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096)) {
            serializer.serialize(outputStream, cards);
            outputArray = outputStream.toByteArray();
        }

        assertTrue(outputArray.length > 0, "There should have been contents in array.");

        final HandOfCards actual;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputArray)) {
            actual = serializer.deserialize(inputStream, TypeReference.createInstance(HandOfCards.class));
        }

        assertNotNull(actual);
        assertNotNull(actual.getCards());
        assertEquals(cards.getCards().size(), actual.getCards().size());
    }

    /**
     * This schema should already exist in the portal.
     */
    @Test
    public void serializeFromPortal() throws IOException {
        // Arrange
        final Schema schema = SchemaBuilder.record("Person").namespace("com.example")
            .fields()
            .name("name").type().stringType().noDefault()
            .name("favourite_number").type().nullable().intType().noDefault()
            .name("favourite_colour").type().nullable().stringType().noDefault()
            .endRecord();

        final GenericData.Record record = new GenericRecordBuilder(schema)
            .set("name", "Pearson")
            .set("favourite_number", 10)
            .set("favourite_colour", "blue")
            .build();

        final SchemaRegistryAsyncClient schemaRegistryAsyncClient = builder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(schemaGroup)
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .avroSpecificReader(true)
            .buildSerializer();

        final SchemaRegistrySchema personSchema = schemaRegistryAsyncClient.getSchema("e4df2ce183664c01afc3ff6e123460d2")
            .block(Duration.ofSeconds(10));
        System.err.println("------------- Start definition -------------");
        System.err.println(personSchema.getDefinition());
        System.err.println("------------- End definition -------------");

        // Act
        byte[] outputArray;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096)) {
            StepVerifier.create(serializer.serializeAsync(outputStream, record))
                .expectComplete()
                .verify(Duration.ofSeconds(30));

            outputArray = outputStream.toByteArray();
        }

        assertTrue(outputArray.length > 0, "There should have been contents in array.");
    }
}
