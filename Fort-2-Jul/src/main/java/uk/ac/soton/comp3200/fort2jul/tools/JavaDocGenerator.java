package uk.ac.soton.comp3200.fort2jul.tools;

import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.json.JSONException;

public class JavaDocGenerator {
    public static void main(String[] args) throws IOException, JSONException {
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/json/grammar_rules.json")));
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONObject rules = jsonObject.getJSONObject("rules");

        List<String> javaLines = Files.readAllLines(Paths.get("src/main/java/uk/ac/soton/comp3200/fort2jul/parser/Parser.java"));
        List<String> modifiedLines = new ArrayList<>();

        for (String line : javaLines) {
            for (String ruleName : rules.keySet()) {
                JSONArray productions = rules.getJSONArray(ruleName).getJSONObject(0).getJSONArray("production");
                if (line.contains("boolean " + ruleName + "(")) {
                    StringBuilder javadoc = new StringBuilder();
                    javadoc.append("\t/**\n\t* ").append(ruleName).append(" ::= ");
                    for (int i = 0; i < productions.length(); i++) {
                        javadoc.append("\t" + productions.getString(i));
                        if (i < productions.length() - 1) {
                            javadoc.append("\n\t*           | ");
                        }
                    }
                    javadoc.append("\n\t*/");
                    modifiedLines.add(javadoc.toString());
                }
            }
            modifiedLines.add(line);
        }

        Files.write(Paths.get("Parser.java"), modifiedLines);
    }
}