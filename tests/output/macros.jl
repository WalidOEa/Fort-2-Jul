# macros.jl

macro define_const(varname, value)
	return esc(:(const $varname = $value))
end

macro declare_integer_var(vartype, varname)
	return esc(:(local $varname::$vartype))
end

macro create_1d_array(x, y, var)
	quote
		collect($(x):$(y))
	end
end

macro create_constant_array(val, x, y, var)
	quote
		fill($(val), $(num[y] - num[x] + 1))
	end
end

macro create_strided_array(val, x, y)
	quote
		collect($(x):$(val):$(y))
	end
end

macro loop_and_concat_array(arr, x, y, var)
	quote
		result = []
		for elem in $(esc(arr))[$(esc(x)):$(esc(y))]
			result = vcat(result, elem)
		end
		result
	end
end

function get_arg(index, args...)
	return values[index]
end

function try_parse(str, T)
	try
		parse(T,str)
		true
	catch
		false
	end
end

function create_array(t, dims::Int...)
	if t == "INTEGER"
		return Array{Int}(undef, dims...)
	elseif t == "REAL" || t == "DOUBLE" || t == "DOUBLEPRECISION"
		return Array{Float64}(undef, dims...)
	elseif t == "COMPLEX"
		return Array{Complex{Float64}}(undef, dims...)
	elseif t == "CHARACTER"
		return Array{String}(undef, dims...)
	else
		return Array{Float64}(undef,dims...)
	end
end
function mod(a, b)
	return a % b
end
function parse_input(a)
	if try_parse(a, Int) == true
		return parse(Int, a)
	elseif try_parse(a, Float64) == true
		return parse(Float64, a)
	else
		return a
	end
end
