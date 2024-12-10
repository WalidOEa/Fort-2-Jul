include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1306

using Printf

global RESULT = 0.0
global PI = 0.0
global N = 0.0
global R = 0.0
global PI = 3.14159265358 

#  STIRL CALCULATES AN APPROXIMATION TO N! FOR LARGE N
global STIRL(X) = sqrt(2. *PI) *X^(X+0.5 ) *exp(-X) 
global N = 10. 
global R = 7. 
#  NUMBER OF POSSIBLE COMBINATIONS THAT CAN BE FORMED WHEN
#  R OBJECTS ARE SELECTED OUT OF A GROUP OF N
#  N!/(R!(N-R)!)
global RESULT = STIRL(N) /(STIRL(R) *STIRL(N-R) ) 
println("FOR N =", N)
println("R = ", R)
println("NUMBER OF COMBINATIONS WHEN ", R, " OBJECTS")
println("ARE SELECTED OUT OF A GROUP OF ", N)
println("IS ", RESULT)
