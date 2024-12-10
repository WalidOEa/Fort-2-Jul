include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1801

using Printf
function MULT(X, Y, Z, FUN)
		global FUN = X*Y^Z
	return X, Y, Z, FUN
end

global A2 = 0.0
global A = 0.0
global B = 0.0
global C = 0.0
global FN = 0.0
global X = 0.0
println("INPUT A,B,C & X")
A = readline()
global A = parse_input(A)
B = readline()
global B = parse_input(B)
C = readline()
global C = parse_input(C)
X = readline()
global X = parse_input(X)
global A,B,C,FN = MULT(A,B,C,FN)
global A2 = FN/X
println("FN = ", FN, " A2 = ", A2)
