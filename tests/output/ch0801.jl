include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0801

using Printf

global CURRNT = create_array("null",20,)

global RESIST = 0.0
global VOLTAG = 0
println("INPUT RESISTANCE")
RESIST = readline()
global RESIST = parse_input(RESIST)
for VOLTAG = -20:20

	CURRNT[VOLTAG] = VOLTAG/RESIST
	println(VOLTAG, " ", CURRNT[VOLTAG] )
@label _10
end
