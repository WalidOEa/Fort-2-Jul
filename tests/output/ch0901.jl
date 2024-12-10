include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0901

using Printf


global T = 0
println(" ")
println(" TWELVE TIMES TABLE")
println(" ")
for T = 1:12

	@printf(" %3d * 12 = %3d\n", T, T*12)
	@label _10
end
