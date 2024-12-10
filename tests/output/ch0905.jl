include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0905

using Printf


global I = 0
global SMALL = 0.0
global BIG = 0.0
global BIG = 1.0 
global SMALL = 1.0 
for I = 1:50

	@printf(" %3d %10.4e %10.4e\n", I, SMALL, BIG)
		global SMALL = SMALL/10.0 
	global BIG = BIG*10.0 
@label _10
end
