include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1303

using Printf

global YEAR = 0
global METCYC = 0
global CENTRY = 0
global ERROR1 = 0
global ERROR2 = 0
global DAY = 0
global EPACT = 0
global LUNA = 0
println(" INPUT THE YEAR FOR WHICH EASTER")
println(" IS TO BE CALCULATED")
println(" ENTER THE WHOLE YEAR, E.G. 1978 ")
YEAR = readline()
global YEAR = parse_input(YEAR)
global METCYC = mod(YEAR, 19) +1
if YEAR<=1582
	global DAY = (5*YEAR) /4
	global EPACT = mod(11*METCYC-4, 30) +1
else 
#  CALCULATING THE CENTURY-CENTRY
	global CENTRY = (YEAR/100) +1
#  ACCOUNTING FOR ARITHMETIC INACCURACIES
#  IGNORES LEAP YEARS ETC.
	global ERROR1 = (3*CENTRY/4) -12
	global ERROR2 = ((8*CENTRY+5) /25) -5
#  LOCATING SUNDAY
	global DAY = (5*YEAR/4) -ERROR1-10
#  LOCATING THE EPACT(FULL MOON)
	global EPACT = mod(11*METCYC+20+ERROR2-ERROR1, 30) 
	if (EPACT<0) 
		global EPACT = 30+EPACT

	end
	if (EPACT==25&&METCYC>11) ||EPACT==24
		global EPACT = EPACT+1
	 end
end
global LUNA = 44-EPACT
if LUNA<21
	global LUNA = LUNA+30
 end
global LUNA = LUNA+7-(mod(DAY+LUNA, 7) ) 
#  LOCATING THE CORRECT MONTH
if LUNA>31
	global LUNA = LUNA-31
	println(" FOR THE YEAR ", YEAR)
	println(" EASTER FALLS ON APRIL ", LUNA)
else 
	println(" FOR THE YEAR ", YEAR)
	println(" EASTER FALLS ON MARCH ", LUNA)
end
