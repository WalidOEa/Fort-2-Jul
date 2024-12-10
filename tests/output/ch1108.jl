include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1108

using Printf


global IB1 = 0
global IB2 = 0
global N1 = 0
global N2 = 0
global LTRIM = 0
global BUFFER = Vector{String}(undef,22)
global BUFF1 = Vector{String}(undef,22)
global BUFF2 = Vector{String}(undef,22)
println("INPUT MICAEL''S NUMBERS")
BUFFER = readline( )
global BUFFER = parse_input(BUFFER)
global IB1 = findfirst(BUFFER, " ") 
global IB2 = LTRIM[BUFFER] 
global BUFF1 = BUFFER[2:IB1-1] 
global BUFF2 = BUFFER[IB1+1:IB2] 
N1 = readline(BUFF1 )
global N1 = parse_input(N1)
N2 = readline(BUFF2 )
global N2 = parse_input(N2)
println("N1 = ", N1)
println("N2 = ", N2)
