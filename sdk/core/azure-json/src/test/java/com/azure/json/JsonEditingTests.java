package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JsonEditingTests {

    JsonObject object = new JsonObject()
        .setProperty("EntryVariable", new JsonString("First"))
        .setProperty("EntryObject", new JsonObject().setProperty("InnerKey", new JsonNumber(20)))
        .setProperty("EntryArray", new JsonArray().addElement(new JsonString("Value")).addElement(new JsonString("Value2")));
    JsonArray array = new JsonArray()
        .addElement(new JsonString("EntryVariable"))
        .addElement(new JsonObject().setProperty("InnerKey", new JsonString("Data")))
        .addElement(new JsonArray().addElement(JsonNull.getInstance()).addElement(JsonBoolean.getInstance(false)));


    //Tests for add, edit and remove methods.
    @Test
    public void addObjectPropertyNewKey(){
        object.setProperty("New Key", new JsonString("New Value"));
        assertEquals("New Value", object.getProperty("New Key").toString());
    }

    @Test
    public void addObjectPropertyExistingKey(){ //If key already exists, acts as replace.
        object.setProperty("EntryVariable", new JsonString("Changed"));
        assertEquals("Changed", object.getProperty("EntryVariable").toString());
    }

    @Test
    public void addArrayElementNewEntry(){
        array.addElement(new JsonString("New Value"));
        assertEquals("New Value", array.getElement(3).toString());
    }

    @Test
    public void addArrayElementExistingEntry(){ //Test that the value is set to the correct index.
        array.addElement(1, new JsonString("New Value"));
        assertEquals("New Value", array.getElement(1).toString());
    }

    @Test
    public void addArrayElementExistingEntryFullJson() throws IOException { //Test that values behind the index have been properly shifted, and not replaced.
        array.addElement(1, new JsonString("New Value"));
        assertEquals("[\"EntryVariable\",\"New Value\",{\"InnerKey\":\"Data\"},[null,false]]", array.toJson());
    }

//    @Test TODO this behaviour doesn't exist in the current implementation. Can probably remove this test
//    public void addArrayElementBigIndex() throws IOException { //If the index exceeds the size of the array, simply add to the end.
//        array.addElement(400, new JsonString("New Value"));
//        assertEquals("[\"EntryVariable\",{\"InnerKey\":\"Data\"},[null,false],\"New Value\"]", array.toJson());
//    }


    // Editing --------------------------------------------------------

    @Test
    public void editObjectPropertyVariableSameType() throws IOException {
        object.setProperty("EntryVariable", new JsonString("Second"));
        assertEquals("Second", object.getProperty("EntryVariable").toString());
    }

    @Test
    public void editObjectPropertyVariableNewType() throws IOException {
        object.setProperty("EntryVariable", new JsonNumber(6512));
        assertEquals("6512", object.getProperty("EntryVariable").toString());
    }

    @Test
    public void editObjectPropertyVariableNullJsonNumber(){
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                Number numberTest = null;
                object.setProperty("EntryVariable", new JsonNumber(numberTest));
            }
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "true", "false", " 0 0 ", "0 1 2 3", "1. 2", "1 . 2", "1a1", "1-", "-1-", "abc", "-a", "0b01010101", "0B10101", "0x10", "0xFF", "0x1A", ".", "-."})
    public void editObjectPropertyVariableInvalidJsonNumberString(String value){
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                object.setProperty("EntryVariable", new JsonNumber(value));
            }
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" 123", "123 ", " -123", "-123 ", "00000", "0123", "123", "-1934234", "929.12342", "-1.2345", ".12345", "0.0", "-.12345", ".0", "-.0", " .0", ".0 ", "1000000000000000000000000000000", "-1000000000000000000000000000000"})
    public void editObjectPropertyVariableValidJsonNumberString(String value){
        assertDoesNotThrow(
            () -> {
                object.setProperty("EntryVariable", new JsonNumber(value));
            }
        );
    }

    @Test
    public void editObjectPropertyObjectNewType() throws IOException {
        object.setProperty("EntryObject", JsonNull.getInstance());
        assertEquals("null", object.getProperty("EntryObject").toString());
    }

    @Test
    public void editObjectPropertyArrayNewType() throws IOException {
        object.setProperty("EntryArray", JsonBoolean.getInstance(false));
        assertEquals("false", object.getProperty("EntryArray").toString());
    }

//    @Test TODO this does not throw error anymore. Can probably remove test.
//    public void editObjectPropertyNotExist() {
//        assertThrows(IOException.class, ()-> object.setProperty("EntryFake", JsonNull.getInstance()));
//    }
    //-------------------------------------------------------------------------

    @Test
    public void deleteObjectPropertyVariable() throws IOException {
        object.removeProperty("EntryVariable");
        assertEquals("{\"EntryObject\":{\"InnerKey\":20},\"EntryArray\":[\"Value\",\"Value2\"]}", object.toJson());
    }

    @Test
    public void deleteObjectPropertyObject() throws IOException {
        object.removeProperty("EntryObject");
        assertEquals("{\"EntryVariable\":\"First\",\"EntryArray\":[\"Value\",\"Value2\"]}", object.toJson());
    }

    @Test
    public void deleteObjectPropertyArray() throws IOException {
        object.removeProperty("EntryArray");
        assertEquals("{\"EntryVariable\":\"First\",\"EntryObject\":{\"InnerKey\":20}}", object.toJson());
    }

    @Test
    public void deleteObjectPropertyObjectInner() throws IOException {
        // NOTE: removing JsonArray and JsonObject APIs in JsonElement means we
        // must explicitly cast the returned JsonElement type of getElement and
        // getProperty to the appropriate JsonArray or JsonObject type to
        // chain further JsonArray or JsonObject method calls.
        ((JsonObject)object.getProperty("EntryObject")).removeProperty("InnerKey");
        assertEquals("{\"EntryVariable\":\"First\",\"EntryObject\":{},\"EntryArray\":[\"Value\",\"Value2\"]}", object.toJson());
    }

    @Test
    public void deleteObjectPropertyArrayInner() throws IOException {
        // NOTE: removing JsonArray and JsonObject APIs in JsonElement means we
        // must explicitly cast the returned JsonElement type of getElement and
        // getProperty to the appropriate JsonArray or JsonObject type to
        // chain further JsonArray or JsonObject method calls.
        ((JsonArray)object.getProperty("EntryArray")).removeElement(0);
        assertEquals("{\"EntryVariable\":\"First\",\"EntryObject\":{\"InnerKey\":20},\"EntryArray\":[\"Value2\"]}", object.toJson());
    }

    @Test
    public void deleteObjectPropertyNotExist() throws IOException {
        assertNull(object.removeProperty("EntryFake"));
        //assertThrows(IOException.class, ()-> object.removeProperty("EntryFake"));
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    @Test
    public void editArrayElementVariableSameType(){
        array.setElement(0, new JsonString("New Value"));
        assertEquals("New Value", array.getElement(0).toString());
    }

    @Test
    public void editArrayElementVariableNewType(){
        array.setElement(0, new JsonNumber(24));
        assertEquals("24", array.getElement(0).toString());
    }

    @Test
    public void editArrayElementObjectNewType(){
        array.setElement(1, JsonBoolean.getInstance(true));
        assertEquals("true", array.getElement(1).toString());
    }

    @Test
    public void editArrayElementArrayNewType(){
        array.setElement(2, JsonNull.getInstance());
        assertEquals("null", array.getElement(2).toString());
    }


    //---------------------------------------

    @Test
    public void deleteArrayPropertyVariable() throws IOException {
        array.removeElement(0);
        assertEquals("[{\"InnerKey\":\"Data\"},[null,false]]", array.toJson());
    }

    @Test
    public void deleteArrayPropertyObject() throws IOException {
        array.removeElement(1);
        assertEquals("[\"EntryVariable\",[null,false]]", array.toJson());
    }

    @Test
    public void deleteArrayPropertyArray() throws IOException {
        array.removeElement(2);
        assertEquals("[\"EntryVariable\",{\"InnerKey\":\"Data\"}]", array.toJson());
    }

    @Test
    public void deleteArrayPropertyObjectInner() throws IOException {
        // NOTE: removing JsonArray and JsonObject APIs in JsonElement means we
        // must explicitly cast the returned JsonElement type of getElement and
        // getProperty to the appropriate JsonArray or JsonObject type to
        // chain further JsonArray or JsonObject method calls.
        ((JsonObject)array.getElement(1)).removeProperty("InnerKey");
        assertEquals("[\"EntryVariable\",{},[null,false]]", array.toJson());
    }

    @Test
    public void deleteArrayPropertyArrayInner() throws IOException {
        // NOTE: removing JsonArray and JsonObject APIs in JsonElement means we
        // must explicitly cast the returned JsonElement type of getElement and
        // getProperty to the appropriate JsonArray or JsonObject type to
        // chain further JsonArray or JsonObject method calls.
        ((JsonArray)array.getElement(2)).removeElement(1);
        assertEquals("[\"EntryVariable\",{\"InnerKey\":\"Data\"},[null]]", array.toJson());
    }

    @Test
    public void deleteArrayPropertyNotExist(){
        assertThrows(IndexOutOfBoundsException.class, ()-> array.removeElement(3));
        //assertThrows(IOException.class, ()-> object.removeProperty("EntryFake"));
    }


}
