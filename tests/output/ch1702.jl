include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1702

using Printf


global L = ""
global I = 0
global LINE = 0
global _1 = open("C1702A.TXT", "r+");
global LINE = 1

@label _1
L = readline(_1 )
global L = parse_input(L)
global I = findfirst(L, "GEOLOGY") 
println("READING LINE ", LINE)
if I!=0
	println("STRING \"GEOLOGY\" FOUND AT POSITION ", I)
else 
	println("STRING \"GEOLOGY\" NOT FOUND")
end
global LINE = LINE+1
@goto _1
@label _10

close(_1) 
