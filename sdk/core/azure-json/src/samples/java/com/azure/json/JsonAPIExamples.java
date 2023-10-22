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
    .setProperty("books", new JsonArray()
        .addElement(new JsonObject()
            .setProperty("title", new JsonString("Fairy Tales for Kids"))
            .setProperty("author", new JsonString("Jamie"))
            .setProperty("release year", new JsonNumber(2014))
            .setProperty("child friendly", JsonBoolean.getInstance(true))
        )
        .addElement(new JsonObject()
            .setProperty("title", new JsonString("Applied Robotics"))
            .setProperty("author", new JsonString("Rob"))
            .setProperty("release year", new JsonNumber(2007))
            .setProperty("child friendly", JsonBoolean.getInstance(false))
        )
        .addElement(new JsonObject()
            .setProperty("title", new JsonString("Cooking with the Family"))
            .setProperty("author", new JsonString("Zachery"))
            .setProperty("release year", new JsonNumber(2022))
            .setProperty("child friendly", JsonBoolean.getInstance(true))
        )
        .addElement(new JsonObject()
            .setProperty("title", new JsonString("The History of Space War 1"))
            .setProperty("author", new JsonString("Bronson"))
            .setProperty("release year", new JsonNumber(2043))
            .setProperty("child friendly", JsonBoolean.getInstance(false))
        )
    );

    public static void main(String[] args) throws IOException {
        System.out.println("Original Library Content:\n" + library.toJson() + "\n");

        JsonArray bookLog = (JsonArray)((JsonObject)library).getProperty("books");
        JsonElement childLibrary = new JsonArray();
        for (int i = 0; i < bookLog.size(); i++){
            JsonBoolean getCheck = (JsonBoolean)((JsonObject)(bookLog.getElement(i))).getProperty("child friendly");
            if (getCheck.getValue()){
                ((JsonArray)childLibrary).addElement(bookLog.getElement(i));
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
        JsonElement result = builder.build(content);
        System.out.println("Result of Test 1: Getting JSON Model from file\n" + result.toJson() + "\n");
        demonstration2(result);
    }

    public static void demonstration2(JsonElement element) throws IOException {
        JsonArray database = (JsonArray)((JsonObject)element).getProperty("movies");
        JsonArray validData = new JsonArray();
        for (int i = 0; i < database.size(); i++){
            if(((JsonObject)(database.getElement(i))).getProperty("release").toString().equals("Oct")){
                validData.addElement(database.getElement(i));
            }
        }
        System.out.println("Result of Test 2: All movies in October\n" + validData.toJson() + "\n");
        demonstration3(element);
    }

    public static void demonstration3(JsonElement element) throws IOException {
        JsonArray database = (JsonArray)((JsonObject)element).getProperty("movies");
        for (int i = 0; i < database.size(); i++){
            String movieName = ((JsonObject)(database.getElement(i))).getProperty("name").toString();
            movieName = movieName.replace("Movie", "");
            int movieNumber = Integer.parseInt(movieName);
            if(movieNumber % 2 == 0){
                ((JsonObject)(database.getElement(i))).setProperty("evenNum", new JsonString("TRUE"));
            }
        }
        String result = element.toJson();
        result = result.replace("},", "},\n");
        System.out.println("Result of Test 3: All even numbered movies are marked\n" + result + "\n");
        demonstration4(element);
    }

    public static void demonstration4(JsonElement element) throws IOException {
        JsonArray database = (JsonArray)((JsonObject)element).getProperty("movies");
        for (int i = 0; i < database.size(); i++){
            int rating = Integer.parseInt(((JsonObject)(database.getElement(i))).getProperty("rating").toString());
            if(rating < 8){
                database.removeElement(i);
            }
        }
        System.out.println("Result of Test 4: Remove Movies rated less than 8\n" + element.toJson() + "\n");

        //String path = "src/test/resources/JsonTestFileEdited.json";
        //Files.writeString(Path.of(path), element.toJson());
    }
}
