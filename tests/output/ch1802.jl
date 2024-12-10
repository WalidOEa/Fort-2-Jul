include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1802

using Printf
function MINMAX(V, N, VMAX, VMIN)
			
	global VMIN = V[1] 
	global VMAX = VMIN
	for I = 2:N

		if V[I] >VMAX
			global VMAX = V[I] 
		elseif V[I] <VMIN
			global VMIN = V[I] 
		 end
@label _1
end
return V, N, VMAX, VMIN
end

global A = 0.0
global AMIN = 0.0
global AMAX = 0.0
global I = 0
global M = 0
global A = create_array("REAL",100,)

println("INPUT NUMBER OF NUMBERS (<= 100)")
M = readline()
global M = parse_input(M)
println("INPUT ", M, " NUMBERS")
for I in 1:M
	AI = readline()
	global A[I]  = parse_input(AI)
end
global A,M,AMAX,AMIN = MINMAX(A,M,AMAX,AMIN)
println("MINIMUM = ", AMIN)
println("MAXIMUM = ", AMAX)
