package com.azure.json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class acts purely as a playground for testing and playing around with
 * the current implementation of the JSON model related classes:
 *      - JsonElement
 *      - JsonArray
 *      - JsonBoolean
 *      - JsonNull
 *      - JsonNumber
 *      - JsonObject
 *      - JsonString
 * NOTE: can be removed later.
 */
public class JsonAPIExamples {
    static JsonElement library = new JsonObject()
    .addProperty("books", new JsonArray()
        .addElement(new JsonObject()
            .addProperty("title", new JsonString("Fairy Tales for Kids"))
            .addProperty("author", new JsonString("Jamie"))
            .addProperty("release year", new JsonNumber(2014))
            .addProperty("child friendly", JsonBoolean.getInstance(true))
        )
        .addElement(new JsonObject()
            .addProperty("title", new JsonString("Applied Robotics"))
            .addProperty("author", new JsonString("Rob"))
            .addProperty("release year", new JsonNumber(2007))
            .addProperty("child friendly", JsonBoolean.getInstance(false))
        )
        .addElement(new JsonObject()
            .addProperty("title", new JsonString("Cooking with the Family"))
            .addProperty("author", new JsonString("Zachery"))
            .addProperty("release year", new JsonNumber(2022))
            .addProperty("child friendly", JsonBoolean.getInstance(true))
        )
        .addElement(new JsonObject()
            .addProperty("title", new JsonString("The History of Space War 1"))
            .addProperty("author", new JsonString("Bronson"))
            .addProperty("release year", new JsonNumber(2043))
            .addProperty("child friendly", JsonBoolean.getInstance(false))
        )
    );

    public static void main(String[] args) throws IOException {
        System.out.println("Original Library Content:\n" + library.toJson() + "\n");

        JsonElement bookLog = library.getProperty("books");
        JsonElement childLibrary = new JsonArray();
        for (int i = 0; i < bookLog.asArray().arrayLength(); i++){
            if (bookLog.getElement(i).getProperty("child friendly").asBoolean().getBooleanValue()){
                childLibrary.addElement(bookLog.getElement(i));
                bookLog.removeElement(i);
            }
        }
        System.out.println("Contents of Child Section:\n" + childLibrary.toJson() + "\n");
        System.out.println("Contents of Adult Section:\n" + library.toJson() + "\n");
        demonstration1();
    }

    public static void demonstration1() throws IOException {
        String path = "src/test/resources/JsonTestFileA.json";
        String content = Files.readString(Paths.get(path));

        JsonBuilder builder = new JsonBuilder();
        JsonElement result = builder.deserialize(content);
        System.out.println("Result of Test 1: Getting JSON Model from file\n" + result.toJson() + "\n");
        demonstration2(result);
    }

    public static void demonstration2(JsonElement element) throws IOException {
        JsonElement database = element.getProperty("movies");
        JsonArray validData = new JsonArray();
        for (int i = 0; i < database.asArray().arrayLength(); i++){
            if(Objects.equals(database.getElement(i).getProperty("release").toString(), "Oct")){
                validData.addElement(database.getElement(i));
            }
        }
        System.out.println("Result of Test 2: All movies in October\n" + validData.toJson() + "\n");
        demonstration3(element);
    }

    public static void demonstration3(JsonElement element) throws IOException {
        JsonElement database = element.getProperty("movies");
        for (int i = 0; i < database.asArray().arrayLength(); i++){
            String movieName = database.getElement(i).getProperty("name").toString();
            movieName = movieName.replace("Movie", "");
            int movieNumber = Integer.parseInt(movieName);
            if(movieNumber % 2 == 0){
                database.getElement(i).addProperty("evenNum", new JsonString("TRUE"));
            }
        }
        String result = element.toJson();
        result = result.replace("},", "},\n");
        System.out.println("Result of Test 3: All even numbered movies are marked\n" + result + "\n");
        demonstration4(element);
    }

    public static void demonstration4(JsonElement element) throws IOException {
        JsonElement database = element.getProperty("movies");
        for (int i = 0; i < database.asArray().arrayLength(); i++){
            int rating = Integer.parseInt(database.getElement(i).getProperty("rating").toString());
            if(rating < 8){
                database.removeElement(i);
            }
        }
        System.out.println("Result of Test 4: Remove Movies rated less than 8\n" + element.toJson() + "\n");

        //String path = "src/test/resources/JsonTestFileEdited.json";
        //Files.writeString(Path.of(path), element.toJson());
    }
}
