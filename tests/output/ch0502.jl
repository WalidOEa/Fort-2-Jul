include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0502

using Printf

# 
#  THIS PROGRAM READS IN THREE NUMBERS AND SUMS
#  AND AVERAGES THEM.
# 

global NUMBR1 = 0.0
global NUMBR2 = 0.0
global NUMBR3 = 0.0
global AVRAGE = 0.0
global TOTAL = 0.0
global N = 0
global N = 3
global TOTAL = 0.0 
println("TYPE IN THREE NUMBERS")
println("SEPARATED BY SPACES OR COMMAS")
NUMBR1 = readline()
global NUMBR1 = parse_input(NUMBR1)
NUMBR2 = readline()
global NUMBR2 = parse_input(NUMBR2)
NUMBR3 = readline()
global NUMBR3 = parse_input(NUMBR3)
global TOTAL = NUMBR1+NUMBR2+NUMBR3
global AVRAGE = TOTAL/N
println("TOTAL OF NUMBERS IS", TOTAL)
println("AVERAGE OF THE NUMBERS IS", AVRAGE)
