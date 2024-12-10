include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0908

using Printf


global FNAME = Vector{String}(undef,15)
global AGE = 0
global WEIGHT = 0.0
global SEX = Vector{String}(undef,1)
println(" TYPE IN YOUR FIRST NAME ")
FNAME = readline()
global FNAME = parse_input(FNAME)
println(" TYPE IN YOUR AGE IN YEARS")
AGE = readline()
global AGE = parse_input(AGE)
println(" TYPE IN YOUR WEIGHT IN KILOS")
WEIGHT = readline()
global WEIGHT = parse_input(WEIGHT)
println(" TYPE IN YOUR SEX (F/M)")
SEX = readline()
global SEX = parse_input(SEX)
println(" YOUR PERSONAL DETAILS ARE")
println()
@printf("    FIRST NAME    AGE WEIGHT  SEX\n", )
@printf(" %s  %3d  %5.2f  %s\n", FNAME, AGE, WEIGHT, SEX)
