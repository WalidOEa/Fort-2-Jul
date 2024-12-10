include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch0501

using Printf

# 
#  This program reads in and prints out a name
# 
global NAME = ""
println(" Type in your name, up to 20 characters")
println(" enclosed in quotes")
NAME = readline()
global NAME = parse_input(NAME)
println(NAME)
