include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0702

using Printf


# 
#  THE PROGRAM READS UP TO 10 WEIGHTS INTO THE
#  ARRAY WEIGHT
#  VARIABLES USED
#  WEIGHT, HOLDS THE WEIGHT OF THE PEOPLE
#  PERSON, AN INDEX INTO THE ARRAY
#  TOTAL, TOTAL WEIGHT
#  AVERAG, AVERAGE OF THE WEIGHTS
# 
#  THE WEIGHTS ARE WRITTEN OUT SO THAT THEY CAN BE CHECKED
# 
global WEIGHT = 0.0
global TOTAL = 0.0
global AVERAG = 0.0
global PERSON = 0
global WEIGHT = create_array("REAL",10,)

global TOTAL = 0.0 
println("INPUT 10 WEIGHTS")
for PERSON = 1:10

		WEIGHTPERSON = readline()
	global WEIGHT[PERSON]  = parse_input(WEIGHTPERSON)
	global TOTAL = TOTAL+WEIGHT[PERSON] 
@label _100
end
global AVERAG = TOTAL/10
println(" SUM OF NUMBERS IS ", TOTAL)
println(" AVERAGE WEIGHT IS ", AVERAG)
println(" 10 WEIGHTS WERE ")
for PERSON = 1:10

	println(WEIGHT[PERSON] )
@label _200
end
