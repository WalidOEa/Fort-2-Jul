package uk.ac.soton.comp3200.fort2jul.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateReservedKeywords {

    public static void main(String[] args) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/json/grammar_rules.json")));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray reservedKeywords = jsonObject.getJSONArray("keywords");

        generateReservedKeywordsEnum(reservedKeywords);
    }

    private static void generateReservedKeywordsEnum(JSONArray reservedKeywords) throws IOException {
        StringBuilder enumContent = new StringBuilder("package uk.ac.soton.comp3200.fort2jul.lexer;\n\npublic enum ReservedKeywords {\n");

        for (int i = 0; i < reservedKeywords.length(); i++) {
            String keyword = reservedKeywords.getString(i).replace("\"", "").toUpperCase();  // Convert keyword to upper case
            enumContent.append(keyword);  // Add keyword to the enum content

            if (i < reservedKeywords.length() - 1) {
                enumContent.append(",\n");  // Add a comma and a new line if it's not the last keyword
            }
        }

        enumContent.append("\n}\n");  // Close the enum declaration

        Files.write(Paths.get("src/main/java/uk/ac/soton/comp3200/fort2jul/lexer/ReservedKeywords.java"), enumContent.toString().getBytes());
    }
}