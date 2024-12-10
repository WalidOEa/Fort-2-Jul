package uk.ac.soton.comp3200.fort2jul.controller;

import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.Fort2Jul;
import uk.ac.soton.comp3200.fort2jul.lexer.Scanner;
import uk.ac.soton.comp3200.fort2jul.lexer.Token;
import uk.ac.soton.comp3200.fort2jul.parser.Ast;
import uk.ac.soton.comp3200.fort2jul.parser.Parser;
import uk.ac.soton.comp3200.fort2jul.transpiler.JuliaCodeGenerator;
import uk.ac.soton.comp3200.fort2jul.transpiler.JuliaMacroGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for utilising transpiler to convert source to target. Returns converted code to view
 */
public class SourceController {

    private static final Logger logger = Logger.getLogger(SourceController.class);

    /**
     * Transpile contents of source
     * @param content source content
     * @param filePath path of source
     * @return converted source code into target code
     */
    public String transpileContent(String content, String filePath) {
        logger.info("Transpiling contents... [SourceController]");

        List<String> filePaths = Arrays.stream(filePath.split("/")).toList();
        String fileName = filePaths.get(filePaths.size() - 1);
        fileName = (fileName.split("\\."))[0];

        logger.info("Scanning contents... [SourceController]");

        Scanner scanner = new Scanner(content);
        List<Token> tokens = scanner.scanTokens();

        logger.info("Scan successful -> Generated list of tokens [SourceController]");
        logger.info("Parsing token stream [SourceController]");

        Parser parser = new Parser(tokens);
        Ast<String> ast = parser.parseTokens();

        logger.info("Parse successful -> Generated AST [SourceController]");
        logger.info("Converting code... [SourceController]");

        try {
            StringBuilder juliaCode = new StringBuilder();

            JuliaMacroGenerator juliaMacroGenerator = new JuliaMacroGenerator(filePath);
            juliaMacroGenerator.generateMacroCode();

            logger.info("Generated macros.jl in " + filePath + " [SourceController]");

            JuliaCodeGenerator juliaCodeGenerator = new JuliaCodeGenerator(ast, filePath, juliaCode, fileName);

            logger.info("Generated jl file in " + filePath + " [SourceController]");
            logger.info("Returning converted code [SourceController]");
            logger.info("Transpilation successful [SourceController]");

            return juliaCodeGenerator.generateJuliaCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
