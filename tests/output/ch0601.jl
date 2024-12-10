include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0601

using Printf


# 
#  EXAMPLE OF A FORTRAN PROGRAM TO CALCULATE NET PAY
#  GIVEN AN EMPLOYEE'S GROSS PAY
# 
global GROSS = 0.0
global NET = 0.0
global TAX = 0.0
global TAXRAT = 0.0
global PA = 0
global PNAME = Vector{String}(undef,60)
global TAXRAT = 0.25 
global PA = 4800
println("INPUT EMPLOYEES NAME")
PNAME = readline()
global PNAME = parse_input(PNAME)
println("INPUT GROSS WAGE")
GROSS = readline()
global GROSS = parse_input(GROSS)
global TAX = (GROSS-PA) *TAXRAT
global NET = GROSS-TAX
println("EMPLOYEE: ", PNAME)
println("GROSS PAY: ", GROSS)
println("TAX: ", TAX)
println("NET PAY:", NET)
