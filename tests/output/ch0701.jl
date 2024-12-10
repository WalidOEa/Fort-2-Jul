include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0701

using Printf


global RAINFL = 0.0
global SUM = 0.0
global AVERGE = 0.0
global RAINFL = create_array("REAL",12,)

global MONTH = 0
println(" Type in the rainfall values")
println(" one per line")
for MONTH = 1:12

		RAINFLMONTH = readline()
	global RAINFL[MONTH]  = parse_input(RAINFLMONTH)
@label _10
end
global SUM = 0.0 
for MONTH = 1:12

	global SUM = SUM+RAINFL[MONTH] 
@label _20
end
global AVERGE = SUM/12
println(" Average monthly rainfall was")
println(AVERGE)
