include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0909

using Printf

global X = 0.0
global X = create_array("REAL",100,)

global SUM = 0.0
global I = 0
global N = 0
global _1 = open("DATA.TXT", "r+");
global SUM = 0.0 
N = readline(_1 )
global N = parse_input(N)
for I = 1:N

		XI = readline(_1 )
	global X[I]  = parse_input(XI)
	global SUM = SUM+X[I] 
@label _100
end
@printf("%3d VALUES ARE:\n", N)
for I = 1:N

	@printf(" %10.3e\n", X[I] )
	@label _300
end
println("SUM OF ", N, " VALUES IS ", SUM)
close(_1) 
