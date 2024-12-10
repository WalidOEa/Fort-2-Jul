include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1402

using Printf


global YEAR = 0
global N = 0
global MONTH = 0
global DAY = 0
global T = 0
println(" YEAR, FOLLOWED BY DAY WITHIN YEAR")
YEAR = readline()
global YEAR = parse_input(YEAR)
N = readline()
global N = parse_input(N)
if (YEAR/4) *4==YEAR
	global T = 1
	if (YEAR/400) *400==YEAR
		global T = 1
	elseif (YEAR/100) *100==YEAR
		global T = 0
	 end
else 
	global T = 0
end
if N>(59+T) 
	global DAY = N+2-T
else 
	global DAY = N
end
global MONTH = (DAY+91) *100/3055
global DAY = (DAY+91) -(MONTH*3055) /100
global MONTH = MONTH-2
println(" CALENDAR DATE IS ", DAY, MONTH, YEAR)
