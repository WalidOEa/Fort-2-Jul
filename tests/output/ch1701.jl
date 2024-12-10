include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1701

using Printf


global STRING = Vector{String}(undef,80)
global STRIP = Vector{String}(undef,80)
global IPOS = 0
global I = 0
println("INPUT A PHRASE OR SENTENCE (MAX 80 CHARS)")
STRING = readline()
global STRING = parse_input(STRING)
global IPOS = 0
global _LEN = 80
global STRIP = " "
for I = 1:_LEN

	if STRING[I:I] !=" "
		global IPOS = IPOS+1
		STRIP[IPOS:IPOS] = STRING[I:I] 
	 end
@label _1
end
println("ORIGINAL:")
println(STRING)
println("WITH BLANKS REMOVED:")
println(STRIP)
