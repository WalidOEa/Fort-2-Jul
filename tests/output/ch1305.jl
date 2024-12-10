include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1305

using Printf
function FACT(N) 
		global _FACT = 1
# 
#  THERE ARE THREE CASES.
#  1) N > 1 FACTORIAL EVALUATED
#  2) N = 0 OR N = 1 FACTORIAL IS 1
#  3) N < 0 FACTORIAL ILLEGAL
# 
	if N<0
		println(" NEGATIVE VALUE FOR FACTORIAL")
		println(" NOT DEFINED")
		global _FACT = 0
		else 
		for I = 2:N

			global _FACT = _FACT*I
		@label _1
		end
	end
return _FACT
end

global I = 0
global J = 0
for I = -2:10

	global J = FACT(I) 
	@printf(" %4d FACTORIAL IS %10d\n", I, J)
@label _1
end
