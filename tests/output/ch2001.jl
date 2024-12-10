include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch2001

using Printf
global C = 0.0
global E = 0.0
global PI = 0.0
global C = 299792458.0*10^0 
global E = 2.71828182845904523*10^0 
global PI = 3.14159265358979323*10^0 
function SUB1(RADIUS, AREA, CIRCUM)
	
					global AREA = PI*RADIUS*RADIUS
	global CIRCUM = 2.0*10^0 *PI*RADIUS
	return RADIUS, AREA, CIRCUM
end


global R = 0.0
global A = 0.0
global C = 0.0
global I = 0
for I = 1:5

	println("RADIUS?")
		R = readline()
	global R = parse_input(R)
		global R,A,C = SUB1(R,A,C)
	println(" FOR RADIUS = ", R)
	println(" AREA = ", A)
	println(" CIRCUMFERENCE = ", C)
@label _10
end
