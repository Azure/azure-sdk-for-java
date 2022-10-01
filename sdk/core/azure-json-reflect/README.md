# Azure Json Reflect
***
Azure Json Reflect is intended to remove any hard dependencies on Jackson and Gson.
This library uses reflection to generate Json readers and writers at runtime. 
This requires a working version of Jackson or Gson on the class path. 

## Dependencies
***
- Azure.Json (Azure sdk dependent)

## Getting started
***

### Supported versions

#### Jackson

#### Gson

## Key Concepts
***
This package searches the relative class path for a version of Jackson or Gson.
If it finds a valid version, then it uses reflection to get the constructor and 
any required methods for the Json reader or writer to work. It then emulates the behavior 
of an actual Jackson/Gson JsonReader/Writer.

#### Entry point
The entry point to the library is the JsonFactoryBuilder. 
Use JsonFactoryBuilder.Build to get a JsonFactory. This factory will be either a 
JacksonJsonFactory, or a GsonJsonFactory depending on what is found on the class path. 
NOTE: Jackson has priority over Gson - it checks for Jackson before Gson. 

You can also access the library via the Gson/JacksonJsonFactory. However, this requires 
`Package jsonPackage` which is the Jackson/Gson package on the relative class path. Therefore, 
it is recommended that you use the JsonFactoryBuilder as the entry point.



## Examples
***


## Contributing
***
For details on contributing to this repository, see the <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md"> contributing guide</a>.
1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some features`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new pull Request
