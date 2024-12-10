include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1304

using Printf
function FUN(X, Y, Z) 
		global _FUN = X*Y^Z
	return _FUN
end

global A = 0.0
global B = 0.0
global C = 0.0
global V1 = 0.0
println("INPUT A,B & C")
A = readline()
global A = parse_input(A)
B = readline()
global B = parse_input(B)
C = readline()
global C = parse_input(C)
global V1 = FUN(A, B, C) 
println("V1 = ", V1)
