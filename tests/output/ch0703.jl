include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0703

using Printf


# 
#  THE PROGRAM READS UP TO 10 WEIGHTS INTO THE
#  ARRAY WEIGHT
#  VARIABLES USED
#  N - NUMBER OF WEIGHTS
#  WEIGHT, HOLDS THE WEIGHT OF THE PEOPLE
#  PERSON, AN INDEX INTO THE ARRAY
#  TOTAL, TOTAL WEIGHT
#  AVERAG, AVERAGE OF THE WEIGHTS
# 
#  THE WEIGHTS ARE WRITTEN OUT SO THAT THEY CAN BE CHECKED
#  UPDATED - N IS NOW A PARAMETER
# 
global WEIGHT = 0.0
global TOTAL = 0.0
global AVERAG = 0.0
global N = 0
global PERSON = 0
global N = 10

global WEIGHT = create_array("REAL",N,)

global TOTAL = 0.0 
println("INPUT ", N, " WEIGHTS")
for PERSON = 1:N

		WEIGHTPERSON = readline()
	global WEIGHT[PERSON]  = parse_input(WEIGHTPERSON)
	global TOTAL = TOTAL+WEIGHT[PERSON] 
@label _100
end
global AVERAG = TOTAL/N
println(" SUM OF NUMBERS IS ", TOTAL)
println(" AVERAGE WEIGHT IS ", AVERAG)
println(N, " WEIGHTS WERE ")
for PERSON = 1:N

	println(WEIGHT[PERSON] )
@label _200
end
