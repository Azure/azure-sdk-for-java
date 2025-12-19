# Azure AI Speech Transcription client library tests for Java

This directory contains tests for the Azure AI Speech Transcription client library for Java.

## Test Structure

The tests are organized as follows:

- **TranscriptionClientTestBase.java**: Base class containing common test infrastructure, helper methods, and validation logic. Includes support for both file-based and URL-based transcription.
- **TranscriptionClientTest.java**: Tests for the synchronous `TranscriptionClient` (14 tests)
- **TranscriptionAsyncClientTest.java**: Tests for the asynchronous `TranscriptionAsyncClient` (16 tests)
- **generated/**: Auto-generated test templates (for reference only)

## Prerequisites

Before running the tests, you need:

1. An Azure Cognitive Services Speech resource. Create one using the [Azure Portal](https://portal.azure.com/).
2. Java Development Kit (JDK) 8 or later
3. Maven 3.x or later
4. A sample audio file for testing (WAV, MP3, or OGG format, shorter than 2 hours, smaller than 250 MB)

## Set Environment Variables

Set the following environment variables to run live tests:

### Windows (PowerShell)

```powershell
$env:SPEECH_ENDPOINT = "https://<your-resource-name>.cognitiveservices.azure.com"
$env:SPEECH_API_KEY = "<your-api-key>"
```

### Windows (Command Prompt)

```cmd
set SPEECH_ENDPOINT=https://<your-resource-name>.cognitiveservices.azure.com
set SPEECH_API_KEY=<your-api-key>
```

### Linux/macOS (Bash)

```bash
export SPEECH_ENDPOINT="https://<your-resource-name>.cognitiveservices.azure.com"
export SPEECH_API_KEY="<your-api-key>"
```

## Configure Test Proxy

The Azure SDK for Java uses a test proxy for recording and playing back HTTP interactions. This library has been migrated to use the test proxy following the [Test Proxy Migration Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/TestProxyMigrationGuide.md).

Test recordings are stored in the [azure-sdk-assets](https://github.com/Azure/azure-sdk-assets) repository and referenced via the `assets.json` file. Configure the test mode by setting the `AZURE_TEST_MODE` environment variable:

### Live Mode (against live service)

```powershell
$env:AZURE_TEST_MODE = "LIVE"
```

This mode makes real HTTP calls to the Azure service. Use this when you want to test against the actual service.

### Record Mode (record interactions)

```powershell
$env:AZURE_TEST_MODE = "RECORD"
```

This mode makes real HTTP calls and records them for later playback. Recordings are managed by the test-proxy tool and can be pushed to the azure-sdk-assets repository using:

```bash
test-proxy push -a assets.json
```

### Playback Mode (use recordings)

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
```

This mode uses previously recorded HTTP interactions instead of making real calls. This is the default mode and doesn't require credentials.

## Running Tests

### Run All Tests

From the `sdk/transcription/azure-ai-speech-transcription` directory:

```bash
mvn clean test
```

### Run Specific Test Class

```bash
mvn test -Dtest=TranscriptionClientTest
```

or

```bash
mvn test -Dtest=TranscriptionAsyncClientTest
```

### Run a Specific Test Method

```bash
mvn test -Dtest=TranscriptionClientTest#testTranscribeSyncBasicFromFile
```

## Test Organization

### Synchronous Tests (TranscriptionClientTest)

Tests for the synchronous `TranscriptionClient` (14 tests), including:

- Basic transcription from file
- Transcription from URL (using publicly accessible audio URL)
- Transcription with language specification
- Transcription with multiple languages
- Transcription with speaker diarization
- Transcription with profanity filtering
- Transcription with word-level timestamps
- Tests using `transcribeWithResponse()` method
- Tests with custom RequestOptions

### Asynchronous Tests (TranscriptionAsyncClientTest)

Tests for the asynchronous `TranscriptionAsyncClient` (16 tests), mirroring the synchronous tests but using reactive programming patterns with `Mono` and `Flux`. Includes additional tests for:

- Transcription from URL (using publicly accessible audio URL)
- Error handling with invalid language codes
- Placeholder tests for empty audio data and cancellation scenarios

## Authentication

The tests support two authentication methods:

1. **Key-based authentication** (default): Uses the API key from `SPEECH_API_KEY` environment variable
2. **Token-based authentication**: Uses Entra ID credentials via `DefaultAzureCredential`

To test with token-based authentication, some tests use `createClient(false, true, sync)` where the first parameter is `false`.

## Troubleshooting

### Common Issues

1. **Missing environment variables**: Ensure `SPEECH_ENDPOINT` and `SPEECH_API_KEY` are set correctly
2. **Missing sample audio file**: Make sure you have a `sample.wav` file in the test directory (WAV, MP3, or OGG format, shorter than 2 hours, smaller than 250 MB)
3. **URL transcription failures**: URL-based transcription requires a specific API key tier that supports this feature. If URL tests fail with 401 errors, verify your Speech resource supports URL transcription.
4. **Test proxy issues**: If playback tests fail, try running in LIVE or RECORD mode first to regenerate recordings
5. **Network issues**: Check your network connection and firewall settings

### Enable Detailed Logging

To enable detailed HTTP logging during tests, set the logging level in your `logback-test.xml` or via environment variables:

```powershell
$env:AZURE_LOG_LEVEL = "verbose"
```

## Additional Resources

- [Azure SDK for Java Test Documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/README.md)
- [TypeSpec Java QuickStart - Adding Tests](https://github.com/Azure/azure-sdk-for-java/wiki/TypeSpec-Java-QuickStart#adding-tests)
- [Azure Speech Service Documentation](https://learn.microsoft.com/azure/cognitive-services/speech-service/)
- [Azure SDK for Java Contributing Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md)

## Test Coverage

The current tests cover:

- ✅ Client instantiation with different authentication methods
- ✅ Basic transcription functionality from files
- ✅ Transcription from publicly accessible URLs
- ✅ Transcription with various options (language, diarization, profanity filter, timestamps)
- ✅ Both synchronous and asynchronous clients
- ✅ Methods with and without `Response` wrappers
- ✅ Custom RequestOptions and headers
- ✅ Error handling (invalid language codes)

Areas for future enhancement:

- ⏳ Empty audio data handling (placeholder test exists)
- ⏳ Cancellation scenarios (placeholder test exists)
- ⬜ Performance tests
- ⬜ Concurrent request handling
- ⬜ Edge cases (very long audio, multiple channels, etc.)

## Recording Sanitizers

The tests use the test-proxy's built-in sanitizers to automatically redact sensitive information from recordings:

- API keys and authentication tokens
- Connection strings and passwords
- Account names and identifiers
- Hostnames in URLs

Some default sanitizers (AZSDK2003, AZSDK2030, AZSDK3430, AZSDK3493) are explicitly removed to preserve resource identifiers needed for proper request matching during playback.

## Managing Test Recordings

### Restore recordings from assets repo

```bash
test-proxy restore -a assets.json
```

### Push new recordings to assets repo

```bash
test-proxy push -a assets.json
```

This creates a new tag in the azure-sdk-assets repository and updates `assets.json` with the new tag reference.
