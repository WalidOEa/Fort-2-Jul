include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0704

using Printf


# 
#  VARIABLES USED
#  HEIGHT - USED TO HOLD THE HEIGHTS ABOVE SEA LEVEL
#  LONGIT - USED TO REPRESENT THE LONGITUDE
#  RESTRICTED TO INTEGER VALUES.
#  LATUDE - USED TO REPRESENT THE LATITUDE
#  AGAIN RESTRICTED TO INTEGER VALUES.
#  CORECT - A CORRECTION FACTOR
global HEIGHT = 0.0
global CORECT = 0.0
global LONGIT = 0
global LATUDE = 0
global N = 0
global CORECT = 10.0 
global N = 3

global HEIGHT = create_array("REAL",N,N,)

for LATUDE = 1:N

	for LONGIT = 1:N

		println("INPUT HEIGHT FOR LATUDE = ", LATUDE, " LONGIT = ", LONGIT)
				HEIGHTLATUDELONGIT = readline()
		global HEIGHT[LATUDE, LONGIT]  = parse_input(HEIGHTLATUDELONGIT)
		HEIGHT[LATUDE, LONGIT] = HEIGHT[LATUDE, LONGIT] +CORECT
	@label _9
	end
@label _8
end
println("CORRECTED VALUES ARE:")
for LATUDE = 1:N

	for LONGIT = 1:N

		println(HEIGHT[LATUDE, LONGIT] )
	@label _11
	end
@label _10
end
