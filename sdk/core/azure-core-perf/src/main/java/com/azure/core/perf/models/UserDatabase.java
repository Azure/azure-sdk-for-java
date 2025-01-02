// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.models;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase implements JsonSerializable<UserDatabase>, XmlSerializable<UserDatabase> {
    private List<UserData> userList;

    public UserDatabase() {
    }

    public List<UserData> getUserList() {
        return userList;
    }

    public void setUserList(List<UserData> userList) {
        this.userList = userList;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeArrayField("userdata", userList, JsonWriter::writeJson)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link UserDatabase} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link UserDatabase}, or null the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static UserDatabase fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UserDatabase userDatabase = new UserDatabase();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("userdata".equals(fieldName)) {
                    userDatabase.userList = reader.readArray(UserData::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            return userDatabase;
        });
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String s) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(s) ? "UserDatabase" : s;
        xmlWriter.writeStartElement(finalRootElementName);
        if (this.userList != null) {
            for (UserData element : this.userList) {
                xmlWriter.writeXml(element);
            }
        }
        return xmlWriter.writeEndElement();
    }

    /**
     * Reads an instance of {@link UserDatabase} from the {@link XmlReader}.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @return An instance of {@link UserDatabase}, or null the {@link XmlReader} was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the {@link XmlReader}.
     */
    public static UserDatabase fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of {@link UserDatabase} from the {@link XmlReader}.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of {@link UserDatabase}, or null the {@link XmlReader} was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the {@link XmlReader}.
     */
    public static UserDatabase fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "UserDatabase" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            UserDatabase userDatabase = new UserDatabase();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("userdata".equals(elementName)) {
                    while (reader.nextElement() != XmlToken.END_ELEMENT) {
                        elementName = reader.getElementName().getLocalPart();

                        if ("UserData".equals(elementName)) {
                            if (userDatabase.userList == null) {
                                userDatabase.userList = new ArrayList<>();
                            }

                            userDatabase.userList.add(UserData.fromXml(xmlReader, "UserData"));
                        }
                    }
                } else {
                    reader.skipElement();
                }
            }

            return userDatabase;
        });
    }
}
