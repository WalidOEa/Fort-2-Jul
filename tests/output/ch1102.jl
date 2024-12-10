include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1102

using Printf


global N = 0
global N = 10

global H = 0.0
global W = 0.0
global BMI = 0.0
global H = create_array("REAL",N,)

global W = create_array("REAL",N,)

global BMI = create_array("REAL",N,)

global I = 0
for I = 1:N

		HI = readline()
	global H[I]  = parse_input(HI)
	WI = readline()
	global W[I]  = parse_input(WI)
		BMI[I] = W[I] /(H[I] *H[I] ) 
@label _10
end

for I = 1:N

	@printf("  %5.0f\n", BMI[I] )
	@label _20
end

