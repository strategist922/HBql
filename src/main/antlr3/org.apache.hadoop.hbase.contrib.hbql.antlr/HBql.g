grammar HBql;

options {superClass=ParserSupport;}

tokens {
	SEMI = ';';
	DOT = '.';
	DOLLAR = '$';
	COLON = ':';
	QMARK = '?';
	STAR = '*';
	DIV = '/';
	COMMA = ',';
	PLUS = '+';
	MINUS = '-';
	MOD = '%';
	EQ = '=';
	LT = '<';
	GT = '>';
	LTGT = '<>';	
	LTEQ = '<=';	
	GTEQ = '>=';	
	BANGEQ = '!=';	
	DQUOTE = '"';
	SQUOTE = '\'';
	LPAREN = '(';
	RPAREN = ')';
	LBRACE = '[';
	RBRACE = ']';
	LCURLY = '{';
	RCURLY = '}';
}

@rulecatch {catch (RecognitionException re) {handleRecognitionException(re);}}

@header {
package org.apache.hadoop.hbase.contrib.hbql.antlr;

import org.apache.hadoop.hbase.contrib.hbql.parser.*;
import org.apache.hadoop.hbase.contrib.hbql.statement.*;
import org.apache.hadoop.hbase.contrib.hbql.statement.args.*;
import org.apache.hadoop.hbase.contrib.hbql.statement.select.*;
import org.apache.hadoop.hbase.contrib.hbql.schema.*;

import org.apache.expreval.expr.*;
import org.apache.expreval.expr.node.*;
import org.apache.expreval.expr.betweenstmt.*;
import org.apache.expreval.expr.calculation.*;
import org.apache.expreval.expr.casestmt.*;
import org.apache.expreval.expr.compare.*;
import org.apache.expreval.expr.function.*;
import org.apache.expreval.expr.ifthenstmt.*;
import org.apache.expreval.expr.instmt.*;
import org.apache.expreval.expr.literal.*;
import org.apache.expreval.expr.nullcomp.*;
import org.apache.expreval.expr.stringpattern.*;
import org.apache.expreval.expr.var.*;
import org.apache.expreval.util.*;
}

@lexer::header {
package org.apache.hadoop.hbase.contrib.hbql.antlr;
}

shellCommand returns [List<ShellStatement> retval]
@init {retval = Lists.newArrayList();}
	: c1=commandStmt {retval.add($c1.retval);} (SEMI (c2=commandStmt {retval.add($c2.retval);})? )*;
	
commandStmt returns [ShellStatement retval]
options {backtrack=true;}	
	: s1=schemaStmt					{retval = $s1.retval;}
	| s2=schemaManagerStmt				{retval = $s2.retval;}
	| s3=tableStatement				{retval = $s3.retval;}
	| keyLIST keyTABLES 		 		{retval = new ShowTablesStatement();}
	| keyVERSION					{retval = new VersionStatement();}
	| keyHELP					{retval = new HelpStatement();}
	| keySET t=simpleName EQ? val=QUOTED	 	{retval = new SetStatement($t.text, $val.text);}
	;						

tableStatement returns [TableStatement retval]
	: keyDESCRIBE keyTABLE t=simpleName 		{retval = new DescribeTableStatement($t.text);}
	| keyDISABLE keyTABLE t=simpleName 		{retval = new DisableTableStatement($t.text);}
	| keyDROP keyTABLE t=simpleName 		{retval = new DropTableStatement($t.text);}
	| keyENABLE keyTABLE t=simpleName 		{retval = new EnableTableStatement($t.text);}
	;
	 
schemaManagerStmt returns [SchemaStatement retval]
	: keyCREATE keySCHEMA t=simpleName (keyFOR keyTABLE a=simpleName)? LPAREN l=attribList RPAREN
							{retval = new CreateSchemaStatement($t.text, $a.text, $l.retval);}
	| keyDROP keySCHEMA t=simpleName 		{retval = new DropSchemaStatement($t.text);}
	;

schemaStmt returns [SchemaStatement retval]
	: keyCREATE keyTABLE keyUSING keySCHEMA t=simpleName 	
							{retval = new CreateTableStatement($t.text);}
	| keyDELETE keyFROM t=simpleName w=withClause?	{retval = new DeleteStatement($t.text, $w.retval);}
	| keyINSERT keyINTO t=simpleName LPAREN e=exprList RPAREN ins=insertValues
							{retval = new InsertStatement($t.text, $e.retval, $ins.retval);}
	| sel=selectStatement				{retval = $sel.retval;}			
	;
		
selectStatement returns [SelectStatement retval]
	: keySELECT c=selectElems keyFROM t=simpleName w=withClause?
							{retval = new SelectStatement($c.retval, $t.text, $w.retval);};
							
insertValues returns [InsertValueSource retval]
	: keyVALUES LPAREN e=insertExprList RPAREN	{retval = new InsertSingleRow($e.retval);}
	| sel=selectStatement				{retval = new InsertSelectValues($sel.retval);}			
	;
	
withClause returns [WithArgs retval]
@init {retval = new WithArgs();}
	: keyWITH withElements[retval]+;

withElements[WithArgs withArgs] 
	: k=keysRange					{withArgs.setKeyRangeArgs($k.retval);}
	| t=timestampOption				{withArgs.setTimestampRangeArgs($t.retval);}	
	| v=versions					{withArgs.setVersionArgs($v.retval);}
	| l=scanLimit					{withArgs.setScanLimitArgs($l.retval);}
	| q=queryLimit					{withArgs.setQueryLimitArgs($q.retval);}
	| s=serverFilter				{withArgs.setServerExpressionTree($s.retval);}
	| c=clientFilter				{withArgs.setClientExpressionTree($c.retval);}
	;
	
keysRange returns [KeyRangeArgs retval]
	: keyKEYS k=rangeList				{retval = new KeyRangeArgs($k.retval);}	
	| keyKEYS keyALL				{retval = new KeyRangeArgs();}	
	;

rangeList returns [List<KeyRangeArgs.Range> retval]
@init {retval = Lists.newArrayList();}
	: k1=keyRange {retval.add($k1.retval);} (COMMA k2=keyRange {retval.add($k2.retval);})*;
	
keyRange returns [KeyRangeArgs.Range retval]
options {backtrack=true;}	
	: q1=valPrimary keyTO keyLAST			{retval = KeyRangeArgs.newLastRange($q1.retval);}
	| q1=valPrimary keyTO q2=valPrimary		{retval = KeyRangeArgs.newRange($q1.retval, $q2.retval);}
	| q1=valPrimary 				{retval = KeyRangeArgs.newSingleKey($q1.retval);}
	;
		
timestampOption returns [TimestampRangeArgs retval]
	: keyTIMESTAMP keyRANGE d1=valPrimary keyTO d2=valPrimary	
							{retval = new TimestampRangeArgs($d1.retval, $d2.retval);}
	| keyTIMESTAMP d1=valPrimary			{retval = new TimestampRangeArgs($d1.retval, $d1.retval);}
	;
		
versions returns [VersionArgs retval]
	: keyVERSIONS v=valPrimary			{retval = new VersionArgs($v.retval);}
	| keyVERSIONS keyMAX				{retval = new VersionArgs(new IntegerLiteral(Integer.MAX_VALUE));}
	;
	
scanLimit returns [LimitArgs retval]
	: keySCAN keyLIMIT v=valPrimary			{retval = new LimitArgs($v.retval);};
	
queryLimit returns [LimitArgs retval]
	: keyQUERY keyLIMIT v=valPrimary		{retval = new LimitArgs($v.retval);};
	
clientFilter returns [ExpressionTree retval]
	: keyCLIENT keyFILTER keyWHERE w=descWhereExpr	
							{retval = $w.retval;};
	
serverFilter returns [ExpressionTree retval]
	: keySERVER keyFILTER keyWHERE w=descWhereExpr	
							{retval = $w.retval;};
	
nodescWhereExpr returns [ExpressionTree retval]
	 : e=topExpr					{retval = ExpressionTree.newExpressionTree($e.retval);};

descWhereExpr returns [ExpressionTree retval]
	: s=schemaDesc? e=topExpr			{retval = ExpressionTree.newExpressionTree($e.retval); if ($s.retval != null) retval.setSchema($s.retval);};

// Expressions
topExpr returns [GenericValue retval]
	: o=orExpr					{retval = $o.retval;};
				
orExpr returns [GenericValue retval]
	: e1=andExpr (keyOR e2=orExpr)?			{$orExpr.retval = ($e2.text == null) ? $e1.retval : new BooleanCompare($e1.retval, Operator.OR, $e2.retval);};

andExpr returns [GenericValue retval]
	: e1=booleanNot (keyAND e2=andExpr)?		{$andExpr.retval = ($e2.text == null) ? $e1.retval : new BooleanCompare($e1.retval, Operator.AND, $e2.retval);};

booleanNot returns [GenericValue retval]			 
	: (n=keyNOT)? p=booleanPrimary			{retval = ($n.text != null) ? new BooleanNot(true, $p.retval) :  $p.retval;};

booleanPrimary returns [GenericValue retval]
	: b=eqneCompare					{retval = $b.retval;};

eqneCompare returns [GenericValue retval]
options {backtrack=true; memoize=true;}	
	: v1=ltgtCompare o=eqneOp v2=ltgtCompare 	{retval = new DelegateCompare($v1.retval, $o.retval, $v2.retval);}	
	| c=ltgtCompare					{retval = $c.retval;}
	;

ltgtCompare returns [GenericValue retval]
options {backtrack=true; memoize=true;}	
	: v1=valPrimary o=ltgtOp v2=valPrimary		{retval = new DelegateCompare($v1.retval, $o.retval, $v2.retval);}
	| b=booleanFunctions				{retval = $b.retval;}
	| p=valPrimary					{retval = $p.retval;}
	;

// Value Expressions
valPrimary returns [GenericValue retval] 
@init {List<GenericValue> exprList = Lists.newArrayList(); List<Operator> opList = Lists.newArrayList(); }
	: m=multExpr {exprList.add($m.retval);} (op=plusMinus n=multExpr {opList.add($op.retval); exprList.add($n.retval);})*	
							{retval = getLeftAssociativeGenericValues(exprList, opList);};
	
multExpr returns [GenericValue retval]
@init {List<GenericValue> exprList = Lists.newArrayList(); List<Operator> opList = Lists.newArrayList(); }
	: m=signedExpr {exprList.add($m.retval);} (op=multDiv n=signedExpr {opList.add($op.retval); exprList.add($n.retval);})*	
							{retval = getLeftAssociativeGenericValues(exprList, opList);};
	
signedExpr returns [GenericValue retval]
	: (s=plusMinus)? n=parenExpr 			{$signedExpr.retval = ($s.retval == Operator.MINUS) ? new DelegateCalculation($n.retval, Operator.NEGATIVE, new IntegerLiteral(0)) : $n.retval;};

// The order here is important.  atomExpr has to come after valueFunctions to avoid simpleName conflict
parenExpr returns [GenericValue retval]
options {backtrack=true; memoize=true;}	
	: f=valueFunctions				{retval = $f.retval;}
	| n=atomExpr					{retval = $n.retval;}
	| LPAREN s=topExpr RPAREN			{retval = $s.retval;}
	;
	   						 
atomExpr returns [GenericValue retval]
	: s=stringLiteral				{retval = $s.retval;}
	| i=integerLiteral				{retval = $i.retval;}
	| l=longLiteral					{retval = $l.retval;}
	| d=doubleLiteral				{retval = $d.retval;}
	| b=booleanLiteral				{retval = $b.retval;}
	| keyNULL					{retval = new StringNullLiteral();}
	| p=paramRef					{retval = new NamedParameter($p.text);}
	| v=varRef					{retval = new DelegateColumn($v.text);}
	;

// Literals		
stringLiteral returns [StringLiteral retval]
	: v=QUOTED 					{retval = new StringLiteral($v.text);};
	
integerLiteral returns [IntegerLiteral retval]
	: v=INT						{retval = new IntegerLiteral($v.text);};	

longLiteral returns [LongLiteral retval]
	: v=LONG					{retval = new LongLiteral($v.text);};	


doubleLiteral returns [GenericValue retval]
	: v1=DOUBLE1					{retval = DoubleLiteral.valueOf($v1.text);}
	| v2=DOUBLE2					{retval = DoubleLiteral.valueOf($v2.text);}
	;	

booleanLiteral returns [BooleanLiteral retval]
	: t=keyTRUE					{retval = new BooleanLiteral($t.text);}
	| f=keyFALSE					{retval = new BooleanLiteral($f.text);}
	;

// Functions
booleanFunctions returns [BooleanValue retval]
options {backtrack=true; memoize=true;}	
	: s1=valPrimary n=keyNOT? keyCONTAINS s2=valPrimary		
							{retval = new ContainsStmt($s1.retval, ($n.text != null), $s2.retval);}
	| s1=valPrimary n=keyNOT? keyLIKE s2=valPrimary {retval = new LikeStmt($s1.retval, ($n.text != null), $s2.retval);}
	| s1=valPrimary n=keyNOT? keyBETWEEN s2=valPrimary keyAND s3=valPrimary		
							{retval = new DelegateBetweenStmt($s1.retval, ($n.text != null), $s2.retval, $s3.retval);}
	| s1=valPrimary n=keyNOT? keyIN LPAREN l=exprList RPAREN			
							{retval = new DelegateInStmt($s1.retval, ($n.text != null), $l.retval);} 
	| s1=valPrimary keyIS n=keyNOT? keyNULL		{retval = new DelegateNullCompare(($n.text != null), $s1.retval);}	
	;

valueFunctions returns [GenericValue retval]
options {backtrack=true; memoize=true;}	
	: keyIF t1=topExpr keyTHEN t2=topExpr keyELSE t3=topExpr keyEND	
							{retval = new DelegateIfThen($t1.retval, $t2.retval, $t3.retval);}
	
	| c=caseStmt					{retval = $c.retval;} 						

	| t=simpleName LPAREN a=exprList? RPAREN	{retval = new DelegateFunction($t.text, $a.retval);}
	;

caseStmt returns [DelegateCase retval]
	: keyCASE 					{retval = new DelegateCase();}
	   whenItem[retval]+
	   (keyELSE t=topExpr)? 			{retval.addElse($t.retval);}
	  keyEND
	;
	
whenItem [DelegateCase stmt] 
	: keyWHEN t1=topExpr keyTHEN t2=topExpr		{stmt.addWhen($t1.retval, $t2.retval);}
	;
	
attribList returns [List<ColumnDescription> retval] 
@init {retval = Lists.newArrayList();}
	: (a1=defineAttrib {retval.add($a1.retval);} (COMMA a2=defineAttrib {retval.add($a2.retval);})*)?;
	
defineAttrib returns [ColumnDescription retval]
	: c=varRef type=simpleName (b=LBRACE RBRACE)? m=keyKACMAP? (keyALIAS a=simpleName)? (keyDEFAULT t=topExpr)?	
							{retval = ColumnDescription.newColumn($c.text, $a.text, $m.text!=null, false, $type.text, $b.text!=null, $t.retval);}
	| f=familyRef (keyALIAS a=simpleName)?		{retval = ColumnDescription.newFamilyDefault($f.text, $a.text);}
	;

selectElems returns [List<SelectElement> retval]
	: STAR						{retval = FamilySelectElement.newAllFamilies();}
	| c=selectElemList				{retval = $c.retval;}
	;
	
selectElemList returns [List<SelectElement> retval]
@init {retval = Lists.newArrayList();}
	: c1=selectElem {retval.add($c1.retval);} (COMMA c2=selectElem {retval.add($c2.retval);})*;

selectElem returns [SelectElement retval]
options {backtrack=true; memoize=true;}	
	: b=topExpr (keyAS i2=simpleName)?		{$selectElem.retval = SingleExpression.newSingleExpression($b.retval, $i2.text);}
	| f=familyRef					{$selectElem.retval = FamilySelectElement.newFamilyElement($f.text);}
	;

exprList returns [List<GenericValue> retval]
@init {retval = Lists.newArrayList();}
	: i1=topExpr {retval.add($i1.retval);} (COMMA i2=topExpr {retval.add($i2.retval);})*;
				
insertExprList returns [List<GenericValue> retval]
@init {retval = Lists.newArrayList();}
	: i1=insertExpr {retval.add($i1.retval);} (COMMA i2=insertExpr {retval.add($i2.retval);})*;

insertExpr returns [GenericValue retval]
	: t=topExpr					{retval = $t.retval;} 
	| keyDEFAULT					{retval = new DefaultKeyword();}
	;
					
schemaDesc returns [Schema retval]
	: LCURLY a=attribList RCURLY			{retval = newDefinedSchema(input, $a.retval);};
	
ltgtOp returns [Operator retval]
	: GT 						{retval = Operator.GT;}
	| GTEQ 						{retval = Operator.GTEQ;}
	| LT 						{retval = Operator.LT;}
	| LTEQ 						{retval = Operator.LTEQ;}
	;
			
eqneOp returns [Operator retval]
	: EQ EQ?					{retval = Operator.EQ;}
	| (LTGT | BANGEQ)				{retval = Operator.NOTEQ;}
	;
				
qstring	: QUOTED ;					

plusMinus returns [Operator retval]
	: PLUS						{retval = Operator.PLUS;}
	| MINUS						{retval = Operator.MINUS;}
	;
	
multDiv returns [Operator retval]
	: STAR						{retval = Operator.MULT;}
	| DIV						{retval = Operator.DIV;}
	| MOD						{retval = Operator.MOD;}
	;

simpleName
 	: ID;
 	
varRef 
	: ID (COLON ID)?;
	
familyRef 
	: ID COLON STAR;
	
paramRef
	: COLON ID;
		
INT	: DIGIT+;
LONG	: DIGIT+ ('L' | 'l');
DOUBLE1	: DIGIT+ (DOT DIGIT*)? ('D' | 'd' | 'F' | 'f');
DOUBLE2	: DIGIT+ DOT DIGIT*;

ID : CHAR (CHAR | DOT | MINUS | DOLLAR | DIGIT)*; // DOLLAR is for inner class table names
	 
QUOTED		
@init {final StringBuilder sbuf = new StringBuilder();}	
	: DQUOTE (options {greedy=false;} : any=. {sbuf.append((char)$any);})* DQUOTE {setText(sbuf.toString());}
	| SQUOTE (options {greedy=false;} : any=. {sbuf.append((char)$any);})* SQUOTE {setText(sbuf.toString());}
	;

fragment
DIGIT	: '0'..'9'; 

fragment
CHAR 	: 'a'..'z' | 'A'..'Z'; 

WS 	: (' ' |'\t' |'\n' |'\r' )+ {skip();};

keySELECT 	: {isKeyword(input, "SELECT")}? ID;
keyDELETE 	: {isKeyword(input, "DELETE")}? ID;
keyCREATE 	: {isKeyword(input, "CREATE")}? ID;
keyDESCRIBE 	: {isKeyword(input, "DESCRIBE")}? ID;
keyLIST 	: {isKeyword(input, "LIST")}? ID;
keyENABLE 	: {isKeyword(input, "ENABLE")}? ID;
keyDISABLE 	: {isKeyword(input, "DISABLE")}? ID;
keyDROP 	: {isKeyword(input, "DROP")}? ID;
keyTABLE 	: {isKeyword(input, "TABLE")}? ID;
keySCHEMA 	: {isKeyword(input, "SCHEMA")}? ID;
keyTABLES 	: {isKeyword(input, "TABLES")}? ID;
keyWHERE	: {isKeyword(input, "WHERE")}? ID;
keyUSING	: {isKeyword(input, "USING")}? ID;
keyWITH		: {isKeyword(input, "WITH")}? ID;
keyFILTER	: {isKeyword(input, "FILTER")}? ID;
keyFROM 	: {isKeyword(input, "FROM")}? ID;
keySET 		: {isKeyword(input, "SET")}? ID;
keyIN 		: {isKeyword(input, "IN")}? ID;
keyIS 		: {isKeyword(input, "IS")}? ID;
keyIF 		: {isKeyword(input, "IF")}? ID;
keyALIAS	: {isKeyword(input, "ALIAS")}? ID;
keyAS		: {isKeyword(input, "AS")}? ID;
keyTHEN 	: {isKeyword(input, "THEN")}? ID;
keyELSE 	: {isKeyword(input, "ELSE")}? ID;
keyEND 		: {isKeyword(input, "END")}? ID;
keyLAST		: {isKeyword(input, "LAST")}? ID;
keySCAN 	: {isKeyword(input, "SCAN")}? ID;
keyQUERY 	: {isKeyword(input, "QUERY")}? ID;
keyLIMIT 	: {isKeyword(input, "LIMIT")}? ID;
keyLIKE		: {isKeyword(input, "LIKE")}? ID;
keyTO 		: {isKeyword(input, "TO")}? ID;
keyOR 		: {isKeyword(input, "OR")}? ID;
keyAND 		: {isKeyword(input, "AND")}? ID;
keyNOT 		: {isKeyword(input, "NOT")}? ID;
keyTRUE 	: {isKeyword(input, "TRUE")}? ID;
keyFALSE 	: {isKeyword(input, "FALSE")}? ID;
keyBETWEEN 	: {isKeyword(input, "BETWEEN")}? ID;
keyNULL 	: {isKeyword(input, "NULL")}? ID;
keyFOR	 	: {isKeyword(input, "FOR")}? ID;
keyCLIENT	: {isKeyword(input, "CLIENT")}? ID;
keySERVER	: {isKeyword(input, "SERVER")}? ID;
keyVERSIONS	: {isKeyword(input, "VERSIONS")}? ID;
keyVERSION	: {isKeyword(input, "VERSION")}? ID;
keyTIMESTAMP	: {isKeyword(input, "TIMESTAMP")}? ID;
keyRANGE	: {isKeyword(input, "RANGE")}? ID;
keyMAX		: {isKeyword(input, "MAX")}? ID;
keyKEYS		: {isKeyword(input, "KEYS")}? ID;
keyALL		: {isKeyword(input, "ALL")}? ID;
keyCONTAINS	: {isKeyword(input, "CONTAINS")}? ID;
keyKACMAP	: {isKeyword(input, "MAPKEYSASCOLUMNS")}? ID;
keyDEFAULT	: {isKeyword(input, "DEFAULT")}? ID;
keyCASE		: {isKeyword(input, "CASE")}? ID;
keyWHEN		: {isKeyword(input, "WHEN")}? ID;
keyINSERT	: {isKeyword(input, "INSERT")}? ID;
keyINTO		: {isKeyword(input, "INTO")}? ID;
keyVALUES	: {isKeyword(input, "VALUES")}? ID;
keyHELP		: {isKeyword(input, "HELP")}? ID;
