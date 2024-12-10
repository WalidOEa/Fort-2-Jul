include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0804

using Printf

# 
#  VARIABLES USED ARE
#  MEAN - FOR THE RUNNING MEAN
#  SSQ - THE RUNNING CORRECTED SUM OF SQUARES
#  X - INPUT VALUES FOR WHICH MEAN AND SD REQUIRED
#  W - LOCAL WORK VARIABLE
#  SD - STANDARD DEVIATION
#  R - ANOTHER LOCAL WORK VARIABLE
# 
global MEAN = 0.0
global SSQ = 0.0
global X = 0.0
global W = 0.0
global SD = 0.0
global R = 0.0
global I = 0
global MEAN = 0.0 
global SSQ = 0.0 
println(" ENTER THE NUMBER OF READINGS")
N = readline()
global N = parse_input(N)
println(" ENTER THE ", N, " VALUES, ONE PER LINE")
for I = 1:N

		X = readline()
	global X = parse_input(X)
	global W = X-MEAN
	global R = I-1
	global MEAN = (R*MEAN+X) /I
	global SSQ = SSQ+W*W*R/I
@label _1
end
global SD = (SSQ/R) ^0.5 
println(" MEAN IS ", MEAN)
println(" STANDARD DEVIATION IS ", SD)
