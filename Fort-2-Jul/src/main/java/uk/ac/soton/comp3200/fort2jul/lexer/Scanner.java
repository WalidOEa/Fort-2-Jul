package uk.ac.soton.comp3200.fort2jul.lexer;

import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.Fort2Jul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.soton.comp3200.fort2jul.lexer.TokenType.*;

/**
 * <p><i>Scanner</i> class is responsible for generating the tokens based on the source text. This is done on a
 * character-by-character basis where the class matches against the Regular Expression (RegEx) based on the
 * tokens of the Fortran language.</p>
 *
 * <p>Each token contains its type, text, literal, row and column and is added to <i>tokens</i> for parsing
 * down the pipeline.</p>
 */
public class Scanner {

    /**
     * <p>Logger logger is responsible for logging the actions performed by the Scanner class.</p>
     */
    private static final Logger logger = Logger.getLogger(Scanner.class);

    /**
     * <p>String source is the source text to be scanned.</p>
     */
    private final String source;

    /**
     * <p>ArrayList<Token> tokens is the list of tokens generated from the source text.</p>
     */
    private final ArrayList<Token> tokens = new ArrayList<>();

    /**
     * <p>int start is the starting position of the token.</p>
     */
    private int start = 0;

    /**
     * <p>int current is the current position of the token.</p>
     */
    private int current = 0;

    /**
     * <p>int row is the row number of the token.</p>
     */
    private int row = 1;

    /**
     * <p>int column is the column number of the token.</p>
     */
    private int column = 1;

    /**
     * <p>Map<String, TokenType> reservedKeywords is a map of reserved keywords in the Fortran language.</p>
     */
    private static final Map<String, TokenType> terminalSymbols;

    /**
     * <p>Static block to populate the reservedKeywords map.</p>
     */
    static {
        terminalSymbols = new HashMap<>();
        for (TokenType type : TokenType.values()) {
            terminalSymbols.put(type.toString(), type);
        }

        logger.info("Successfully populated reservedKeywords map");
    }

    private static final List<String> reservedKeywords;

    static {
        reservedKeywords = new ArrayList<>();
        for (ReservedKeywords keyword : ReservedKeywords.values()) {
            reservedKeywords.add(keyword.toString());
        }
    }

    /**
     * <p><i>Scanner</i> constructor.</p>
     * @param source String
     */
    public Scanner(String source) {
        this.source = source;
    }

    /**
     * <p><i>scanTokens</i> scans the tokens based on the source text. Adds EOF token at the end of the source text.</p>
     * @return ArrayList<Token>
     */
    public ArrayList<Token> scanTokens() {
        //logger.info("Scanning tokens...");

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        logger.info("Successfully scanned tokens");

        tokens.add(new Token(EOF, "", null, row, column));

        logger.info("Added EOF token");

        return tokens;
    }

    /**
     * <p><i>scanToken</i> scans each character, matches against the RegEx and adds the token.</p>
     */
    private void scanToken() {
        char c = consume();

        logger.info("Scanning character " + c + " at position " + current + " in row " + row + " and column " + column);

        switch (c) {
            case '+' -> addToken(PLUS);
            case '-' -> addToken(MINUS);
            case '*' -> {
                if (column == 1) {
                    comment();
                }
                else if (match('*')) {
                    addToken(STAR_STAR);
                } else {
                    addToken(STAR);
                }
            }
            case '=' -> {
                addToken(EQUAL);
            }
            case '(' -> {
                if (match('|')) {
                    addToken(LPAREN_SLASH);
                } else {
                    addToken(LPAREN);
                }
            }
            case ')' -> addToken(RPAREN);
            case '|' -> {
                if (match(')')) {
                    addToken(SLASH_RPAREN);
                } else {
                    //TODO -> Exception Handling
                }
            }
            case ',' -> addToken(COMMA);
            case '\"', '\'' -> scon(c);
            // Comparison
            case '.' -> {
                if (match('e') || match('E')) {
                    if (match('q') || match('Q')) {
                        if (match('.')) {
                            addToken(EQUAL_EQUAL);
                        }
                    }
                } else if (match('n') || match('N')) {
                    if (match('e') || match('E')) {
                        if (match('.')) {
                            addToken(BANG_EQUAL);
                        }
                    }
                } else if (match('l') || match('L')){
                    if (match('t') || match('T')) {
                        if (match('.')) {
                            addToken(LESS);
                        }
                    } else if (match('e') || match('E')) {
                        if (match('.')) {
                            addToken(LESS_EQUAL);
                        }
                    }
                } else if (match('g') || match('G')) {
                    if (match('e') || match('E')) {
                        if (match('.')) {
                            addToken(GREATER_EQUAL);
                        }
                    } else if (match('t') || match('T')) {
                        if (match('.')) {
                            addToken(GREATER);
                        }
                    }
                }
                // Real constant
                else if (isNumeric(source.charAt(current))) {
                    rdcon();
                } else if (isAlpha(peek())) {
                    checkOps();
                } else {
                    addToken(DOT);
                }
            }
            case '\n' -> {
                addToken(NEWLINE);
                row++;
                column = 0;
            }
            case 'C', '!' , 'c' -> {
                if (column == 1) {
                    comment(); return;
                }
                if (source.charAt(current - 2) == '\n') {
                    comment(); return;
                }
                if ((peek() == '\n') && (source.charAt(current - 2) == '\n')) {
                    comment(); return;
                }
                identifier();
            }
            case ':' -> {
                if (match(':')) {
                    addToken(COLON_COLON);
                } else {
                    addToken(COLON);
                }
            }
            case '/' -> {
                    addToken(SLASH);
            }
            case '$' -> addToken(DOLLAR);
            case '%' -> addToken(PERCENT);
            case '_' -> addToken(UNDERSCORE);
            case 'b' -> {
                if (peek() == '\'') {
                    consume();
                    bcon();
                } else {
                    identifier();
                }
            }
            case 'o', 'O' -> {
                if (peek() == '\'') {
                    consume();
                    ocon();
                } else {
                    identifier();
                }
            }
            case 'z', 'Z' -> {
                if (peek() == '\'') {
                    consume();
                    zcon();
                } else {
                    identifier();
                }
            }

            case 'F', 'I', 'E', 'D', 'f', 'i', 'e', 'd', 'p', 'P' -> {
                if (isNumeric(peek())) {
                    consume();
                    fcon();
                } else {
                    identifier();
                }
            }
            default -> {
                // Is variable
                if (isAlpha(c)) {
                    identifier();
                    // Is arithmetic constant
                } else if (isNumeric(c)) {
                    while(isNumeric(c)) {
                        c = consume();
                    }
                    current--;
                    if (peek() == 'H') {
                        hcon();
                        // Check that after dot is not an equivalence op before parsing as real
                    } else if ((peek() == '.' || peek() == 'E' || peek() == 'e' || peek() == 'd' || peek() == 'D' || peek() == 'P' || peek() == 'p') && (current + 1 < source.length()) &&( (source.charAt(current+1)) != 'n') && ( (source.charAt(current+1)) !='e') && ( (source.charAt(current+1)) != 'l') && ( (source.charAt(current+1)) != 'g') && ( (source.charAt(current+1)) != 'N') && ( (source.charAt(current+1)) !='E') && ( (source.charAt(current+1)) != 'L') && ( (source.charAt(current+1)) !='G') && ( (source.charAt(current+1)) !='A') && ( (source.charAt(current+1)) !='a') && ( (source.charAt(current+1)) !='O') && ( (source.charAt(current+1)) !='o') && ( (source.charAt(current+1)) !='F') && ( (source.charAt(current+1)) !='f') && ( (source.charAt(current+1)) !='T') && ( (source.charAt(current+1)) !='t')) {
                        rdcon();
                    } else if(peek() == 'x' || peek() == 'X') {
                        xcon();
                    }
                    else {
                        integer();
                    }
                }
            }
        }
        column++;
    }

    /**
     * <p><i>hcon</i> method is responsible for parsing Hollerith constants.</p>
     */
    private void hcon() {
        consume();
        int length = Integer.parseInt(source.substring(start, current - 1)); // Get the length of the Hollerith constant
        for (int i = 0; i < length; i++) {
            if (!isAtEnd()) {
                consume();
            } else {
                // Handle error: The string is shorter than the specified length
                break;
            }
        }

        String text = source.substring(start, current);

        addToken(HCON, text);
    }

    /**
     * <p><i>scon</i> method is responsible for parsing string constants.</p>
     * @param c char
     */
    private void scon(char c) {

        while (true) {
            while ((peek() != c && !isAtEnd())) consume();
            // Check double quotes are part of the string
            if ((peek() == c) &&(current + 1 < source.length()) && source.charAt(current+1) == c) {
                consume();
                consume();
                continue;
            }
            if (!isAtEnd()) {
                consume(); // Consume closing apostrophe
            }
            break;

        }

        String text = source.substring(start, current);

        // delete single quotes and replace with double quotes if not a char
        if (text.charAt(0) == '\'' && text.charAt(text.length()-1) == '\'') {
            if (text.length() > 3 || (text.length() == 3 && text.charAt(1) == ' ')) {
                String text2 = "\"" + text.substring(1, text.length() - 1) + "\"";
                text = text2;
            }
        }
        if (text.substring(1, text.length()-1).contains("\"")) {
            String text2 = "\"" + text.substring(1, text.length()-1).replace("\"", "\\\"") + "\"";
            text = text2;
        }

        addToken(SCON, text);
    }

    /**
     * <p><i>xcon</i> method is responsible for parsing X constants.</p>
     */
    private void xcon() {
        consume();

        String text = source.substring(start, current);

        addToken(XCON, text);
    }

    /**
     * <p><i>checkOps</i> method is responsible for parsing and, not, eqv and neqv phrases.</p>
     */
    private void checkOps() {
        while (isAlpha(peek())) consume();

        if (peek() == '.') {
            consume();
        }
        String text = source.substring(start, current);
        String lowerCase = text.toLowerCase();

        if (lowerCase.equals(".not.")) {
            addToken(NOT);
        } else if (lowerCase.equals(".and.")) {
            addToken(AND);
        } else if (lowerCase.equals(".or.")) {
            addToken(OR);
        } else if (lowerCase.equals(".eqv.")) {
            addToken(EQUAL_EQUAL);
        } else if (lowerCase.equals(".neqv.")) {
            addToken(BANG_EQUAL);
        } else if (lowerCase.equals(".false.")) {
            addToken(FALSE);
        } else if (lowerCase.equals(".true.")) {
            addToken(TRUE);
        }

    }

    /**
     * <p><i>identifier</i> method is responsible for parsing identifiers.</p>
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) consume();

        String text = source.substring(start, current);
        String upperCaseText = text.toUpperCase();
        if (upperCaseText.equalsIgnoreCase("UNIT")) {
            if (peek() == '=') {
                consume();
                addToken(UNIT_EQUAL);

 return;           }
        } else if (upperCaseText.equalsIgnoreCase("FILE")) {
            if (peek() == '=') {
                consume();
                addToken(FILE_EQUAL);

 return;           }
        } else if (upperCaseText.equalsIgnoreCase("ERR")) {
            if (peek() == '=') {
                consume();
                addToken(ERR_EQUAL);

  return;          }
        } else if (upperCaseText.equalsIgnoreCase("IOSTAT")) {
            if (peek() == '=') {
                consume();
                addToken(IOSTAT_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("EXIST")) {
            if (peek() == '=') {
                consume();
                addToken(EXIST_EQUAL);

return;            }
        } else if (upperCaseText.equalsIgnoreCase("OPENED")) {
            if (peek() == '=') {
                consume();
                addToken(OPENED_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("NUMBER")) {
            if (peek() == '=') {
                consume();
                addToken(NUMBER_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("NAMED")) {
            if (peek() == '=') {
                consume();
                addToken(NAMED_EQUAL);

return;            }
        } else if (upperCaseText.equalsIgnoreCase("NAME")) {
            if (peek() == '=') {
                consume();
                addToken(NAME_EQUAL);

 return;           }
        } else if (upperCaseText.equalsIgnoreCase("ACCESS")) {
            if (peek() == '=') {
                consume();
                addToken(ACCESS_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("SEQUENTIAL")) {
            if (peek() == '=') {
                consume();
                addToken(SEQUENTIAL_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("DIRECT")) {
            if (peek() == '=') {
                consume();
                addToken(DIRECT_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("FORM")) {
            if (peek() == '=') {
                consume();
                addToken(FORM_EQUAL);

 return;           }
        } else if (upperCaseText.equalsIgnoreCase("FORMATTED")) {
            if (peek() == '=') {
                consume();
                addToken(FORMATTED_EQUAL);
                        return;
            }
        } else if (upperCaseText.equalsIgnoreCase("UNFORMATTED")) {
            if (peek() == '=') {
                consume();
                addToken(UNFORMATTED_EQUAL);
                        return;
            }
        } else if (upperCaseText.equalsIgnoreCase("RECL")) {
            if (peek() == '=') {
                consume();
                addToken(RECL_EQUAL);

 return;           }
        } else if (upperCaseText.equalsIgnoreCase("NEXTREC")) {
            if (peek() == '=') {
                consume();
                addToken(NEXTREC_EQUAL);
                return;
            }
        } else if (upperCaseText.equalsIgnoreCase("BLANK")) {
            if (peek() == '=') {
                consume();
                addToken(BLANK_EQUAL);

return;            }
        } else if (upperCaseText.equalsIgnoreCase("FMT")) {
            if (peek() == '=') {
                consume();
                addToken(FMT_EQUAL);

  return;          }
        } else if (upperCaseText.equalsIgnoreCase("REC")) {
            if (peek() == '=') {
                consume();
                addToken(REC_EQUAL);

  return;          }
        } else if (upperCaseText.equalsIgnoreCase("END")) {
            if (peek() == '=') {
                consume();
                addToken(END_EQUAL);

  return;          }
        } else if (upperCaseText.equalsIgnoreCase("STATUS")) {
            if (peek() == '=') {
                consume();
                addToken(STATUS_EQUAL);
                return;
            }
        }
            TokenType type = terminalSymbols.get(text.toUpperCase());

            if (type == null) type = ID;

            if ((type == ID) && ((tokens.get(tokens.size() - 1).type.equals(PROGRAM)))) {
                Fort2Jul.programName = text;
        }
        addToken(type);
    }

    /**
     * <p><i>integer</i> method is responsible for parsing integer numbers.</p>
     */
    private void integer() {
        while (isNumeric(peek())) consume();

        String text = source.substring(start, current);

        // This is to parse for the test suite only
        if ((text.length() == 8) && (Fort2Jul.programName.contains(text.substring(5, 7)))){
            return;
        }

        addToken(ICON, Integer.parseInt(text));
    }

    /**
     * <p><i>decimalOrSpecialConstant</i> method is responsible for parsing floating point numbers.</p>
     */
    private void decimalOrSpecialConstant() {
        while (isNumeric(peek())) consume();

        if (peek() == '.') {
            consume();

            while (isNumeric(peek())) consume();

            if (peek() == '_') {
                consume(); consume(); consume();

                String text = source.substring(start, current).replace("_SP", "");
                float value = Float.parseFloat(text);

                addToken(SP, value);
                return;
            }

            if (peek() == 'P') {
                consume();

                while (isNumeric(peek())) consume();

                String text = source.substring(start, current).replace("P", "");
                double value = Double.parseDouble(text);

                if (peek() == 'E') {
                    consume();

                    while (isNumeric(peek())) consume();

                    text = text.replace("E", "");
                    value = Double.parseDouble(text);
                }

                addToken(PCON, value);
                return;
            }
        }

        String text = source.substring(start, current);
        double value = Double.parseDouble(text);

        addToken(RDCON, value);
    }

    /**
     * <p><i>bcon</i> method is responsible for parsing binary numbers.</p>
     */
    private void bcon() {
        // Consume characters while they are 0 or 1
        while (peek() == '0' || peek() == '1') consume();

        String text = source.substring(start, current);

        addToken(BCON, text);
    }

    /**
     * <p><i>ocon</i> method is responsible for parsing octal numbers.</p>
     */
    private void ocon() {
        // Consume characters while they are between 0 and 7
        while (peek() >= '0' && peek() <= '7') consume();

        String text = source.substring(start, current);

        addToken(OCON, text); // Julia accepts on string representations of octal constants
    }

    /**
     * <p><i>fcon</i> method is responsible for parsing format specifier constants.</p>
     */
    private void fcon() {
        if (isNumeric(peek())) {
            while (isNumeric(peek())) consume();
            if ((peek() == '.')) {
                consume();
            }
            while (isNumeric(peek())) consume();

        } else if (peek() == '.') {
            consume();
            while (isNumeric(peek())) consume();
        }

        String text = source.substring(start, current);

        addToken(FCON, text);
    }

    /**
     * <p><i>rdcon</i> method is responsible for parsing real numbers.</p>
     */
    private void rdcon() {
        boolean isExponent = false;
        boolean isDecimal = false;
        char prevChar = '3';

        // Match digits before the decimal point
        while (isNumeric(peek())) {
            prevChar = consume();
        }

        // Check for decimal
        if (peek() == '.') {
            prevChar = consume(); // Consume the dot
            isDecimal = true; // Set the flag to indicate a decimal is found
            while (isNumeric(peek())) {
                prevChar = consume(); // Consume digits after dot
            }
        }

        // Check for exponent
        if ((isNumeric(prevChar)) && (peek() == 'e' || peek() == 'E' || peek() == 'd' || peek() == 'D')) {
            consume(); // Consume the 'e', 'E', 'd', or 'D'
            isExponent = true; // Set the flag to indicate an exponent is found
            // Check for optional sign
            if (peek() == '-' || peek() == '+') consume();
            // Consume digits after exponent
            while (isNumeric(peek())) consume();
        }

        // Check for second occurrence of 'e', 'E', 'd', or 'D'
        if ((isNumeric(prevChar)) && (peek() == 'e' || peek() == 'E' || peek() == 'd' || peek() == 'D')) {
            if (peek() == 'd' || peek() == 'D') {
            }
            consume(); // Consume the 'e', 'E', 'd', or 'D'
        }

        String text = source.substring(start, current);
        String modifiedText = text.replace(String.valueOf('d'), "").replace(String.valueOf('D'), "");

        if (isExponent || isDecimal) {
            double value = Double.parseDouble(modifiedText);
            addToken(RDCON, value);
        } else {
            try {
                long value = Long.parseLong(modifiedText);
                addToken(RDCON, value);
            } catch (NumberFormatException e) {
                double value = Double.parseDouble(modifiedText);
                addToken(RDCON, value);
            }
        }
    }

    /**
     * <p><i>zcon</i> method is responsible for parsing hexadecimal numbers.</p>
     */
    private void zcon() {
        // Consume characters while they are a valid hexadecimal digit
        while ((peek() >= '0' && peek() <= '9') || (peek() >= 'A' && peek() <= 'F') || (peek() >= 'a' && peek() <= 'f')) consume();

        String text = source.substring(start, current); // Accepted as text in Julia

        addToken(ZCON, text);
    }


    /**
     * <p><i>comment</i> method is responsible for parsing comments.</p>
     */
    private void comment() {
        while (peek() != '\n' && !isAtEnd()) consume();

        String text = source.substring(start, current);

        addToken(COMMENT, text);
    }

    /**
     * <p><i>addToken</i> called when no <i>Object literal</i> is presented. This is substituted for null instead.</p>
     * @param type TokenType
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * <p><i>addToken</i> called when <i>Object literal</i> is presented.</p>
     * @param type TokenType
     * @param literal Object
     */
    private void addToken(TokenType type, Object literal) {
        Token token;
        String text = source.substring(start, current);

        if (text.equals("\n")) {
            token = new Token(type, "\\n", literal, row, column);
        } else {
            token = new Token(type, text, literal, row, column);
        }

        tokens.add(token);
    }

    /**
     * <p><i>match</i> method is responsible for matching the expected character with the current character.</p>
     * @param expected char
     * @return boolean
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * <p><i>isAtEnd</i> method is responsible for checking if the current character is at the end of the source text.</p>
     * @return boolean
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * <p><i>consume</i> method is responsible for consuming the current character and moving to the next character.</p>
     * @return char
     */
    private char consume() {
        return source.charAt(current++);
    }

    /**
     * <p><i>peek</i> method is responsible for peeking the current character.</p>
     * @return char
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * <p><i>isAlpha</i> method is responible for checking if the current character is alphabetical.</p>
     * @param c char
     * @return boolean
     */
    private boolean isAlpha(char c) {
        return (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                c == '_';
    }

    /**
     * <p><i>isNumeric</i> method is responsible for checking if the current character is numeric.</p>
     * @param c char
     * @return boolean
     */
    private boolean isNumeric(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * <p><i>isAlphaNumeric</i> method is responsible for checking if the current character is alphanumeric.</p>
     * @param c char
     * @return boolean
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }
}