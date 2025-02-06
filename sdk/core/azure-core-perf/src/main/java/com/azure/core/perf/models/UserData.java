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

public class UserData implements JsonSerializable<UserData>, XmlSerializable<UserData> {
    private String id;
    private Integer index;
    private String guid;
    private Boolean isActive;
    private String balance;
    private String picture;
    private Integer age;
    private String eyeColor;
    private String company;
    private String about;

    public UserData() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(final Integer index) {
        this.index = index;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(final Boolean active) {
        isActive = active;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(final String balance) {
        this.balance = balance;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(final String picture) {
        this.picture = picture;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(final String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(final String about) {
        this.about = about;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("id", id)
            .writeNumberField("index", index)
            .writeStringField("guid", guid)
            .writeBooleanField("isActive", isActive)
            .writeStringField("balance", balance)
            .writeStringField("picture", picture)
            .writeNumberField("age", age)
            .writeStringField("eyeColor", eyeColor)
            .writeStringField("company", company)
            .writeStringField("about", about)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link UserData} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link UserData}, or null the {@link JsonReader} was pointing to {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static UserData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UserData userData = new UserData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    userData.id = reader.getString();
                } else if ("index".equals(fieldName)) {
                    userData.index = reader.getNullable(JsonReader::getInt);
                } else if ("guid".equals(fieldName)) {
                    userData.guid = reader.getString();
                } else if ("isActive".equals(fieldName)) {
                    userData.isActive = reader.getNullable(JsonReader::getBoolean);
                } else if ("balance".equals(fieldName)) {
                    userData.balance = reader.getString();
                } else if ("picture".equals(fieldName)) {
                    userData.picture = reader.getString();
                } else if ("age".equals(fieldName)) {
                    userData.age = reader.getNullable(JsonReader::getInt);
                } else if ("eyeColor".equals(fieldName)) {
                    userData.eyeColor = reader.getString();
                } else if ("company".equals(fieldName)) {
                    userData.company = reader.getString();
                } else if ("about".equals(fieldName)) {
                    userData.about = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return userData;
        });
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "UserData" : rootElementName;
        return xmlWriter.writeStartElement(finalRootElementName)
            .writeStringElement("id", id)
            .writeNumberElement("index", index)
            .writeStringElement("guid", guid)
            .writeBooleanElement("isActive", isActive)
            .writeStringElement("balance", balance)
            .writeStringElement("picture", picture)
            .writeNumberElement("age", age)
            .writeStringElement("eyeColor", eyeColor)
            .writeStringElement("company", company)
            .writeStringElement("about", about)
            .writeEndElement();
    }

    /**
     * Reads an instance of {@link UserData} from the {@link XmlReader}.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @return An instance of {@link UserData}, or null the {@link XmlReader} was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the {@link XmlReader}.
     */
    public static UserData fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of {@link UserData} from the {@link XmlReader}.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of {@link UserData}, or null the {@link XmlReader} was pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the {@link XmlReader}.
     */
    public static UserData fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "UserData" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            UserData userData = new UserData();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("id".equals(elementName)) {
                    userData.id = reader.getStringElement();
                } else if ("index".equals(elementName)) {
                    userData.index = reader.getNullableElement(Integer::parseInt);
                } else if ("guid".equals(elementName)) {
                    userData.guid = reader.getStringElement();
                } else if ("isActive".equals(elementName)) {
                    userData.isActive = reader.getNullableElement(Boolean::parseBoolean);
                } else if ("balance".equals(elementName)) {
                    userData.balance = reader.getStringElement();
                } else if ("picture".equals(elementName)) {
                    userData.picture = reader.getStringElement();
                } else if ("age".equals(elementName)) {
                    userData.age = reader.getNullableElement(Integer::parseInt);
                } else if ("eyeColor".equals(elementName)) {
                    userData.eyeColor = reader.getStringElement();
                } else if ("company".equals(elementName)) {
                    userData.company = reader.getStringElement();
                } else if ("about".equals(elementName)) {
                    userData.about = reader.getStringElement();
                } else {
                    reader.skipElement();
                }
            }

            return userData;
        });
    }
}
