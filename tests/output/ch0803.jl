include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0803

using Printf

global FAHREN = 0
global CELSIU = 0.0
for FAHREN = -100:200

	global CELSIU = (FAHREN-32) *5. /9. 
	println(FAHREN, CELSIU)
@label _100
end
