include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0802

using Printf

global TIME = create_array("null",180,)

global VALUE = 0.0
global DEGREE = 0
global STRIP = 0
for DEGREE in -180:15:165

	global VALUE = DEGREE/15. 
	for STRIP = 0:14

		TIME[DEGREE+STRIP] = VALUE
	@label _11
	end
@label _10
end
for DEGREE = -180:180

	println(DEGREE, " ", TIME[DEGREE] )
@label _12
end
