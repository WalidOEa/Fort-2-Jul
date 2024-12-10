include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0902

using Printf


global BIG = 0
global I = 0
global BIG = 10
for I = 1:40

	@printf(" %3d  %12d\n", I, BIG)
		global BIG = BIG*10
@label _10
end
