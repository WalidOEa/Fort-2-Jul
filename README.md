# Fort-2-Jul

## Requirements
Java Development Kit (JDK) 17 or newer

## Running Fort-2-Jul Quickly,
To run Fort-2-Jul either run,

    java -jar Fort-2-Jul-1.0.jar 

For the GUI or for command line run,

    java -jar Fort-2-Jul-1.0.jar program_to_transpiler.for

Supported file extensions are: *.f, *.FOR, *.for

The transpiler currently works best with programs written in Fortran 77. Future work may include expanding the 
scope of the mapped domain, adding support for extensions of Fortran such as Fortran 90. 

## Running GUI or Command Line?
Fort-2-Jul can be ran with either a GUI or Command line. You can run the GUI by not supplying a argument in command line. Likewise, supplying a argument in command will transpile it in command line. Remember, the resultant code is always in the same folder as the source file, the file to be transpiled.

The GUI provides a visual display of transpilation, further allowing you to manipulate the file to transpile without having to open another editor. The command line is intended for lower end machines, ideally with a Unix-based OS. The functionality is limited in command line but is purely intended for invoking the tool.

## Transpiling (GUI)
If you are using the GUI, first open your desired Fortran program you want to transpile. This can be done by following,

    File -> Open

Next, select the transpile button in the leftmost text field to transpile the opened file. Once completed, the Julia file is automatically copied into the rightmost text field. If you're unhappy with the resultant file, you can edit the leftmost field or the rightmost field. However, it is important to note that the files are immutable and modifying the text fields will not be reflected in the original files. Only transpiling saves the state of the file.

The transpiled file is saved into the same location, as well as the macros file, of the Fortran file.

For now, the Julia Linter button does not function.

## Transpiling (Command Line)
Run the jar file with the argument specifiying the whole path to the file to transpile. This option is intended for lower end machines.

## Examples
This repo contains examples of Fortran 77 and their Julia equivalent transpiled using this tool. Explore this directory under _tests_ to further grasp the power of this tool.

## Unsupported Statements and Constructs
Note: Below is a list of statements and constructs that are not supported in Fort-2-Jul. This may change in the forseeable future.

    - End
    - Implicit
    - Equivalence 
    - External functions
    - Intrinsic
    - Save
    - Arrays with custom index ranges
    - Assigned GoTo
    - Arithmetic If
    - Pause
    - IO Control Spec options
    - Inquire
    - Backspace
    - Endfile

However, it is advised to still transpile your program then refactor it despite the prescence of statements or constructs that are not supported as certain constructs and statements such as End that are not supported do not affect program correctness as Julia has no need for the keyword.

## TODO
- Implement linter
- Expand the domain of transpiled Fortran 77
- Transpile Fortran 90
