# Android Testing for Azure SDK for Java
The purpose of this project is to improve Android support for the Azure SDK, by determining a baseline level of compatibility at API levels 26 and above. It is currently in-progress, with many service libraries yet to be investigated.
Currently, Azure SDK services are not guaranteed to work in Android, though limited sampling at API levels >= 26 has shown that some services should be relatively compatible with Android.
The project structure currently contains:
- Ported service samples in `azure-samples` that are set up to run within an Android environment. 
- Instrumented tests to automate the running of samples and address specific issues that have been identified.

## Sampled Libraries
The following libraries have been sampled in Android, and are likely to be compatible:
- `azure-data-appconfiguration`
- `azure-security-keyvault-keys`
- `azure-security-keyvault-secrets`
- `azure-security-keyvault-certificates`
- `azure-messaging-eventhubs`

## Known Issues
The following libraries have known issues with Android:
- `azure-core-http-netty` - Netty uses several Android-incompatible methods.  Using `azure-core-http-okhttp` instead resolves this.
- `azure-storage-blob` - This has issues with `azure.core.implementation.jackson.serializable` XmlFactory methods in API 26 (issue Azure/azure-sdk-for-java#37035).
    It is encountering an error with ReflectionUtils in higher API levels.
- `azure-xml` requires an external [StAX dependency](https://mvnrepository.com/artifact/stax/stax) as Android is missing the `javax.xml.stream` package.
- `azure-core`'s `ReflectionSerializable` class also requires an external StAX dependency.

## Dependency management
- Recommend `azure-core` version `1.44.0` or greater.  This adds behaviour to ReflectionUtils that improves Android compatibility.
- Recommend `com.fasterxml.jackson:jackson-core`, `com.fasterxml.jackson:jackson-databind`, `com.fasterxml.jackson:jackson-dataformat-xml`, `com.fasterxml.jackson:jackson-datatype-jsr310` version `2.15.0` or greater for transitive dependencies.  An example of how to do this is found in the build.gradle.kts file in the android-samples app.
- The use of Jackson also requires an external StAX dependency.

## Credential management on Android
- The method used in the samples to pass credentials from System Environment Variables to the sample app on a device or emulator via the BuildConfig class is not suitable for production or use in real apps.  There is a risk of keys being exposed, as data in BuildConfig is stored in plaintext in the APK on the device.  

## Reporting and troubleshooting errors when using the SDK
If you encounter an error caused by the SDK that occurs in Android only, it is best to make an issue in the Azure SDK for Java repository, beginning the issue name with [ANDROID] to help distinguish it.
The following errors are a few examples that may occur when using the SDK in Android:
- `ExceptionInInitializerError`: An exception has occurred in the initializer for a class, and is most likely caused by one of the following two errors.
- `NoClassDefFoundError`: A package may be missing, as the Android JDKs have differences to the standard JDKs.
  - The error message will identify which package needs to be added as a dependency in your Gradle build.
- `NoSuchMethodError`: The SDK may be using a method that is not included in the current dependencies.
  - Updating the dependencies could resolve this issue. If an Azure SDK library is calling older dependency versions transitively, you may need to import the latest version in Gradle and make the build choose that version, like what has been done for the Jackson versions in the build.gradle.kts of the android-samples app.
  - If you refer to documentation and the method is not included in any version of the dependencies available for Android, this could be due to the differences in JDKs. You may need to create an issue in the Azure SDK for Java repository so that this could potentially be resolved.
  - You could also try to run the code in emulators at multiple API levels from 26 and above, as newer API levels may introduce changes that cause incompatibilities. This could help to identify the root cause and assist with resolution.

If you are able to identify possible solutions to errors, i.e using alternate methods that are mutually compatible, then recording them in an issue and/or making a Pull Request to propose the fix could greatly expedite the issue resolution process.
