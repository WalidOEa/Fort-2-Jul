package uk.ac.soton.comp3200.fort2jul.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenerateTokenType {
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

    }

    public static void main(String[] args) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/json/grammar_rules.json")));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray terminalSymbols = jsonObject.getJSONArray("terminal_symbols");

        generateTokenTypeEnum(terminalSymbols);
    }

    private static void generateTokenTypeEnum(JSONArray terminalSymbols) throws IOException {
        StringBuilder enumContent = new StringBuilder("package uk.ac.soton.comp3200.fort2jul.lexer;\n\npublic enum TokenType {\n");

        Set<String> addedTokens = new HashSet<>();

        // Add terminal symbols
        for (int i = 0; i < terminalSymbols.length(); i++) {
            String symbol = terminalSymbols.getString(i).replaceAll("\"", "").trim().toUpperCase();

            // If the symbol is a special symbol, use its mapped name
            if (specialCharacters.containsKey(symbol)) {
                symbol = specialCharacters.get(symbol);
            }

            // Skip if the token has already been added
            if (addedTokens.contains(symbol)) {
                continue;
            }

            enumContent.append(symbol);
            addedTokens.add(symbol);

            if (i < terminalSymbols.length() - 1) {
                enumContent.append(", ");
                if ((i + 1) % 10 == 0) {
                    enumContent.append("\n");
                } else {
                }
            }
        }

        enumContent.append(", ");

        int specialCharacterCount = 0;
        for (String specialCharacter : specialCharacters.values()) {
            String symbol = specialCharacter.trim().toUpperCase();

            // Skip if the token has already been added
            if (addedTokens.contains(symbol)) {
                continue;
            }

            enumContent.append(symbol);
            addedTokens.add(symbol);

            specialCharacterCount++;
            if (specialCharacterCount % 10 == 0) {
                enumContent.append(",\n");
            } else {
                enumContent.append(", ");
            }
        }

        enumContent.append("\n}\n");

        Files.write(Paths.get("src/main/java/uk/ac/soton/comp3200/fort2jul/lexer/TokenType.java"), enumContent.toString().getBytes());
    }
}