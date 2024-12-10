package uk.ac.soton.comp3200.fort2jul.transpiler;

import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.parser.Ast;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Converts code by traversing AST. To ease with this process, this class utilises macros defined by MacroCodeGenerator
 */
public class JuliaCodeGenerator {

    private static final Logger logger = Logger.getLogger(JuliaCodeGenerator.class);

    /**
     * Resultant AST of parser
     */
    private final Ast<String> ast;

    /**
     * Output directory of julia file
     */
    private final String outputDir;

    /**
     * File writer
     */
    private FileWriter fileWriter = null;

    /**
     * Julia code to write into file
     */
    private final StringBuilder juliaCode;

    /**
     * Tracks indentation level
     */
    private String currentIndent = "";

    /**
     * Name of old file
     */
    private String oldFileName = "";

    private final HashMap<String, String> labelsToStatements = new HashMap<>();

    private final List<String> listOfFunctions = new ArrayList<String>();

    private final List<String> listOfArrays = new ArrayList<String>();

    private final HashMap<String, String> listOfStdFunctions;

    private final HashMap<String, String> varToTypes = new HashMap<>();

    private final HashMap<String,HashSet<String>> subroutine2Vars = new HashMap<>();

    private String currSubroutine = "";

    private final HashMap<String, List<Integer>> removedParamsPos = new HashMap<>();

    private final HashMap<String, List<String>> removedParams = new HashMap<>();

    private boolean compilingSubroutine = false;
    private boolean compilingFunction = false;
    private final List<String> inDo = new ArrayList<>();

    private String programName = "";

    /**
     * JuliaCodeGenerator generates Julia file
     * @param ast AST
     * @param outputDir Output directory of Julia file
     * @param juliaCode Contents
     * @param oldFileName File name of source
     * @throws IOException
     */
    public JuliaCodeGenerator(Ast<String> ast, String outputDir, StringBuilder juliaCode, String oldFileName) throws IOException {
        this.ast = ast;
        this.outputDir = outputDir;
        this.juliaCode = juliaCode;
        this.oldFileName = oldFileName;
        this.fileWriter = new FileWriter(outputDir + "/" + oldFileName + ".jl");
        this.listOfStdFunctions = generateStdFunctionMap();
    }

    /**
     * Generates Julia code based on the resultant AST
     * @return Contents of file to write
     * @throws IOException
     */
    public String generateJuliaCode() throws IOException {
        Ast.Node<String> rootNode = ast.getRootNode();

        logger.info("Beginning code conversion for file in " + outputDir);

        String finalCode = "";
        juliaCode.append("include(\"macros.jl\")\n\n");

        juliaCode.append("# Original file located at: " + outputDir + "/" + oldFileName + "\n\n");

        juliaCode.append("using Printf\n");

        if (ast.getRootNode().getData().equals("program")) {
            finalCode = transpileProgram(ast.getChildren(rootNode));
        }

        for (String label : labelsToStatements.keySet()) {
            finalCode = finalCode.replace(label, labelsToStatements.get(label));
        }

        for (String func : listOfStdFunctions.keySet()) {
            finalCode = finalCode.replace(func + "(", listOfStdFunctions.get(func) + "(");
        }
        juliaCode.append(finalCode);
        fileWriter.write(juliaCode.toString());
        fileWriter.close();

        logger.info("Finished writing into Julia file -> Conversion successful");

        return juliaCode.toString();
    }

    private HashMap<String, String> generateStdFunctionMap() {
        HashMap<String, String> map = new HashMap<>();

        map.put("ABS", "abs");
        map.put("CMPLX", "complex");
        map.put("SQRT", "sqrt");
        map.put("EXP", "exp");
        map.put("MOD", "mod");
        map.put("SIN", "sin");
        map.put("COS", "cos");
        map.put("TAN", "tan");
        map.put("LEN", "length");
        map.put("FLOAT", "float");
        map.put("INDEX", "findfirst");

        listOfFunctions.addAll(map.keySet());
        listOfFunctions.addAll(map.values());

        return map;
    }

    private String transpileProgram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CommentOrNewline" -> result.append(transpileCommentOrNewline(ast.getChildren(child)));
                case "ExecutableProgram" -> result.append(transpileExecutableProgram(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileExecutableProgram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("ProgramUnit")) {
                result.append(transpileProgramUnit(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileProgramUnit(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "MainProgram" -> result.append(currentIndent + transpileMainProgram(ast.getChildren(child)));
                case "FunctionSubprogram" -> {
                    compilingFunction = true;
                    result.append(currentIndent + transpileFunctionSubprogram(ast.getChildren(child)));
                    compilingFunction = false;
                }
                case "SubroutineSubprogram" -> {
                    compilingSubroutine = true;
                    result.append(currentIndent + transpileSubroutineSubprogram(ast.getChildren(child)));
                    compilingSubroutine = false;
                }
                case "BlockDataSubprogram" -> result.append(currentIndent + transpileBlockDataSubprogram(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileMainProgram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ProgramStmt" -> result.append(transpileProgramStmt(ast.getChildren(child)));
                case "MainRange" -> result.append(transpileMainRange(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileBlockDataSubprogram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "BlockDataStmt" -> {
                } // Not needed in Julia
                case "BlockDataBody" -> result.append(transpileBlockDataBody(ast.getChildren(child)));
                case "EndBlockDataStmt" -> {
                    result.append(transpileEOS(ast.getChildren(child)));
                }
            }
        }
        return result.toString();
    }

    private String transpileBlockDataBody(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "BlockDataBody" -> result.append(transpileBlockDataBody(ast.getChildren(child)));
                case "BlockDataBodyConstruct" -> result.append(transpileBlockDataBodyConstruct(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileBlockDataBodyConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("SpecificationPartConstruct")) {
                result.append(transpileSpecificationPartConstruct(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubroutineSubprogram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String name = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "SUBROUTINE" -> result.append(transpileSUBROUTINE(ast.getChildren(child)));
                case "SubroutineName" -> {
                    name = transpileSubroutineName(ast.getChildren(child));
                    subroutine2Vars.put(name, new HashSet<>());
                    currSubroutine = name;
                    result.append(transpileSubroutineName(ast.getChildren(child)));
                }
                case "SubroutineRange" -> {
                    pushTab();
                    result.append(transpileSubroutineRange(ast.getChildren(child), name));
                }
            }
        }
        return result.toString();
    }

    private String transpileSubroutineName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Pass out all input variables as results to mimic pass-by-reference behaviour
    private String transpileSubroutineRange(List<Ast.Node<String>> childNodes, String name) throws IOException {
        StringBuilder result = new StringBuilder();
        String params = "";
        String body = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SubroutineParList" -> {
                    params = transpileSubroutineParList(ast.getChildren(child));
                    result.append("(" + params + ")\n");
            }
                case "EOS", "EndSubroutineStmt" -> {}
                case "Body" ->  result.append(transpileBody(ast.getChildren(child)));
                }
            }
        result.append(currentIndent + "return " + params + "\n");
        popTab();
        result.append(currentIndent + "end\n");

        return result.toString();
    }

    private String transpileFunctionSubprogram(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String name = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "FunctionPrefix" -> result.append(transpileFunctionPrefix(ast.getChildren(child)));
                case "FunctionName" -> {
                    name = transpileFunctionName(ast.getChildren(child));
                    result.append(name);
                    listOfFunctions.add(transpileFunctionName(ast.getChildren(child)));
                }
                case "FunctionRange" -> {
                    pushTab();
                    result.append(transpileFunctionRange(ast.getChildren(child)));
                    if (!transpileFunctionRange(ast.getChildren(child)).contains("return")) {
                        result.append(currentIndent + "return _" + name + "\n");
                    } else if (transpileFunctionRange(ast.getChildren(child)).contains("return\n")) {
                        result = new StringBuilder(result.toString().replace("return", "return _" + name));
                    }
                    popTab();
                    result.append(currentIndent + "end\n");
                }
            }
        }
        return result.toString();
    }

    private String transpileFunctionPrefix(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("FUNCTION")) {
                result.append(transpileFUNCTION(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionRange(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "FunctionParList" -> result.append(transpileFunctionParList(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
                case "Body" -> result.append(transpileBody(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileFunctionParList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "FunctionPars" -> result.append(transpileFunctionPars(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionPars(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "FunctionPar" -> result.append(transpileFunctionPar(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "FunctionPars" -> result.append(transpileFunctionPars(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionPar(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("DummyArgName")) {
                result.append(transpileDummyArgName(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileProgramStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "PROGRAM" -> {
                }
                case "ProgramName" -> programName = transpileProgramName(ast.getChildren(child));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileEOS(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("CommentOrNewline")) {
                result.append(transpileCommentOrNewline(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileProgramName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCommentOrNewline(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "COMMENT" -> result.append(transpileCOMMENT(ast.getChildren(child)));
                case "NEWLINE" -> result.append(transpileNEWLINE(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileIdent(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("ID")) {
                result.append(transpileId(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileId(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append(child.getData());

        }
        return result.toString();
    }

    private String transpileLblDef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Label")) {
                result.append(transpileLabel(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLabel(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("ICON")) {
                result.append(transpileICON(ast.getChildren(child), true));
            }
        }
        return result.toString();
    }

    private String transpileICON(List<Ast.Node<String>> childNodes, boolean isLabel) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (isLabel) {
                result.append("_" + child.getData());
            } else {
                result.append(child.getData());
            }
        }
        return result.toString();
    }

    private String transpileMainRange(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Body" -> result.append(transpileBody(ast.getChildren(child)));
                case "EndProgramStmt" -> {
                } // Not needed in Julia
            }
        }

        return result.toString();
    }

    private String transpileBody(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "BodyConstruct" -> result.append(transpileBodyConstruct(ast.getChildren(child)));
                case "Body" -> result.append(transpileBody(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileBodyConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SpecificationPartConstruct" -> result.append(transpileSpecificationPartConstruct(ast.getChildren(child)));
                case "ExecutableConstruct" -> result.append(transpileExecutableConstruct(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileExecutableConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ActionStmt" -> result.append(transpileActionStmt(ast.getChildren(child), ""));
                case "DoConstruct" -> result.append(currentIndent + transpileDoConstruct(ast.getChildren(child)));
                case "IfConstruct" -> result.append(currentIndent + transpileIfConstruct(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileDoConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("LabelDoStmt")) {
                result.append(transpileLabelDoStmt(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLabelDoStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        inDo.add("");
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "DO" -> pushTab();
                case "CommaLoopControl" -> result.append(transpileCommaLoopControl(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCommaLoopControl(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "COMMA" -> {
                }
                case "LoopControl" -> result.append(transpileLoopControl(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileIfConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "IfThenStmt" -> {
                    pushTab();
                    result.append(transpileIfThenStmt(ast.getChildren(child)));
                }
                case "ThenPart" -> {
                    result.append(transpileThenPart(ast.getChildren(child)));
                }
            }
        }
        return result.toString();
    }

    private String transpileThenPart(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EndIfStmt" -> {
                    result.append(currentIndent + " end\n");
                }
                case "ConditionalBody" -> {
                    result.append(transpileConditionalBody(ast.getChildren(child)));
                    popTab();
                }
                case "ElseIfConstruct" -> {
                    result.append(currentIndent + transpileElseIfConstruct(ast.getChildren(child)));
                    popTab();
                }
                case "ElseConstruct" -> {
                    result.append(currentIndent + transpileElseConstruct(ast.getChildren(child)));
                    popTab();
                }
            }
        }
        return result.toString();
    }

    private String transpileConditionalBody(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("ExecutionPartConstruct")) {
                result.append(transpileExecutionPartConstruct(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileElseIfConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ElseIfStmt" -> {
                    result.append(transpileElseIfStmt(ast.getChildren(child)));
                    pushTab();
                }
                case "ThenPart" -> result.append(transpileThenPart(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileElseConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ElseStmt" -> {
                    result.append(currentIndent + transpileElseStmt(ast.getChildren(child)));
                    pushTab();
                }
                case "ElsePart" -> result.append(transpileElsePart(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileElsePart(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EndIfStmt" -> {
                    popTab();
                    result.append(currentIndent + "end\n");
                }
                case "ConditionalBody" -> result.append(transpileConditionalBody(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileElseIfStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "ELSEIF", "ELSE" -> result.append(transpileELSEIF(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    private String transpileElseStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "ELSE" -> result.append(transpileELSE(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    private String transpileExecutionPartConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ExecutableConstruct" -> result.append(transpileExecutableConstruct(ast.getChildren(child)));
                case "FormatStmt" -> result.append(currentIndent + transpileFormatStmt(ast.getChildren(child)));
                case "DataStmt" -> result.append(currentIndent + transpileDataStmt(ast.getChildren(child)));
                case "EntryStmt" -> result.append(currentIndent + transpileEntryStmt(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileDataStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<List<List<String>>> listOfLists = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "Datalist" -> listOfLists = transpileDatalist(ast.getChildren(child));
                case "DATA", "EOS" -> {
                }
            }
        }
        for (List<List<String>> pairs : listOfLists) {
            for (int i = 0; i < pairs.get(0).size(); i++) {
                    result.append(currentIndent + "global " + pairs.get(0).get(i) + " = " + pairs.get(1).get(i) + "\n");
            }
        }
        return result.toString();
    }

    private List<List<List<String>>> transpileDatalist(List<Ast.Node<String>> childNodes) throws IOException {
        List<List<List<String>>> listOfLists = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DataStmtSet" -> listOfLists.add(transpileDataStmtSet(ast.getChildren(child)));
                case "COMMA" -> {
                }
                case "Datalist" -> listOfLists.addAll(transpileDatalist(ast.getChildren(child)));
            }
        }
        return listOfLists;
    }

    private List<List<String>> transpileDataStmtSet(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> vars = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<List<String>> varsAndValues = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DataStmtObjectList" -> vars = transpileDataStmtObjectList(ast.getChildren(child));
                case "SLASH" -> {
                }
                case "DataStmtValueList" -> values = transpileDataStmtValueList(ast.getChildren(child));
            }
        }

        // Return list of variables and assigned values
        varsAndValues.add(vars);
        varsAndValues.add(values);
        return varsAndValues;
    }

    private List<String> transpileDataStmtObjectList(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> vars = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DataStmtObject" -> vars.add(transpileDataStmtObject(ast.getChildren(child)));
                case "COMMA" -> {
                }
                case "DataStmtObjectList" -> vars.addAll(transpileDataStmtObjectList(ast.getChildren(child)));
            }
        }
        return vars;
    }

    private List<String> transpileDataStmtValueList(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> vars = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DataStmtValue" -> vars.add(transpileDataStmtValue(ast.getChildren(child)));
                case "COMMA" -> {
                }
                case "DataStmtValueList" -> vars.addAll(transpileDataStmtValueList(ast.getChildren(child)));
            }
        }
        return vars;
    }

    private String transpileDataStmtValue(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Constant" -> result.append(transpileConstant(ast.getChildren(child)));
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
                case "NamedConstantUse" -> result.append(transpileNamedConstantUse(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileConstant(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PlusMinus" -> result.append(transpilePlusMinus(ast.getChildren(child)));
                case "UnsignedArithmeticConstant" -> result.append(transpileUnsignedArithmeticConstant(ast.getChildren(child)));
                case "SCON" -> result.append(transpileSCON(ast.getChildren(child)));
                case "LogicalConstant" -> result.append(transpileLogicalConstant(ast.getChildren(child)));
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "UNDERSCORE" -> result.append(transpileUNDERSCORE(ast.getChildren(child)));
                case "NamedConstantUse" -> result.append(transpileNamedConstantUse(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpilePlusMinus(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PLUS" -> result.append(transpilePLUS(ast.getChildren(child)));
                case "MINUS" -> result.append(transpileMINUS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileDataStmtObject(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Variable" -> result.append(transpileVariable(ast.getChildren(child)));
                case "DataImpliedDo" -> result.append(transpileDataImpliedDo(ast.getChildren(child)));
            }
        }
        return result.toString();
    }
    private String transpileDataImpliedDo(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> listExpr = new ArrayList<>();
        List<String> elements = new ArrayList<>();
        String doVar = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Expr" -> {
                    listExpr.add(transpileExpr(ast.getChildren(child)));
                }
                case "COMMA", "EQUAL" -> {
                }
                case "ImpliedDoVariable" -> {
                    doVar = transpileImpliedDoVariable(ast.getChildren(child));
                }
                case "DataIDoObjectList" -> {
                    elements = transpileDataIDoObjectList(ast.getChildren(child));
                }
            }
        }


        if (listExpr.size() == 2) {
            result.append(currentIndent + "reduce(vcat, [[" + String.join(",", elements) + "] for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(1) + "])\n");
        } else if (listExpr.size() == 3) {
            result.append(currentIndent + "reduce(vcat, [[" + String.join(",", elements) + "] for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(2) + ":" + listExpr.get(1) + "])\n");
        }

        return result.toString();
    }

    private String transpileIfThenStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "IF" -> result.append(transpileIF(ast.getChildren(child)));
                case "LPAREN", "RPAREN", "THEN" -> {
                }
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private List<String> transpileDataIDoObjectList(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> elements = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DataIDoObject" -> elements.add(transpileDataIDoObject(ast.getChildren(child)));
                case "DataIDoObjectList" -> elements.addAll(transpileDataIDoObjectList(ast.getChildren(child)));
            }
        }
        return elements;
    }

    private String transpileDataIDoObject(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder("");
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ArrayElement" -> result.append(transpileArrayElement(ast.getChildren(child)));
                case "DataImpliedDo" -> result.append(transpileDataImpliedDo(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileArrayElement(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder("");
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "VariableName" -> result.append(transpileVariableName(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "SectionSubscriptList" -> result.append(transpileSectionSubscriptList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSpecificationPartConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ImplicitStmt" -> result.append(currentIndent + transpileImplicitStmt(ast.getChildren(child)));
                case "ParameterStmt" -> result.append(currentIndent + transpileParameterStmt(ast.getChildren(child)));
                case "FormatStmt" -> result.append(currentIndent + transpileFormatStmt(ast.getChildren(child)));
                case "EntryStmt" -> result.append(currentIndent + transpileEntryStmt(ast.getChildren(child)));
                case "DeclarationConstruct" -> result.append(currentIndent + transpileDeclarationConstruct(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileImplicitStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private void transpileImplicitRanges(List<Ast.Node<String>> childNodes) throws IOException {
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ImplicitRange" -> transpileImplicitRange(ast.getChildren(child));
                case "," -> juliaCode.append(child.getData() + " ");
            }
        }
    }

    private void transpileImplicitRange(List<Ast.Node<String>> childNodes) throws IOException {
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Ident" -> transpileIdent(ast.getChildren(child));
                case "-" -> juliaCode.append(child.getData() + " ");
            }
        }
    }

    private String transpileParameterStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "PARAMETER", "(", ")" -> {
                }
                case "NamedConstantDefList" -> result.append(transpileNamedConstantDefList(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileNamedConstantDefList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "NamedConstantDef" -> result.append(transpileNamedConstantDef(ast.getChildren(child)) + "\n");
                case "COMMA" -> {
                }
                case "NamedConstantDefList" -> result.append(transpileNamedConstantDefList(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileNamedConstantDef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "NamedConstant" -> {
                    if (listOfFunctions.contains(transpileNamedConstant(ast.getChildren(child)))) {
                        result.append("_");
                    } else {
                        result.append("global ");
                    }
                    result.append(transpileNamedConstant(ast.getChildren(child)) + " = ");
                }
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileNamedConstant(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileNamedConstantUse(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private void transpileGenericName(List<Ast.Node<String>> childNodes) throws IOException {
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                transpileIdent(ast.getChildren(child));
            }
        }
    }

    private String transpileFormatStmt(List<Ast.Node<String>> childNodes) throws IOException {
        String result = "";
        String label = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> {
                    label = transpileLblDef(ast.getChildren(child));
                }
                case "FmtSpec" -> {
                    labelsToStatements.put(label, "\"" + transpileFmtSpec(ast.getChildren(child)) + "\\n\"");
                }
                case "EOS" -> {
                }
            }
        }
        return result;
    }

    private String transpileFmtSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "FormatEdit" -> {
                    if (transpileFormatEdit(ast.getChildren(child)).equals("A")) {
                        result.append("%s");
                    } else {
                        result.append(transpileFormatEdit(ast.getChildren(child)));
                    }
                }
                case "FmtSpec" -> result.append(transpileFmtSpec(ast.getChildren(child)));
            }
        }
        return result.toString();

    }

    private String transpileFormatEdit(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EditElement" -> result.append(transpileEditElement(ast.getChildren(child)));
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "FCON" -> result.append(transpileFCON(ast.getChildren(child)));
                case "RDCON" -> result.append(transpileRDCON(ast.getChildren(child)));
                case "BCON" -> result.append(transpileBCON(ast.getChildren(child)));
                case "OCON" -> result.append(transpileOCON(ast.getChildren(child)));
                case "ZCON" -> result.append(transpileZCON(ast.getChildren(child)));
                case "XCON" -> result.append(transpileXCON(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileEditElement(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "FCON" -> result.append(transpileFCON(ast.getChildren(child)));
                case "SCON" -> {
                    String str = transpileSCON(ast.getChildren(child));
                    str = str.replace("\"", "");
                    result.append(str);
                }
                case "Ident" -> result.append(transpileIdent(ast.getChildren(child)));
                case "FmtSpec" -> result.append(transpileFmtSpec(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileXCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            int xIdx = child.getData().indexOf('X');
            if (xIdx == -1) {
                xIdx = child.getData().indexOf('x');
            }
            String width = child.getData().substring(0, xIdx);
            int num = Integer.parseInt(width);

            for (int i = 0; i < num; i++) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    private String transpileFCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            // Integer formatting
            if (text.contains("I")) {
                result.append("%" + text.substring(1) + "d");
                // Float formatting
            } else if (text.contains("F")) {
                result.append("%" + text.substring(1) + "f");
                // Exponent formatting
            } else if (text.contains("E")) {
                result.append("%" + text.substring(1) + "e");
            }
        }
        return result.toString();

    }

    private String transpileEntryStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "EntryName" -> result.append(transpileEntryName(ast.getChildren(child)));
                case "SubroutineParList" -> result.append(transpileSubroutineParList(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileEntryName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubroutineParList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("SubroutinePars")) {
                result.append(transpileSubroutinePars(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubroutinePars(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SubroutinePar" -> {
                    result.append(transpileSubroutinePar(ast.getChildren(child)));
                }
                case "SubroutinePars" -> result.append(transpileSubroutinePars(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubroutinePar(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "DummyArgName" -> result.append(transpileDummyArgName(ast.getChildren(child)));
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileDeclarationConstruct(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "TypeDeclarationStmt" -> result.append(transpileTypeDeclarationStmt(ast.getChildren(child)));
                case "SpecificationStmt" -> result.append(transpileSpecificationStmt(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileTypeDeclarationStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> types = new ArrayList<>();
        List<String> vars = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "TypeSpec" -> types = transpileTypeSpec(ast.getChildren(child));
                case "EntityDeclList" -> {
                    vars = transpileEntityDeclList(ast.getChildren(child));
                    if (types.size() > 1 && vars.get(0).contains("array")) {
                        List<String> newVars = List.of(vars.get(0).split(" "));
                        vars = new ArrayList<>();
                        vars.add(newVars.get(1));

                    }
                }
            }
        }

        for (String var : vars) {
            if (!var.contains("array")) {
                varToTypes.put(var, types.get(0));
            }
        }
        if (types.size() > 1) {
            for (String var : vars) {
                result.append("global " + var + " = Vector{String}(undef," + types.get(1) + ")\n");
            }
        } else if ((!compilingFunction && !compilingSubroutine)){
            for (String var : vars) {
                if (!var.contains("array") && !listOfArrays.contains(var) && !listOfFunctions.contains(var)) { // Do not overwrite array
                    result.append("global " + var + " = " + convertToJuliaType(types.get(0)) + "\n");
                }
                }
            }
        return result.toString();
    }

    private List<String> transpileEntityDeclList(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> vars = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EntityDecl" -> vars.add(transpileEntityDecl(ast.getChildren(child)));
                case "EntityDeclList" -> vars.addAll(transpileEntityDeclList(ast.getChildren(child)));
            }
        }

        return vars;
    }

    private String transpileEntityDecl(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String name = "";
        String dims = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ObjectName" -> name = transpileObjectName(ast.getChildren(child));
                case "ArraySpec" -> dims = transpileArraySpec(ast.getChildren(child));

            }
        }

        if (dims.length() > 0) {
            listOfArrays.add(name);
            result.append(currentIndent + "global " + name + " = create_array(\"REAL\"," + dims + ")\n");
        } else {
            result.append(name);
        }

        return result.toString();
    }

    private String transpileObjectName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private List<String> transpileTypeSpec(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> types = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LengthSelector" -> types.add(transpileLengthSelector(ast.getChildren(child)));
                case "CHARACTER" -> types.add("CHARACTER");
                case "DOUBLE" -> types.add("DOUBLE");
                case "DOUBLEPRECISION" -> types.add("DOUBLEPRECISION");
                case "INTEGER" -> types.add("INTEGER");
                case "REAL" -> types.add("REAL");
                case "LOGICAL" -> types.add("LOGICAL");
                case "COMPLEX" -> types.add("COMPLEX");
            }
        }

        return types;
    }

    private String transpileLengthSelector(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CharLength" -> result.append(transpileCharLength(ast.getChildren(child)));
                case "TypeParamValue" -> result.append(transpileTypeParamValue(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCharLength(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Constant" -> result.append(transpileConstant(ast.getChildren(child)));
                case "TypeParamValue" -> result.append(transpileTypeParamValue(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileTypeParamValue(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Expr")) {
                result.append(transpileExpr(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileSpecificationStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CommonStmt" -> {
                } // For-2-Jul uses global variables for now
                case "DataStmt" -> result.append(transpileDataStmt(ast.getChildren(child)));
                case "DimensionStmt" -> result.append(transpileDimensionStmt(ast.getChildren(child)));
                case "EquivalenceStmt" -> {
                } // Not supported
                case "ExternalStmt" -> {
                } // Julia resolves function calls
                case "IntrinsicStmt" -> {
                } // No need to declare intrinsic functions
                case "SaveStmt" -> {
                } // No direct equivalent in Julia
            }
        }

        return result.toString();
    }

    // Equivalent to global variables in Julia
    private String transpileCommonStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "Comlist" -> result.append(transpileComlist(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileComlist(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Comblock" -> result.append(transpileComblock(ast.getChildren(child)));
                case "CommonBlockObject" -> result.append(transpileCommonBlockObject(ast.getChildren(child)));
                case "Comlist" -> result.append(transpileComlist(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileComblock(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("CommonBlockName")) {
                result.append("global " + transpileCommonBlockName(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCommonBlockName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCommonBlockObject(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ArrayDeclarator" -> result.append(transpileArrayDeclarator(ast.getChildren(child)));
                case "VariableName" -> result.append("global " + transpileVariableName(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileDimensionStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "ArrayDeclaratorList" -> result.append(transpileArrayDeclaratorList(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileArrayDeclaratorList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ArrayDeclarator" -> result.append(transpileArrayDeclarator(ast.getChildren(child)));
                case "ArrayDeclaratorList" -> result.append(transpileArrayDeclaratorList(ast.getChildren(child)));
                case "COMMA" -> {
                } //result.append(transpileCOMMA(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileArrayDeclarator(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String dims = "";
        String name = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "VariableName" -> {
                    name = transpileVariableName(ast.getChildren(child));
                    listOfArrays.add(name);
                    if (compilingSubroutine) {
                        break;
                    }
                    result.append("global " + name + " = ");
                }
                case "ArraySpec" -> {
                    if (compilingSubroutine) {
                        break;
                    }
                    dims = transpileArraySpec(ast.getChildren(child));
                    result.append("create_array(\"" + varToTypes.get(name) + "\"," + dims + ")\n");
                    listOfArrays.add(varToTypes.get(name));
                }
            }
        }

        return result.toString();
    }

    private String transpileArraySpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> dimensions = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ExplicitShapeSpecList" -> dimensions = transpileExplicitShapeSpecList(ast.getChildren(child));
                case "AssumedSizeSpec" -> dimensions = transpileAssumedSizeSpec(ast.getChildren(child));
            }
        }
        StringBuilder dims = new StringBuilder();

        for (String dim : dimensions) {
            dims.append(dim);
            dims.append(",");
        }

        return dims.toString();
    }

    private List<String> transpileAssumedSizeSpec(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> dimensions = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "STAR" -> dimensions.add(transpileSTAR(ast.getChildren(child)));
                case "ExplicitShapeSpecList" -> dimensions.addAll(transpileExplicitShapeSpecList(ast.getChildren(child)));
            }
        }

        return dimensions;
    }

    private List<String> transpileExplicitShapeSpecList(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> dimensions = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ExplicitShapeSpec" -> dimensions.add(transpileExplicitShapeSpec(ast.getChildren(child)));
                case "ExplicitShapeSpecList" -> dimensions.addAll(transpileExplicitShapeSpecList(ast.getChildren(child)));
            }
        }

        return dimensions;
    }

    // Only transpiling upper bounds as we cannot have custom indexes in julia
    private String transpileExplicitShapeSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("UpperBound")) {
                result.append(transpileUpperBound(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileUpperBound(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Expr")) {
                result.append(transpileExpr(ast.getChildren(child)));
            }
        }

        return result.toString();
    }


    private String transpileExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Level5Expr" -> result.append(transpileLevel5Expr(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "DefinedBinaryOp" -> result.append(transpileDefinedBinaryOp(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLevel5Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EquivOperand" -> result.append(transpileEquivOperand(ast.getChildren(child)));
                case "EquivOp" -> result.append(transpileEquivOp(ast.getChildren(child)));
                case "Level5Expr" -> result.append(transpileLevel5Expr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileEquivOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "OrOperand" -> result.append(transpileOrOperand(ast.getChildren(child)));
                case "OrOp" -> result.append(transpileOrOp(ast.getChildren(child)));
                case "EquivOperand" -> result.append(transpileEquivOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileOrOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("OR")) {
                result.append(transpileOR(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileEquivOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EQUAL_EQUAL" -> result.append(transpileEQUAL_EQUAL(ast.getChildren(child)));
                case "BANG_EQUAL" -> result.append(transpileBANG_EQUAL(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileOrOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "AndOperand" -> result.append(transpileAndOperand(ast.getChildren(child)));
                case "AndOp" -> result.append(transpileAndOp(ast.getChildren(child)));
                case "OrOperand" -> result.append(transpileOrOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileAndOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "NotOp" -> result.append(transpileNotOp(ast.getChildren(child)));
                case "Level4Expr" -> result.append(transpileLevel4Expr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileNotOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "NOT" -> result.append(transpileNOT(ast.getChildren(child)));
                case "SP" -> {
                }
            }
        }
        return result.toString();
    }

    private String transpileAndOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("AND")) {
                result.append(transpileAND(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLevel4Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Level3Expr" -> result.append(transpileLevel3Expr(ast.getChildren(child)));
                case "RelOpLevel3Expr" -> result.append(transpileRelOpLevel3Expr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLevel3Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Level2Expr" -> result.append(transpileLevel2Expr(ast.getChildren(child)));
                case "ConcatOpLevel2Expr" -> result.append(transpileConcatOpLevel2Expr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRelOpLevel3Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "RelOp" -> result.append(transpileRelOp(ast.getChildren(child)));
                case "Level3Expr" -> result.append(transpileLevel3Expr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRelOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "EQUAL_EQUAL" -> result.append(transpileEQUAL_EQUAL(ast.getChildren(child)));
                case "BANG_EQUAL" -> result.append(transpileBANG_EQUAL(ast.getChildren(child)));
                case "LESS" -> result.append(transpileLESS(ast.getChildren(child)));
                case "LESS_EQUAL" -> result.append(transpileLESS_EQUAL(ast.getChildren(child)));
                case "GREATER" -> result.append(transpileGREATER(ast.getChildren(child)));
                case "GREATER_EQUAL" -> result.append(transpileGREATER_EQUAL(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLevel2Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Sign" -> result.append(transpileSign(ast.getChildren(child)));
                case "AddOperand" -> result.append(transpileAddOperand(ast.getChildren(child)));
                case "AddOpAddOperand" -> result.append(transpileAddOpAddOperand(ast.getChildren(child)));
                case "Level2Expr" -> result.append(transpileLevel2Expr(ast.getChildren(child)));
                case "AddOp" -> result.append(transpileAddOp(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileConcatOpLevel2Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ConcatOp" -> result.append(transpileConcatOp(ast.getChildren(child)));
                case "Level3Expr" -> result.append(transpileLevel3Expr(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileSign(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PLUS" -> result.append(transpilePLUS(ast.getChildren(child)));
                case "MINUS" -> result.append(transpileMINUS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileDefinedBinaryOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "RDCON" -> result.append(transpileRDCON(ast.getChildren(child)));
                case "OCON" -> result.append(transpileOCON(ast.getChildren(child)));
                case "ZCON" -> result.append(transpileZCON(ast.getChildren(child)));
                case "BCON" -> result.append(transpileBCON(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileAddOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "MultOperand" -> result.append(transpileMultOperand(ast.getChildren(child)));
                case "MultOpMultOperand" -> result.append(transpileMultOpMultOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileAddOpAddOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "AddOp" -> result.append(transpileAddOp(ast.getChildren(child)));
                case "AddOperand" -> result.append(transpileAddOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileAddOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PLUS" -> result.append(transpilePLUS(ast.getChildren(child)));
                case "MINUS" -> result.append(transpileMINUS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileMultOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Level1Expr" -> result.append(transpileLevel1Expr(ast.getChildren(child)));
                case "PowerOpMultOperand" -> result.append(transpilePowerOpMultOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileMultOpMultOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "MultOp" -> result.append(transpileMultOp(ast.getChildren(child)));
                case "MultOperand" -> result.append(transpileMultOperand(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileMultOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
                case "SLASH" -> result.append(transpileSLASH(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileLevel1Expr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Primary")) {
                result.append(transpilePrimary(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCommaExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Expr")) {
                result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLoopControl(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String var = "";
        String i = "";
        String j = "";
        String step = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "VariableName" -> var = transpileVariableName(ast.getChildren(child));
                case "EQUAL", "COMMA" -> {
                }
                case "Expr" -> {
                    if (i.length() == 0) {
                        i = transpileExpr(ast.getChildren(child));
                    } else {
                        j = transpileExpr(ast.getChildren(child));
                    }
                }
                case "CommaExpr" -> step = transpileCommaExpr(ast.getChildren(child));
            }
        }

        if (step.length() == 0) {
            result.append("for " + var + " = " + i + ":" + j + "\n");
        } else {
            result.append("for " + var + " in " + i + ":" + step + ":" + j + "\n");
        }

        return result.toString();
    }

    private String transpilePrimary(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UnsignedArithmeticConstant" -> result.append(transpileUnsignedArithmeticConstant(ast.getChildren(child)));
                case "NameDataRef" -> result.append(transpileNameDataRef(ast.getChildren(child)));
                case "FunctionReference" -> result.append(transpileFunctionReference(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "SCON" -> result.append(transpileSCON(ast.getChildren(child)));
                case "LogicalConstant" -> result.append(transpileLogicalConstant(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileLogicalConstant(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "TRUE" -> result.append(transpileTRUE(ast.getChildren(child)));
                case "FALSE" -> result.append(transpileFALSE(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileUnsignedArithmeticConstant(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "RDCON" -> result.append(transpileRDCON(ast.getChildren(child)));
                case "ComplexConst" -> result.append(transpileComplexConst(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileComplexDataRefTail(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String l = "(";
        String r = ")";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SectionSubscriptRef" -> result.append(transpileSectionSubscriptRef(ast.getChildren(child)).replace("(", l).replace(")", r));
                case "PERCENT" -> result.append(transpilePERCENT(ast.getChildren(child)));
                case "Name" -> {
                    if (listOfArrays.contains(transpileName(ast.getChildren(child)))) {
                        l = "[";
                        r = "]";
                    }
                    result.append(transpileName(ast.getChildren(child)));
                }
            }
        }
        return result.toString();
    }

    private String transpileNameDataRef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String l = "";
        String r = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Name" -> {
                    if (listOfFunctions.contains(transpileName(ast.getChildren(child)))) {
                        l = "(";
                        r = ")";
                        result.append("_");
                    } else {
                        l = "[";
                        r = "]";
                    }
                    result.append(transpileName(ast.getChildren(child)));
                }
                case "ComplexDataRefTail" -> {
                    if (l.equals("(")) { // Function call
                        result = result.deleteCharAt(0);
                    }
                    result.append(transpileComplexDataRefTail(ast.getChildren(child)).replace("(", l).replace(")", r));
                }

            }
        }
        return result.toString();
    }

    private String transpileName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionReference(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Name" -> result.append(transpileName(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "FunctionArgList" -> result.append(transpileFunctionArgList(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionArgList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "FunctionArg" -> result.append(transpileFunctionArg(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SectionSubscriptList" -> result.append(transpileSectionSubscriptList(ast.getChildren(child)));
                case "FunctionArgList" -> result.append(transpileFunctionArgList(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileFunctionArg(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Name" -> result.append(result.append(transpileName(ast.getChildren(child))) + " = ");
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRDCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String text = "";
        for (Ast.Node<String> child : childNodes) {
            text = child.getData();
        }

        int i = text.indexOf("D");
        if (i == -1) {
            i = text.indexOf('d');
        }
        if (i != -1) {
            String power = text.substring(i + 1);
            result.append(text.substring(0, i) + "*10^" + power + " ");
        } else {
            result.append(text + " ");
        }

        return result.toString();
    }

    private String transpileOCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("\"" + child.getData() + "\" ");
        }

        return result.toString();
    }

    private String transpileZCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("\"" + child.getData() + "\" ");
        }

        return result.toString();
    }

    private String transpileBCON(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("\"" + child.getData() + "\" ");
        }

        return result.toString();
    }

    private String transpileComplexConst(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean isImaginary = false;
        String real = "";
        String im = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "ComplexComponent" -> {
                    if (isImaginary) {
                        result.append(transpileComplexComponent(ast.getChildren(child)) + "im");
                    } else {
                        result.append(transpileComplexComponent(ast.getChildren(child)));
                        isImaginary = true;
                    }
                }
                case "COMMA" -> result.append(" " + transpilePLUS(ast.getChildren(child)) + " ");
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    private String transpileComplexComponent(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Sign" -> result.append(transpileSign(ast.getChildren(child)));
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "RDCON" -> result.append(transpileRDCON(ast.getChildren(child)));
                case "Name" -> result.append(transpileName(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    private String transpileDummyArgName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private List<String> transpileInputItemList(List<Ast.Node<String>> childNodes, boolean stdin, String filename) throws IOException {
        List<String> inputList = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "InputItem" -> inputList.add(transpileInputItem(ast.getChildren(child), stdin, filename));
                case "COMMA" -> {
                }
                case "InputItemList" -> inputList.addAll(transpileInputItemList(ast.getChildren(child), stdin, filename));
            }
        }
        return inputList;
    }

    private String transpileInputItem(List<Ast.Node<String>> childNodes, boolean stdin, String filename) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Variable" -> result.append(transpileVariable(ast.getChildren(child)));
                case "InputImpliedDo" -> result.append(transpileInputImpliedDo(ast.getChildren(child), stdin, filename));
            }
        }
        return result.toString();
    }


    private String transpileVariable(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String l = "(";
        String r = ")";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "VariableName" -> {
                    if (listOfArrays.contains(transpileVariableName(ast.getChildren(child)))) {
                        l = "[";
                        r = "]";
                    }
                    result.append(transpileVariableName(ast.getChildren(child)));
                }
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "SubscriptList" -> result.append(transpileSubscriptList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "SubstringRange" -> result.append(transpileSubstringRange(ast.getChildren(child)));
            }
        }
        return result.toString().replace("(", l).replace(")", r);
    }

    private String transpileVariableName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private void transpileEquivalenceObject(List<Ast.Node<String>> childNodes) throws IOException {
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Variable")) {
                transpileVariable(ast.getChildren(child));
            }
        }
    }

    private String transpileSFExprList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "COLON" -> result.append(transpileCOLON(ast.getChildren(child)));
                case "COLON_COLON" -> result.append(transpileCOLON_COLON(ast.getChildren(child)));
                case "SFExprList" -> result.append(transpileSFExprList(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "SFDummyArgNameList" -> result.append(transpileSFDummyArgNameList(ast.getChildren(child)));
                case "SFExpr" -> result.append(transpileSFExpr(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SectionSubscript" -> result.append(transpileSectionSubscript(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SFTerm" -> result.append(transpileSFTerm(ast.getChildren(child)));
                case "Sign" -> result.append(transpileSign(ast.getChildren(child)));
                case "AddOperand" -> result.append(transpileAddOperand(ast.getChildren(child)));
                case "AddOp" -> result.append(transpileAddOp(ast.getChildren(child)));
                case "SFExpr" -> result.append(transpileSFExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFTerm(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SFTerm" -> result.append(transpileSFTerm(ast.getChildren(child)));
                case "MultOperand" -> result.append(transpileMultOperand(ast.getChildren(child)));
                case "MultOp" -> result.append(transpileMultOp(ast.getChildren(child)));
                case "SFFactor" -> result.append(transpileSFFactor(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFFactor(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PowerOp" -> result.append(transpilePowerOp(ast.getChildren(child)));
                case "MultOperand" -> result.append(transpileMultOperand(ast.getChildren(child)));
                case "SFPrimary" -> result.append(transpileSFPrimary(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFPrimary(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "SFVarName" -> result.append(transpileSFVarName(ast.getChildren(child)));
                case "ComplexDataRef" -> result.append(transpileComplexDataRef(ast.getChildren(child)));
                case "FunctionReference" -> result.append(transpileFunctionReference(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileComplexDataRef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Name" -> result.append(transpileName(ast.getChildren(child)));
                case "SectionSubscriptList" -> result.append(transpileSectionSubscriptList(ast.getChildren(child)));
                case "ComplexDataRef" -> result.append(transpileComplexDataRef(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFVarName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Ident" -> result.append(transpileIdent(ast.getChildren(child)));
                case "DOLLAR" -> result.append(transpileDOLLAR(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCommaSectionSubscript(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String arrSlice;
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SectionSubscript" -> {
                    arrSlice = transpileSectionSubscript(ast.getChildren(child));
                    if (arrSlice.length() - arrSlice.replace(":", "").length() == 2) {
                        arrSlice = swapIndices(arrSlice);
                    }
                    result.append(arrSlice);
                }

            }
        }
        return result.toString();
    }

    private String transpileInputImpliedDo(List<Ast.Node<String>> childNodes, boolean stdin, String filename) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> listExpr = new ArrayList<>();
        List<String> inputVars = new ArrayList<>();
        boolean isInputVar = true;
        String doVar = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Expr" -> {
                    if (isInputVar) {
                        inputVars.add(transpileExpr(ast.getChildren(child)));
                    } else {
                        listExpr.add(transpileExpr(ast.getChildren(child)));
                    }
                }
                case "COMMA" -> {
                }
                case "ImpliedDoVariable" -> {
                    doVar = transpileImpliedDoVariable(ast.getChildren(child));
                    isInputVar = false; // Next expressions are bounds
                }
                case "EQUAL" -> {
                }
                case "InputItemList" -> {
                    inputVars.addAll(transpileInputItemList(ast.getChildren(child), stdin, filename));
                }
            }
        }

        // Case of 2 bounds
        if (listExpr.size() == 2) {
            result.append(currentIndent + "for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(1) + "\n");
            pushTab();
            for (String var : inputVars) {
                String amendedVar = "";
                if (var.contains("[")) {
                    amendedVar = var.replaceAll("[^a-zA-Z]", "");
                } else {
                    amendedVar = var;
                }
                if (stdin) {
                    result.append(currentIndent + amendedVar + " = " + "readline()\n");
                } else {
                    result.append(currentIndent + amendedVar + " = " + "readline(" + filename + ")\n");
                }
                result.append(currentIndent + "global " + var + " = " + "parse_input(" + amendedVar + ")\n");
            }
            popTab();
            result.append(currentIndent + "end\n");
        }
        // Case of 1 bound
        else if (listExpr.size() == 2) {
            String toPrint = listExpr.get(0);
            result.append(currentIndent + "for " + doVar + " in 1:" + listExpr.get(1) + "\n");
            pushTab();
            for (String var : inputVars) {
                String amendedVar = "";
                if (var.contains("[")) {
                    amendedVar = var.replaceAll("[^a-zA-Z]", "");
                } else {
                    amendedVar = var;
                }
                if (stdin) {
                    result.append(currentIndent + amendedVar + " = " + "readline()\n");
                } else {
                    result.append(currentIndent + amendedVar + " = " + "readline(" + filename + ")\n");
                }
                result.append(currentIndent + "global " + var + " = " + "parse_input(" + amendedVar + ")\n");
            }
            popTab();
            result.append(currentIndent + "end\n");
            // Case of strided bounds
        } else if (listExpr.size() == 3) {
            result.append(currentIndent + "for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(1) + ":" + listExpr.get(2) + "\n");
            pushTab();
            for (String var : inputVars) {
                String amendedVar = "";
                if (var.contains("[")) {
                    amendedVar = var.replaceAll("[^a-zA-Z]", "");
                } else {
                    amendedVar = var;
                }
                if (stdin) {
                    result.append(currentIndent + amendedVar + " = " + "readline()\n");
                } else {
                    result.append(currentIndent + amendedVar + " = " + "readline(" + filename + ")\n");
                }
                result.append(currentIndent + "global " + var + " = " + "parse_input(" + amendedVar + ")\n");
            }
            popTab();
            result.append(currentIndent + "end\n");
        }
        return result.toString();
    }

    private String transpileSubscriptList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Subscript" -> result.append(transpileSubscript(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SubscriptList" -> result.append(transpileSubscriptList(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    private String transpileSubscript(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Expr")) {
                result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpilePowerOpMultOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PowerOp" -> result.append(transpilePowerOp(ast.getChildren(child)));
                case "MultOperand" -> result.append(transpileMultOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileActionStmt(List<Ast.Node<String>> childNodes, String args) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PrintStmt" -> result.append(currentIndent + transpilePrintStmt(ast.getChildren(child)));
                case "ArithmeticIfStmt" -> result.append(currentIndent + transpileArithmeticIfStmt(ast.getChildren(child)));
                case "AssignmentStmt" -> result.append(currentIndent + transpileAssignmentStmt(ast.getChildren(child)));
                case "AssignStmt" -> result.append(currentIndent + transpileAssignStmt(ast.getChildren(child)));
                case "BackspaceStmt" -> result.append(currentIndent + transpileBackspaceStmt(ast.getChildren(child)));
                case "CallStmt" -> result.append(currentIndent + transpileCallStmt(ast.getChildren(child)));
                case "CloseStmt" -> result.append(currentIndent + transpileCloseStmt(ast.getChildren(child)));
                case "ContinueStmt" -> {
                    popTab();
                    result.append(currentIndent + transpileContinueStmt(ast.getChildren(child)));
                }
                case "EndfileStmt" -> result.append(currentIndent + transpileEndfileStmt(ast.getChildren(child)));
                case "GotoStmt" -> result.append(currentIndent + transpileGotoStmt(ast.getChildren(child)));
                case "ComputedGotoStmt" -> result.append(currentIndent + transpileComputedGotoStmt(ast.getChildren(child)));
                case "AssignedGotoStmt" -> result.append(currentIndent + transpileAssignedGotoStmt(ast.getChildren(child)));
                case "IfStmt" -> {
                    result.append(currentIndent + transpileIfStmt(ast.getChildren(child), "") + "\n" + currentIndent + "end\n");
                }
                case "InquireStmt" -> result.append(currentIndent + transpileInquireStmt(ast.getChildren(child)));
                case "OpenStmt" -> result.append(currentIndent + transpileOpenStmt(ast.getChildren(child)));
                case "PauseStmt" -> result.append(currentIndent + transpilePauseStmt(ast.getChildren(child)));
                case "ReadStmt" -> result.append(currentIndent + transpileReadStmt(ast.getChildren(child)));
                case "ReturnStmt" -> result.append(currentIndent + transpileReturnStmt(ast.getChildren(child), args));
                case "RewindStmt" -> result.append(currentIndent + transpileRewindStmt(ast.getChildren(child)));
                case "StmtFunctionStmt" -> result.append(currentIndent + transpileStmtFunctionStmt(ast.getChildren(child)));
                case "StopStmt" -> result.append(currentIndent + transpileStopStmt(ast.getChildren(child)));
                case "WriteStmt" -> result.append(transpileWriteStmt(ast.getChildren(child)));

            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileArithmeticIfStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileAssignStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileBackspaceStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileEndfileStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpilePauseStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileAssignedGotoStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    // Not supported
    private String transpileInquireStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("EOS")) {
                result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileWriteStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> items = new ArrayList<>();
        String writeStr = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "WRITE", "LPAREN", "RPAREN" -> {
                }
                case "IoControlSpecList" -> writeStr = transpileIoControlSpecList(ast.getChildren(child), true);
                case "OutputItemList" -> items = List.of(transpileOutputItemList(ast.getChildren(child)).split(","));
            }
        }

        String resStr = writeStr;

        for (String item : items) {
            resStr = resStr + "," + item;
        }

        if (resStr.contains("sprintf")) {
            resStr = resStr + ")";
        }

        result.append(currentIndent + resStr + ")\n");
        return result.toString();
    }

    private String transpileIoControlSpecList(List<Ast.Node<String>> childNodes, boolean topLevel) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean stdout = false;
        boolean filename = false;
        String file = "";
        boolean fmt = false;
        String fmtString = "";
        String varStr = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UnitIdentifier" -> {
                    String res = transpileUnitIdentifier(ast.getChildren(child));
                    if (res.equals("*") || res.equals("_6")) {
                        stdout = true;
                    } else {
                        file = res;
                        filename = true;
                    }
                }
                case "DOLLAR", "COMMA" -> {
                }
                case "FormatIdentifier" -> {
                    fmtString = transpileFormatIdentifier(ast.getChildren(child));
                    fmt = true;
                }
                case "IoControlSpec" -> {
                    String var = transpileIoControlSpec(ast.getChildren(child));
                    if (var.contains("_6")) {
                        stdout = true;
                    } else if (ast.getChildren(child).get(0).getData().equals("UNIT_EQUAL")) {
                        filename = true;
                        file = var;
                    } else if (ast.getChildren(child).get(0).getData().equals("FMT_EQUAL")) {
                        fmt = true;
                        fmtString = var;
                    }
                }
                case "IoControlSpecList" -> varStr = varStr + transpileIoControlSpecList(ast.getChildren(child), false);
            }
        }

        // Stdout prints to console, otherwise write to specific file
        if (stdout && topLevel) {
            result.append("println(");
        } else if (filename && topLevel) {
            result.append("write(" + file +",");
        }
        // Format string
        if (fmt) {
            result.append("@sprintf(" + fmtString);
        }
        if (varStr.length() > 0) {
            result.append(varStr);
        }
        return result.toString();
    }

    private String transpileOutputItemList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "OutputItemList1" -> result.append(transpileOutputItemList1(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileOutputItemList1(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "OutputImpliedDo" -> result.append(transpileOutputImpliedDo(ast.getChildren(child)));
                case "OutputItemList1" -> result.append(transpileOutputItemList1(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCallStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<List<String>> args = new ArrayList<>();
        List<String> assignVars = new ArrayList<>();
        List<String> argVars = new ArrayList<>();
        String name = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "SubroutineNameUse" -> name = transpileSubroutineNameUse(ast.getChildren(child));
                case "SubroutineArgList" -> args.addAll(transpileSubroutineArgList(ast.getChildren(child)));
            }
        }

        for (List<String> pair : args) {
            if (pair.get(1).equals("o")) {
                assignVars.add(pair.get(0)); // If not number or string, assign as output to function
            }
            argVars.add(pair.get(0));
        }

        if (assignVars.size() > 0) {
            result.append(currentIndent + "global " + String.join(",", assignVars) + " = ");
        }

        result.append(name + "(" +  String.join(",", argVars) + ")\n");

        return result.toString();
    }

    private String transpileStopStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "STOP" -> result.append(transpileSTOP(ast.getChildren(child)));
                case "ICONOrScon" -> result.append(transpileICONOrScon(ast.getChildren(child)));
                case "EOS" -> result.append(")" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileICONOrScon(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), false));
                case "SCON" -> result.append(transpileSCON(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileStmtFunctionStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "Name" -> result.append(transpileName(ast.getChildren(child)));
                case "StmtFunctionRange" -> result.append(transpileStmtFunctionRange(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileStmtFunctionRange(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "SFDummyArgNameList" -> result.append(transpileSFDummyArgNameList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "EQUAL" -> result.append(transpileEQUAL(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRewindStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "REWIND" -> result.append("seek(");
                case "UnitIdentifier" -> result.append(transpileUnitIdentifier(ast.getChildren(child)));
                case "PositionSpecList" -> result.append(transpilePositionSpecList(ast.getChildren(child)));
                case "EOS" -> result.append(", 0)" + transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileReadStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String unit = "";
        List<String> vars = new ArrayList<>();
        boolean stdin = false;
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "READ" -> {
                }
                // Read from file
                case "RdCtlSpec" -> {
                    String readType = transpileRdCtlSpec(ast.getChildren(child));
                    if (readType.equals("(*)")) {
                        stdin = true;
                    } else {
                        readType = readType.replace("(", "");
                        readType = readType.replace(")", "");
                        unit = readType;
                        if (unit.contains("_5")) {
                            stdin = true;
                        }
                    }
                }
                // Read from stdin
                case "RdFmtId" -> {
                    String readType = transpileRdFmtId(ast.getChildren(child));
                    stdin = true;
                    // No equivalent of formatting read statements in Julia
                }
                case "CommaInputItemList" -> vars = transpileInputItemList(ast.getChildren(child), stdin, unit);
                case "InputItemList" -> vars = transpileInputItemList(ast.getChildren(child), stdin, unit);
                case "EOS" -> {
                }
            }
        }

        if (stdin) {
            for (String var : vars) {
                if (!var.contains("readline")) {
                    String amendedVar = "";
                    if (var.contains("[")) {
                        amendedVar = var.replaceAll("[^a-zA-Z]", "");
                    } else {
                        amendedVar = var;
                    }
                    result.append(currentIndent + amendedVar + " = readline()\n");
                    // Julia does not have implicit casting of input, so need to check
                    result.append(currentIndent + "global " + var + " = " + "parse_input(" + amendedVar + ")\n");
                    listOfArrays.add(var);
                } else {
                    result.append(var);
                }
            }
        } else {
            for (String var : vars) {
                if (!var.contains("readline")) {
                    String amendedVar = "";
                    if (var.contains("[")) {
                        listOfArrays.add(var);
                        amendedVar = var.replaceAll("[^a-zA-Z]", "");
                    } else {
                        amendedVar = var;
                    }
                    result.append(currentIndent + amendedVar + " = readline(" + unit + ")\n");
                    // Julia does not have implicit casting of input, so need to check
                    result.append(currentIndent + "global " + var + " = " + "parse_input(" + amendedVar + ")\n");
                } else {
                    result.append(var);
                }
            }
        }
        return result.toString();
    }

    // Returns are handled by subprogram and function transpiler
    private String transpileReturnStmt(List<Ast.Node<String>> childNodes, String args) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean altReturn = false;
        String expr = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "RETURN", "EOS" -> {
                }
                case "Expr" -> {
                    altReturn = true;
                    expr = transpileExpr(ast.getChildren(child));
                }
            }
/*
            if (!altReturn) {
                result.append("return\n");
            } else {
                result.append("return get_arg(" + expr + ", " + args + ")\n");
            }

 */
        }
        return result.toString();
    }

    private String transpileRdCtlSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "RdUnitId" -> result.append(transpileRdUnitId(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "RdIoCtlSpecList" -> result.append(transpileRdIoCtlSpecList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRdFmtId(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblRef" -> result.append(transpileLblRef(ast.getChildren(child)));
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
                case "COperand" -> result.append(transpileCOperand(ast.getChildren(child)));
                case "ConcatOp" -> result.append(transpileConcatOp(ast.getChildren(child)));
                case "CPrimary" -> result.append(transpileCPrimary(ast.getChildren(child)));
                case "RdFmtIdExpr" -> result.append(transpileRdFmtIdExpr(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRdFmtIdExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "UFExpr" -> result.append(transpileUFExpr(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpilePositionSpecList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "PositionSpec" -> result.append(transpilePositionSpec(ast.getChildren(child)));
                case "UnitIdentifier" -> result.append(transpileUnitIdentifier(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpilePositionSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("UnitIdentifier")) {
                result.append(transpileUnitIdentifier(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRdIoCtlSpecList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UnitIdentifier" -> result.append(transpileUnitIdentifier(ast.getChildren(child)));
                case "IoControlSpec" -> result.append(transpileIoControlSpec(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileIoControlSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UnitIdentifier" -> result.append(transpileUnitIdentifier(ast.getChildren(child)));
                case "FormatIdentifier" -> result.append(transpileFormatIdentifier(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileRdUnitId(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "UFExpr" -> {
                    String res = transpileUFExpr(ast.getChildren(child));
                    if (Pattern.matches("-?\\d+(\\.\\d+)?", res)) {
                        result.append("_");
                    }
                    result.append(res);
                }
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileOpenStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> vars = new ArrayList<>();
        String unit = "";
        String fileName = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "OPEN", "LPAREN", "RPAREN", "EOS" -> {
                }
                case "ConnectSpecList" -> vars = transpileConnectSpecList(ast.getChildren(child));
            }
        }

        for (String var : vars) {
            if (var.charAt(0) == '_') {
                unit = var;
            } else {
                fileName = var;
            }
        }

        result.append("global " + unit + " = open(" + fileName + ", \"r+\");\n");
        return result.toString();
    }

    private List<String> transpileConnectSpecList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> vars = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ConnectSpec" -> vars.addAll(transpileConnectSpec(ast.getChildren(child)));
                case "UnitIdentifier" -> {
                    String res = transpileUFExpr(ast.getChildren(child));
                    if (Pattern.matches("-?\\d+(\\.\\d+)?", res)) {
                        result.append("_");
                    }
                    result.append(res);
                }
                case "ConnectSpecList" -> vars.addAll(transpileConnectSpecList(ast.getChildren(child)));
            }
        }
        return vars;
    }

    private List<String> transpileConnectSpec(List<Ast.Node<String>> childNodes) throws IOException {
        List<String> vars = new ArrayList<>();
        boolean nextIsFile = false;
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UnitIdentifier" -> vars.add(transpileUnitIdentifier(ast.getChildren(child)));
                case "FILE_EQUAL" -> nextIsFile = true;
                case "CExpr" -> {
                    if (nextIsFile) {
                        vars.add(transpileCExpr(ast.getChildren(child)));
                    }
                    nextIsFile = false;
                }

            }
        }

        // Return list of label names
        return vars;
    }

    private String transpileContinueStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "CONTINUE" -> {
                    if (inDo.size() > 0) {
                        result.append("end");
                        inDo.remove(inDo.size() - 1);
                    }
                }
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileGotoStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "GoToKw" -> result.append(transpileGoToKw(ast.getChildren(child)));
                case "LblRef" -> result.append(transpileLblRef(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileComputedGotoStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String var = "";
        List<String> lblList = new ArrayList<>();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "GoToKw", "LPAREN", "RPAREN", "COMMA" -> {
                }
                case "LblRefList" -> lblList = Arrays.asList(transpileLblRefList(ast.getChildren(child)).split(","));
                case "Expr" -> var = transpileExpr(ast.getChildren(child));
            }
        }

        for (int i = 0; i < lblList.size(); i++) {
            result.append("conditional_goto(" + var + ", " + lblList.get(i) + ", " + (i + 1) + ")\n");
        }
        return result.toString();
    }

    private String transpileCloseStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "CLOSE" -> result.append(transpileCLOSE(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "CloseSpecList" -> result.append(transpileCloseSpecList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileIfStmt(List<Ast.Node<String>> childNodes, String args) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "IF" -> result.append(transpileIF(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "ActionStmt" -> {
                    pushTab();
                    result.append("\n" + transpileActionStmt(ast.getChildren(child), args));
                }
            }
        }
        popTab();
        return result.toString();
    }

    private String transpileGoToKw(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "GOTO", "GO" -> result.append(transpileGOTO(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileSubroutineNameUse(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private List<List<String>> transpileSubroutineArgList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<List<String>> args = new ArrayList<>(new ArrayList<>());
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SubroutineArg" -> {
                    List<String> pair = new ArrayList<>();
                    pair.add(transpileSubroutineArg(ast.getChildren(child)));
                    if (Pattern.matches("[a-zA-Z]+", transpileSubroutineArg(ast.getChildren(child)))
                            && !transpileSubroutineArg(ast.getChildren(child)).contains("\"")) {
                        // Not a number or string then is output
                        pair.add("o");
                    } else {
                        pair.add("n"); // Number or string so not a variable
                    }
                    args.add(pair);
                }
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SubroutineArgList" -> args.addAll(transpileSubroutineArgList(ast.getChildren(child)));
            }
        }

        return args;
    }

    private String transpileCloseSpecList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CloseSpec" -> result.append(transpileCloseSpec(ast.getChildren(child)));
                case "COMMA" -> {
                }
                case "CloseSpecList" -> {
                }
                case "UnitIdentifier" -> result.append(transpileUnitIdentifier(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileLblRefList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblRef" -> result.append(transpileLblRef(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "LblRefList" -> result.append(transpileLblRefList(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCloseSpec(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("UnitIdentifier")) {
                result.append(transpileUnitIdentifier(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileUnitIdentifier(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("UFExpr")) {
                String res = transpileUFExpr(ast.getChildren(child));
                if (Pattern.matches("-?\\d+(\\.\\d+)?", res)) {
                    result.append("_");
                }
                result.append(res);
            }
        }

        return result.toString();
    }

    private String transpileUFExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Sign" -> result.append(transpileSign(ast.getChildren(child)));
                case "UFTerm" -> result.append(transpileUFTerm(ast.getChildren(child)));
                case "AddOp" -> result.append(transpileAddOp(ast.getChildren(child)));
                case "UFExpr" -> result.append(transpileUFExpr(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileUFTerm(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UFFactor" -> result.append(transpileUFFactor(ast.getChildren(child)));
                case "MultOp" -> result.append(transpileMultOp(ast.getChildren(child)));
                case "UFTerm" -> result.append(transpileUFTerm(ast.getChildren(child)));
                case "UFPrimary" -> result.append(transpileUFPrimary(ast.getChildren(child)));
                case "ConcatOp" -> result.append(transpileConcatOp(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileUFFactor(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "UFPrimary" -> result.append(transpileUFPrimary(ast.getChildren(child)));
                case "PowerOp" -> result.append(transpilePowerOp(ast.getChildren(child)));
                case "UFFactor" -> result.append(transpileUFFactor(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileUFPrimary(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "ICON" -> result.append(transpileICON(ast.getChildren(child), ast.findParent(child, "Label")));
                case "SCON" -> result.append(transpileSCON(ast.getChildren(child)));
                case "NameDataRef" -> result.append(transpileNameDataRef(ast.getChildren(child)));
                case "FunctionReference" -> result.append(transpileFunctionReference(ast.getChildren(child)));
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "UFExpr" -> result.append(transpileUFExpr(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileSubroutineArg(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "Name" -> result.append(transpileName(ast.getChildren(child)) + " = ");
                case "LblRef" -> result.append(transpileLblRef(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileLblRef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Label")) {
                result.append(transpileLabel(ast.getChildren(child)));
            }
        }
        return result.toString();
    }


    private String transpileAssignmentStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        String substringSlice;
        String l = "(";
        String r = ")";
        String name = "";
        String fullName = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "Name" -> {
                    name = transpileName(ast.getChildren(child));
                    if (listOfArrays.contains(name)) {
                        l = "[";
                        r = "]";
                    } else {
                        result.append("global ");
                        if (listOfFunctions.contains(name)) {
                            result.append("_");
                        }
                    }
                    result.append(name);
                    fullName += name;
                }
                case "LPAREN" -> {
                    if (!listOfArrays.contains(name)) {
                        listOfFunctions.add(name);
                    }
                    result.append(l);
                    fullName += l;
                }
                case "EQUAL" -> result.append(" = ");
                case "SFDummyArgNameList" -> {
                    result.append(transpileSFDummyArgNameList(ast.getChildren(child)));
                    fullName += transpileSFDummyArgNameList(ast.getChildren(child));
                }
                case "RPAREN" -> {
                    result.append(r);
                    fullName += r;
                }
                case "SFExprList" -> result.append(transpileSFExprList(ast.getChildren(child)));
                case "PERCENT" -> result.append(transpilePERCENT(ast.getChildren(child)));
                case "NameDataRef" -> result.append(transpileNameDataRef(ast.getChildren(child)));
                case "SubstringRange" -> {
                    substringSlice = transpileSubstringRange(ast.getChildren(child));
                    substringSlice = substringSlice.substring(1, substringSlice.length() - 1);
                    // If stride included, swap indices
                    if (substringSlice.length() - substringSlice.replace(":", "").length() == 2) {
                        substringSlice = swapIndices(substringSlice);
                    }
                }
                case "Expr" -> {
                    String expr = transpileExpr(ast.getChildren(child));
                    if (expr.contains("\"")) {
                        listOfArrays.add(name);
                        result.append(expr);
                    } else {
                        result.append(expr);
                    }
                }
                case "EOS" -> result.append(transpileEOS(ast.getChildren(child)));
            }
        }

        if (compilingSubroutine) {
            HashSet<String> oldVars = subroutine2Vars.get(currSubroutine);
            oldVars.add(name);
            subroutine2Vars.put(currSubroutine, oldVars);
        }

        return result.toString();
    }

    private String transpilePrintStmt(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblDef" -> result.append(addLabel(transpileLblDef(ast.getChildren(child))));
                case "PRINT" -> {
                }
                case "FormatIdentifier" -> {
                    String fmt = transpileFormatIdentifier(ast.getChildren(child));
                    if (fmt.equals("*")) {
                        result.append("println(");
                    } else {
                        result.append("@printf(" + fmt + ", ");
                    }
                }
                case "COMMA" -> {
                }
                case "OutputItemList" -> result.append(transpileOutputItemList(ast.getChildren(child)));
                case "EOS" -> {
                    result.append(")");
                    result.append(transpileEOS(ast.getChildren(child)));
                }
            }
        }
        return result.toString();
    }

    private String transpileFormatIdentifier(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LblRef" -> result.append(transpileLblRef(ast.getChildren(child)));
                case "CExpr" -> result.append(transpileCExpr(ast.getChildren(child)));
                case "STAR" -> result.append(transpileSTAR(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileOutputImpliedDo(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> listExpr = new ArrayList<>();
        List<String> outputVars = new ArrayList<>();
        boolean isOutputVar = true;
        String doVar = "";
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "Expr" -> {
                    if (isOutputVar) {
                        outputVars.add(transpileExpr(ast.getChildren(child)));
                    } else {
                        listExpr.add(transpileExpr(ast.getChildren(child)));
                    }
                }
                case "COMMA" -> {
                }
                case "ImpliedDoVariable" -> {
                    doVar = transpileImpliedDoVariable(ast.getChildren(child));
                    isOutputVar = false; // next expressions are bounds
                }
                case "EQUAL" -> {
                }
                case "OutputItemList1" -> {
                    outputVars.addAll(List.of(transpileOutputItemList1(ast.getChildren(child)).split(",")));
                }
            }
        }

        StringBuilder outVars = new StringBuilder();

        for (String var : outputVars) {
            outVars.append(var + " ");
        }

        // Case of 2 bounds
        if (listExpr.size() == 2) {
            result.append("for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(1) + "\n");
            pushTab();
            result.append(currentIndent + "println(" + outVars + ")\n"); // Output so print
            popTab();
            result.append(currentIndent + "end\n");
        }
        // Case of 1 bound
        else if (listExpr.size() == 2) {
            String toPrint = listExpr.get(0);
            result.append("for " + doVar + " in 1:" + listExpr.get(1) + "\n");
            pushTab();
            result.append(currentIndent + "println(" + outVars + ")\n"); // Output so print
            popTab();
            result.append(currentIndent + "end\n");
            // Case of strided bounds
        } else if (listExpr.size() == 3) {
            result.append(currentIndent + "for " + doVar + " in " + listExpr.get(0) + ":" + listExpr.get(1) + ":" + listExpr.get(2) + "\n");
            pushTab();
            result.append(currentIndent + "println(" + outVars + ")\n"); // Output so print
            popTab();
            result.append(currentIndent + "end\n");
        }

        return result.toString();
    }

    private String transpileCExpr(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CPrimary" -> result.append(transpileCPrimary(ast.getChildren(child)));
                case "CPrimaryConcatOp" -> result.append(transpileCPrimaryConcatOp(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCPrimaryConcatOp(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "CPrimary" -> result.append(transpileCPrimary(ast.getChildren(child)));
                case "ConcatOp" -> result.append(transpileConcatOp(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileImpliedDoVariable(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpileCPrimary(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "CExpr" -> result.append(transpileCExpr(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
                case "COperand" -> result.append(transpileCOperand(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileCOperand(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SCON" -> result.append(transpileSCON(ast.getChildren(child)));
                case "NameDataRef" -> result.append(transpileNameDataRef(ast.getChildren(child)));
                case "FunctionReference" -> result.append(transpileFunctionReference(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSectionSubscriptRef(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "SectionSubscriptList" -> result.append(transpileSectionSubscriptList(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubstringRange(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "LPAREN" -> result.append(transpileLPAREN(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "SubscriptTripletTail" -> result.append(transpileSubscriptTripletTail(ast.getChildren(child)));
                case "RPAREN" -> result.append(transpileRPAREN(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSectionSubscriptList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SectionSubscript" -> result.append(transpileSectionSubscript(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SectionSubscriptList" -> result.append(transpileSectionSubscriptList(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFDummyArgNameList(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SFDummyArgName" -> result.append(transpileSFDummyArgName(ast.getChildren(child)));
                case "COMMA" -> result.append(transpileCOMMA(ast.getChildren(child)));
                case "SFDummyArgNameList" -> result.append(transpileSFDummyArgNameList(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSFDummyArgName(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            if (child.getData().equals("Ident")) {
                result.append(transpileIdent(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSubscriptTripletTail(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "COLON" -> result.append(transpileCOLON(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
                case "COLON_COLON" -> result.append(transpileCOLON_COLON(ast.getChildren(child)));
            }
        }
        return result.toString();
    }

    private String transpileSectionSubscript(List<Ast.Node<String>> childNodes) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            switch (child.getData()) {
                case "SubscriptTripletTail" -> result.append(transpileSubscriptTripletTail(ast.getChildren(child)));
                case "Expr" -> result.append(transpileExpr(ast.getChildren(child)));
            }
        }

        return result.toString();
    }

    private String transpilePERCENT(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append(".");

        }
        return result.toString();
    }

    private String transpilePRINT(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("println(");
        }
        return result.toString();
    }

    private String transpileGOTO(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("@goto ");
        }
        return result.toString();
    }


    private String transpileLPAREN(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("(");

        }
        return result.toString();
    }

    private String transpileRPAREN(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append(") ");

        }
        return result.toString();
    }

    private String transpileCOMMA(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append(", ");

        }
        return result.toString();
    }

    private String transpileSTAR(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("*");

        }
        return result.toString();
    }

    private String transpileSLASH(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("/");

        }
        return result.toString();
    }

    private String transpileConcatOp(List<Ast.Node<String>> childNodes) {
        return "*";
    }


    private String transpileCOLON(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append(":");

        }
        return result.toString();
    }

    private String transpileEQUAL(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append(" = ");

        }
        return result.toString();
    }

    private String transpileUNDERSCORE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("_");

        }
        return result.toString();
    }

    private String transpileSTOP(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("exit(");

        }
        return result.toString();
    }

    private String transpileTRUE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("true ");

        }
        return result.toString();
    }

    private String transpileFALSE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("false ");

        }
        return result.toString();
    }

    private String transpileCOLON_COLON(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("::");
        }
        return result.toString();
    }

    private String transpileNOT(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("!");

        }
        return result.toString();
    }

    private String transpilePLUS(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("+");

        }
        return result.toString();
    }

    private String transpileMINUS(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("-");

        }
        return result.toString();
    }

    private String transpileEQUAL_EQUAL(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("==");

        }
        return result.toString();
    }

    private String transpileOR(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("||");

        }
        return result.toString();
    }

    private String transpileBANG_EQUAL(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append("!=");

        }
        return result.toString();
    }

    private String transpileAND(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("&&");

        }
        return result.toString();
    }

    private String transpileLESS(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("<");
        }
        return result.toString();
    }

    private String transpileLESS_EQUAL(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("<=");
        }
        return result.toString();
    }

    private String transpileGREATER(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append(">");
        }
        return result.toString();
    }

    private String transpileFUNCTION(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("function ");
        }
        return result.toString();
    }

    private String transpilePowerOp(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("^");
        }
        return result.toString();
    }

    private String transpileGREATER_EQUAL(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append(">=");
        }
        return result.toString();
    }

    private String transpileCOMMENT(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData().substring(1);
            result.append("# " + text);
        }
        return result.toString();
    }

    private String transpileNEWLINE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("\n");
        }
        return result.toString();
    }

    private String transpileCLOSE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("close");
        }
        return result.toString();
    }

    private String transpileIF(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("if ");
        }
        return result.toString();
    }

    private String transpileELSEIF(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("elseif ");
        }
        return result.toString();
    }

    private String transpileELSE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("else ");
        }
        return result.toString();
    }

    private String transpileDOLLAR(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("$");
        }
        return result.toString();
    }

    private String transpileSUBROUTINE(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("function ");
        }
        return result.toString();
    }

    private String transpileEND(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            result.append("end");
        }
        return result.toString();
    }

    private String transpileSCON(List<Ast.Node<String>> childNodes) {
        StringBuilder result = new StringBuilder();
        for (Ast.Node<String> child : childNodes) {
            String text = child.getData();
            result.append(text);
        }
        return result.toString();
    }

    private String convertToJuliaType(String forType) {

        switch (forType) {
            case "DOUBLE", "REAL", "DOUBLEPRECISION" -> {
                return "0.0";
            }
            case "CHARACTER" -> {
                return "\"\"";
            }
            case "INTEGER" -> {
                return "0";
            }
            case "COMPLEX" -> {
                return "(0,0)";
            }
            case "LOGICAL" -> {
                return "false";
            }
            default -> {
                return "";
            }
        }
    }

        private String addLabel (String label){
            return "@label " + label + "\n" + currentIndent;
        }

        private String swapIndices (String slice){
            String a = slice.substring(0, slice.indexOf(":"));
            String b = slice.substring(slice.indexOf(":") + 1, slice.lastIndexOf(":"));
            String c = "";
            if (slice.lastIndexOf(":") != slice.length() - 1) {
                c = slice.substring(slice.lastIndexOf(":") + 1, slice.length() - 1);
            }

            return a + ":" + c + ":" + b;
        }

        private void pushTab () {
            currentIndent = currentIndent + "\t";
        }

        private void popTab () {
            if (currentIndent.length() > 0) {
                currentIndent = currentIndent.substring(0, currentIndent.length() - 1);
            }
    }
}