include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0906

using Printf

#  THIS PROGRAM READS IN AND PRINTS OUT
#  YOUR FIRST NAME

global FNAME = Vector{String}(undef,20)
println(" TYPE IN YOUR FIRST NAME.")
println(" UP TO 20 CHARACTERS")
FNAME = readline()
global FNAME = parse_input(FNAME)
@printf(" %s\n", FNAME)
