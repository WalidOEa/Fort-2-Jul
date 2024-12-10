package uk.ac.soton.comp3200.fort2jul.transpiler;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Automatically generates macro file for any Julia programming.
 */
public class JuliaMacroGenerator {

    private static final Logger logger = Logger.getLogger(JuliaMacroGenerator.class);

    /**
     * Output directory for macros
     */
    private final String outputDir;

    /**
     * Macros file
     */
    private final File file;

    /**
     * Filewriter
     */
    private FileWriter fileWriter = null;

    /**
     * Generates macro file to their specified output directory
     * @param outputDir
     */
    public JuliaMacroGenerator(String outputDir) {
        logger.info("Creating new macros.jl file in " + outputDir);

        this.outputDir = outputDir;
        this.file = new File(outputDir, "macros.jl");
    }

    /**
     * Writes macro definitions in macros file
     * @throws IOException
     */
    public void generateMacroCode() throws IOException {
        logger.info("Writing into macros.jl...");

        setFileWriter(new FileWriter(file));

        fileWriter.write("# macros.jl\n\n");

        writeDefineConst();

        writeDeclareIntegerVar();

        writeCreate1DArray();

        writeCreateConstantArray();

        writeCreateStridedArray();

        writeLoopAndConcatArray();

        //writeConditionalGoto();

        writeGetArg();

        writeTryParse();

        writeCreateArray();

        writeModFunc();

        writeParseInput();

        fileWriter.close();

        logger.info("Finished writing into macros.jl");
    }

    private void writeDefineConst() throws IOException {
        fileWriter.write("macro define_const(varname, value)\n\treturn esc(:(const $varname = $value))\nend\n\n");
    }

    private void writeDeclareIntegerVar() throws IOException {
        fileWriter.write("macro declare_integer_var(vartype, varname)\n\treturn esc(:(local $varname::$vartype))\nend\n\n");
    }

    private void writeCreate1DArray() throws IOException {
        fileWriter.write("macro create_1d_array(x, y, var)\n\tquote\n\t\tcollect($(x):$(y))\n\tend\nend\n\n");
    }

    private void writeCreateConstantArray() throws IOException {
        fileWriter.write("macro create_constant_array(val, x, y, var)\n\tquote\n\t\tfill($(val), $(num[y] - num[x] + 1))\n\tend\nend\n\n");
    }

    private void writeCreateStridedArray() throws IOException {
        fileWriter.write("macro create_strided_array(val, x, y)\n\tquote\n\t\tcollect($(x):$(val):$(y))\n\tend\nend\n\n");
    }

    private void writeLoopAndConcatArray() throws IOException {
        fileWriter.write("macro loop_and_concat_array(arr, x, y, var)\n\tquote\n\t\tresult = []\n\t\tfor elem in $(esc(arr))[$(esc(x)):$(esc(y))]\n\t\t\tresult = vcat(result, elem)\n\t\tend\n\t\tresult\n\tend\nend\n\n");
    }

    private void writeConditionalGoto() throws IOException {
        fileWriter.write("function conditional_goto(var, name, int)\n\tif var == int\n\t\t@goto name\n\tend\nend\n\n");
    }

    private void writeGetArg() throws IOException {
        fileWriter.write("function get_arg(index, args...)\n\treturn values[index]\nend\n\n");
    }

    private void writeTryParse() throws IOException {
        fileWriter.write("function try_parse(str, T)\n\ttry\n\t\tparse(T,str)\n\t\ttrue\n\tcatch\n\t\tfalse\n\tend\nend\n\n");
    }

    private void writeCreateArray() throws IOException {
        fileWriter.write("function create_array(t, dims::Int...)\n\tif t == \"INTEGER\"\n\t\treturn Array{Int}(undef, dims...)\n\telseif t == \"REAL\" || t == \"DOUBLE\" || t == \"DOUBLEPRECISION\"\n\t\treturn Array{Float64}(undef, dims...)\n\telseif t == \"COMPLEX\"\n\t\treturn Array{Complex{Float64}}(undef, dims...)\n\telseif t == \"CHARACTER\"\n\t\treturn Array{String}(undef, dims...)\n\telse\n\t\treturn Array{Float64}(undef,dims...)\n\tend\nend\n");}

    private void writeModFunc() throws IOException {
        fileWriter.write("function mod(a, b)\n\treturn a % b\nend\n");
    }

    private void writeParseInput() throws IOException {
        fileWriter.write("function parse_input(a)\n\tif try_parse(a, Int) == true\n\t\treturn parse(Int, a)\n\telseif try_parse(a, Float64) == true\n\t\treturn parse(Float64, a)\n\telse\n\t\treturn a\n\tend\nend\n");
    }

    public void setFileWriter(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }
}
