package uk.ac.soton.comp3200.fort2jul.tools;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.soton.comp3200.fort2jul.lexer.TokenType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GenerateParser {

    private static final Logger logger = Logger.getLogger(GenerateParser.class);

    private static final Map<String, String> specialCharacters = new HashMap<>();

    static {
        specialCharacters.put("(", "LPAREN");
        specialCharacters.put(")", "RPAREN");
        specialCharacters.put("(/", "LPAREN_SLASH");
        specialCharacters.put("/)", "SLASH_RPAREN");
        specialCharacters.put(",", "COMMA");
        specialCharacters.put(".", "DOT");
        specialCharacters.put(":", "COLON");
        specialCharacters.put("::", "COLON_COLON");
        specialCharacters.put("=", "EQUAL");
        specialCharacters.put("<", "LESS");
        specialCharacters.put(">", "GREATER");
        specialCharacters.put("+", "PLUS");
        specialCharacters.put("**", "STAR_STAR");
        specialCharacters.put("$", "DOLLAR");
        specialCharacters.put("-", "MINUS");
        specialCharacters.put("_", "UNDERSCORE");
        specialCharacters.put("*", "STAR");
        specialCharacters.put("/", "SLASH");
        specialCharacters.put("%", "PERCENT");
        specialCharacters.put("==", "EQUAL_EQUAL");
        specialCharacters.put("!=", "BANG_EQUAL");
        specialCharacters.put("<=", "LESS_EQUAL");
        specialCharacters.put(">=", "GREATER_EQUAL");
        specialCharacters.put("/=", "SLASH_EQUAL");
        specialCharacters.put("=>", "EQUAL_GREATER");
        specialCharacters.put(".eqv.", "EQUAL_EQUAL");
        specialCharacters.put(".neqv.", "BANG_EQUAL");
        specialCharacters.put(".lt.", "LESS");
        specialCharacters.put(".gt.", "GREATER");
        specialCharacters.put(".le.", "LESS_EQUAL");
        specialCharacters.put(".ge.", "GREATER_EQUAL");
        specialCharacters.put(".and.", "AND");
        specialCharacters.put(".or.", "OR");
        specialCharacters.put(".not.", "NOT");
        specialCharacters.put(".eq.", "EQUAL_EQUAL");
        specialCharacters.put(".ne.", "BANG_EQUAL");
        specialCharacters.put(".true.", "TRUE");
        specialCharacters.put(".false.", "FALSE");
        specialCharacters.put("fmt=", "FMT_EQUAL");
        specialCharacters.put("unit=", "UNIT_EQUAL");
        specialCharacters.put("rec=", "REC_EQUAL");
        specialCharacters.put("end=", "END_EQUAL");
        specialCharacters.put("err=", "ERR_EQUAL");
        specialCharacters.put("iostat=", "IOSTAT_EQUAL");
        specialCharacters.put("file=", "FILE_EQUAL");
        specialCharacters.put("status=", "STATUS_EQUAL");
        specialCharacters.put("access=", "ACCESS_EQUAL");
        specialCharacters.put("form=", "FORM_EQUAL");
        specialCharacters.put("recl=", "RECL_EQUAL");
        specialCharacters.put("blank=", "BLANK_EQUAL");
        specialCharacters.put("exist=", "EXIST_EQUAL");
        specialCharacters.put("opened=", "OPENED_EQUAL");
        specialCharacters.put("number=", "NUMBER_EQUAL");
        specialCharacters.put("named=", "NAMED_EQUAL");
        specialCharacters.put("name=", "NAME_EQUAL");
        specialCharacters.put("sequential=", "SEQUENTIAL_EQUAL");
        specialCharacters.put("direct=", "DIRECT_EQUAL");
        specialCharacters.put("formatted=", "FORMATTED_EQUAL");
        specialCharacters.put("unformatted=", "UNFORMATTED_EQUAL");
        specialCharacters.put("nextrec=", "NEXTREC_EQUAL");
        specialCharacters.put("position=", "POSITION_EQUAL");
        specialCharacters.put("action=", "ACTION_EQUAL");
        specialCharacters.put("delim=", "DELIM_EQUAL");
        specialCharacters.put("pad=", "PAD_EQUAL");
        specialCharacters.put("nml=", "NML_EQUAL");
        specialCharacters.put("advance=", "ADVANCE_EQUAL");
        specialCharacters.put("size=", "SIZE_EQUAL");
        specialCharacters.put("eor=", "EOR_EQUAL");
        specialCharacters.put("len=", "LEN_EQUAL");
        specialCharacters.put("kind=", "KIND_EQUAL");
        specialCharacters.put("sign=", "SIGN_EQUAL");
        specialCharacters.put("iolength=", "IOLENGTH_EQUAL");
        specialCharacters.put("read=", "READ_EQUAL");
        specialCharacters.put("write=", "WRITE_EQUAL");
        specialCharacters.put("readwrite=", "READWRITE_EQUAL");
        specialCharacters.put("stat=", "STAT_EQUAL");

        for (TokenType tokenType : TokenType.values()) {
            specialCharacters.put(tokenType.name().toLowerCase(), tokenType.name());
        }
    }

    private static final Map<String, String[]> replacements = new HashMap<>();

    static {
        replacements.put("Dop", new String[]{"ICON", "HCON", "FCON", "SP", "PCON", "RDCON", "BCON", "OCON", "ZCON"});
        replacements.put("Xcon", new String[]{"ICON", "HCON", "FCON", "SP", "PCON", "RDCON", "BCON", "OCON", "ZCON"});
        replacements.put("SPOFF", new String[]{"ICON", "HCON", "FCON", "SP", "PCON", "RDCON", "BCON", "OCON", "ZCON"});
        replacements.put("SPON", new String[]{"ICON", "HCON", "FCON", "SP", "PCON", "RDCON", "BCON", "OCON", "ZCON"});
    }

    private final List<String> disallowedMethods = Arrays.asList("EX_2", "IN_2", "TAB_2", "TAB_9", "EX_6", "TAB_7", "IN_6");

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error("Usage: generate_parser <output directory>");
            logger.info("Exiting with error code 64");

            System.exit(64);
        }

        String outputDir = args[0];

        GenerateParser generateParser = new GenerateParser();
        generateParser.parseJSON("src/main/resources/json/grammar_rules.json", outputDir);
    }

    private void parseJSON(String filePath, String outputDir) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader fileReader = new FileReader(filePath)) {
            logger.info("Parsing json file at " + filePath);

            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
            FileWriter fileWriter = new FileWriter(outputDir + "/Parser.java");

            logger.info("Begin writing to Java file at " + outputDir + "/Parser.java");

            writeImportsPackages(fileWriter);

            writeBoilerplateCode(fileWriter);

            writeParserStateClass(fileWriter);

            writeClassDefinition(fileWriter);

            writeParseTokensMethod(fileWriter);

            writeTopProductionMethod(fileWriter);

            writeRuleMethod(fileWriter, jsonObject);

            writeTerminalSymbolMethod(fileWriter, jsonObject);

            writeHelperMethods(fileWriter);

            fileWriter.write("}");

            logger.info("Finished writing to class");

            fileWriter.close();

            logger.info("Closed file writer");
        } catch (IOException | ParseException e) {
            if (e instanceof IOException) {
                logger.error("File not found at " + outputDir);
            } else {
                logger.error("Error parsing JSON file at " + filePath);
            }

            e.printStackTrace();
        }
    }

    private void writeImportsPackages(FileWriter fileWriter) throws IOException {
        fileWriter.write("package uk.ac.soton.comp3200.fort2jul.parser;\n");
        fileWriter.write("import org.apache.log4j.Logger;\n\n");
        fileWriter.write("import static uk.ac.soton.comp3200.fort2jul.lexer.TokenType.*;\n");
        fileWriter.write("import uk.ac.soton.comp3200.fort2jul.lexer.Token;\n");
        fileWriter.write("import uk.ac.soton.comp3200.fort2jul.lexer.TokenType;\n");
        fileWriter.write("import uk.ac.soton.comp3200.fort2jul.parser.Ast;\n\n");
        fileWriter.write("import java.util.*;\n\n");
    }

    private void writeBoilerplateCode(FileWriter fileWriter) throws IOException {
        fileWriter.write("public class Parser {\n\n");
        fileWriter.write("\tprivate static final Logger logger = Logger.getLogger(Parser.class);\n\n");
        fileWriter.write("\tprivate static List<Token> tokens = new ArrayList<>();\n");
        fileWriter.write("\tprivate static int current = 0;\n");
        fileWriter.write("\tprivate Ast<String> ast;\n");
        fileWriter.write("\tprivate List<Ast.Pair<String, String>> astPairs = new ArrayList<>();\n\n");
    }

    private void writeParserStateClass(FileWriter fileWriter) throws IOException {
        fileWriter.write("\tprivate class ParserState {\n");
        fileWriter.write("\t\tint current;\n");
        fileWriter.write("\t\tList<Ast.Pair<String, String>> astPairs;\n\n");
        fileWriter.write("\t\tParserState(int current, List<Ast.Pair<String, String>> astPairs) {\n");
        fileWriter.write("\t\t\tthis.current = current;\n");
        fileWriter.write("\t\t\tthis.astPairs = new ArrayList<>(astPairs);\n");
        fileWriter.write("\t\t}\n");
        fileWriter.write("\t}\n\n");
    }

    private void writeClassDefinition(FileWriter fileWriter) throws IOException {
        fileWriter.write("\tpublic Parser(List<Token> tokens) {\n");
        fileWriter.write("\t\tthis.tokens = tokens;\n");
        fileWriter.write("\t}\n\n");
    }

    private void writeParseTokensMethod(FileWriter fileWriter) throws IOException {
        fileWriter.write("\tpublic Ast<String> parseTokens() {\n");
        fileWriter.write("\t\tif (!topProduction()) {\n");
        //fileWriter.write("\t\t\tlogger.error(\"Parsing failed\");\n");
        fileWriter.write("\t\t\treturn null;\n\t\t}\n\n");
        //fileWriter.write("\t\tlogger.info(\"Parsing successful\");\n");
        fileWriter.write("\t\treturn constructAst(astPairs, ast);\n");
        fileWriter.write("\t}\n\n");
    }

    private void writeTopProductionMethod(FileWriter fileWriter) throws IOException {
        fileWriter.write("\tpublic boolean topProduction() {\n");
        fileWriter.write("\t\tif (!program()) {\n");
        fileWriter.write("\t\t\tif (!SFVarName()) return false;\n\t\t}\n\n");
        fileWriter.write("\t\treturn true;\n");
        fileWriter.write("\t}\n\n");
    }

    private void writeRuleMethod(FileWriter fileWriter, JSONObject jsonObject) throws IOException {
        JSONObject rules = (JSONObject) jsonObject.get("rules");

        for (Object rule : rules.keySet()) {
            String ruleName = (String) rule;
            JSONArray production = (JSONArray) rules.get(rule);

            if (production.size() > 1) {
                writeMultipleProductionMethod(fileWriter, ruleName, production);
            } else {
                writeProductionMethod(fileWriter, ruleName, production);
            }
        }
    }

    private void writeMultipleProductionMethod(FileWriter fileWriter, String ruleName, JSONArray production) throws IOException {
        //Collections.reverse(productions);

        fileWriter.write("\n\tprivate boolean " + ruleName + "(String parent) {\n");
        fileWriter.write("\t\tParserState state = new ParserState(current, astPairs);\n\n");
        //fileWriter.write("\t\tlogger.info(\"Parsing " + ruleName + "\");\n\n");

        int variableSuffix = 0; // Suffix for variable name
        char variableName = 'a'; // Start from 'a'
        for (Object productionObject : production) {
            JSONObject p = (JSONObject) productionObject;
            JSONArray items = (JSONArray) p.get("production");

            Map<String, Boolean> alphaBoolean = new LinkedHashMap<>();

            for (Object item : items) {
                String itemName = item.toString().replace("\"", "").trim();
                boolean isOptional = itemName.endsWith("?");
                itemName = itemName.replace("?", "");

                if (specialCharacters.containsKey(itemName.toLowerCase())) {
                    itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                }

                if (disallowedMethods.contains(itemName.toUpperCase())) {
                    continue;
                }

                if (itemName.equals("ε")) {
                    itemName = "EPSILON";
                }

                if (replacements.containsKey(itemName)) {
                    itemName.replace("\"", "").trim();

                    StringBuilder replacementBuilder = new StringBuilder();
                    for (String replacement : replacements.get(itemName)) {
                        replacementBuilder.append(replacement + "(\"" + ruleName + "\") || ");
                    }

                    replacementBuilder.delete(replacementBuilder.length() - 4, replacementBuilder.length());

                    String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                    fileWriter.write("\t\tboolean " + variable + " = (" + replacementBuilder + ");\n");
                    alphaBoolean.put(variable, false);

                    variableName++; // Move to the next letter in the alphabet
                    if (variableName > 'z') {
                        variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                        variableSuffix++; // Increment suffix when we start over with 'a'
                    }

                    continue;
                }

                if (itemName.equals(ruleName)) { // Left Recursive

                }

                // Assign each production to a boolean variable
                String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                fileWriter.write("\t\tboolean " + variable + " = " + itemName + "(\"" + ruleName + "\");\n");
                alphaBoolean.put(variable, isOptional);

                variableName++; // Move to the next letter in the alphabet
                if (variableName > 'z') {
                    variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                    variableSuffix++; // Increment suffix when we start over with 'a'
                }
            }

            fileWriter.write("\n\t\tif ((" + constructCondition(alphaBoolean, false) + ") || (" + constructCondition(alphaBoolean, true) + ")) {\n");
            //fileWriter.write("\t\t\tlogger.info(\"Parsed " + ruleName + "\");\n");
            fileWriter.write("\n\t\t\tastPairs.add(new Ast.Pair<>(parent, \"" + ruleName + "\"));\n");
            fileWriter.write("\t\t\treturn true;\n");
            fileWriter.write("\t\t}\n\n");
            fileWriter.write("\t\tcurrent = state.current;\n");
            fileWriter.write("\t\tastPairs = state.astPairs;\n\n");
        }
        //fileWriter.write("\t\tlogger.error(\"Parsing " + ruleName + " failed\");\n\n");
        fileWriter.write("\t\treturn false;\n");
        fileWriter.write("\t}\n");
    }

    private void writeProductionMethod(FileWriter fileWriter, String ruleName, JSONArray production) throws IOException {
        for (Object productionObject : production) {
            JSONObject p = (JSONObject) productionObject;
            JSONArray items = (JSONArray) p.get("production");

            fileWriter.write("\n\tprivate boolean " + ruleName + "(String parent) {\n");
            fileWriter.write("\t\tParserState state = new ParserState(current, astPairs);\n\n");
            //fileWriter.write("\t\tlogger.info(\"Parsing " + ruleName + "\");\n\n");

            Map<String, Boolean> alphaBoolean = new LinkedHashMap<>();

            int variableSuffix = 0;
            char variableName = 'a';

            boolean blah = false;
            boolean yep = false;

            for (Object item : items) {
                String itemName = item.toString();

                if (disallowedMethods.contains(itemName.toUpperCase())) {
                    continue;
                }

                if (replacements.containsKey(itemName)) {
                    itemName.replace("\"", "").trim();

                    StringBuilder replacementBuilder = new StringBuilder();
                    for (String replacement : replacements.get(itemName)) {
                        replacementBuilder.append(replacement + "(\"" + ruleName + "\") || ");
                    }

                    replacementBuilder.delete(replacementBuilder.length() - 4, replacementBuilder.length());

                    String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                    fileWriter.write("\t\tboolean " + variable + " = (" + replacementBuilder + ");\n");
                    alphaBoolean.put(variable, false);

                    variableName++; // Move to the next letter in the alphabet
                    if (variableName > 'z') {
                        variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                        variableSuffix++; // Increment suffix when we start over with 'a'
                    }

                    continue;
                }

                if (itemName.endsWith("?")) {
                    itemName = item.toString().replace("\"", "").trim();
                    boolean isOptional = itemName.endsWith("?");
                    itemName = itemName.replace("?", "");

                    if (specialCharacters.containsKey(itemName.toLowerCase())) {
                        itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                    }

                    if (itemName.equals("ε")) {
                        itemName = "EPSILON";
                    }

                    String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                    fileWriter.write("\t\tboolean " + variable + " = " + itemName + "(\"" + ruleName + "\");\n");
                    alphaBoolean.put(variable, isOptional);

                    variableName++; // Move to the next letter in the alphabet
                    if (variableName > 'z') {
                        variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                        variableSuffix++; // Increment suffix when we start over with 'a'
                    }
                } else if (itemName.endsWith("+")) {
                    itemName = item.toString().replace("\"", "").trim();
                    boolean isOptional = false;
                    itemName = itemName.replace("+", "");

                    if (specialCharacters.containsKey(itemName.toLowerCase())) {
                        itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                    }

                    if (itemName.equals("ε")) {
                        itemName = "EPSILON";
                    }

                    // Assign each production to a boolean variable
                    String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                    fileWriter.write("\t\tboolean " + variable + " = " + itemName + "(\"" + ruleName + "\");\n");
                    fileWriter.write("\t\twhile (" + itemName + "(\"" + ruleName + "\"));\n");
                    alphaBoolean.put(variable, isOptional);

                    variableName++; // Move to the next letter in the alphabet
                    if (variableName > 'z') {
                        variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                        variableSuffix++; // Increment suffix when we start over with 'a'
                    }
                } else if (itemName.startsWith("{") && itemName.endsWith("}*")) {
                    itemName = item.toString().replace("\"", "");
                    String[] groupItems = itemName.replace("{", "").replace("}*", "").split(" ");
                    StringBuilder groupBuilder = new StringBuilder();

                    for (int i = 0; i < groupItems.length; i++) {
                        String groupItem = groupItems[i];
                        if (specialCharacters.containsKey(groupItem.toLowerCase())) {
                            groupItem = specialCharacters.get(groupItem.toLowerCase()).toUpperCase();
                        }

                        if (groupItem.equals("ε")) {
                            groupItem = "EPSILON";
                        }

                        if (i == groupItems.length - 1)
                            groupBuilder.append(groupItem + "(\"" + ruleName + "\")");
                        else
                            groupBuilder.append(groupItem + "(\"" + ruleName + "\") && ");
                    }

                    if (items.size() == 1) {
                        fileWriter.write("\t\twhile (" + groupBuilder + ");\n\n");
                        fileWriter.write("\t\tastPairs.add(new Ast.Pair<>(parent, \"" + ruleName + "\"));\n");
                        fileWriter.write("\t\treturn true;\n");
                        fileWriter.write("\t}\n");
                        blah = true;
                    } else {
                        fileWriter.write("\t\twhile (" + groupBuilder + ");\n");
                    }

                    yep = true;
                } else if (itemName.endsWith("*") && items.size() == 1) {
                    itemName = item.toString().replace("\"", "").trim();
                    itemName = itemName.replace("*", "");

                    if (specialCharacters.containsKey(itemName.toLowerCase())) {
                        itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                    }

                    if (itemName.equals("ε")) {
                        itemName = "EPSILON";
                    }

                    fileWriter.write("\t\twhile (" + itemName + "(\"" + ruleName + "\"));\n\n");
                    fileWriter.write("\t\tastPairs.add(new Ast.Pair<>(parent, \"" + ruleName + "\"));\n");
                    fileWriter.write("\t\treturn true;\n");
                    fileWriter.write("\t}\n");

                    blah = true;
                } else if (itemName.endsWith("*")) {
                    itemName = item.toString().replace("\"", "").trim();
                    itemName = itemName.replace("*", "");

                    if (specialCharacters.containsKey(itemName.toLowerCase())) {
                        itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                    }

                    if (itemName.equals("ε")) {
                        itemName = "EPSILON";
                    }

                    fileWriter.write("\t\twhile (" + itemName + "(\"" + ruleName + "\"));\n");
                } else {
                    itemName = item.toString().replace("\"", "").trim();

                    if (specialCharacters.containsKey(itemName.toLowerCase())) {
                        itemName = specialCharacters.get(itemName.toLowerCase()).toUpperCase();
                    }

                    if (itemName.equals("ε")) {
                        itemName = "EPSILON";
                    }

                    String variable = variableName + (variableSuffix > 0 ? Integer.toString(variableSuffix) : "");
                    fileWriter.write("\t\tboolean " + variable + " = " + itemName + "(\"" + ruleName + "\");\n");
                    alphaBoolean.put(variable, false);

                    variableName++; // Move to the next letter in the alphabet
                    if (variableName > 'z') {
                        variableName = 'a'; // Reset to 'a' if we've gone past 'z'
                        variableSuffix++; // Increment suffix when we start over with 'a'
                    }
                }
            }

            if (blah) {
                blah = false;
            } else {
                if (yep) {
                    fileWriter.write("\n\t\tif ((" + constructCondition(alphaBoolean, false) + ")) {\n");
                    yep = false;
                } else {
                    fileWriter.write("\n\t\tif ((" + constructCondition(alphaBoolean, false) + ") || (" + constructCondition(alphaBoolean, true) + ")) {\n");
                }
                //fileWriter.write("\t\t\tlogger.info(\"Parsed " + ruleName + "\");\n");
                fileWriter.write("\n\t\t\tastPairs.add(new Ast.Pair<>(parent, \"" + ruleName + "\"));\n");
                fileWriter.write("\t\t\treturn true;\n");
                fileWriter.write("\t\t}\n\n");
                fileWriter.write("\t\tcurrent = state.current;\n");
                fileWriter.write("\t\tastPairs = state.astPairs;\n\n");
                //fileWriter.write("\t\tlogger.error(\"Parsing " + ruleName + " failed\");\n\n");
                fileWriter.write("\t\treturn false;\n");
                fileWriter.write("\t}\n");
            }
        }
    }

    private String constructCondition(Map<String, Boolean> alphaBoolean, boolean excludeOptional) {
        StringBuilder conditionBuilder = new StringBuilder();

        for (Map.Entry<String, Boolean> entry : alphaBoolean.entrySet()) {
            if (excludeOptional && entry.getValue()) {
                continue;
            }

            conditionBuilder.append(entry.getKey());

            conditionBuilder.append(" && ");
        }

        // Remove the last " && "
        if (conditionBuilder.length() > 0) {
            conditionBuilder.setLength(conditionBuilder.length() - 4);
        }

        return conditionBuilder.toString();
    }


    private void writeTerminalSymbolMethod(FileWriter fileWriter, JSONObject jsonObject) throws IOException {
        Set<String> generatedMethods = new HashSet<>();

        for (String specialCharacter : specialCharacters.keySet()) {
            specialCharacter = specialCharacter.replace("\"", "").trim();
            specialCharacter = specialCharacters.get(specialCharacter).toUpperCase();

            if (generatedMethods.contains(specialCharacter)) {
                continue;
            }

            fileWriter.write("\tprivate boolean " + specialCharacter + "(String parent) {\n");
            //fileWriter.write("\t\tlogger.info(\"Parsing " + specialCharacter + "\");\n\n");
            fileWriter.write("\t\tif (!match(" + specialCharacter + ")) {\n");
            //fileWriter.write("\t\t\tlogger.error(\"Parsing " + specialCharacter + " failed\");\n\n");
            fileWriter.write("\t\t\treturn false;\n\t\t}\n\n");
            fileWriter.write("\t\tastPairs.add(new Ast.Pair<>(parent, \"" + specialCharacter + "\"));\n");
            fileWriter.write("\t\treturn true;\n\t}\n\n");

            generatedMethods.add(specialCharacter);
        }
    }

    private static void writeHelperMethods(FileWriter fileWriter) throws IOException {
        fileWriter.write("\tprivate Ast<String> constructAst(List<Ast.Pair<String, String>> astPairs, Ast<String> ast) {\n");
        fileWriter.write("\t\tCollections.reverse(astPairs);\n\n");
        fileWriter.write("\t\tfor (Ast.Pair<String, String> pair : astPairs) {\n");
        fileWriter.write("\t\t\tString primary = pair.getPrimary();\n");
        fileWriter.write("\t\t\tString secondary = pair.getSecondary();\n\n");
        fileWriter.write("\t\t\ttry {\n");
        fileWriter.write("\t\t\t\tast.addChildren(primary, secondary);\n");
        fileWriter.write("\t\t\t} catch (NullPointerException e) {\n");
        fileWriter.write("\t\t\t\tlogger.error(\"Error constructing AST\");\n");
        fileWriter.write("\t\t\t}\n");
        fileWriter.write("\t\t}\n");
        fileWriter.write("\t\treturn ast;\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate boolean match(TokenType... tokenType) {\n");
        fileWriter.write("\t\tfor (TokenType type : tokenType) {\n");
        fileWriter.write("\t\t\tif (check(type)) {\n");
        fileWriter.write("\t\t\t\tadvance();\n");
        fileWriter.write("\t\t\t\treturn true;\n");
        fileWriter.write("\t\t\t}\n");
        fileWriter.write("\t\t}\n");
        fileWriter.write("\t\treturn false;\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate boolean check(TokenType tokenType) {\n");
        fileWriter.write("\t\tif (isAtEnd()) return false;\n");
        fileWriter.write("\t\treturn peek().getType() == tokenType;\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate Token advance() {\n");
        fileWriter.write("\t\tif (!isAtEnd()) current++;\n");
        fileWriter.write("\t\treturn previous();\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate boolean isAtEnd() {\n");
        fileWriter.write("\t\treturn peek().getType() == EOF;\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate Token peek() {\n");
        fileWriter.write("\t\treturn tokens.get(current);\n");
        fileWriter.write("\t}\n\n");

        fileWriter.write("\tprivate Token previous() {\n");
        fileWriter.write("\t\treturn tokens.get(current - 1);\n");
        fileWriter.write("\t}\n");
    }
}