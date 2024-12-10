include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1101

using Printf


global N = 0
global N = 12

global I = 0
global X = 0
global Y = 0
global X = create_array("INTEGER",N,)

global Y = create_array("INTEGER",N,)


for I = 1:N

		XI = readline()
	global X[I]  = parse_input(XI)
	YI = readline()
	global Y[I]  = parse_input(YI)
		@printf(" %3d  %3d\n", X[I] , Y[I] )
	@label _10
end
