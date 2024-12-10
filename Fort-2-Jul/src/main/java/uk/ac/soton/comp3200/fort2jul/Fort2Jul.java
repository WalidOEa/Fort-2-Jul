package uk.ac.soton.comp3200.fort2jul;

import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.controller.FileController;
import uk.ac.soton.comp3200.fort2jul.lexer.Scanner;
import uk.ac.soton.comp3200.fort2jul.lexer.Token;
import uk.ac.soton.comp3200.fort2jul.parser.Ast;
import uk.ac.soton.comp3200.fort2jul.parser.Parser;
import uk.ac.soton.comp3200.fort2jul.transpiler.JuliaCodeGenerator;
import uk.ac.soton.comp3200.fort2jul.transpiler.JuliaMacroGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for when no arguments are supplied, allowing the transpiler to run in command line.
 */
public class Fort2Jul {

    private static final Logger logger = Logger.getLogger(Fort2Jul.class);

    /**
     * Name of program to transpile
     */
    public static String programName;

    /**
     * Path of source
     */
    private static String sourcePath;

    /**
     * Expects argument for locating source file
     * @param args Path of source file
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.exit(64);
        } else if (args.length == 1) {
            logger.info("Running Fort-2-Jul...");

            runFile(args[0]);

            logger.info("Successful transpilation");
        }
    }

    /**
     * Reads contents of file and runs the transpiler
     * @param path Path of source file
     * @throws IOException
     */
    public static void runFile(String path) throws IOException {
        Path filePath = Paths.get(path);

        logger.info("Got path to file [Fort2Jul]");

        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            sourcePath = new File(".").getAbsolutePath();
        } else {
            sourcePath = new File(parentPath.toUri()).getAbsolutePath();
        }

        logger.info("Reading file... [Fort2Jul]");

        byte[] bytes = Files.readAllBytes(filePath);

        logger.info("File read successful [Fort2Jul]");

        run(new String(bytes, Charset.defaultCharset()), path);
    }

    /**
     * Runs lexer, parser and code generator
     * @param source Contents of source
     */
    private static void run(String source, String fileName) {
        logger.info("Running transpiler... [Fort2Jul]");

        logger.info("Running scanner... [Fort2Jul]");

        List<Token> tokens = runLexer(source);

        logger.info("Scan successful [Fort2Jul]");
        logger.info("Parsing token stream... [Fort2Jul]");

        Ast<String> ast = runParser(tokens);

        logger.info("Parse successful [Fort2Jul]");
        logger.info("Beginning conversion [Fort2Jul]");

        runTranspiler(ast, fileName);

        logger.info("Conversion successful [Fort2Jul]");
    }

    /**
     * Responsible for lexer
     * @param source Contents of source
     * @return Token stream
     */
    private static List<Token> runLexer(String source) {
        Scanner scanner = new Scanner(source);

        return scanner.scanTokens();
    }

    /**
     * Responsible for parser
     * @param tokens Token stream
     * @return AST
     */
    private static Ast<String> runParser(List<Token> tokens) {
        Parser parser = new Parser(tokens);

        Ast<String> ast = parser.parseTokens();

        return ast;
    }

    /**
     * Responsible for code generator, writes to file after conversion
     * @param ast
     */
    private static void runTranspiler(Ast<String> ast, String filePath) {
        logger.info("Running transpiler [Fort-2-Jul]");

        List<String> filePaths = Arrays.stream(filePath.split("/")).toList();
        String fileName = filePaths.get(filePaths.size() - 1);
        fileName = (fileName.split("\\."))[0];

        try {
            StringBuilder juliaCode = new StringBuilder();

            JuliaMacroGenerator juliaMacroGenerator = new JuliaMacroGenerator(sourcePath);
            juliaMacroGenerator.generateMacroCode();

            JuliaCodeGenerator juliaCodeGenerator = new JuliaCodeGenerator(ast, sourcePath, juliaCode, fileName);
            juliaCodeGenerator.generateJuliaCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Finished running transpiler [Fort-2-Jul]");
    }
}
