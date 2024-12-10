include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0903

using Printf


global FLUID = 0
global LITRES = 0.0
global PINTS = 0.0
for FLUID = 1:10

	global LITRES = FLUID/1.75 
	global PINTS = FLUID*1.75 
	@printf(" %7.3f %3d %7.3f\n", PINTS, FLUID, LITRES)
	@label _10
end
