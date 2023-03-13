package com.azure.cosmos.implementation.Json;

/// <summary>
/// This is meant more as a friends class. Do not use unless you have a strong reason to.
/// </summary>
public interface IJsonTextReaderPrivateImplementation extends IJsonReader
{
    Utf8Memory getBufferedJsonToken();
}
