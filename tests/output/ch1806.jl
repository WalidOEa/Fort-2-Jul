include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1806

using Printf
function SOLVE(A, B, N, X)
			
# 
#  SOLVES A SET OF N SIMULTANEOUS EQUATIONS, OF THE FORM
#  A(1,1)*X(1) + A(1,2)*X(2) + A(1,3)*X(3) .... = B(1)
#  A(2,1)*X(1) + A(2,2)*X(2) + A(2,3)*X(3) .... = B(2)
# 
#  A(N,1)*X(1) + A(N,2)*X(2) + A(N,3)*X(3) .... = B(N)
# 
#  INPUT
#  THE MATRIX A CONTAINS THE COEFFICIENTS ON THE LHS
#  THE VECTOR B CONTAINS THE VALUES ON THE RHS
#  OUTPUT
#  THE VECTOR X RETURNS THE VALUES AS ABOVE
#  NOE THAT THE CONTENTS OF A AND B ARE CHANGED
# 
	for K = 1:N

		for J = K+1:N

			global Y = -A[J, K] /A[K, K] 
			for L = K:N

				A[J, L] = A[J, L] +Y*A[K, L] 
			@label _3
			end
			B[J] = B[J] +Y*B[K] 
		@label _2
		end
	@label _1
	end
#  START THE BACK SUBSTITUTION
	X[N] = B[N] /A[N, N] 
	for J in N-1:-1:1

		global Y = B[J] 
		for K = J+1:N

			global Y = Y-A[J, K] *X[K] 
		@label _5
		end
		X[J] = Y/A[J, J] 
	@label _4
	end
	return A, B, N, X
end

global AA = 0.0
global BB = 0.0
global XX = 0.0
global I = 0
global J = 0
global N = 0
global AA = create_array("REAL",2,2,)
global BB = create_array("REAL",2,)
global XX = create_array("REAL",2,)

global N = 2
println("INPUT ", N, " X ", N, " MATRIX AA ROW BY ROW")
for I = 1:N

		for J in 1:N
		AAIJ = readline()
		global AA[I, J]  = parse_input(AAIJ)
	end
@label _1
end
println("INPUT ", N, " ELEMENTS OF RHS VECTOR BB")
for I in 1:N
	BBI = readline()
	global BB[I]  = parse_input(BBI)
end
global AA,BB,N,XX = SOLVE(AA,BB,N,XX)
println("SOLUTION IS:")
for I = 1:N

	println(XX[I] )
@label _2
end
