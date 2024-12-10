include("macros.jl")

# Original file located at: /Users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul/./ch1810

using Printf

function SUB1(X, WROW, WCOL)
	
				
			for R = 1:WROW

		println(for C in 1:WCOL
			println(X[R, C]  )
		end
)
	@label _10
	end
		return X, WROW, WCOL
end


global MAXROW = 0
global MAXCOL = 0
global WROW = 0
global WCOL = 0
global MAXROW = 5

global MAXCOL = 5

global WROW = 2

global WCOL = 3

global X = create_array("INTEGER",MAXROW,MAXCOL,)

global R = 0
global C = 0
X[1, 1] = 1
X[1, 2] = 2
X[1, 3] = 3
X[2, 1] = 4
X[2, 2] = 5
X[2, 3] = 6

for R = 1:WROW

	println(for C in 1:WCOL
		println(X[R, C]  )
	end
)
@label _10
end

println(" CALL SUB1")

global X,WROW,WCOL = SUB1(X,WROW,WCOL)
println(" WHOLE ARRAY IS")

for R = 1:MAXROW

	println(for C in 1:MAXCOL
		println(X[R, C]  )
	end
)
@label _20
end


