include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1804

using Printf

function JOIN(A, M, B, N, C)
			
	for I = 1:M

		C[I] = A[I] 
	@label _1
	end
	for I = 1:N

		C[I+M] = B[I] 
	@label _2
	end
	return A, M, B, N, C
end

global X = 0.0
global Y = 0.0
global Z = 0.0
global I = 0
global M = 0
global N = 0
global X = create_array("REAL",10,)
global Y = create_array("REAL",15,)
global Z = create_array("REAL",25,)

println("INPUT M (<= 10)")
M = readline()
global M = parse_input(M)
println("INPUT ", M, " VALUES OF X")
for I in 1:M
	XI = readline()
	global X[I]  = parse_input(XI)
end
println("INPUT N (<= 15)")
N = readline()
global N = parse_input(N)
println("INPUT ", N, " VALUES OF Y")
for I in 1:N
	YI = readline()
	global Y[I]  = parse_input(YI)
end
global X,M,Y,N,Z = JOIN(X,M,Y,N,Z)
println("ARRAY Z:")
for I = 1:M+N

	println(Z[I] )
@label _1
end
