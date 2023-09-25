# Android Testing for Azure SDK for Java
The purpose of this project is to improve Android support in the Azure SDK, by determining a baseline level of compatibility at API levels 26 and above. It is currently in-progress, with many priority libraries yet to be investigated.
The project currently contains:
- ported service samples that are set up to run within an Android environment, and may be configured as instrumented tests to be automatically run. 
- instrumented tests to address specific issues identified by users or the Animal Sniffer plugin.
- a Github Actions file to run all instrumented tests with an API level 26 emulator during CI.
- `WikiAdditions.md`, which contains information on sampled/tested service libraries that could be added to the existing [Android Support](https://github.com/Azure/azure-sdk-for-java/wiki/Android-Support) page of the wiki.

The current structure, subject to change, contains two apps:
- `azure-android-compat`, which contains a small number of instrumented tests.
- `azure-samples`, which contains the ported Azure samples.
These two apps could be merged into one, once samples begin to be converted into instrumented tests.

## Testing Efforts
A number of Core libraries have known issues with Android:
- `azure-xml` requires an external [StAX dependency](https://mvnrepository.com/artifact/stax/stax) as Android is missing the `javax.xml.stream package`.
- `azure-core`'s `ReflectionSerializable` class also requires an external StAX dependency.
- `azure-core`'s `ReflectionUtils` causes issues with APIs greater than 27 (resolution in progress).
- `azure-core-management` uses `java.lang.reflect.Type.getTypeName()`, which was added in API level 28.

### Current Investigation Backlog
- Azure Storage
- Azure Identity
- Azure Eventhubs
- Azure Servicebus
- Azure OpenAI
- Azure ResourceManager Compute
