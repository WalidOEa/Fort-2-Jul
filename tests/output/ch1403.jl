include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1403

using Printf

global HI = 0.0
global HR = 0.0
global HLOW = 0.0
global HIGH = 0.0
global HALF = 0.0
global XL = 0.0
global XH = 0.0
global XM = 0.0
global D = 0.0
global TOL = 0.0
global TOL = 10E-6 

#  PROBLEM - FIND HI FROM EXPRESSION GIVEN IN FUNCTION F
global F(A, B, C) = A*(1.0 -0.8 *exp(-0.6 *C/A) ) -B
#  HI IS INCIDENT WAVE HEIGHT (C)
#  HR IS REFORMED WAVE HEIGHT (B)
#  D IS WATER DEPTH AT TERRACE EDGE (A)
println(" GIVE REFORMED WAVE HEIGHT, AND WATER DEPTH")
HR = readline()
global HR = parse_input(HR)
D = readline()
global D = parse_input(D)
global HLOW = HR
global HIGH = HLOW*2.0 
global XL = F(HLOW, HR, D) 
global XH = F(HIGH, HR, D) 
#  BEGINNING OF WHILE
@label _1
	if (XL*XH) >=0.0 
	global HIGH = HIGH*2.0 
	global XH = F(HIGH, HR, D) 
	@goto _1
 end
@label _2
global HALF = (HLOW+HIGH) *0.5 
global XM = F(HALF, HR, D) 
if (XL*XM) <0.0 
	global XH = XM
	global HIGH = HALF
else 
	global XL = XM
	global HLOW = HALF
end
if (abs(HIGH-HLOW) >TOL) 
	@goto _2
#  END OF REPEAT UNTIL

end
println(" INCIDENT WAVE HEIGHT LIES BETWEEN")
println(HLOW, " AND ", HIGH, " METRES")
