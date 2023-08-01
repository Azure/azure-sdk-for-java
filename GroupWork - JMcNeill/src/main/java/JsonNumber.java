/**
 * Class representing the JSON number type 
 */
public class JsonNumber extends JsonElement{
    private String number;

    public JsonNumber(){

    }

    public JsonNumber(Number value){
        number = value.toString();
    }

    public String toString(){
        return number;
    }

    public boolean isNumber() {
        return true;
    }
}
