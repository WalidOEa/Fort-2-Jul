include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1703

using Printf

function OMEGA(A, B) 
	global OMEGA = Vector{String}(undef,10)
	global A = Vector{String}(undef,)
global B = Vector{String}(undef,)
		global LA = length(A) 
	global LB = length(B) 
	if LA+LB<=10
		global _OMEGA = A*B
		else 
		global _OMEGA = "TOO LONG"
	end
return _OMEGA
end

global STR1 = Vector{String}(undef,10)
global STR2 = Vector{String}(undef,10)
global STR3 = Vector{String}(undef,5)
global STR4 = Vector{String}(undef,5)
global OMEGA = Vector{String}(undef,10)
println("INPUT STRING 1 (MAX 10 CHARACTERS)")
STR1 = readline( )
global STR1 = parse_input(STR1)
println("INPUT STRING 2 (MAX 10 CHARACTERS)")
STR2 = readline( )
global STR2 = parse_input(STR2)
println("INPUT STRING 3 (MAX 5 CHARACTERS)")
STR3 = readline( )
global STR3 = parse_input(STR3)
println("INPUT STRING 4 (MAX 5 CHARACTERS)")
STR4 = readline( )
global STR4 = parse_input(STR4)
println("CONCATENATION OF STR1 & STR2 IS:")
println(OMEGA(STR1, STR2) )
println("CONCATENATION OF STR3 & STR4 IS:")
println(OMEGA(STR3, STR4) )
