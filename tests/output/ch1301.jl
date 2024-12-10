include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1301

using Printf


global X = 0.0
println(" TYPE IN AN ANGLE (IN RADIANS)")
X = readline()
global X = parse_input(X)
println(" SINE OF ", X, " = ", sin(X) )
