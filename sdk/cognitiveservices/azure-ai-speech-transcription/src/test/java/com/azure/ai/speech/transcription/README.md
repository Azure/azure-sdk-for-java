# Azure AI Speech Transcription client library tests for Java

This directory contains tests for the Azure AI Speech Transcription client library for Java.

## Test Structure

The tests are organized as follows:

- **TranscriptionClientTestBase.java**: Base class containing common test infrastructure, helper methods, and validation logic
- **TranscriptionClientTest.java**: Tests for the synchronous `TranscriptionClient`
- **TranscriptionAsyncClientTest.java**: Tests for the asynchronous `TranscriptionAsyncClient`
- **generated/**: Auto-generated test templates (for reference only)

## Prerequisites

Before running the tests, you need:

1. An Azure Cognitive Services Speech resource. Create one using the [Azure Portal](https://portal.azure.com/).
2. Java Development Kit (JDK) 8 or later
3. Maven 3.x or later
4. A sample audio file for testing (see [SAMPLE_AUDIO_README.md](SAMPLE_AUDIO_README.md))

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

The Azure SDK for Java uses a test proxy for recording and playing back HTTP interactions. Configure the test mode by setting the `AZURE_TEST_MODE` environment variable:

### Live Mode (against live service)

```powershell
$env:AZURE_TEST_MODE = "LIVE"
```

This mode makes real HTTP calls to the Azure service. Use this when you want to test against the actual service.

### Record Mode (record interactions)

```powershell
$env:AZURE_TEST_MODE = "RECORD"
```

This mode makes real HTTP calls and records them for later playback. Recorded sessions are saved in the `session-records` directory.

### Playback Mode (use recordings)

```powershell
$env:AZURE_TEST_MODE = "PLAYBACK"
```

This mode uses previously recorded HTTP interactions instead of making real calls. This is the default mode and doesn't require credentials.

## Running Tests

### Run All Tests

From the `sdk/cognitiveservices/azure-ai-speech-transcription` directory:

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

Tests for the synchronous `TranscriptionClient`, including:

- Basic transcription from file
- Transcription with language specification
- Transcription with multiple languages
- Transcription with speaker diarization
- Transcription with profanity filtering
- Transcription with word-level timestamps
- Tests using `transcribeWithResponse()` method
- Tests with custom RequestOptions

### Asynchronous Tests (TranscriptionAsyncClientTest)

Tests for the asynchronous `TranscriptionAsyncClient`, mirroring the synchronous tests but using reactive programming patterns with `Mono` and `Flux`.

## Authentication

The tests support two authentication methods:

1. **Key-based authentication** (default): Uses the API key from `SPEECH_API_KEY` environment variable
2. **Token-based authentication**: Uses Azure Active Directory credentials via `DefaultAzureCredential`

To test with token-based authentication, some tests use `createClient(false, true, sync)` where the first parameter is `false`.

## Troubleshooting

### Common Issues

1. **Missing environment variables**: Ensure `SPEECH_ENDPOINT` and `SPEECH_API_KEY` are set correctly
2. **Missing sample audio file**: Make sure you have a `sample.wav` file in the test directory (see [SAMPLE_AUDIO_README.md](SAMPLE_AUDIO_README.md))
3. **Test proxy issues**: If playback tests fail, try running in LIVE or RECORD mode first
4. **Network issues**: Check your network connection and firewall settings

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
- ✅ Basic transcription functionality
- ✅ Transcription with various options (language, diarization, profanity filter, timestamps)
- ✅ Both synchronous and asynchronous clients
- ✅ Methods with and without `Response` wrappers
- ✅ Custom RequestOptions and headers

Areas for future enhancement:

- ⬜ Error handling scenarios (invalid input, network errors, etc.)
- ⬜ Performance tests
- ⬜ Concurrent request handling
- ⬜ Edge cases (very long audio, multiple channels, etc.)
