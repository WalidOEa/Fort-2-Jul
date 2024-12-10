include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0605

using Printf


global AS = 0.0
global BS = 0.0
global RS = 0.0
global AL = 0.0
global BL = 0.0
global RL = 0.0
global BS = 2.0 
global RS = 0.123456789012345 
global BL = 2.0*10^0 
global RL = 0.123456789012345*10^0 
global AS = BS*RS
global AL = BL*RL
println(@sprintf(" AS= %22.15e\n",AS))
println(@sprintf(" AL= %22.15e\n",AL))
