import com.sun.jdi.InvalidTypeException;

import java.io.StringWriter;

public abstract class JsonElement extends StringWriter {

    public JsonElement() {

    }

    // All throw unless isArray is true.
    public JsonArray addElement(JsonElement element) throws InvalidTypeException {
        if (this.isArray()){
            return (this.asArray()).addElement(element);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonArray addElement(int index, JsonElement element) throws InvalidTypeException {
        if (this.isArray()){
            return (this.asArray()).addElement(index, element);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonArray setElement(int index, JsonElement element) throws InvalidTypeException {
        if (this.isArray()){
            return (this.asArray()).setElement(index, element);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonElement getElement(int index) throws InvalidTypeException {
        if (this.isArray()){
            return (this.asArray()).getElement(index);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonElement removeElement(int index) throws InvalidTypeException {
        if (this.isArray()){
            return (this.asArray()).removeElement(index);
        } else {
            throw new InvalidTypeException();
        }
    }

    // All throw unless isObject is true.
    public JsonObject addProperty(String key, Object element) throws InvalidTypeException {
        if (this.isObject()){
            return (this.asObject()).addProperty(key, element);
        } else {
            throw new InvalidTypeException();
        }
    }
    public JsonObject setProperty(String key, Object element) throws InvalidTypeException {
        if (this.isObject()){
            return (this.asObject()).setProperty(key, element);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonElement getProperty(String key) throws InvalidTypeException {
        if (this.isObject()){
            return (this.asObject()).getProperty(key);
        } else {
            throw new InvalidTypeException();
        }
    }

    public JsonElement removeProperty(String key) throws InvalidTypeException {
        if (this.isObject()){
            return (this.asObject()).removeProperty(key);
        } else {
            throw new InvalidTypeException();
        }
    }

    //Conversion Methods
    public boolean isArray(){return false;}
    public JsonArray asArray(){
        return (JsonArray)this;
    }

    public boolean isObject(){return false;}
    public JsonObject asObject(){
        return (JsonObject)this;
    }

    public boolean isBoolean(){return false;}
    public JsonBoolean asBoolean(){
        return (JsonBoolean) this;
    }

    public boolean isNull(){return false;}
    public JsonNull asNull(){
        return (JsonNull)this;
    }

    public boolean isNumber(){return false;}
    public JsonNumber asNumber(){
        return (JsonNumber)this;
    }

    public boolean isString(){return false;}
    public JsonString asString(){
        return new JsonString();
    }

    public abstract String toString(); // toString is an alias for toJson().
    //public String toJson();
}

