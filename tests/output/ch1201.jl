include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1201

using Printf

global A = 0.0
global B = 0.0
global C = 0.0
global TERM = 0.0
global A2 = 0.0
global ROOT1 = 0.0
global ROOT2 = 0.0
println(" GIVE THE COEFFICIENTS A, B AND C")
A = readline()
global A = parse_input(A)
B = readline()
global B = parse_input(B)
C = readline()
global C = parse_input(C)
global TERM = B*B-4. *A*C
global A2 = A*2. 
#  IF TERM < 0, ROOTS ARE COMPLEX
#  IF TERM = 0, ROOTS ARE EQUAL
#  IF TERM > 0, ROOTS ARE REAL AND DIFFERENT
if TERM<0.0 
	println(" ROOTS ARE COMPLEX")
elseif TERM>0.0 
	global TERM = TERM^0.5 
	global ROOT1 = (-B+TERM) /A2
	global ROOT2 = (-B-TERM) /A2
	println(" ROOTS ARE ", ROOT1, " AND ", ROOT2)
else 
	global ROOT1 = -B/A2
	println(" ROOTS ARE EQUAL, AT ", ROOT1)
end
