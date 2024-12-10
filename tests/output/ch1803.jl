include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1803

using Printf
function ADD(A, TOTAL)
		
		global TOTAL = 0.0 
	for I = 1:10

		global TOTAL = TOTAL+A[I] 
	@label _1
	end
	return A, TOTAL
end

global X = 0.0
global SUM = 0.0
global I = 0
global X = create_array("REAL",10,)

println("INPUT 10 NUMBERS")
for I in 1:10
	XI = readline()
	global X[I]  = parse_input(XI)
end
global X,SUM = ADD(X,SUM)
println("SUM OF NUMBERS = ", SUM)
