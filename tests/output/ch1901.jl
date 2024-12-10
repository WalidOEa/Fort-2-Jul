include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1901

using Printf

global X = 0.0
global WHICH = Vector{String}(undef,20)
@label _1
println(@sprintf("('' DATA SET NAME, OR END'')"))
WHICH = readline()
global WHICH = parse_input(WHICH)
if (WHICH=="END") 
	exit()

end
global _1 = open(WHICH, "r+");
X = readline(_1 )
global X = parse_input(X)
println(@sprintf(" FROM FILE %s VALUE OF X = %7.2f\n",WHICH, X))
close(_1) 
@goto _1
