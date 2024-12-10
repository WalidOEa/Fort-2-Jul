include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch2002

using Printf

# 
# 
#      This program uses the UNIRAS software
#      on the VAXen to generate maps of the world
#      with symbols for the Tsunamis organised by
#      region. There are 10 regions, and the idea
#      is to plot all tsunamis in each region with
#      a different symbol.
# 
# 
global TRIAL = false
global SCREEN = false
global LONG = 0.0
global LAT = 0.0
global SCREEN = false 
global TRIAL = false 
#       PRINT *,' Testing on/off'
#       READ (UNIT=*, FMT=*, END=9,ERR=9)TRIAL
# 9     CONTINUE
# 
# 
#      Read in the tsunami data
# 
# 
global TRIAL = DATAIN(TRIAL)
println(" What resolution map do you want")
println()
println(" 1 = 119,650")
println(" 2 =  75,500")
println(" 3 =  43,100")
println(" 4 =  19,300")
println(" 5 =   4,420")
println()
@label _100
IRES = readline( )
global IRES = parse_input(IRES)
@label _200
	if (IRES<1) ||(IRES>5) 
	println(" Please input a number in the range 1 to 5")
	@goto _100
 end
println(" What projection would you like?")
println()
println(" 1 = Lambert       - equal area        -  rectangle")
println(" 2 = Mercator      - equal direction   -  rectangle")
println(" 3 = Hammer        - equal area        -  oval")
println(" 4 = Bonne         -                   -  heart")
println(" 5 = Orthographic  - globe             -  round")
@label _300
IPROJ = readline( )
global IPROJ = parse_input(IPROJ)
@label _400
	if (IPROJ<1) ||(IPROJ>5) 
	println(" Please input a number in the range 1 to 5")
	@goto _300
 end
global LAT = 0.0 
global LONG = 180.0 
# 
# 
#      I offer the user a choice of region
#      to plot.
# 
# 
println(" Which region do you wish to plot?")
println(" 0 = all regions")
println(" 1 = Hawaii")
println(" 2 = New Zealand and South Pacific Islands")
println(" 3 = Papua New Guinea and Solomon Islands")
println(" 4 = Indonesia")
println(" 5 = Philippines")
println(" 6 = Japan")
println(" 7 = Kuril Islands and Kamchatka")
println(" 8 = Alaska incluing Aleutian Islands")
println(" 9 = West Coast - North and Central America")
println(" 10 = West Coast - South America")
NREG = readline( )
global NREG = parse_input(NREG)
@label _8000
	if (NREG<0) ||(NREG>10) 
	println(" Please input a number between 0 and 10 inclusive")
	@goto _8000
 end
println(" Which colour table do you wish to use")
println(" HLS = 1")
println(" CMY = 2")
println(" RGB = 3")
@label _500
ICOL = readline( )
global ICOL = parse_input(ICOL)
@label _600
	if (ICOL<1) ||(ICOL>3) 
	println(" Please input a number in the range 1 to 3")
	@goto _500
 end
println(" Select device, a list of valid devices maybe")
println(" obtained by typing")
println(" list *")
println(" at the GROUTE prompt")
GROUTE(" ")
GOPEN()
GSEGCR(1)
GSURFE()
if IRES==1
	WEXTND()
elseif IRES==2
	WRED1()
elseif IRES==3
	WRED2()
elseif IRES==4
	WRED3()
elseif IRES==5
	WRED4()
 end
global NR = 7
global KOLOR = 0
global NR = WOPEN(0,NR)
global IPROJ = WPROJ(IPROJ)
global LONG,LAT = WCENTR(LONG,LAT)
global KOLOR = WDEFC(KOLOR)
if ICOL==2
	RCMODE("CMY",100)
elseif ICOL==3
	RCMODE("RGB",100)
 end
WPLOT("    ",0.0 )
global TRIAL = CONVRT(TRIAL)
global TRIAL,NREG = PLOTEM(TRIAL,NREG)
GSEGCR(1)
GCLOSE()
