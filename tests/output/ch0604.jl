include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0604

using Printf


global LTYR = 0.0
global LTMIN = 0.0
global DIST = 0.0
global ELAPSE = 0.0
global MINUTE = 0
global SECOND = 0
global LTYR = 9.46 *10^12
global LTMIN = LTYR/(365.25 *24.0 *60.0 ) 
global DIST = 150.0 *10^6
# 
global ELAPSE = DIST/LTMIN
global MINUTE = ELAPSE
global SECOND = (ELAPSE-MINUTE) *60
# 
println(" LIGHT TAKES ", MINUTE, " MINUTES")
println(" ", SECOND, " SECONDS")
println(" TO REACH THE EARTH FROM THE SUN")
