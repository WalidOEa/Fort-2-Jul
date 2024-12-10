package uk.ac.soton.comp3200.fort2jul.parser;

import org.apache.log4j.Logger;
import uk.ac.soton.comp3200.fort2jul.lexer.Token;
import uk.ac.soton.comp3200.fort2jul.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.soton.comp3200.fort2jul.lexer.TokenType.*;

/**
 * Parser class is responsible for parsing the list of tokens and constructing the resultant syntax tree,
 * if and only if the program is syntactically correct. Each method reflects a given rule in Fortran 77. Parser
 * state is restored for each rule failed if there are other rules in a given method.
 */
public class Parser {

	private static final Logger logger = Logger.getLogger(Parser.class);

	/**
	 * Token stream
	 */
	private static List<Token> tokens = new ArrayList<>();

	/**
	 * Pointer of token stream
	 */
	private static int current = 0;

	/**
	 * AST of program
	 */
	private Ast<String> ast = new Ast<>( new Ast.Node<>());

	/**
	 * Saves parser state for back tracking
	 */
	private static class ParserState {
		/**
		 * Current position
		 */
		int current;

		/**
		 * Save current position for backtracing
		 * @param current
		 */
		ParserState(int current) {
			this.current = current;
		}
	}

	/**
	 * Class constructor for parser
	 * @param tokens
	 */
	public Parser(List<Token> tokens) {
		Parser.tokens = tokens;
	}

	/**
	 * Entry point for parsing token stream
	 * @return AST
	 */
	public Ast<String> parseTokens() {
		logger.info("Parsing tokens");

		if (!topProduction()) {
			logger.error("Parsing failed, returning null");

			return null;
		}

		logger.info("Parsing successful, returning syntax tree");

		return ast;
	}

	/**
	 * Entry point for parsing rules
	 * @return Boolean
	 */
	public boolean topProduction() {
		logger.info("Parsing...");

		if (!program()) {
			return SFVarName();
		}

		return true;
	}

	/**
	* DummyArgName ::= 	Ident
	*/
	private boolean DummyArgName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DummyArgName");

		boolean a = Ident(node);

		if (a) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ElseStmt ::= 	LblDef
	*           | 	EX_2
	*           | 	"else"
	*           | 	EOS
	*           | 	IN_2
	*/
	private boolean ElseStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ElseStmt");

		boolean a = LblDef(node);
		boolean b = ELSE(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivalenceObjectList ::= 	EquivalenceObject+
	*/
	private boolean EquivalenceObjectList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivalenceObjectList");

		boolean a = EquivalenceObject(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = EquivalenceObjectList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EquivalenceObjectList");

		boolean e = EquivalenceObject(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean SFExpr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFExpr");

		boolean a = SFTerm(node);

		if (a) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExpr");

		boolean b = Sign(node);
		boolean c = AddOperand(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExpr");

		boolean f = AddOperand(node);
		boolean e = AddOp(node);
		boolean d = false;
		if (f && e) {
			d = SFExpr(node);
		}

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean SFTerm(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFTerm");

		boolean a = SFFactor(node);

		if (a) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFTerm");

		boolean b = MultOperand(node);
		boolean c = MultOp(node);
		boolean d = false;

		if (b && c) {
			d = SFTerm(node);
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean SFFactor(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFFactor");

		boolean a = MultOperand(node);
		boolean b = PowerOp(node);
		boolean c = SFPrimary(node);

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFFactor");

		boolean d = SFPrimary(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean SFPrimary(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFPrimary");

		boolean a = ICON(node);

		if (a) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFPrimary");

		boolean b = SFVarName(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFPrimary");

		boolean c = ComplexDataRef(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFPrimary");

		boolean d = FunctionReference(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFPrimary");

		boolean e = LPAREN(node);
		boolean f = Expr(node);
		boolean g = RPAREN(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SFVarName ::= 	Ident
	*/
	private boolean SFVarName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFVarName");

		boolean a1 = Ident(node);
		boolean b1 = DOLLAR(node);
		boolean c1 = COMMA(node);
		boolean d1 = DOLLAR(node);
		boolean e1 = RPAREN(node);

		if (a1 && b1 && c1 && d1 && e1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SFExprList ::= 	Expr
	*           | 	":"?
	*           | 	Expr?
	*/
	private boolean SFExprList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFExprList");

		boolean a1 = SFDummyArgNameList(node);
		boolean b1 = COMMA(node);
		boolean c1 = Expr(node);
		boolean d1 = COLON(node);
		boolean e1 = Expr(node);

		if (a1 && b1 && c1 && d1 && e1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean w = SFDummyArgNameList(node);
		boolean x = COMMA(node);
		boolean y = Expr(node);
		boolean z = COLON(node);

		if (w && x && y && z) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean p = SFDummyArgNameList(node);
		boolean q = COMMA(node);
		boolean r = COLON(node);
		boolean s = Expr(node);

		if (p && q && r && s) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean b = COLON(node);
		boolean c = Expr(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean g = Expr(node);
		boolean h = COLON(node);
		boolean i = Expr(node);

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean e = Expr(node);
		boolean f = COLON(node);

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean l = SectionSubscript(node);
		boolean k = COMMA(node);
		boolean j = false;

		if (l && k) {
			j = SFExprList(node);
		}

		if (j && k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean m = SFDummyArgNameList(node);
		boolean n = COMMA(node);
		boolean o = COLON(node);

		if (m && n && o) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean t = SFDummyArgNameList(node);
		boolean u = COMMA(node);
		boolean v = Expr(node);

		if (t && u && v) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean o1 = Expr(node);

		if (o1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean d = SFExpr(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean a = COLON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFExprList");

		boolean z1 = SFDummyArgNameList(node);

		if (z1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ActionStmt ::= 	ArithmeticIfStmt
	*/
	private boolean ActionStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ActionStmt");

		boolean p = PrintStmt(node);

		if (p) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean a = ArithmeticIfStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean b = AssignmentStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean c = AssignStmt(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean d = BackspaceStmt(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean e = CallStmt(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean f = CloseStmt(node);

		if (f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean g = ContinueStmt(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean h = EndfileStmt(node);

		if (h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean i = GotoStmt(node);

		if (i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean j = ComputedGotoStmt(node);

		if (j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean k = AssignedGotoStmt(node);

		if (k) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean l = IfStmt(node);

		if (l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean m = InquireStmt(node);

		if (m) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean n = OpenStmt(node);

		if (n) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean o = PauseStmt(node);

		if (o) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean q = ReadStmt(node);

		if (q) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean r = ReturnStmt(node);

		if (r) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean s = RewindStmt(node);

		if (s) {

			ast.addChild(parentNode, node);
			return true;
		}


		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean t = StmtFunctionStmt(node);

		if (t) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean u = StopStmt(node);

		if (u) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ActionStmt");

		boolean v = WriteStmt(node);

		if (v) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;


		return false;
	}

	/**
	* EndfileStmt ::= 	LblDef
	*           | 	"endfile"
	*           | 	UnitIdentifier
	*           | 	EOS
	*/
	private boolean EndfileStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndfileStmt");

		boolean a = LblDef(node);
		boolean b = ENDFILE(node);
		boolean c = UnitIdentifier(node);
		boolean d = EOS(node);

		if ((b && c && d) || (a && b && c && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EndfileStmt");

		boolean e = LblDef(node);
		boolean f = ENDFILE(node);
		boolean g = LPAREN(node);
		boolean h = PositionSpecList(node);
		boolean i = RPAREN(node);
		boolean j = EOS(node);

		if ((f && g && h && i && j) || (e && f && g && h && i && j)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IfStmt ::= 	LblDef
	*           | 	"if"
	*           | 	"("
	*           | 	Expr
	*           | 	")"
	*           | 	ActionStmt
	*/
	private boolean IfStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IfStmt");

		boolean a = LblDef(node);
		boolean b = IF(node);
		boolean c = LPAREN(node);
		boolean d = false;
		if (b && c) {
			d = Expr(node);
		}
		boolean e = RPAREN(node);
		boolean f = false;

		if (b && c && d && e) {
			f = ActionStmt(node);
		}

		if ((b && c && d && e && f) || (a && b && c && d && e && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivalenceSetList ::= 	EquivalenceSet+
	*/
	private boolean EquivalenceSetList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivalenceSetList");

		boolean a = EquivalenceSet(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = EquivalenceSetList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EquivalenceSetList");

		boolean e = EquivalenceSet(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NamedConstantUse ::= 	Ident
	*/
	private boolean NamedConstantUse(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NamedConstantUse");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* OrOperand ::= 	AndOperand
	*           | 	AndOpAndOperand*
	*/
	private boolean OrOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OrOperand");

		boolean a = AndOperand(node);
		if (a) {
			while (AndOp(node) && OrOperand(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UpperBound ::= 	Expr
	*/
	private boolean UpperBound(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UpperBound");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BodyConstruct ::= 	SpecificationPartConstruct
	*/
	private boolean BodyConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BodyConstruct");

		boolean b = ExecutableConstruct(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("BodyConstruct");

		boolean a = SpecificationPartConstruct(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivalenceObject ::= 	Variable
	*/
	private boolean EquivalenceObject(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivalenceObject");

		boolean a = Variable(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Level5Expr ::= 	EquivOperand
	*           | 	EquivOpEquivOperand*
	*/
	private boolean Level5Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Level5Expr");

		boolean a = EquivOperand(node);
		if (a) {
			while (Level5Expr(node) && EquivOp(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LoopControl ::= 	VariableName
	*           | 	"="
	*           | 	Expr
	*           | 	","
	*           | 	Expr
	*           | 	CommaExpr?
	*/
	private boolean LoopControl(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LoopControl");

		boolean a = VariableName(node);
		boolean b = EQUAL(node);
		boolean c = false;
		if (a && b) {
			c = Expr(node);
		}
		boolean d = COMMA(node);
		boolean e = false;
		if (a && b && d && c) {
			e = Expr(node);
		}
		boolean f = CommaExpr(node);

		if ((a && b && c && d && e && f) || (a && b && c && d && e)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* IntrinsicList ::= 	IntrinsicProcedureName+
	*/
	private boolean IntrinsicList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IntrinsicList");

		boolean a = IntrinsicProcedureName(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = IntrinsicList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IntrinsicList");

		boolean e = IntrinsicProcedureName(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* TypeParamValue ::= 	Expr
	*/
	private boolean TypeParamValue(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("TypeParamValue");

		boolean b = STAR(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeParamValue");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineRange ::= 	SubroutineParList
	*           | 	EOS
	*           | 	Body?
	*           | 	EndSubroutineStmt
	*/
	private boolean SubroutineRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineRange");

		boolean a = SubroutineParList(node);
		boolean b = EOS(node);
		boolean c = Body(node);
		boolean d = EndSubroutineStmt(node);

		if ((a && b && c && d) || (b && c && d) || (a && b && d) || (b && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExternalNameList ::= 	ExternalName+
	*/
	private boolean ExternalNameList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExternalNameList");

		boolean a = ExternalName(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = ExternalNameList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExternalNameList");

		boolean d = ExternalName(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubscriptList ::= 	Subscript+
	*/
	private boolean SubscriptList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubscriptList");

		boolean a = Subscript(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = SubscriptList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubscriptList");

		boolean e = Subscript(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AssignedGotoStmt ::= 	LblDef
	*           | 	GoToKw
	*           | 	VariableName
	*           | 	EOS
	*/
	private boolean AssignedGotoStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AssignedGotoStmt");

		boolean a = LblDef(node);
		boolean b = GoToKw(node);
		boolean c = VariableName(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssignedGotoStmt");

		boolean e = LblDef(node);
		boolean f = GoToKw(node);
		boolean g = VariableName(node);
		boolean h = LPAREN(node);
		boolean i = LblRefList(node);
		boolean j = RPAREN(node);
		boolean k = EOS(node);

		if (f && g && h && i && j && k) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssignedGotoStmt");

		boolean l = LblDef(node);
		boolean m = GoToKw(node);
		boolean n = VariableComma(node);
		boolean o = LPAREN(node);
		boolean p = LblRefList(node);
		boolean q = RPAREN(node);
		boolean r = EOS(node);

		if (m && n && o && p && q && r) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean PowerOpMultOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PowerOpMultOperand");

		boolean a = PowerOp(node);
		boolean b = false; //MultOperand(node);

		if (a) {
			b = MultOperand(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Expr ::= 	Level5Expr
	*/
	private boolean Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Expr");

		boolean a = Level5Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivalenceSet ::= 	"("
	*           | 	EquivalenceObject
	*           | 	","
	*           | 	EquivalenceObjectList
	*           | 	")"
	*/
	private boolean EquivalenceSet(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivalenceSet");

		boolean a = LPAREN(node);
		boolean b = EquivalenceObject(node);
		boolean c = COMMA(node);
		boolean d = EquivalenceObjectList(node);
		boolean e = RPAREN(node);

		if (a && b && c && d && e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ElseConstruct ::= 	ElseStmt
	*           | 	ConditionalBody
	*/
	private boolean ElseConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ElseConstruct");

		boolean a = ElseStmt(node);
		boolean b = false;

		if (a) {
			b = ElsePart(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean ElsePart(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ElsePart");

		boolean a = ConditionalBody(node);
		boolean b = EndIfStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExternalName ::= 	Ident
	*/
	private boolean ExternalName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExternalName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AndOp ::= 	SP
	*           | 	".and."
	*           | 	SP
	*/
	private boolean AndOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AndOp");

		boolean a = SP(node);
		boolean b = AND(node);
		boolean c = SP(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AndOp");

		boolean d = AND(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommaExpr ::= 	","
	*           | 	Expr
	*/
	private boolean CommaExpr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommaExpr");

		boolean a = COMMA(node);
		boolean b = false;
		if (a) {
			b = Expr(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* InputItemList ::= 	InputItem+
	*/
	private boolean InputItemList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InputItemList");

		boolean a = InputItem(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = InputItemList(node) ;
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InputItemList");

		boolean d = InputItem(node);
		boolean e = EQUAL(node);

		if (d && !e) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* MultOpMultOperand ::= 	MultOp
	*           | 	MultOperand
	*/
	private boolean MultOpMultOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MultOpMultOperand");

		boolean a = MultOp(node);
		boolean b = MultOperand(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ElseIfConstruct ::= 	ElseIfStmt
	*           | 	ConditionalBody
	*/
	private boolean ElseIfConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ElseIfConstruct");

		boolean a = ElseIfStmt(node);
		boolean b = false;

		if (a) {
			b = ThenPart(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ElseIfStmt ::= 	LblDef
	*           | 	EX_2
	*           | 	"elseif"
	*           | 	"("
	*           | 	Expr
	*           | 	")"
	*           | 	"then"
	*           | 	EOS
	*           | 	IN_2
	*/
	private boolean ElseIfStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ElseIfStmt");

		boolean a = LblDef(node);
		boolean b = ELSEIF(node);
		boolean c = LPAREN(node);
		boolean d = false;
		if (b && c) {
			d = Expr(node);
		}
		boolean e = RPAREN(node);
		boolean f = THEN(node);
		boolean g = EOS(node);

		if ((b && c && d && e && f && g) || (a && b && c && d && e && f && g)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ElseIfStmt");

		boolean a1 = LblDef(node);
		boolean b1 = ELSE(node);
		boolean z = IF(node);
		boolean c1 = LPAREN(node);
		boolean d1 = false;
		if (b1 && c1 && z) {
			d1 = Expr(node);
		}
		boolean e1 = RPAREN(node);
		boolean f1 = THEN(node);
		boolean g1 = EOS(node);

		if ((b1 && c1 && d1 && e1 && f1 && g1 && z) || (a1 && b1 && c1 && d1 && e1 && f1 && g1 && z)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AddOperand ::= 	MultOperand
	*           | 	MultOpMultOperand*
	*/
	private boolean AddOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AddOperand");

		boolean a = MultOperand(node);
		if (a) {
			while (MultOpMultOperand(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* MainProgram ::= 	ProgramStmt?
	*           | 	MainRange
	*/
	private boolean MainProgram(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MainProgram");

		boolean a = ProgramStmt(node);
		boolean b = MainRange(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("MainProgram");

		boolean c = MainRange(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* VariableComma ::= 	VariableName
	*           | 	","
	*/
	private boolean VariableComma(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("VariableComma");

		boolean a = VariableName(node);
		boolean b = COMMA(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* DataStmtValueList ::= 	DataStmtValue+
	*/
	private boolean DataStmtValueList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmtValueList");

		boolean a = DataStmtValue(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c =  (DataStmtValueList(node)) ;
		}

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataStmtValueList");

		boolean d = DataStmtValue(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LblRef ::= 	Label
	*/
	private boolean LblRef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LblRef");

		boolean a = Label(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* CPrimary ::= 	COperand
	*/
	private boolean CPrimary(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CPrimary");

		boolean b = LPAREN(node);
		boolean c = false; //CExpr(node);
		if (b) {
			c = CExpr(node);
		}
		boolean d = RPAREN(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CPrimary");


		boolean a = COperand(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* PauseStmt ::= 	LblDef
	*           | 	"pause"
	*           | 	IconOrScon?
	*           | 	EOS
	*/
	private boolean PauseStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PauseStmt");

		boolean a = LblDef(node);
		boolean b = PAUSE(node);
		boolean c = ICONOrScon(node);
		boolean d = EOS(node);

		if (b && c && d || a && b && d || b && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ArrayDeclaratorList ::= 	ArrayDeclarator+
	*/
	private boolean ArrayDeclaratorList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ArrayDeclaratorList");

		boolean b = ArrayDeclarator(node);
		boolean c = COMMA(node);
		boolean d = false;
		if (b && c) {
			d = ArrayDeclaratorList(node) ;
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ArrayDeclaratorList");

		boolean a = ArrayDeclarator(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExecutableConstruct ::= 	ActionStmt
	*/
	private boolean ExecutableConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExecutableConstruct");

		boolean a = ActionStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExecutableConstruct");

		boolean b = DoConstruct(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExecutableConstruct");

		boolean c = IfConstruct(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FormatStmt ::= 	LblDef
	*           | 	"format"
	*           | 	"("
	*           | 	FmtSpec?
	*           | 	")"
	*           | 	EOS
	*/
	private boolean FormatStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FormatStmt");

		boolean a = LblDef(node);
		boolean b = FORMAT(node);
		boolean c = LPAREN(node);
		boolean d = FmtSpec(node);
		boolean e = RPAREN(node);
		boolean f = EOS(node);

		if ((b && c && e && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* StmtFunctionRange ::= 	"("
	*           | 	SFDummyArgNameList?
	*           | 	")"
	*           | 	"="
	*           | 	Expr
	*           | 	EOS
	*/
	private boolean StmtFunctionRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("StmtFunctionRange");

		boolean a = LPAREN(node);
		boolean b = SFDummyArgNameList(node);
		boolean c = RPAREN(node);
		boolean d = EQUAL(node);
		boolean e = false;
		if (a && c && d) {
			e = Expr(node);
		}
		boolean f = EOS(node);

		if ((a && b && c && d && e && f) || (a && c && d && e && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UnsignedArithmeticConstant ::= 	Icon
	*/
	private boolean UnsignedArithmeticConstant(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UnsignedArithmeticConstant");


		boolean a = ICON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		ast.createNode("UnsignedArithmeticConstant");

		boolean b = RDCON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		ast.createNode("UnsignedArithmeticConstant");

		boolean c = ComplexConst(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RdFmtIdExpr ::= 	"("
	*           | 	UFExpr
	*           | 	")"
	*/
	private boolean RdFmtIdExpr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RdFmtIdExpr");

		boolean a = LPAREN(node);
		boolean b = UFExpr(node);
		boolean c = RPAREN(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* OutputImpliedDo ::= 	"("
	*           | 	Expr
	*           | 	","
	*           | 	ImpliedDoVariable
	*           | 	"="
	*           | 	Expr
	*           | 	","
	*           | 	Expr
	*           | 	CommaExpr?
	*           | 	")"
	*/
	private boolean OutputImpliedDo(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OutputImpliedDo");

		boolean a = LPAREN(node);
		boolean b = false;
		if (a) {
			b = Expr(node);
		}
		boolean c = COMMA(node);
		boolean d = ImpliedDoVariable(node);
		boolean e = EQUAL(node);
		boolean f = false;
		if (a && c && d && e && b) {
			f = Expr(node);
		}
		boolean g = COMMA(node);
		boolean h = false;
		if (a && c && d && e && g && b && f) {
			h = Expr(node);
		}
		boolean i = false; //CommaExpr(node);
		if (a && c && d && e && g && b && f && h) {
			i = COMMA(node);
		}
		boolean q = false;
		if (a && c && d && e && g && b && f && h && i) {
			q = Expr(node);
		}
		boolean j = RPAREN(node);

		if ((a && b && c && d && e && f && g && h && i && q && j) || (a && b && c && d && e && f && g && h && j)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputImpliedDo");

		boolean k = LPAREN(node);
		boolean l = false; //OutputItemList1(node);
		if (k) {
			l = OutputItemList1(node);
		}
		boolean m = COMMA(node);
		boolean n = ImpliedDoVariable(node);
		boolean o = EQUAL(node);
		boolean p = false;
		if (k && m && o) {
			p = Expr(node);
		}
		boolean ll = COMMA(node);
		boolean r = false;
		if (k && m && o && ll && p) {
			r = Expr(node);
		}
		boolean s = false; //CommaExpr(node);
		if (k && m && o && q && p && r && l) {
			s = COMMA(node);
		}
		boolean w = false;
		if (k && m && o && q && p && r && l && s) {
			w = Expr(node);
		}
		boolean t = RPAREN(node);

		if ((k && l && m && n && o && p && ll && r && s && w && t) || (k && l && m && n && o && p && ll && r && t)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SavedEntity ::= 	VariableName
	*/
	private boolean SavedEntity(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SavedEntity");

		boolean b = SavedCommonBlock(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SavedEntity");


		boolean a = VariableName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* MultOperand ::= 	Level1Expr
	*           | 	PowerUpMultOperand?
	*/
	private boolean MultOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MultOperand");

		boolean a = Level1Expr(node);
		boolean b = false; //PowerOpMultOperand(node);
		if (a) {
			b = PowerOpMultOperand(node);
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* InquireStmt ::= 	LblDef
	*           | 	"inquire"
	*           | 	"("
	*           | 	InquireSpecList
	*           | 	")"
	*           | 	EOS
	*/
	private boolean InquireStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InquireStmt");

		boolean a = LblDef(node);
		boolean b = INQUIRE(node);
		boolean c = LPAREN(node);
		boolean d = InquireSpecList(node);
		boolean e = RPAREN(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Level1Expr ::= 	Primary
	*/
	private boolean Level1Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Level1Expr");

		boolean a = Primary(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* StmtFunctionStmt ::= 	LblDef
	*           | 	Name
	*           | 	StmtFunctionRange
	*/
	private boolean StmtFunctionStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("StmtFunctionStmt");

		boolean a = LblDef(node);
		boolean b = Name(node);
		boolean c = StmtFunctionRange(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataStmtValue ::= 	Constant
	*/
	private boolean DataStmtValue(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmtValue");

		boolean b = ICON(node);
		boolean c = STAR(node);
		boolean d = Constant(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataStmtValue");

		boolean e = NamedConstantUse(node);
		boolean f = STAR(node);
		boolean g = Constant(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataStmtValue");


		boolean a = Constant(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NamedConstantDefList ::= 	NamedConstantDef+
	*/
	private boolean NamedConstantDefList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NamedConstantDefList");

		boolean b = NamedConstantDef(node);
		boolean c = COMMA(node);
		boolean d = false;
		if (b && c) {
			d = NamedConstantDefList(node) ;
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("NamedConstantDefList");

		boolean a = NamedConstantDef(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ImplicitRange ::= 	Ident
	*           | 	"-"
	*           | 	Ident
	*/
	private boolean ImplicitRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImplicitRange");

		boolean a = Ident(node);
		boolean b = MINUS(node);
		boolean c = Ident(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* InquireSpec ::= 	"unit="
	*           | 	UnitIdentifier
	*/
	private boolean InquireSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InquireSpec");

		boolean a = UNIT_EQUAL(node);
		boolean b = UnitIdentifier(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean c = FILE_EQUAL(node);
		boolean d = CExpr(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean e = ERR_EQUAL(node);
		boolean f = LblRef(node);

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean g = IOSTAT_EQUAL(node);
		boolean h = ScalarVariable(node);

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean i = EXIST_EQUAL(node);
		boolean j = ScalarVariable(node);

		if (i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean k = OPENED_EQUAL(node);
		boolean l = ScalarVariable(node);

		if (k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean m = NUMBER_EQUAL(node);
		boolean n = ScalarVariable(node);

		if (m && n) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean o = NAMED_EQUAL(node);
		boolean p = ScalarVariable(node);

		if (o && p) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean q = NAME_EQUAL(node);
		boolean r = ScalarVariable(node);

		if (q && r) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean s = ACCESS_EQUAL(node);
		boolean t = ScalarVariable(node);

		if (s && t) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean u = SEQUENTIAL_EQUAL(node);
		boolean v = ScalarVariable(node);

		if (u && v) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean w = DIRECT_EQUAL(node);
		boolean x = ScalarVariable(node);

		if (w && x) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean y = FORM_EQUAL(node);
		boolean z = ScalarVariable(node);

		if (y && z) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean a1 = FORMATTED_EQUAL(node);
		boolean b1 = ScalarVariable(node);

		if (a1 && b1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean c1 = UNFORMATTED_EQUAL(node);
		boolean d1 = ScalarVariable(node);

		if (c1 && d1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean e1 = RECL_EQUAL(node);
		boolean f1 = false;
		if (e1) {
			f1 = Expr(node);
		}

		if (e1 && f1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean g1 = NEXTREC_EQUAL(node);
		boolean h1 = ScalarVariable(node);

		if (g1 && h1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpec");

		boolean i1 = BLANK_EQUAL(node);
		boolean j1 = ScalarVariable(node);

		if (i1 && j1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;


		return false;
	}


	/**
	* DataStmtObjectList ::= 	DataStmtObject+
	*/
	private boolean DataStmtObjectList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmtObjectList");

		boolean a = DataStmtObject(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = DataStmtObjectList(node) ;
		}

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataStmtObjectList");

		boolean d = DataStmtObject(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AssignStmt ::= 	LblDef
	*           | 	"assign"
	*           | 	LblRef
	*           | 	"to"
	*           | 	VariableName
	*           | 	EOS
	*/
	private boolean AssignStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AssignStmt");

		boolean a = LblDef(node);
		boolean b = ASSIGN(node);
		boolean c = LblRef(node);
		boolean d = TO(node);
		boolean e = VariableName(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean FormatEdit(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FormatEdit");

		boolean a = EditElement(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean b = ICON(node);
		boolean c = EditElement(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean d = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node) || XCON(node));

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean h = PCON(node);
		boolean i = ICON(node);
		boolean j = EditElement(node);

		if (h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean f = PCON(node);
		boolean g = EditElement(node);

		if (f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean e = PCON(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatEdit");

		boolean x = XCON(node);

		if (x) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Label ::= 	Icon
	*/
	private boolean Label(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Label");

		boolean a = ICON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RelOp ::= 	SP
	*           | 	".eq."
	*           | 	SP
	*/
	private boolean RelOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RelOp");

		boolean a = SP(node);
		boolean b = EQUAL_EQUAL(node);
		boolean c = SP(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean d = SP(node);
		boolean e = BANG_EQUAL(node);
		boolean f = SP(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean g = SP(node);
		boolean h = LESS(node);
		boolean i = SP(node);

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean j = SP(node);
		boolean k = LESS_EQUAL(node);
		boolean l = SP(node);

		if (j && k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean m = SP(node);
		boolean n = GREATER(node);
		boolean o = SP(node);

		if (m && n && o) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean p = SP(node);
		boolean q = GREATER_EQUAL(node);
		boolean r = SP(node);

		if (p && q && r) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean s = EQUAL_EQUAL(node);

		if (s) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean t = BANG_EQUAL(node);

		if (t) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean u = LESS(node);

		if (u) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean v = LESS_EQUAL(node);

		if (v) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean w = GREATER(node);

		if (w) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RelOp");

		boolean x = GREATER_EQUAL(node);

		if (x) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DimensionStmt ::= 	LblDef
	*           | 	"dimension"
	*           | 	ArrayDeclaratorList
	*           | 	EOS
	*/
	private boolean DimensionStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DimensionStmt");

		boolean a = LblDef(node);
		boolean b = DIMENSION(node);
		boolean c = ArrayDeclaratorList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NamedConstant ::= 	Ident
	*/
	private boolean NamedConstant(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NamedConstant");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SavedEntityList ::= 	SavedEntity+
	*/
	private boolean SavedEntityList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SavedEntityList");

		boolean a = SavedEntity(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = SavedEntityList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SavedEntityList");

		boolean e = SavedEntity(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Name ::= 	Ident
	*/
	private boolean Name(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Name");

		boolean a = Ident(node);

		if (a) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NamedConstantDef ::= 	NamedConstant
	*           | 	"="
	*           | 	Expr
	*/
	private boolean NamedConstantDef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NamedConstantDef");

		boolean a = NamedConstant(node);
		boolean b = EQUAL(node);
		boolean c = false;
		if (a && b) {
			c = Expr(node);
		}

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* PlusMinus ::= 	"+"
	*/
	private boolean PlusMinus(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PlusMinus");

		boolean a = PLUS(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PlusMinus");

		boolean b = MINUS(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommaLoopControl ::= 	","?
	*           | 	LoopControl
	*/
	private boolean CommaLoopControl(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommaLoopControl");

		boolean a = COMMA(node);
		boolean b = LoopControl(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;


		return false;
	}

	/**
	* Comblock ::= 	"/"
	*           | 	SPOFF
	*           | 	"/"
	*           | 	SPON
	*/
	private boolean Comblock(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Comblock");

		boolean e = SLASH(node);
		boolean f = CommonBlockName(node);
		boolean g = SLASH(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Comblock");

		boolean a = SLASH(node);
		//boolean b = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));
		boolean c = SLASH(node);
		//boolean d = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));

		if (a && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataIDoObject ::= 	ArrayElement
	*/
	private boolean DataIDoObject(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataIDoObject");

		boolean a = ArrayElement(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataIDoObject");

		boolean b = DataImpliedDo(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BlockDataSubprogram ::= 	BlockDataStmt
	*           | 	BlockDataBody
	*           | 	EndBlockDataStmt
	*/
	private boolean BlockDataSubprogram(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BlockDataSubprogram");

		boolean a = BlockDataStmt(node);
		boolean b = BlockDataBody(node);
		boolean c = EndBlockDataStmt(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("BlockDataSubprogram");

		boolean d = BlockDataStmt(node);
		boolean e = EndBlockDataStmt(node);

		if (d && e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* GotoStmt ::= 	LblDef
	*           | 	GoToKw
	*           | 	LblRef
	*           | 	EOS
	*/
	private boolean GotoStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("GotoStmt");

		boolean a = LblDef(node);
		boolean b = GoToKw(node);
		boolean c = LblRef(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* OpenStmt ::= 	LblDef
	*           | 	"open"
	*           | 	"("
	*           | 	ConnectSpecList
	*           | 	")"
	*           | 	EOS
	*/
	private boolean OpenStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OpenStmt");

		boolean a = LblDef(node);
		boolean b = OPEN(node);
		boolean c = LPAREN(node);
		boolean d = ConnectSpecList(node);
		boolean e = RPAREN(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BackspaceStmt ::= 	LblDef
	*           | 	"backspace"
	*           | 	UnitIdentifier
	*           | 	EOS
	*/
	private boolean BackspaceStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BackspaceStmt");

		boolean a = LblDef(node);
		boolean b = BACKSPACE(node);
		boolean c = UnitIdentifier(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("BackspaceStmt");

		boolean e = LblDef(node);
		boolean f = BACKSPACE(node);
		boolean g = LPAREN(node);
		boolean h = PositionSpecList(node);
		boolean i = RPAREN(node);
		boolean j = EOS(node);

		if (f && g && h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineArgList ::= 	{SubroutineArg ","}*
	*/
	private boolean SubroutineArgList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineArgList");

		boolean a = SubroutineArg(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = SubroutineArgList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubroutineArgList");

		boolean e = SubroutineArg(node);

			ast.addChild(parentNode, node);
			return true;

	}

	/**
	* ArrayElement ::= 	VariableName
	*           | 	"("
	*           | 	SectionSubscriptList
	*           | 	")"
	*/
	private boolean ArrayElement(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ArrayElement");

		boolean a = VariableName(node);
		boolean b = LPAREN(node);
		boolean c = SectionSubscriptList(node);
		boolean d = RPAREN(node);

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean ICONOrScon(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ICONOrScon");

		boolean a = ICON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ICONOrScon");

		boolean b = SCON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BlockDataBody ::= 	BlockDataBodyConstruct
	*/
	private boolean BlockDataBody(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BlockDataBody");

		boolean b = false; //BlockDataBody("BlockDataBody");
		boolean c = BlockDataBodyConstruct(node);
		if (c) {
			b = BlockDataBody(node);
		}

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("BlockDataBody");

		boolean a = BlockDataBodyConstruct(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Comlist ::= 	Comblock?
	*           | 	CommonBlockObject
	*/
	private boolean Comlist(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Comlist");

		boolean e = Comblock(node);
		boolean f = CommonBlockObject(node);
		boolean d = COMMA(node);
		boolean c = false; //Comlist(node);

		if (d && e && f) {
			c = Comlist(node);
		}

		if (c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Comlist");

		boolean g = false; //Comlist(node);
		boolean h = Comblock(node);
		boolean i = CommonBlockObject(node);
		if (h && i) {
			g = Comlist(node);
		}

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Comlist");

		boolean m = CommonBlockObject(node);
		boolean n = COMMA(node);
		boolean o = false;
		if (m && n) {
			o = Comlist(node);
		}

		if (m && n && o) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Comlist");


		boolean a = Comblock(node);
		boolean b = CommonBlockObject(node);

		if ((a && b)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Comlist");

		boolean l = CommonBlockObject(node);

		if (l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ScalarVariable ::= 	VariableName
	*/
	private boolean ScalarVariable(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ScalarVariable");

		boolean a = VariableName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ScalarVariable");

		boolean b = ArrayElement(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SpecificationPartConstruct ::= 	ImplicitStmt
	*/
	private boolean SpecificationPartConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SpecificationPartConstruct");

		boolean a = ImplicitStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationPartConstruct");

		boolean b = ParameterStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}


		current = state.current;

		node = ast.createNode("SpecificationPartConstruct");

		boolean c = FormatStmt(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationPartConstruct");

		boolean d = EntryStmt(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationPartConstruct");

		boolean e = DeclarationConstruct(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivalenceStmt ::= 	LblDef
	*           | 	"equivalence"
	*           | 	EquivalenceSetList
	*           | 	EOS
	*/
	private boolean EquivalenceStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivalenceStmt");

		boolean a = LblDef(node);
		boolean b = EQUIVALENCE(node);
		boolean c = EquivalenceSetList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LogicalConstant ::= 	SP
	*           | 	".true."
	*           | 	SP
	*/
	private boolean LogicalConstant(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LogicalConstant");

		boolean b = TRUE(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("LogicalConstant");

		boolean e = FALSE(node);

		if ( e ) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* ArrayDeclarator ::= 	VariableName
	*           | 	"("
	*           | 	ArraySpec
	*           | 	")"
	*/
	private boolean ArrayDeclarator(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ArrayDeclarator");

		boolean a = VariableName(node);
		boolean b = LPAREN(node);
		boolean c = ArraySpec(node);
		boolean d = RPAREN(node);

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SavedCommonBlock ::= 	"/"
	*           | 	CommonBlockName
	*           | 	"/"
	*/
	private boolean SavedCommonBlock(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SavedCommonBlock");

		boolean a = SLASH(node);
		boolean b = CommonBlockName(node);
		boolean c = SLASH(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CharLength ::= 	"("
	*           | 	TypeParamValue
	*           | 	")"
	*/
	private boolean CharLength(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CharLength");

		boolean a = LPAREN(node);
		boolean b = false; //TypeParamValue(node);
		if (a) {
			b = TypeParamValue(node);
		}
		boolean c = RPAREN(node);


		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CharLength");

		boolean d = Constant(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ProgramName ::= 	Ident
	*/
	private boolean ProgramName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ProgramName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EndName ::= 	Ident
	*/
	private boolean EndName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EndProgramStmt ::= 	LblDef
	*           | 	"end"
	*           | 	EOS
	*/
	private boolean EndProgramStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndProgramStmt");

		boolean a = LblDef(node);
		boolean b = END(node);
		boolean c = EOS(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		boolean d = LblDef(node);
		boolean e = ENDPROGRAM(node);
		boolean f = EndName(node);
		boolean g = EOS(node);

		if (e && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		boolean h = LblDef(node);
		boolean i = END(node);
		boolean j = PROGRAM(node);
		boolean k = EndName(node);
		boolean l = EOS(node);

		if (i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataStmtObject ::= 	Variable
	*/
	private boolean DataStmtObject(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmtObject");

		boolean a = Variable(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataStmtObject");

		boolean b = DataImpliedDo(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* FmtSpec ::= 	Formatedit
	*/
	private boolean FmtSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FmtSpec");

		boolean s = FormatEdit(node);
		boolean r = Formatsep(node);
		boolean q = COMMA(node);
		boolean p = false; //FmtSpec(node);

		if (q && r && s) {
			p = FmtSpec(node);
		}

		if (p && q && r && s) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");

		boolean g = FormatEdit(node);
		boolean h = Formatsep(node);
		boolean i = false; // FormatEdit(node);

		if (h && g) {
			i = FmtSpec(node);
		}

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");

		boolean j = FormatEdit(node); //FmtSpec(node);
		boolean k = COMMA(node);
		boolean l = false; //FormatEdit(node);

		if (j && k) {
			l = FmtSpec(node);
		}

		if (j && k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");

		boolean m = Formatsep(node);
		boolean n = COMMA(node);
		boolean o = false; // Formatsep(node);

		if (n && m) {
			o = FmtSpec(node);
		}

		if (m && n && o) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");


		boolean d = FormatEdit(node);
		boolean c = Formatsep(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");

		boolean e = false;
		boolean f = Formatsep(node);
		if (f) {
			e = FmtSpec(node);
		}

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");


		boolean a = FormatEdit(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FmtSpec");

		boolean b = Formatsep(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ArithmeticIfStmt ::= 	LblDef
	*           | 	"if"
	*           | 	"("
	*           | 	Expr
	*           | 	")"
	*           | 	LblRef
	*           | 	","
	*           | 	LblRef
	*           | 	","
	*           | 	LblRef
	*           | 	EOS
	*/
	private boolean ArithmeticIfStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ArithmeticIfStmt");

		boolean a = LblDef(node);
		boolean b = IF(node);
		boolean c = LPAREN(node);
		boolean d = false;
		if (b && c) {
			d = Expr(node);
		}
		boolean e = RPAREN(node);
		boolean f = LblRef(node);
		boolean g = COMMA(node);
		boolean h = LblRef(node);
		boolean i = COMMA(node);
		boolean j = LblRef(node);
		boolean k = EOS(node);

		if (b && c && d && e && f && g && h && i && j && k) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ReadStmt ::= 	LblDef
	*           | 	"read"
	*           | 	RdCtlSpec
	*           | 	InputItemList?
	*           | 	EOS
	*/
	private boolean ReadStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ReadStmt");

		boolean a = LblDef(node);
		boolean b = READ(node);
		boolean c = RdCtlSpec(node);
		boolean d = InputItemList(node);
		boolean e = EOS(node);

		if ((a && b && c && d && e) ||(b && c && d && e) || (a && b && c && e)|| (b && c && e)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ReadStmt");

		boolean f = LblDef(node);
		boolean g = READ(node);
		boolean h = RdFmtId(node);
		boolean i = CommaInputItemList(node);
		boolean j = EOS(node);

		if ((f && g && h && i && j) || (g && h && i && j) ||(f && g && h && j)||(g && h && j)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SpecificationStmt ::= 	CommonStmt
	*/
	private boolean SpecificationStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SpecificationStmt");

		boolean a = CommonStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean b = DataStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean c = DimensionStmt(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean d = EquivalenceStmt(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean e = ExternalStmt(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean f = IntrinsicStmt(node);

		if (f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SpecificationStmt");

		boolean g = SaveStmt(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ConditionalBody ::= 	ExecutionPartConstruct*
	*/
	private boolean ConditionalBody(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ConditionalBody");

		while (ExecutionPartConstruct(node)) ;

		ast.addChild(parentNode, node);
		return true;
	}

	/**
	* InquireSpecList ::= 	UnitIdentifier?
	*           | 	{InquireSpec ","}*
	*/
	private boolean InquireSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InquireSpecList");

		boolean a = InquireSpec(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = InquireSpecList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InquireSpecList");

		boolean e = UnitIdentifier(node);

		if ((e)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RewindStmt ::= 	LblDef
	*           | 	"rewind"
	*           | 	UnitIdentifier
	*           | 	EOS
	*/
	private boolean RewindStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RewindStmt");

		boolean a = LblDef(node);
		boolean b = REWIND(node);
		boolean c = UnitIdentifier(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RewindStmt");

		boolean e = LblDef(node);
		boolean f = REWIND(node);
		boolean g = LPAREN(node);
		boolean h = PositionSpecList(node);
		boolean i = RPAREN(node);
		boolean j = EOS(node);

		if (f && g && h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SectionSubscriptRef ::= 	"("
	*           | 	SectionSubscriptList
	*           | 	")"
	*/
	private boolean SectionSubscriptRef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SectionSubscriptRef");

		boolean a = LPAREN(node);
		boolean b = false; //SectionSubscriptList(node);
		if (a) {
			b = SectionSubscriptList(node);
		}
		boolean c = RPAREN(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommonBlockName ::= 	Ident
	*/
	private boolean CommonBlockName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommonBlockName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ImplicitRanges ::= 	{ImplicitRange ","}*
	*/
	private boolean ImplicitRanges(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImplicitRanges");

		boolean a = ImplicitRange(node);
		boolean b = COMMA(node);

		if (a && b) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		boolean c = ImplicitRange(node);

		if (c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CPrimaryConcatOp ::= 	CPrimary
	*           | 	ConcatOp
	*/
	private boolean CPrimaryConcatOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CPrimaryConcatOp");

		boolean a = false; // CPrimary("CPrimaryConcatOp");
		boolean b = ConcatOp(node);

		if (b) {
			a = CPrimary(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* VariableName ::= 	Ident
	*/
	private boolean VariableName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("VariableName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Subscript ::= 	Expr
	*/
	private boolean Subscript(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Subscript");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataStmt ::= 	LblDef
	*           | 	"data"
	*           | 	Datalist
	*           | 	EOS
	*/
	private boolean DataStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmt");

		boolean a = LblDef(node);
		boolean b = DATA(node);
		boolean c = Datalist(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LengthSelector ::= 	"*"
	*           | 	CharLength
	*/
	private boolean LengthSelector(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LengthSelector");

		boolean a = STAR(node);
		boolean b = false; //CharLength(node);

		if (a) {
			b = CharLength(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("LengthSelector");

		boolean c = LPAREN(node);
		boolean d = false; //TypeParamValue(node);
		if (c) {
			d = TypeParamValue(node);
		}
		boolean e = RPAREN(node);

		if (c && d && e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* TypeDeclarationStmt ::= 	LblDef
	*           | 	TypeSpec
	*           | 	EntityDeclList
	*           | 	EOS
	*/
	private boolean TypeDeclarationStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("TypeDeclarationStmt");

		boolean a = LblDef(node);
		boolean b = TypeSpec(node);
		boolean c = EntityDeclList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* COperand ::= 	Scon
	*/
	private boolean COperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("COperand");

		boolean a = SCON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("COperand");

		boolean b = NameDataRef(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("COperand");

		boolean c = FunctionReference(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SectionSubscriptList ::= 	SectionSubscript+
	*/
	private boolean SectionSubscriptList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SectionSubscriptList");

		boolean b = SectionSubscript(node);
		boolean c = COMMA(node);
		boolean d = false;
		if (b && c) {
			d = SectionSubscriptList(node) ;
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SectionSubscriptList");

		boolean a = SectionSubscript(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AndOperand ::= 	NotOp?
	*           | 	Level4Expr
	*/
	private boolean AndOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AndOperand");

		boolean a = NotOp(node);
		boolean b = Level4Expr(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}



	/**
	* EquivOperand ::= 	OrOperand
	*           | 	OrUpOrOperand*
	*/
	private boolean EquivOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivOperand");

		boolean a = OrOperand(node);
		if (a) {
			while (OrOp(node) && EquivOperand(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineNameUse ::= 	Ident
	*/
	private boolean SubroutineNameUse(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineNameUse");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NameDataRef ::= 	Name
	*           | 	ComplexDataRefTail*
	*/
	private boolean NameDataRef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NameDataRef");

		boolean a = Name(node);
		if (a) {
			while (ComplexDataRefTail(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubscriptTripletTail ::= 	":"
	*           | 	Expr?
	*/
	private boolean SubscriptTripletTail(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubscriptTripletTail");

		boolean c = COLON(node);
		boolean d = false;
		if (c) {
			d = Expr(node);
		}
		boolean e = COLON(node);
		boolean f = false;
		if (c && e && d) {
			f = Expr(node);
		}

		if (c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubscriptTripletTail");

		boolean g = COLON_COLON(node);
		boolean h = false;

		if (g) {
			h = Expr(node);
		}

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubscriptTripletTail");


		boolean a = COLON(node);
		boolean b = false;
		if (a) {
			b = Expr(node);
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataStmtSet ::= 	DataStmtObjectList
	*           | 	"/"
	*           | 	DataStmtValueList
	*           | 	"/"
	*/
	private boolean DataStmtSet(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataStmtSet");

		boolean a = DataStmtObjectList(node);
		boolean b = SLASH(node);
		boolean c = DataStmtValueList(node);
		boolean d = SLASH(node);

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EOS ::= 	CommentOrNewline
	*           | 	CommentOrNewline*
	*/
	private boolean EOS(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EOS");

		boolean a = CommentOrNewline(node);
		if (a) {
			while (CommentOrNewline(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ComplexConst ::= 	"("
	*           | 	ComplexComponent
	*           | 	","
	*           | 	ComplexComponent
	*           | 	")"
	*/
	private boolean ComplexConst(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ComplexConst");

		boolean a = LPAREN(node);
		boolean b = ComplexComponent(node);
		boolean c = COMMA(node);
		boolean d = ComplexComponent(node);
		boolean e = RPAREN(node);

		if (a && b && c && d && e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CallStmt ::= 	LblDef
	*           | 	"call"
	*           | 	SubroutineNameUse
	*           | 	EOS
	*/
	private boolean CallStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CallStmt");

		boolean a = LblDef(node);
		boolean b = CALL(node);
		boolean c = SubroutineNameUse(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CallStmt");

		boolean e = LblDef(node);
		boolean f = CALL(node);
		boolean g = SubroutineNameUse(node);
		boolean h = LPAREN(node);
		boolean i = SubroutineArgList(node);
		boolean j = RPAREN(node);
		boolean k = EOS(node);

		if (f && g && h && i && j && k || f && g && h && j && k || e && f && g && h && i && j && k) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* OutputItemList ::= 	Expr
	*/
	private boolean OutputItemList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OutputItemList");

		boolean b = OutputItemList1(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputItemList");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;

	}

	/**
	* FunctionPar ::= 	DummyArgName
	*/
	private boolean FunctionPar(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionPar");

		boolean a = DummyArgName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineName ::= 	Ident
	*/
	private boolean SubroutineName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* ObjectName ::= 	Ident
	*/
	private boolean ObjectName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ObjectName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UFFactor ::= 	UFPrimary
	*/
	private boolean UFFactor(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UFFactor");

		boolean b = UFPrimary(node);
		boolean c = PowerOp(node);
		boolean d = false; //UFFactor(node);

		if (b && c) {
			d = UFFactor(node);
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFFactor");


		boolean a = UFPrimary(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExplicitShapeSpec ::= 	LowerBound
	*           | 	":"
	*           | 	UpperBound
	*/
	private boolean ExplicitShapeSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExplicitShapeSpec");

		boolean a = LowerBound(node);
		boolean b = COLON(node);
		boolean c = false; //UpperBound(node);

		if (a && b) {
			c = UpperBound(node);
		}

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExplicitShapeSpec");

		boolean d = UpperBound(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UFPrimary ::= 	Icon
	*/
	private boolean UFPrimary(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UFPrimary");

		boolean a = ICON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFPrimary");

		boolean b = SCON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFPrimary");

		boolean c = NameDataRef(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFPrimary");

		boolean d = FunctionReference(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFPrimary");

		boolean e = LPAREN(node);
		boolean f = false; // UFExpr(node);
		if (e) {
			f = UFExpr(node);
		}
		boolean g = RPAREN(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean MislexedFCON(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MislexedFCON");

		boolean a = RDCON(node);
		boolean b = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));
		boolean c = RDCON(node);
		boolean d = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("MislexedFCON");

		boolean e = Ident(node);
		boolean f = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));
		boolean g = RDCON(node);
		boolean h = (ICON(node) || HCON(node) || FCON(node) || SP(node) || PCON(node) || RDCON(node) || BCON(node) || OCON(node) || ZCON(node));

		if (e && f && g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ComplexDataRefTail ::= 	SectionSubscriptRef
	*/
	private boolean ComplexDataRefTail(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ComplexDataRefTail");

		boolean a = SectionSubscriptRef(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ComplexDataRefTail");

		boolean b = PERCENT(node);
		boolean c = Name(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean ComplexDataRef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ComplexDataRef");

		boolean a = Name(node);
		boolean b = LPAREN(node);
		boolean c = SectionSubscriptList(node);
		boolean d = RPAREN(node);

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ComplexDataRef");

		boolean f = LPAREN(node);
		boolean g = SectionSubscriptList(node);
		boolean h = RPAREN(node);
		boolean e = false;
		if (f && g && h) {
			e = ComplexDataRef(node);
		}

		if (a && b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* InputItem ::= 	NameDataRef
	*/
	private boolean InputItem(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InputItem");

		boolean a = Variable(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("InputItem");

		boolean b = InputImpliedDo(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;


		return false;
	}

	/**
	* SubroutineParList ::= 	"("
	*           | 	SubroutinePars
	*           | 	")"
	*/
	private boolean SubroutineParList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineParList");

		boolean a = LPAREN(node);
		boolean b = SubroutinePars(node);
		boolean c = RPAREN(node);

		if ((a && b && c) || (a && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Level2Expr ::= 	Sign?
	*           | 	AddOperand
	*           | 	AddOpAddOperand*
	*/
	private boolean Level2Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Level2Expr");

		boolean a = Sign(node);
		boolean b = AddOperand(node);
		if (b) {
			while (AddOpAddOperand(node)) ;
		}

		if ((a && b)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Level2Expr");

		boolean c = AddOperand(node);
		if (c) {
			while(Level2Expr(node) && AddOp(node));
		}

		if ((c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FormatIdentifier ::= 	LblRef
	*/
	private boolean FormatIdentifier(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FormatIdentifier");

		boolean a = LblRef(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatIdentifier");

		boolean b = CExpr(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FormatIdentifier");

		boolean c = STAR(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IoControlSpec ::= 	"fmt="
	*           | 	FormatIdentifier
	*/
	private boolean IoControlSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IoControlSpec");

		boolean a = FMT_EQUAL(node);
		boolean b = false; //FormatIdentifier(node);
		if (a) {
			b = FormatIdentifier(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpec");

		boolean c = UNIT_EQUAL(node);
		boolean d = false; //UnitIdentifier(node);

		if (c) {
			d = UnitIdentifier(node);
		}

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpec");

		boolean e = REC_EQUAL(node);
		boolean f = false; // Expr(node);
		if (e) {
			f = Expr(node);
		}

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpec");

		boolean g = END_EQUAL(node);
		boolean h = LblRef(node);

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpec");

		boolean i = ERR_EQUAL(node);
		boolean j = LblRef(node);

		if (i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpec");

		boolean k = IOSTAT_EQUAL(node);
		boolean l = false;
		if (k) {
			l = ScalarVariable(node);
		}

		if (k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* GoToKw ::= 	"goto"
	*/
	private boolean GoToKw(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("GoToKw");

		boolean a = GOTO(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("GoToKw");

		boolean b = GO(node);
		boolean c = TO(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RelOpLevel3Expr ::= 	RelOp
	*           | 	Level3Expr
	*/
	private boolean RelOpLevel3Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RelOpLevel3Expr");

		boolean a = RelOp(node);
		boolean b = Level3Expr(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RdIoCtlSpecList ::= 	UnitIdentifier
	*           | 	","
	*           | 	IoControlSpec
	*/
	private boolean RdIoCtlSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RdIoCtlSpecList");

		boolean a = UnitIdentifier(node);
		boolean b = COMMA(node);
		boolean c = IoControlSpec(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdIoCtlSpecList");

		boolean d = UnitIdentifier(node);
		boolean e = COMMA(node);
		boolean f = FormatIdentifier(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdIoCtlSpecList");

		boolean h = false; //RdIoCtlSpecList("RdIoCtlSpecList");
		boolean j = IoControlSpec(node);
		boolean i = COMMA(node);

		if (i && j) {
			h = RdIoCtlSpecList(node);
		}

		if (h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdIoCtlSpecList");

		boolean g = IoControlSpec(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineSubprogram ::= 	LblDef
	*           | 	"subroutine"
	*           | 	SubroutineName
	*           | 	SubroutineRange
	*/
	private boolean SubroutineSubprogram(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineSubprogram");

		boolean a = LblDef(node);
		boolean b = SUBROUTINE(node);
		boolean c = SubroutineName(node);
		boolean d = false; // SubroutineRange(node);

		if (b && c) {
			d = SubroutineRange(node);
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CExpr ::= 	CPrimary
	*           | 	CPrimaryConcatOp*
	*/
	private boolean CExpr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CExpr");

		boolean a = CPrimary(node);
		if (a) {
			while (CPrimaryConcatOp(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* program ::= 	IN_6
	*           | 	CommentOrNewline*
	*           | 	ExecutableProgram
	*           | 	CommentOrNewline*
	*           | 	EX_6
	*/
	private boolean program() {
		ParserState state = new ParserState(current);
		
		Ast.Node<String> node = ast.createNode("program");

		while (CommentOrNewline(node)) ;
		boolean a = ExecutableProgram(node);
		if (a) {
			while (CommentOrNewline(node)) ;
		}

		if (a) {

			ast = new Ast<>(node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ComputedGotoStmt ::= 	LblDef
	*           | 	GoToKw
	*           | 	"("
	*           | 	LblRefList
	*           | 	")"
	*           | 	","?
	*           | 	Expr
	*           | 	EOS
	*/
	private boolean ComputedGotoStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ComputedGotoStmt");

		boolean a = LblDef(node);
		boolean b = GoToKw(node);
		boolean c = LPAREN(node);
		boolean d = LblRefList(node);
		boolean e = RPAREN(node);
		boolean f = COMMA(node);
		boolean g = false;
		if (b && c && d && e) {
			g = Expr(node);
		}
		boolean h = EOS(node);

		if ((a && b && c && d && e && f && g && h) || (b && c && d && e && f && g && h) || (a && b && c && d && e && g && h) || (b && c && d && e && g && h)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DeclarationConstruct ::= 	TypeDeclarationStmt
	*/
	private boolean DeclarationConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DeclarationConstruct");

		boolean a = TypeDeclarationStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DeclarationConstruct");

		boolean b = SpecificationStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutinePars ::= 	{SubroutinePar ","}*
	*/
	private boolean SubroutinePars(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutinePars");

		boolean a = SubroutinePar(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = SubroutinePars(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubroutinePars");

		boolean d = SubroutinePar(node);

		if (d) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EquivOp ::= 	SP
	*           | 	".eqv."
	*           | 	SP
	*/
	private boolean EquivOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EquivOp");

		boolean a = SP(node);
		boolean b = EQUAL_EQUAL(node);
		boolean c = SP(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EquivOp");

		boolean d = SP(node);
		boolean e = BANG_EQUAL(node);
		boolean f = SP(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EquivOp");

		boolean g = BANG_EQUAL(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EquivOp");

		boolean h = EQUAL_EQUAL(node);

		if (h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ArraySpec ::= 	ExplicitShapeSpecList
	*/
	private boolean ArraySpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ArraySpec");

		boolean a = ExplicitShapeSpecList(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ArraySpec");

		boolean b = AssumedSizeSpec(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommonStmt ::= 	LblDef
	*           | 	"common"
	*           | 	Comlist
	*           | 	EOS
	*/
	private boolean CommonStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommonStmt");

		boolean a = LblDef(node);
		boolean b = COMMON(node);
		boolean c = Comlist(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ConcatOpLevel2Expr ::= 	ConcatOp
	*           | 	Level2Expr
	*/
	private boolean ConcatOpLevel2Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ConcatOpLevel2Expr");

		boolean a = ConcatOp(node);
		boolean b = Level3Expr(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* StopStmt ::= 	LblDef
	*           | 	"stop"
	*           | 	IconOrScon?
	*           | 	EOS
	*/
	private boolean StopStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("StopStmt");

		boolean a = LblDef(node);
		boolean b = STOP(node);
		boolean c = ICONOrScon(node);
		boolean d = EOS(node);

		if ((a && b && c && d) ||( b && c && d) || (a && b && d)|| (b && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CloseSpec ::= 	"unit="
	*           | 	UnitIdentifier
	*/
	private boolean CloseSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CloseSpec");

		boolean a = UNIT_EQUAL(node);
		boolean b = UnitIdentifier(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CloseSpec");

		boolean c = ERR_EQUAL(node);
		boolean d = LblRef(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CloseSpec");

		boolean e = STATUS_EQUAL(node);
		boolean f = CExpr(node);

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CloseSpec");

		boolean g = IOSTAT_EQUAL(node);
		boolean h = ScalarVariable(node);

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExplicitShapeSpecList ::= 	ExplicitShapeSpec+
	*/
	private boolean ExplicitShapeSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExplicitShapeSpecList");

		boolean b = ExplicitShapeSpec(node);
		boolean c = COMMA(node);
		boolean d = false;
		if (b && c) {
			d = ExplicitShapeSpecList(node) ;
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExplicitShapeSpecList");

		boolean a = ExplicitShapeSpec(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IoControlSpecList ::= 	UnitIdentifier
	*           | 	"$"
	*           | 	","
	*/
	private boolean IoControlSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IoControlSpecList");

		boolean a = UnitIdentifier(node);
		boolean b = DOLLAR(node);
		boolean c = COMMA(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpecList");

		boolean d = UnitIdentifier(node);
		boolean e = COMMA(node);
		boolean f = FormatIdentifier(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpecList");

		boolean g = UnitIdentifier(node);
		boolean h = COMMA(node);
		boolean i = IoControlSpec(node);

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpecList");

		boolean m = IoControlSpec(node);
		boolean l = COMMA(node);
		boolean k = false;
		if (m && l) {
			k = IoControlSpecList(node);
		}

		if (k && l && m) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("IoControlSpecList");

		boolean j = IoControlSpec(node);

		if (j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ImpliedDoVariable ::= 	Ident
	*/
	private boolean ImpliedDoVariable(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImpliedDoVariable");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExecutionPartConstruct ::= 	ExecutableConstruct
	*/
	private boolean ExecutionPartConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExecutionPartConstruct");

		boolean a = ExecutableConstruct(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExecutionPartConstruct");

		boolean b = FormatStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExecutionPartConstruct");

		boolean c = DataStmt(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ExecutionPartConstruct");

		boolean d = EntryStmt(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EntityDeclList ::= 	EntityDecl+
	*/
	private boolean EntityDeclList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EntityDeclList");

		boolean a = EntityDecl(node);
		boolean b = false;
		boolean c = false; //EntityDecl(node) ;

		if (a) {
			b = COMMA(node);
		}

		if (a && b) {
			c = EntityDeclList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EntityDeclList");

		boolean d = EntityDecl(node);


		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UFExpr ::= 	UFTerm
	*/
	private boolean UFExpr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UFExpr");

		boolean b = Sign(node);
		boolean c = UFTerm(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFExpr");

		boolean d = false; //UFExpr("UFExpr");
		boolean f = UFTerm(node);
		boolean e = AddOp(node);

		if (e && f) {
			d = UFExpr(node);
		}

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFExpr");

		boolean a = UFTerm(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EntryName ::= 	Ident
	*/
	private boolean EntryName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EntryName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SFDummyArgNameList ::= 	SFDummyArgName+
	*/
	private boolean SFDummyArgNameList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFDummyArgNameList");

		boolean a = SFDummyArgName(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = SFDummyArgNameList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SFDummyArgNameList");

		boolean e = SFDummyArgName(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommonBlockObject ::= 	VariableName
	*/
	private boolean CommonBlockObject(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommonBlockObject");

		boolean b = ArrayDeclarator(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CommonBlockObject");

		boolean a = VariableName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RdFmtId ::= 	LblRef
	*/
	private boolean RdFmtId(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RdFmtId");

		boolean a = LblRef(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdFmtId");

		boolean b = STAR(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdFmtId");

		boolean d = COperand(node);
		boolean e = ConcatOp(node);
		boolean f = CPrimary(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdFmtId");

		boolean g = RdFmtIdExpr(node);
		boolean h = ConcatOp(node);
		boolean i = CPrimary(node);

		if (g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdFmtId");

		boolean c = COperand(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EntityDecl ::= 	ObjectName
	*/
	private boolean EntityDecl(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EntityDecl");

		boolean f = ObjectName(node);
		boolean g = STAR(node);
		boolean h = CharLength(node);

		if (f && g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EntityDecl");

		boolean i = ObjectName(node);
		boolean j = LPAREN(node);
		boolean k = ArraySpec(node);
		boolean l = RPAREN(node);
		boolean m = STAR(node);
		boolean n = CharLength(node);

		if (i && j && k && l && m && n) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EntityDecl");

		boolean b = ObjectName(node);
		boolean c = LPAREN(node);
		boolean d = ArraySpec(node);
		boolean e = RPAREN(node);

		if (b && c && d && e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EntityDecl");

		boolean a = ObjectName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutineArg ::= 	Expr
	*/
	private boolean SubroutineArg(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutineArg");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubroutineArg");

		boolean b = HCON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubroutineArg");

		boolean c = STAR(node);
		boolean d = LblRef(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* PositionSpecList ::= 	UnitIdentifierComma?
	*           | 	PositionSpec+
	*/
	private boolean PositionSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PositionSpecList");

		boolean a = PositionSpec(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = PositionSpecList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PositionSpecList");

		boolean e = UnitIdentifier(node);
		boolean d = COMMA(node);
		boolean f = PositionSpec(node);
		if ((e && d && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PositionSpecList");

		boolean g = PositionSpec(node);
		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ConcatOp ::= 	"/"
	*           | 	SPOFF
	*           | 	"/"
	*           | 	SPON
	*/
	private boolean ConcatOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ConcatOp");

		boolean a = SLASH(node);
		//boolean b = (ICON("ConcatOp") || HCON("ConcatOp") || FCON("ConcatOp") || SP("ConcatOp") || PCON("ConcatOp") || RDCON("ConcatOp") || BCON("ConcatOp") || OCON("ConcatOp") || ZCON("ConcatOp"));
		boolean c = SLASH(node);
		//boolean d = (ICON("ConcatOp") || HCON("ConcatOp") || FCON("ConcatOp") || SP("ConcatOp") || PCON("ConcatOp") || RDCON("ConcatOp") || BCON("ConcatOp") || OCON("ConcatOp") || ZCON("ConcatOp"));

		if (a && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IntrinsicProcedureName ::= 	Ident
	*/
	private boolean IntrinsicProcedureName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IntrinsicProcedureName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionSubprogram ::= 	LblDef
	*           | 	FunctionPrefix
	*           | 	FunctionName
	*           | 	FunctionRange
	*/
	private boolean FunctionSubprogram(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionSubprogram");

		boolean a = LblDef(node);
		boolean b = FunctionPrefix(node);
		boolean c = FunctionName(node);
		boolean d = false; // FunctionRange(node);
		if (b && c) {
			d = FunctionRange(node);
		}
		if ((a && b && c && d) || (b && c && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataIDoObjectList ::= 	DataIDoObject+
	*/
	private boolean DataIDoObjectList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataIDoObjectList");

		boolean a = DataIDoObject(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = DataIDoObjectList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataIDoObjectList");

		boolean e = DataIDoObject(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionPars ::= 	{FunctionPar ","}*
	*/
	private boolean FunctionPars(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionPars");

		boolean a  = FunctionPar(node);
		boolean b = COMMA(node);
		boolean c = false; // FunctionPars(node);
		if (a && b) {
			c = FunctionPars(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FunctionPars");

		boolean d  = FunctionPar(node);

		if (d) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FunctionPars");

		ast.addChild(parentNode, node);
		return true;

	}

	/**
	* CommaInputItemList ::= 	","
	*           | 	InputItemList
	*/
	private boolean CommaInputItemList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommaInputItemList");

		boolean a = COMMA(node);
		boolean b = InputItemList(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SFDummyArgName ::= 	Ident
	*/
	private boolean SFDummyArgName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFDummyArgName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ImplicitSpec ::= 	TypeSpec
	*           | 	ImplicitRanges
	*/
	private boolean ImplicitSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImplicitSpec");

		boolean g = TypeSpec(node);
		boolean d = LPAREN(node);
		if (g && d) {
			while(ImplicitRanges(node));
		}
		boolean f = RPAREN(node);

		if ((g && d && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ImplicitSpec");

		boolean a = TypeSpec(node);
		if (a) {
			while(ImplicitRanges(node));
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ImplicitSpec");

		boolean h = TypeSpec(node);

		if ((h)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IntrinsicStmt ::= 	LblDef
	*           | 	"intrinsic"
	*           | 	IntrinsicList
	*           | 	EOS
	*/
	private boolean IntrinsicStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IntrinsicStmt");

		boolean a = LblDef(node);
		boolean b = INTRINSIC(node);
		boolean c = IntrinsicList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LowerBound ::= 	Expr
	*/
	private boolean LowerBound(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LowerBound");

		boolean a = Expr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* Level3Expr ::= 	Level2Expr
	*           | 	ConcatOpLevel2Expr*
	*/
	private boolean Level3Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Level3Expr");

		boolean a = Level2Expr(node);
		if (a) {
			while (ConcatOpLevel2Expr(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ImplicitStmt ::= 	LblDef
	*           | 	"implicit"
	*           | 	ImplicitSpecList
	*           | 	EOS
	*/
	private boolean ImplicitStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImplicitStmt");

		boolean a = LblDef(node);
		boolean b = IMPLICIT(node);
		boolean c = ImplicitSpecList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SaveStmt ::= 	LblDef
	*           | 	"save"
	*           | 	EOS
	*/
	private boolean SaveStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SaveStmt");

		boolean a = LblDef(node);
		boolean b = SAVE(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SaveStmt");

		boolean d = LblDef(node);
		boolean e = SAVE(node);
		boolean f = SavedEntityList(node);
		boolean g = EOS(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* MainRange ::= 	Body?
	*           | 	EndProgramStmt
	*/
	private boolean MainRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MainRange");

		boolean a = Body(node);
		boolean b = EndProgramStmt(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionReference ::= 	Name
	*           | 	"("
	*           | 	")"
	*/
	private boolean FunctionReference(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionReference");

		boolean a = Name(node);
		boolean b = LPAREN(node);
		boolean c = RPAREN(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* MultOp ::= 	"*"
	*/
	private boolean MultOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("MultOp");

		boolean a = STAR(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("MultOp");

		boolean b = SLASH(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AssumedSizeSpec ::= 	"*"
	*/
	private boolean AssumedSizeSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AssumedSizeSpec");

		boolean a = STAR(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssumedSizeSpec");

		boolean b = LowerBound(node);
		boolean c = COLON(node);
		boolean d = STAR(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssumedSizeSpec");

		boolean e = ExplicitShapeSpecList(node);
		boolean f = COMMA(node);
		boolean g = STAR(node);

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssumedSizeSpec");

		boolean h = ExplicitShapeSpecList(node);
		boolean i = COMMA(node);
		boolean j = false;// LowerBound(node);
		if (h && i) {
			j = LowerBound(node);
		}
		boolean k = COLON(node);
		boolean l = STAR(node);

		if (h && i && j && k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DataImpliedDo ::= 	"("
	*           | 	DataIDoObjectList
	*           | 	","
	*           | 	ImpliedDoVariable
	*           | 	"="
	*           | 	Expr
	*           | 	","
	*           | 	Expr
	*           | 	")"
	*/
	private boolean DataImpliedDo(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DataImpliedDo");

		boolean j = LPAREN(node);
		boolean k = false; // DataIDoObjectList(node);
		if (j) {
			k = DataIDoObjectList(node);
		}
		boolean l = COMMA(node);
		boolean m = ImpliedDoVariable(node);
		boolean n = EQUAL(node);
		boolean o = false; // Expr(node);
		if (j && l && m && n && k) {
			o = Expr(node);
		}
		boolean p = COMMA(node);
		boolean q = false; //Expr(node);
		if (j && l && m && n && p && k && o) {
			q = Expr(node);
		}
		boolean r = COMMA(node);
		boolean s = false; // Expr(node);
		if (j && l && m && n && p && r && k && o && q) {
			s = Expr(node);
		}
		boolean t = RPAREN(node);

		if (j && k && l && m && n && o && p && q && r && s && t) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("DataImpliedDo");


		boolean a = LPAREN(node);
		boolean b = false; //DataIDoObjectList(node);
		if (a) {
			b = DataIDoObjectList(node);
		}
		boolean c = COMMA(node);
		boolean d = ImpliedDoVariable(node);
		boolean e = EQUAL(node);
		boolean f = false; //Expr(node);
		if (a && b && c && d && e) {
			f = Expr(node);
		}
		boolean g = COMMA(node);
		boolean h = false; //Expr(node);
		if (a && b && c && d && e && f && g) {
			h = Expr(node);
		}
		boolean i = RPAREN(node);

		if (a && b && c && d && e && f && g && h && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* OrOp ::= 	SP
	*           | 	".or."
	*           | 	SP
	*/
	private boolean OrOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OrOp");

		boolean a = SP(node);
		boolean b = OR(node);
		boolean c = SP(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OrOp");

		boolean d = OR(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionRange ::= 	FunctionParList
	*           | 	EOS
	*           | 	Body?
	*           | 	EndFunctionStmt
	*/
	private boolean FunctionRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionRange");

		boolean a = FunctionParList(node);
		boolean b = EOS(node);
		boolean c = Body(node);
		boolean d = EndFunctionStmt(node);

		if ((a && b && c && d) || (b && c && d) || (a && b && d) || (b && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubroutinePar ::= 	DummyArgName
	*/
	private boolean SubroutinePar(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubroutinePar");

		boolean a = DummyArgName(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SubroutinePar");

		boolean b = STAR(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DoLblRef ::= 	Icon
	*/
	private boolean DoLblRef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DoLblRef");

		boolean a = ICON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* OutputItemList1 ::= 	Expr
	*           | 	","
	*           | 	Expr
	*/
	private boolean OutputItemList1(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("OutputItemList1");

		boolean d = Expr(node);
		boolean e = COMMA(node);
		boolean f = false; //OutputImpliedDo(node);
		if (d && e) {
			f = OutputImpliedDo(node);
		}

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputItemList1");

		boolean h = Expr(node);
		boolean i = COMMA(node);
		boolean j = false; //Expr(node);
		if (h && i) {
			j = OutputItemList1(node);
		}

		if (h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputItemList1");

		boolean m = OutputImpliedDo(node);
		boolean l = COMMA(node);
		boolean k = false;//OutputItemList1(node);
		if (m && l) {
			k = OutputItemList1(node);
		}

		if (k && l && m) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputItemList1");

		boolean a = Expr(node);
		boolean b = COMMA(node);
		boolean c = false; //Expr(node);
		if (a && b) {
			c = Expr(node);
		}

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("OutputItemList1");

		boolean g = OutputImpliedDo(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AddOpAddOperand ::= 	AddOp
	*           | 	AddOperand
	*/
	private boolean AddOpAddOperand(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AddOpAddOperand");

		boolean a = AddOp(node);
		boolean b = AddOperand(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ConnectSpecList ::= 	UnitIdentifierComma?
	*           | 	{ConnectSpec ","}*
	*/
	private boolean ConnectSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ConnectSpecList");

		boolean a = ConnectSpec(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = ConnectSpecList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpecList");

		boolean d = ConnectSpec(node);

		if ((d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpecList");

		boolean e = UnitIdentifier(node);

		if ((e)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CloseStmt ::= 	LblDef
	*           | 	"close"
	*           | 	"("
	*           | 	CloseSpecList
	*           | 	")"
	*           | 	EOS
	*/
	private boolean CloseStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CloseStmt");

		boolean a = LblDef(node);
		boolean b = CLOSE(node);
		boolean c = LPAREN(node);
		boolean d = CloseSpecList(node);
		boolean e = RPAREN(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SectionSubscript ::= 	Expr
	*           | 	SubscriptTripletTail?
	*/
	private boolean SectionSubscript(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SectionSubscript");

		boolean c = SubscriptTripletTail(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("SectionSubscript");

		boolean a = Expr(node);
		boolean b = false; //SubscriptTripletTail(node);
		if (a) {
			b = SubscriptTripletTail(node);
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* ImplicitSpecList ::= 	ImplicitSpec+
	*/
	private boolean ImplicitSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ImplicitSpecList");

		boolean a = ImplicitSpec(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = ImplicitSpecList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ImplicitSpecList");

		boolean e = ImplicitSpec(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SFVarName ::= 	Ident
	*/
	private boolean SFVarName() {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SFVarName");

		boolean a = Ident(node);

		if (a) {

			ast = new Ast<>(node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* PositionSpec ::= 	"unit="
	*           | 	UnitIdentifier
	*/
	private boolean PositionSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PositionSpec");

		boolean a = UNIT_EQUAL(node);
		boolean b = UnitIdentifier(node);

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PositionSpec");

		boolean c = ERR_EQUAL(node);
		boolean d = LblRef(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PositionSpec");

		boolean e = IOSTAT_EQUAL(node);
		boolean f = ScalarVariable(node);

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CommentOrNewline ::= 	comment
	*/
	private boolean CommentOrNewline(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CommentOrNewline");

		boolean a = COMMENT(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CommentOrNewline");

		boolean b = NEWLINE(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExternalStmt ::= 	LblDef
	*           | 	"external"
	*           | 	ExternalNameList
	*           | 	EOS
	*/
	private boolean ExternalStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExternalStmt");

		boolean a = LblDef(node);
		boolean b = EXTERNAL(node);
		boolean c = ExternalNameList(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BlockDataStmt ::= 	LblDef
	*           | 	"blockdata"
	*           | 	BlockDataName?
	*           | 	EOS
	*/
	private boolean BlockDataStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BlockDataStmt");

		boolean a = LblDef(node);
		boolean b = BLOCKDATA(node);
		boolean c = BlockDataName(node);
		boolean d = EOS(node);

		if ((a && b && c && d) || (b && c && d) || (a && b && d)|| (b && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("BlockDataStmt");

		boolean e = LblDef(node);
		boolean f = BLOCK(node);
		boolean g = DATA(node);
		boolean h = BlockDataName(node);
		boolean i = EOS(node);

		if ((e && f && g && h && i) || (f && g && h && i) ||(e && f && g && i)||(f && g && i)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AddOp ::= 	"+"
	*/
	private boolean AddOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AddOp");

		boolean a = PLUS(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AddOp");

		boolean b = MINUS(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LblRefList ::= 	LblRef+
	*/
	private boolean LblRefList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LblRefList");

		boolean a = LblRef(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = LblRefList(node);
		}

		if (a && b && c) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("LblRefList");

		boolean e = LblRef(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionPrefix ::= 	"function"
	*/
	private boolean FunctionPrefix(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionPrefix");

		boolean a = FUNCTION(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("FunctionPrefix");

		boolean b = TypeSpec(node);
		boolean c = FUNCTION(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* SubstringRange ::= 	"("
	*           | 	Expr?
	*           | 	SubscriptTripletTail
	*           | 	")"
	*/
	private boolean SubstringRange(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("SubstringRange");

		boolean a = LPAREN(node);
		boolean b = false; //Expr(node);
		if (a) {
			b = Expr(node);
		}
		boolean c = false; //SubscriptTripletTail(node);
		if (a) {
			c = SubscriptTripletTail(node);
		}
		boolean d = RPAREN(node);

		if ((a && b && c && d) || (a && c && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* DoConstruct ::= 	LabelDoStmt
	*/
	private boolean DoConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("DoConstruct");

		boolean a = LabelDoStmt(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Formatsep ::= 	"/"
	*/
	private boolean Formatsep(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Formatsep");

		boolean a = SLASH(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Formatsep");

		boolean b = COLON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* NotOp ::= 	SP
	*           | 	".not."
	*           | 	SP
	*/
	private boolean NotOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("NotOp");

		boolean a = SP(node);
		boolean b = NOT(node);
		boolean c = SP(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("NotOp");

		boolean d = NOT(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ConnectSpec ::= 	"unit="
	*           | 	UnitIdentifier
	*/
	private boolean ConnectSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ConnectSpec");

		boolean a = UNIT_EQUAL(node);
		boolean b = false; //UnitIdentifier(node);
		if (a) {
			b = UnitIdentifier(node);
		}

		if (a && b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean c = ERR_EQUAL(node);
		boolean d = LblRef(node);

		if (c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean e = FILE_EQUAL(node);
		boolean f = false; //CExpr("ConnectSpec");

		if (e) {
			f = CExpr(node);
		}

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean g = STATUS_EQUAL(node);
		boolean h = false; //CExpr(node);

		if (g) {
			h = CExpr(node);
		}

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean i = ACCESS_EQUAL(node);
		boolean j = false; // CExpr(node);

		if (i) {
			j = CExpr(node);
		}

		if (i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean k = FORM_EQUAL(node);
		boolean l = false; //CExpr(node);

		if (k) {
			l = CExpr(node);
		}

		if (k && l) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean m = RECL_EQUAL(node);
		boolean n = false; // Expr(node);

		if (m) {
			n = Expr(node);
		}

		if (m && n) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean o = BLANK_EQUAL(node);
		boolean p = false; //CExpr(node);

		if (o) {
			p = CExpr(node);
		}

		if (o && p) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ConnectSpec");

		boolean q = IOSTAT_EQUAL(node);
		boolean r = false; //ScalarVariable(node);

		if (q) {
			r = ScalarVariable(node);
		}

		if (q && r) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionName ::= 	Ident
	*/
	private boolean FunctionName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UFTerm ::= 	UFFactor
	*/
	private boolean UFTerm(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UFTerm");

		boolean d = UFFactor(node);
		boolean c = MultOp(node);
		boolean b = false; //UFTerm(node);
		if (c && d) {
			b = UFTerm(node);
		}

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFTerm");

		boolean g = UFPrimary(node);
		boolean f = ConcatOp(node);
		boolean e = false; //UFTerm(node);
		if (g && f) {
			e = UFTerm(node);
		}

		if (e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UFTerm");


		boolean a = UFFactor(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Primary ::= 	UnsignedArithmeticConstant
	*/
	private boolean Primary(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Primary");

		boolean a = UnsignedArithmeticConstant(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Primary");

		boolean c = FunctionReference(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Primary");

		boolean b = NameDataRef(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Primary");

		boolean d = LPAREN(node);
		boolean e = false; //Expr(node);
		if (d) {
			e = Expr(node);
		}
		boolean f = RPAREN(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Primary");

		boolean g = SCON(node);

		if (g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Primary");

		boolean h = LogicalConstant(node);

		if (h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ExecutableProgram ::= 	ProgramUnit+
	*/
	private boolean ExecutableProgram(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ExecutableProgram");

		boolean a = ProgramUnit(node);
		if (a) {
			while (ProgramUnit(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* PrintStmt ::= 	LblDef
	*           | 	"print"
	*           | 	FormatIdentifier
	*           | 	","
	*           | 	OutputItemList
	*           | 	EOS
	*/
	private boolean PrintStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PrintStmt");

		boolean a = LblDef(node);
		boolean b = PRINT(node);
		boolean c = FormatIdentifier(node);
		boolean d = COMMA(node);
		boolean e = OutputItemList(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("PrintStmt");

		boolean g = LblDef(node);
		boolean h = PRINT(node);
		boolean i = FormatIdentifier(node);
		boolean j = EOS(node);

		if (h && i && j) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* UnitIdentifier ::= 	UFExpr
	*/
	private boolean UnitIdentifier(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("UnitIdentifier");

		boolean a = UFExpr(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("UnitIdentifier");

		boolean b = STAR(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* FunctionParList ::= 	"("
	*           | 	FunctionPars
	*           | 	")"
	*/
	private boolean FunctionParList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("FunctionParList");

		boolean a = LPAREN(node);
		boolean b = FunctionPars(node);
		boolean c = RPAREN(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ProgramUnit ::= 	MainProgram
	*/
	private boolean ProgramUnit(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ProgramUnit");

		boolean a = MainProgram(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ProgramUnit");

		boolean b = FunctionSubprogram(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ProgramUnit");

		boolean c = SubroutineSubprogram(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ProgramUnit");

		boolean d = BlockDataSubprogram(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* WriteStmt ::= 	LblDef
	*           | 	"write"
	*           | 	"("
	*           | 	IoControlSpecList
	*           | 	")"
	*           | 	OutputItemList?
	*           | 	EOS
	*/
	private boolean WriteStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("WriteStmt");

		boolean a = LblDef(node);
		boolean b = WRITE(node);
		boolean c = LPAREN(node);
		boolean d = false; // IoControlSpecList(node);
		if ( b && c) {
			d = IoControlSpecList(node);
		}
		boolean e = RPAREN(node);
		boolean f = false; //OutputItemList(node);
		if (b && c && e && d) {
			f = OutputItemList(node);
		}
		boolean g = EOS(node);

		if ((a && b && c && d && e && f && g) || (b && c && d && e && f && g) ||(a && b && c && d && e && g)||( b && c && d && e && g)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* InputImpliedDo ::= 	"("
	*           | 	InputItemList
	*           | 	","
	*           | 	ImpliedDoVariable
	*           | 	"="
	*           | 	Expr
	*           | 	","
	*           | 	Expr
	*           | 	CommaExpr?
	*           | 	")"
	*/
	private boolean InputImpliedDo(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("InputImpliedDo");

		boolean a = LPAREN(node);
		boolean b = false; // InputItemList(node);
		if (a) {
			b = InputItemList(node);
		}
		boolean c = COMMA(node);
		boolean d = false; // ImpliedDoVariable(node);
		if (a && c && b) {
			d = ImpliedDoVariable(node);
		}
		boolean e = EQUAL(node);
		boolean f = false; //Expr(node);
		if (a && c && e && b && d) {
			f = Expr(node);
		}
		boolean g = COMMA(node);
		boolean h = false; //Expr(node);
		if (a && c && e && g && b && d && f) {
			h = Expr(node);
		}
		boolean i = false; //CommaExpr(node);
		if (a && c && e && g && b && d && f && h) {
			i = COMMA(node);
		}
		boolean w = false;
		if (a && c && e && g && b && d && f && h && i) {
			w = Expr(node);
		}
		boolean j = RPAREN(node);

		if ((a && b && c && d && e && f && g && h && i && j) || (a && b && c && d && e && f && g && h && j)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BlockDataName ::= 	Ident
	*/
	private boolean BlockDataName(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BlockDataName");

		boolean a = Ident(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ReturnStmt ::= 	LblDef
	*           | 	"return"
	*           | 	Expr?
	*           | 	EOS
	*/
	private boolean ReturnStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ReturnStmt");

		boolean a = LblDef(node);
		boolean b = RETURN(node);
		boolean c = false; //Expr(node);
		if (b) {
			c = Expr(node);
		}
		boolean d = EOS(node);

		if ((a && b && c && d) ||(b && c && d) || (a && b && d)|| (b && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* PowerOp ::= 	"**"
	*/
	private boolean PowerOp(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("PowerOp");

		boolean a = STAR_STAR(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* TypeSpec ::= 	"integer"
	*/
	private boolean TypeSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("TypeSpec");

		boolean g = CHARACTER(node);
		boolean h = false; //LengthSelector(node);
		if (g) {
			h = LengthSelector(node);
		}

		if (g && h) {

			ast.addChild(parentNode, node);
			return true;
		}


		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean m = DOUBLE(node);
		boolean n = PRECISION(node);

		if (m && n) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean f = CHARACTER(node);

		if (f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");


		boolean a = INTEGER(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean b = REAL(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean c = DOUBLEPRECISION(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean d = COMPLEX(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("TypeSpec");

		boolean e = LOGICAL(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Variable ::= 	VariableName
	*           | 	SubscriptListRef?
	*           | 	SubstringRange?
	*/
	private boolean Variable(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Variable");

		boolean a = VariableName(node);
		boolean b = LPAREN(node);
		boolean c = SubscriptList(node);
		boolean d = RPAREN(node);


		if ((a && b && c && d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Variable");

		boolean e = VariableName(node);
		boolean f = LPAREN(node);
		boolean g = SubscriptList(node);
		boolean h = RPAREN(node);
		boolean j = SubstringRange(node);

		if ((e && f && g && h && j)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Variable");

		boolean l = VariableName(node);
		boolean k = SubstringRange(node);

		if ((l && k)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Variable");

		boolean m = VariableName(node);

		if ((m)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Constant ::= 	NamedConstantUse
	*/
	private boolean Constant(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Constant");

		boolean b = PlusMinus(node);
		boolean c = UnsignedArithmeticConstant(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Constant");

		boolean d = SCON(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Constant");

		boolean e = HCON(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Constant");

		boolean f = LogicalConstant(node);

		if (f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Constant");

		boolean a = NamedConstantUse(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EndFunctionStmt ::= 	LblDef
	*           | 	"end"
	*           | 	EOS
	*/
	private boolean EndFunctionStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndFunctionStmt");

		boolean a = LblDef(node);
		boolean b = END(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LblDef ::= 	ε
	*/
	private boolean LblDef(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LblDef");

		boolean b = Label(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* IfThenStmt ::= 	LblDef
	*           | 	"if"
	*           | 	"("
	*           | 	Expr
	*           | 	")"
	*           | 	"then"
	*           | 	EOS
	*           | 	IN_2
	*/
	private boolean IfThenStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IfThenStmt");

		boolean a = LblDef(node);
		boolean b = IF(node);
		boolean c = LPAREN(node);
		boolean d = false; //Expr(node);
		if (b && c) {
			d = Expr(node);
		}
		boolean e = RPAREN(node);
		boolean f = THEN(node);
		boolean g = EOS(node);

		if (b && c && d && e && f && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* IfConstruct ::= 	IfThenStmt
	*           | 	ConditionalBody
	*           | 	ElseIfConstruct*
	*           | 	ElseConstruct?
	*           | 	EndIfStmt
	*/
	private boolean IfConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("IfConstruct");

		boolean a = IfThenStmt(node);
		boolean b = false;
		if (a) {
			b = ThenPart(node);
		}

		if ((a && b)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean ThenPart(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ThenPart");

		boolean b = ConditionalBody(node);
		boolean c = EndIfStmt(node);

		if ((b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ThenPart");

		boolean e = ConditionalBody(node);
		boolean f = ElseIfConstruct(node);

		if ((e && f)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ThenPart");

		boolean g = ConditionalBody(node);
		boolean h = ElseConstruct(node);

		if ((g && h)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ThenPart");

		boolean i = ElseConstruct(node);

		if ((i)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ThenPart");

		boolean a = EndIfStmt(node);

		if ((a)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ThenPart");

		boolean d = ElseIfConstruct(node);

		if ((d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}


	/**
	* EndIfStmt ::= 	LblDef
	*           | 	EX_2
	*           | 	"endif"
	*           | 	EOS
	*/
	private boolean EndIfStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndIfStmt");

		boolean a = LblDef(node);
		boolean b = ENDIF(node);
		boolean c = EOS(node);

		if ((b && c) || (a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EndIfStmt");

		boolean d = LblDef(node);
		boolean e = END(node);
		boolean f = IF(node);
		boolean g = EOS(node);

		if ((d && e && f && g) || (e && f && g)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* BlockDataBodyConstruct ::= 	SpecificationPartConstruct
	*/
	private boolean BlockDataBodyConstruct(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("BlockDataBodyConstruct");

		boolean a = SpecificationPartConstruct(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Level4Expr ::= 	Level3Expr
	*           | 	RelOpLevel3Expr*
	*/
	private boolean Level4Expr(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Level4Expr");

		boolean a = Level3Expr(node);
		if (a) {
			while (RelOpLevel3Expr(node)) ;
		}

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EndSubroutineStmt ::= 	LblDef
	*           | 	"end"
	*           | 	EOS
	*/
	private boolean EndSubroutineStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndSubroutineStmt");

		boolean a = LblDef(node);
		boolean b = END(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EndSubroutineStmt");

		boolean e = LblDef(node);
		boolean f = ENDSUBROUTINE(node);
		boolean h = EndName(node);
		boolean g = EOS(node);

		if (f && h && g) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EndSubroutineStmt");

		boolean i = LblDef(node);
		boolean j = END(node);
		boolean k = SUBROUTINE(node);
		boolean l = EndName(node);
		boolean m = EOS(node);

		if (j && k && l && m) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* LabelDoStmt ::= 	LblDef
	*           | 	"do"
	*           | 	DoLblRef
	*           | 	CommaLoopControl
	*           | 	EOS
	*           | 	IN_2
	*           | 	ExecutionPartConstruct*
	*           | 	EX_2
	*           | 	DoLblDef
	*           | 	DoLabelStmt
	*/
	private boolean LabelDoStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("LabelDoStmt");

		boolean a1 = LblDef(node);
		boolean b1 = DO(node);
		boolean c1 = DoLblRef(node);
		boolean d1 = CommaLoopControl(node);
		boolean e1 = EOS(node);

		if (b1 && c1 && d1 && e1) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EndBlockDataStmt ::= 	LblDef
	*           | 	"end"
	*           | 	EOS
	*/
	private boolean EndBlockDataStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EndBlockDataStmt");

		boolean a = LblDef(node);
		boolean b = END(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Body ::= 	BodyConstruct+
	*/
	private boolean Body(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Body");

		boolean b = false;
		boolean c = BodyConstruct(node);

		if (c) {
			b = Body(node);
		}

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Body");

		boolean a = BodyConstruct(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Datalist ::= 	DataStmtSet
	*/
	private boolean Datalist(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Datalist");

		boolean d = DataStmtSet(node);
		boolean c = COMMA(node);
		boolean b = false; //Datalist(node);

		if (c && d) {
			b = Datalist(node);
		}

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Datalist");

		boolean e = DataStmtSet(node);
		boolean f = false;
		if (e) {
			f = Datalist(node);
		}

		if (e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Datalist");


		boolean a = DataStmtSet(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ComplexComponent ::= 	Sign?
	*           | 	Icon
	*/
	private boolean ComplexComponent(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ComplexComponent");

		boolean a = Sign(node);
		boolean b = ICON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ComplexComponent");

		boolean c = RDCON(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("ComplexComponent");

		boolean d = Name(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Ident ::= 	id
	*/
	private boolean Ident(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Ident");

		boolean a = ID(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EntryStmt ::= 	LblDef
	*           | 	"entry"
	*           | 	EntryName
	*           | 	SubroutineParList
	*           | 	"result"
	*           | 	"("
	*           | 	Name
	*           | 	")"
	*           | 	EOS
	*/
	private boolean EntryStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EntryStmt");

		boolean a = LblDef(node);
		boolean b = ENTRY(node);
		boolean c = EntryName(node);
		boolean d = SubroutineParList(node);
		boolean i = EOS(node);

		if (b && c && d && i) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ProgramStmt ::= 	LblDef
	*           | 	"program"
	*           | 	ProgramName
	*           | 	EOS
	*/
	private boolean ProgramStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ProgramStmt");

		boolean a = LblDef(node);
		boolean b = PROGRAM(node);
		boolean c = ProgramName(node);
		boolean d = EOS(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RdCtlSpec ::= 	RdUnitId
	*/
	private boolean RdCtlSpec(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RdCtlSpec");

		boolean a = RdUnitId(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdCtlSpec");

		boolean b = LPAREN(node);
		boolean c = RdIoCtlSpecList(node);
		boolean d = RPAREN(node);

		if (b && c && d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* RdUnitId ::= 	"("
	*           | 	UFExpr
	*           | 	")"
	*/
	private boolean RdUnitId(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("RdUnitId");

		boolean a = LPAREN(node);
		boolean b = UFExpr(node);
		boolean c = RPAREN(node);

		if (a && b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("RdUnitId");

		boolean d = LPAREN(node);
		boolean e = STAR(node);
		boolean f = RPAREN(node);

		if (d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ParameterStmt ::= 	LblDef
	*           | 	"parameter"
	*           | 	"("
	*           | 	NamedConstantDefList
	*           | 	")"
	*           | 	EOS
	*/
	private boolean ParameterStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ParameterStmt");

		boolean a = LblDef(node);
		boolean b = PARAMETER(node);
		boolean c = LPAREN(node);
		boolean d = NamedConstantDefList(node);
		boolean e = RPAREN(node);
		boolean f = EOS(node);

		if (b && c && d && e && f) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* ContinueStmt ::= 	LblDef
	*           | 	"continue"
	*           | 	EOS
	*/
	private boolean ContinueStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("ContinueStmt");

		boolean a = LblDef(node);
		boolean b = CONTINUE(node);
		boolean c = EOS(node);

		if (b && c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* AssignmentStmt ::= 	LblDef
	*           | 	Name
	*           | 	SFExprListRef?
	*           | 	SubstringRange?
	*           | 	"="
	*           | 	Expr
	*           | 	EOS
	*/
	private boolean AssignmentStmt(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("AssignmentStmt");

		boolean a = LblDef(node);
		boolean b = Name(node);
		boolean e = EQUAL(node);
		boolean f = false; //Expr(node);
		if ( b && e) {
			f = Expr(node);
		}
		boolean g = EOS(node);

		if ((b && e && f && g)) {
			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssignmentStmt");

		boolean h = LblDef(node);
		boolean i = Name(node);
		boolean j = LPAREN(node);
		boolean k = SFExprList(node);
		boolean l = RPAREN(node);
		boolean m = EQUAL(node);
		boolean n = false;
		if (i && k && m && l) {
			n = Expr(node);
		}
		boolean o = EOS(node);

		if ((i && j && k && l && m && n && o)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("AssignmentStmt");

		boolean p = LblDef(node);
		boolean q = Name(node);
		boolean r = LPAREN(node);
		boolean s = SFExprList(node);
		boolean t = RPAREN(node);
		boolean u = SubstringRange(node);
		boolean w = EQUAL(node);
		boolean x = false; //Expr(node);
		if (q && r && s && t && u && w) {
			x = Expr(node);
		}
		boolean y = EOS(node);

		if (q && r && s && t && u && w && x && y) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* Sign ::= 	"+"
	*/
	private boolean Sign(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("Sign");

		boolean a = PLUS(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("Sign");

		boolean b = MINUS(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* EditElement ::= 	Fcon
	*/
	private boolean EditElement(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("EditElement");

		boolean a = FCON(node);

		if (a) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EditElement");

		boolean b = MislexedFCON(node);

		if (b) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EditElement");

		boolean c = SCON(node);

		if (c) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EditElement");

		boolean d = HCON(node);

		if (d) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EditElement");

		boolean e = Ident(node);

		if (e) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("EditElement");

		boolean f = LPAREN(node);
		boolean g = false; //FmtSpec(node);
		if (f) {
			g = FmtSpec(node);
		}
		boolean h = RPAREN(node);

		if (f && g && h) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	/**
	* CloseSpecList ::= 	UnitIdentifierComma?
	*           | 	{CloseSpec ","}*
	*/
	private boolean CloseSpecList(Ast.Node<String> parentNode) {
		ParserState state = new ParserState(current);

		Ast.Node<String> node = ast.createNode("CloseSpecList");

		boolean a = CloseSpec(node);
		boolean b = COMMA(node);
		boolean c = false;
		if (a && b) {
			c = CloseSpecList(node);
		}

		if ((a && b && c)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CloseSpecList");

		boolean d = CloseSpec(node);

		if ((d)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		node = ast.createNode("CloseSpecList");

		boolean e = UnitIdentifier(node);

		if ((e)) {

			ast.addChild(parentNode, node);
			return true;
		}

		current = state.current;

		return false;
	}

	private boolean EQUAL_EQUAL(Ast.Node<String> parentNode) {
		if (!match(EQUAL_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("EQUAL_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COLON_COLON(Ast.Node<String> parentNode) {
		if (!match(COLON_COLON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COLON_COLON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean CHARACTER(Ast.Node<String> parentNode) {
		if (!match(CHARACTER)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("CHARACTER");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COMPLEX(Ast.Node<String> parentNode) {
		if (!match(COMPLEX)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COMPLEX");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean SCON(Ast.Node<String> parentNode) {
		if (!match(SCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SCON");
		Ast.Node<String> lexNode = ast.createNode((String) peek().getLiteral());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean XCON(Ast.Node<String> parentNode) {
		if (!match(XCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("XCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FCON(Ast.Node<String> parentNode) {
		if (!match(FCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean EQUIVALENCE(Ast.Node<String> parentNode) {
		if (!match(EQUIVALENCE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("EQUIVALENCE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DOLLAR(Ast.Node<String> parentNode) {
		if (!match(DOLLAR)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DOLLAR");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PERCENT(Ast.Node<String> parentNode) {
		if (!match(PERCENT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PERCENT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean NEXTREC_EQUAL(Ast.Node<String> parentNode) {
		if (!match(NEXTREC_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NEXTREC_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean LPAREN(Ast.Node<String> parentNode) {
		if (!match(LPAREN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("LPAREN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FORM_EQUAL(Ast.Node<String> parentNode) {
		if (!match(FORM_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FORM_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean RPAREN(Ast.Node<String> parentNode) {
		if (!match(RPAREN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("RPAREN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean STAR(Ast.Node<String> parentNode) {
		if (!match(STAR)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("STAR");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean HCON(Ast.Node<String> parentNode) {
		if (!match(HCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("HCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PLUS(Ast.Node<String> parentNode) {
		if (!match(PLUS)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PLUS");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean THEN(Ast.Node<String> parentNode) {
		if (!match(THEN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("THEN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COMMA(Ast.Node<String> parentNode) {
		if (!match(COMMA)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COMMA");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean MINUS(Ast.Node<String> parentNode) {
		if (!match(MINUS)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("MINUS");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean NUMBER_EQUAL(Ast.Node<String> parentNode) {
		if (!match(NUMBER_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NUMBER_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean SLASH(Ast.Node<String> parentNode) {
		if (!match(SLASH)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SLASH");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean BLANK_EQUAL(Ast.Node<String> parentNode) {
		if (!match(BLANK_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BLANK_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean GREATER_EQUAL(Ast.Node<String> parentNode) {
		if (!match(GREATER_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("GREATER_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean STOP(Ast.Node<String> parentNode) {
		if (!match(STOP)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("STOP");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COLON(Ast.Node<String> parentNode) {
		if (!match(COLON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COLON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean LESS(Ast.Node<String> parentNode) {
		if (!match(LESS)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("LESS");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean BANG_EQUAL(Ast.Node<String> parentNode) {
		if (!match(BANG_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BANG_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean EQUAL(Ast.Node<String> parentNode) {
		if (!match(EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean GREATER(Ast.Node<String> parentNode) {
		if (!match(GREATER)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("GREATER");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean UNIT_EQUAL(Ast.Node<String> parentNode) {
		if (!match(UNIT_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("UNIT_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean OPENED_EQUAL(Ast.Node<String> parentNode) {
		if (!match(OPENED_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("OPENED_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean UNFORMATTED_EQUAL(Ast.Node<String> parentNode) {
		if (!match(UNFORMATTED_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("UNFORMATTED_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ICON(Ast.Node<String> parentNode) {
		if (!match(ICON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ICON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean NAMED_EQUAL(Ast.Node<String> parentNode) {
		if (!match(NAMED_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NAMED_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean NOT(Ast.Node<String> parentNode) {
		if (!match(NOT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NOT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FORMATTED_EQUAL(Ast.Node<String> parentNode) {
		if (!match(FORMATTED_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FORMATTED_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean CLOSE(Ast.Node<String> parentNode) {
		if (!match(CLOSE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("CLOSE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean SP(Ast.Node<String> parentNode) {
		if (!match(SP)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SP");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean RDCON(Ast.Node<String> parentNode) {
		if (!match(RDCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("RDCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean STATUS_EQUAL(Ast.Node<String> parentNode) {
		if (!match(STATUS_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("STATUS_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean LOGICAL(Ast.Node<String> parentNode) {
		if (!match(LOGICAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("LOGICAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean CALL(Ast.Node<String> parentNode) {
		if (!match(CALL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("CALL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DIRECT_EQUAL(Ast.Node<String> parentNode) {
		if (!match(DIRECT_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DIRECT_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ERR_EQUAL(Ast.Node<String> parentNode) {
		if (!match(ERR_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ERR_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean TO(Ast.Node<String> parentNode) {
		if (!match(TO)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("TO");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean OPEN(Ast.Node<String> parentNode) {
		if (!match(OPEN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("OPEN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean LESS_EQUAL(Ast.Node<String> parentNode) {
		if (!match(LESS_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("LESS_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean OCON(Ast.Node<String> parentNode) {
		if (!match(OCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("OCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DO(Ast.Node<String> parentNode) {
		if (!match(DO)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DO");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean CONTINUE(Ast.Node<String> parentNode) {
		if (!match(CONTINUE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("CONTINUE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ACCESS_EQUAL(Ast.Node<String> parentNode) {
		if (!match(ACCESS_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ACCESS_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ELSEIF(Ast.Node<String> parentNode) {
		if (!match(ELSEIF)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ELSEIF");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean IOSTAT_EQUAL(Ast.Node<String> parentNode) {
		if (!match(IOSTAT_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("IOSTAT_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean INTRINSIC(Ast.Node<String> parentNode) {
		if (!match(INTRINSIC)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("INTRINSIC");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean EXTERNAL(Ast.Node<String> parentNode) {
		if (!match(EXTERNAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("EXTERNAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean AND(Ast.Node<String> parentNode) {
		if (!match(AND)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("AND");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean BACKSPACE(Ast.Node<String> parentNode) {
		if (!match(BACKSPACE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BACKSPACE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COMMON(Ast.Node<String> parentNode) {
		if (!match(COMMON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COMMON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean END(Ast.Node<String> parentNode) {
		if (!match(END)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("END");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean GO(Ast.Node<String> parentNode) {
		if (!match(GO)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("GO");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FALSE(Ast.Node<String> parentNode) {
		if (!match(FALSE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FALSE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ENDFILE(Ast.Node<String> parentNode) {
		if (!match(ENDFILE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ENDFILE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean END_EQUAL(Ast.Node<String> parentNode) {
		if (!match(END_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("END_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ZCON(Ast.Node<String> parentNode) {
		if (!match(ZCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ZCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PRINT(Ast.Node<String> parentNode) {
		if (!match(PRINT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PRINT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean REWIND(Ast.Node<String> parentNode) {
		if (!match(REWIND)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("REWIND");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ENDIF(Ast.Node<String> parentNode) {
		if (!match(ENDIF)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ENDIF");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean COMMENT(Ast.Node<String> parentNode) {
		if (!match(COMMENT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("COMMENT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean ASSIGN(Ast.Node<String> parentNode) {
		if (!match(ASSIGN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ASSIGN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean EXIST_EQUAL(Ast.Node<String> parentNode) {
		if (!match(EXIST_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("EXIST_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PROGRAM(Ast.Node<String> parentNode) {
		if (!match(PROGRAM)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PROGRAM");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean SEQUENTIAL_EQUAL(Ast.Node<String> parentNode) {
		if (!match(SEQUENTIAL_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SEQUENTIAL_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ELSE(Ast.Node<String> parentNode) {
		if (!match(ELSE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ELSE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean BLOCK(Ast.Node<String> parentNode) {
		if (!match(BLOCK)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BLOCK");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ID(Ast.Node<String> parentNode) {
		if (!match(ID)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ID");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean IF(Ast.Node<String> parentNode) {
		if (!match(IF)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("IF");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean WRITE(Ast.Node<String> parentNode) {
		if (!match(WRITE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("WRITE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean INQUIRE(Ast.Node<String> parentNode) {
		if (!match(INQUIRE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("INQUIRE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DOUBLE(Ast.Node<String> parentNode) {
		if (!match(DOUBLE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DOUBLE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PCON(Ast.Node<String> parentNode) {
		if (!match(PCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean NAME_EQUAL(Ast.Node<String> parentNode) {
		if (!match(NAME_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NAME_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean STAR_STAR(Ast.Node<String> parentNode) {
		if (!match(STAR_STAR)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("STAR_STAR");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean IMPLICIT(Ast.Node<String> parentNode) {
		if (!match(IMPLICIT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("IMPLICIT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean SAVE(Ast.Node<String> parentNode) {
		if (!match(SAVE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SAVE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}
	private boolean GOTO(Ast.Node<String> parentNode) {
		if (!match(GOTO)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("GOTO");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FMT_EQUAL(Ast.Node<String> parentNode) {
		if (!match(FMT_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FMT_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean RETURN(Ast.Node<String> parentNode) {
		if (!match(RETURN)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("RETURN");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DATA(Ast.Node<String> parentNode) {
		if (!match(DATA)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DATA");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PRECISION(Ast.Node<String> parentNode) {
		if (!match(PRECISION)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PRECISION");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean SUBROUTINE(Ast.Node<String> parentNode) {
		if (!match(SUBROUTINE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("SUBROUTINE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean INTEGER(Ast.Node<String> parentNode) {
		if (!match(INTEGER)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("INTEGER");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean FILE_EQUAL(Ast.Node<String> parentNode) {
		if (!match(FILE_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FILE_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}


	private boolean NEWLINE(Ast.Node<String> parentNode) {
		if (!match(NEWLINE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("NEWLINE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean RECL_EQUAL(Ast.Node<String> parentNode) {
		if (!match(RECL_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("RECL_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FUNCTION(Ast.Node<String> parentNode) {
		if (!match(FUNCTION)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FUNCTION");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean REC_EQUAL(Ast.Node<String> parentNode) {
		if (!match(REC_EQUAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("REC_EQUAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean READ(Ast.Node<String> parentNode) {
		if (!match(READ)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("READ");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean FORMAT(Ast.Node<String> parentNode) {
		if (!match(FORMAT)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("FORMAT");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean OR(Ast.Node<String> parentNode) {
		if (!match(OR)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("OR");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean REAL(Ast.Node<String> parentNode) {
		if (!match(REAL)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("REAL");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PAUSE(Ast.Node<String> parentNode) {
		if (!match(PAUSE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PAUSE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean BLOCKDATA(Ast.Node<String> parentNode) {
		if (!match(BLOCKDATA)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BLOCKDATA");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ENTRY(Ast.Node<String> parentNode) {
		if (!match(ENTRY)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ENTRY");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean TRUE(Ast.Node<String> parentNode) {
		if (!match(TRUE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("TRUE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean BCON(Ast.Node<String> parentNode) {
		if (!match(BCON)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("BCON");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean PARAMETER(Ast.Node<String> parentNode) {
		if (!match(PARAMETER)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("PARAMETER");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DIMENSION(Ast.Node<String> parentNode) {
		if (!match(DIMENSION)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DIMENSION");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ENDPROGRAM(Ast.Node<String> parentNode) {
		if (!match(ENDPROGRAM)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ENDPROGRAM");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean ENDSUBROUTINE(Ast.Node<String> parentNode) {
		if (!match(ENDSUBROUTINE)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("ENDSUBROUTINE");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	private boolean DOUBLEPRECISION(Ast.Node<String> parentNode) {
		if (!match(DOUBLEPRECISION)) {
			return false;
		}

		Ast.Node<String> node = ast.createNode("DOUBLEPRECISION");
		Ast.Node<String> lexNode = ast.createNode(peek().getLexeme());

		ast.addChild(parentNode, node);
		ast.addChild(node, lexNode);
		advance();
		return true;
	}

	/**
	 * Match arguments with current position in token stream
	 * @param tokenType Token type
	 * @return Boolean
	 */
	private boolean match(TokenType... tokenType) {
		for (TokenType type : tokenType) {
			if (check(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check current position in token stream matches with argument
	 * @param tokenType Token type
	 * @return Boolean
	 */
	private boolean check(TokenType tokenType) {
		if (isAtEnd()) return false;
		return peek().getType() == tokenType;
	}

	/**
	 * Advance in token stream
	 * @return Previous token
	 */
	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}
		return previous();
	}

	/**
	 * Check next token is EOF in token stream
	 * @return Boolean
	 */
	private boolean isAtEnd() {
		return peek().getType() == EOF;
	}

	/**
	 * Return next token in token stream
	 * @return Next token
	 */
	private Token peek() {
		return tokens.get(current);
	}

	/**
	 * Return previous token in token stream
	 * @return Previous token
	 */
	private Token previous() {
		return tokens.get(current - 1);
	}
}
