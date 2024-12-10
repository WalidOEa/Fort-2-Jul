include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1704

using Printf

global X = 0.0
global Y = 0.0
global XMIN = 0.0
global YMIN = 0.0
global XMAX = 0.0
global YMAX = 0.0
global XW = 0.0
global YW = 0.0
global X = create_array("REAL",10,)
global Y = create_array("REAL",10,)

global I = 0
global ILEN = 0
global JLEN = 0
global N = 0
global IPOS = 0
global JPOS = 0
global DIAG = Vector{String}(undef,40)
global ILEN = 20
global JLEN = 40

global _7 = open("C1704A.TXT", "r+");
println(@sprintf(" GIVE NUMBER OF PAIRS FOR PLOTTING\n"))
N = readline()
global N = parse_input(N)
println(@sprintf(" GIVE %5d PAIRS OF POINTS,X-VALUE,Y-VALUE\n",N))
for I in 1:N
	XI = readline()
	global X[I]  = parse_input(XI)
	YI = readline()
	global Y[I]  = parse_input(YI)
end
println(@sprintf(" GIVE MAXIMUM AND MINIMUM FOR X-VALUES\n"))
XMIN = readline()
global XMIN = parse_input(XMIN)
XMAX = readline()
global XMAX = parse_input(XMAX)
println(@sprintf(" GIVE MAXIMUM AND MINIMUM FOR Y-VALUES\n"))
YMIN = readline()
global YMIN = parse_input(YMIN)
YMAX = readline()
global YMAX = parse_input(YMAX)
global XW = (XMAX-XMIN) /(JLEN-1) 
global YW = (YMAX-YMIN) /(ILEN-1) 
#  INITIALISE THE CHARACTER STRING TO ALL BLANKS
for I = 1:ILEN

	DIAG[I] = " "
@label _1
end
for I = 1:N

	global JPOS = (X[I] -XMIN) /XW+1
	global IPOS = (Y[I] -YMIN) /YW+1
#  ELIMINATING POINTS OUTSIDE THE DIAGRAM
	if IPOS<1||IPOS>ILEN
		write(_7,@sprintf(" POINT OUT OF RANGE 2%10.4f\n",X[I] , Y[I] ))
	elseif JPOS<1||JPOS>JLEN
		write(_7,@sprintf(" POINT OUT OF RANGE 2%10.4f\n",X[I] , Y[I] ))
		else 
#  THESE ARE INSIDE
		DIAG[21-IPOS] = '*'
	end
@label _2
end
#  NOW WRITE OUT THE COMPLETED DIAGRAM
for I = 1:ILEN

	write(_7,@sprintf("(1X,'':'',A)",DIAG[I] ))
@label _3
end
write(_7,@sprintf("(1X,40(''-''))"))
close(_7) 
