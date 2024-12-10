package uk.ac.soton.comp3200.fort2jul.lexer;

import org.apache.log4j.Logger;

/**
 * Token creates Token objects containing their type, lexeme, literal, row number
 * and column number.</p>
 */
public class Token {

    /**
     * <p>Logger logger is responsible for logging the actions performed by the <i>Token</i> class.</p>
     */
    private static final Logger logger = Logger.getLogger(Scanner.class);

    /**
     * <p>TokenType type is the type of the token.</p>
     */
    public final TokenType type;
    /**
     * <p>String lexeme is the lexeme of the token.</p>
     */
    public final String lexeme;
    /**
     * <p>Object literal is the literal of the token.</p>
     */
    final Object literal;
    /**
     * <p>int row_no is the row number of the token.</p>
     */
    final int row_no;
    /**
     * <p>int column_no is the column number of the token.</p>
     */
    final int column_no;

    /**
     * <p>Token constructor creates a Token object with the given type, lexeme, literal, row number.</p>
     * @param type TokenType
     * @param lexeme String
     * @param literal Object
     * @param row_no int
     * @param column_no int
     */
    public Token(TokenType type, String lexeme, Object literal, int row_no, int column_no) {
        logger.info("Token object created with type: " + type + ", lexeme: " + lexeme + ", literal: "
                + literal + ", row number: " + row_no + ", column number: " + column_no);

        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.row_no = row_no;
        this.column_no = column_no;
    }

    /**
     * <p><i>toString()</i> method returns the type, lexeme and literal of the token.</p>
     * @return String
     */
    @Override
    public String toString() {
        return type + " lex " + lexeme + " lit " + literal;
    }

    /**
     * <p><i>getType</i> method returns the type of the token.</p>
     * @return TokenType
     */
    public TokenType getType() {
        return type;
    }

    /**
     * <p><i>getLexeme</i> method returns the lexeme of the token.</p>
     * @return String
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * <p><i>getLiteral</i> method returns the literal of the token.</p>
     * @return Object
     */
    public Object getLiteral() {
        return literal;
    }

    /**
     * <p><i>getRow_no</i> method returns the row number of the token.</p>
     * @return int
     */
    public int getRow_no() {
        return row_no;
    }

    /**
     * <p><i>getColumn_no</i> method returns the column number of the token.</p>
     * @return int
     */
    public int getColumn_no() {
        return column_no;
    }
}
