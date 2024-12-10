package uk.ac.soton.comp3200.fort2jul;

import java.io.IOException;

/**
 * Serves as the entry point of launching the GUI or the transpiler from command line
 */
public class Launcher {

    /**
     * Entry point into transpiler, supply argument for command line use, default into GUI if no arguments supplied.
     * @param args Path of Fortran file
     */
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                Fort2Jul.main(args);
            } else if (args.length == 0) {
                App.main(args);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
