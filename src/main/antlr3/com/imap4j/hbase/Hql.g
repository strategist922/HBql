grammar Hql;

options {
	superClass=HBaseParser;
	backtrack=true;
}

tokens {
	DOT = '.';
	COLON = ':';
	STAR = '*';
	DIV = '/';
	COMMA = ',';
	PLUS = '+';
	MINUS = '-';
	EQ = '=';
	LT = '<';
	GT = '>';
	LTGT = '<>';	
	LTEQ = '<=';	
	GTEQ = '>=';	
	DQUOTE = '"';
	SQUOTE = '\'';
	LPAREN = '(';
	RPAREN = ')';
}

@rulecatch {
catch (RecognitionException re) {
	handleRecognitionException(re);
}
}

@header {
package com.imap4j.hbase;
import com.imap4j.hbase.hql.*;
import com.imap4j.hbase.hql.expr.*;
import com.imap4j.hbase.antlr.*;
import com.google.common.collect.Lists;
import java.util.Date;
import com.imap4j.imap.antlr.imap.AntlrActions;
}

@lexer::header {
package com.imap4j.hbase;
import com.google.common.collect.Lists;
}

selectStmt returns [QueryArgs retval]
	: keySELECT (STAR | cols=columnList) 
	  keyFROM table=dottedValue 
	  where=whereClause?				{retval = new QueryArgs($cols.retval, $table.text, $where.retval);};

execCommand returns [ExecArgs retval]
	: create=createStmt				{retval = $create.retval;}
	| desc=describeStmt 				{retval = $desc.retval;}
	| show=showStmt 				{retval = $show.retval;}
	| del=deleteStmt 				{retval = $del.retval;}
	| set=setStmt					{retval = $set.retval;}
	;

createStmt returns [CreateArgs retval]
	: keyCREATE keyTABLE table=ID 			{retval = new CreateArgs($table.text);};

describeStmt returns [DescribeArgs retval]
	: keyDESCRIBE keyTABLE table=ID 		{retval = new DescribeArgs($table.text);};

showStmt returns [ShowArgs retval]
	: keySHOW keyTABLES 		 		{retval = new ShowArgs();};

deleteStmt returns [DeleteArgs retval]
	: keyDELETE keyFROM table=ID 
	  where=whereClause?				{retval = new DeleteArgs($table.text, $where.retval);};

setStmt returns [SetArgs retval]
	: keySET var=ID (keyTO | EQ)? val=dottedValue 	{retval = new SetArgs($var.text, $val.text);};

whereClause returns [WhereExpr retval]
	: keyWHERE c=orExpr 				{retval = new WhereExpr($c.retval);};
		
orExpr returns [OrExpr retval]
	: expr1=andExpr (keyOR expr2=orExpr)?		{retval= new OrExpr($expr1.retval, $expr2.retval);;}
	//| cond_expr keyOR cond_term
	;

andExpr returns [AndExpr retval]
	: expr1=condFactor (keyAND expr2=andExpr)?	{retval = new AndExpr($expr1.retval, $expr2.retval);}
	//| cond_term keyAND cond_factor
	;
	
condFactor returns [CondFactor retval]			 
	: k=keyNOT? p=condPrimary			{retval = new CondFactor(($k.text != null), $p.retval);}
	;

condPrimary returns [CondPrimary retval]
	: simpleCondExpr  				{retval = new CondPrimary($simpleCondExpr.retval);}
	| LPAREN orExpr RPAREN				{retval = new CondPrimary($orExpr.retval);}
	;

simpleCondExpr returns [SimpleCondExpr retval]
	: betweenExpr					{retval = new SimpleCondExpr($betweenExpr.retval);}
	| likeExpr					//{retval = new SimpleCondExpr($likeExpr.retval);}
	| inExpr					{retval = new SimpleCondExpr($inExpr.retval);}
	| nullCompExpr					//{retval = new SimpleCondExpr($nullCompExpr.retval);}
	| compareExpr 					{retval = new SimpleCondExpr($compareExpr.retval);}
	;

betweenExpr returns [BetweenExpr retval]
	: a=attribRef[ExprType.NumberType] n=keyNOT? 
	  keyBETWEEN n1=numberExpr keyAND n2=numberExpr	{retval = new BetweenExpr(ExprType.NumberType, $a.retval, ($n.text != null), $n1.retval, $n2.retval);}
	| a=attribRef[ExprType.StringType] n=keyNOT?  
	  keyBETWEEN s1=stringExpr keyAND s2=stringExpr	{retval = new BetweenExpr(ExprType.StringType, $a.retval, ($n.text != null), $n1.retval, $n2.retval);}
	| a=attribRef[ExprType.DateType] n=keyNOT? 
	  keyBETWEEN d1=dateExpr keyAND d2=dateExpr
	;

likeExpr 
	: attribRef[ExprType.StringType] keyNOT? keyLIKE pattern_value=stringLiteral; // ('ESCAPE' escape_character=string_literal)?;

inExpr returns [InExpr retval]
	: a=attribRef[ExprType.NumberType] n=keyNOT? keyIN 
	  LPAREN i=intItemList RPAREN			{retval = new InExpr(ExprType.NumberType, $a.retval, ($n.text != null), $i.retval);} 
	| a=attribRef[ExprType.StringType] n=keyNOT? keyIN 
	  LPAREN s=strItemList RPAREN			{retval = new InExpr(ExprType.StringType, $a.retval, ($n.text != null), $s.retval);} 
	;

intItem returns [NumberLiteral retval]
	: num=numberLiteral				{retval = $num.retval;};

strItem : stringLiteral;

nullCompExpr
	: attribRef[ExprType.StringType] keyIS (keyNOT)? keyNULL;

compareExpr returns [CompareExpr retval]
	: a=attribRef[ExprType.StringType] o=compOp stringExpr	{retval = new StringCompareExpr($a.retval, $o.retval, $stringExpr.retval);}
	| a=attribRef[ExprType.DateType] o=compOp dateExpr 
	| a=attribRef[ExprType.NumberType] o=compOp numberExpr	{retval = new NumberCompareExpr($a.retval, $o.retval, $numberExpr.retval);}
	| stringExpr o=compOp a=attribRef[ExprType.StringType]	{retval = new StringCompareExpr($stringExpr.retval, $o.retval, $a.retval);}
	| dateExpr o=compOp a=attribRef[ExprType.DateType]
	| numberExpr o=compOp a=attribRef[ExprType.NumberType]	{retval = new NumberCompareExpr($numberExpr.retval, $o.retval, $a.retval);}
	;
	
compOp returns [CompareExpr.Operator retval]
	: EQ 		{retval = CompareExpr.Operator.EQ;}
	| GT 		{retval = CompareExpr.Operator.GT;}
	| GTEQ 		{retval = CompareExpr.Operator.GTEQ;}
	| LT 		{retval = CompareExpr.Operator.LT;}
	| LTEQ 		{retval = CompareExpr.Operator.LTEQ;}
	| LTGT		{retval = CompareExpr.Operator.LTGT;}
	;

numberExpr returns [NumberExpr retval]
	: simpleNumberExpr
	;

simpleNumberExpr
	: numberTerm ((PLUS | MINUS) simpleNumberExpr)?
	//| simpleNumberExpr (PLUS | MINUS) numberTerm
	;

numberTerm
	: numberFactor ((STAR | DIV) numberTerm)?
	//| numberTerm (STAR | DIV) numberFactor
	;

numberFactor
	: (PLUS | MINUS)? numberPrimary;

numberPrimary
	: numberLiteral
	| LPAREN simpleNumberExpr RPAREN
	| funcsReturningNumeric
	;

stringExpr returns [StringExpr retval]
	: lit=stringLiteral				{retval = new StringExpr($lit.retval);}
	| func=funcReturningStrings
	| attrib=attribRef[ExprType.StringType]		{retval = new StringExpr($attrib.retval);}
	;

dateExpr
	: funcReturningDatetime
	;

funcsReturningNumeric
	: keyLENGTH LPAREN stringExpr RPAREN
	| keyABS LPAREN simpleNumberExpr RPAREN
	| keyMOD LPAREN simpleNumberExpr COMMA simpleNumberExpr RPAREN
	;

funcReturningDatetime
	: keyCURRENT_DATE
	| keyCURRENT_TIME
	| keyCURRENT_TIMESTAMP
	;

funcReturningStrings
	: keyCONCAT LPAREN stringExpr COMMA stringExpr RPAREN
	| keySUBSTRING LPAREN stringExpr COMMA simpleNumberExpr COMMA simpleNumberExpr RPAREN
	| keyTRIM LPAREN stringExpr RPAREN
	| keyLOWER LPAREN stringExpr RPAREN
	| keyUPPER LPAREN stringExpr RPAREN
	;

attribRef [ExprType type] returns [AttribRef retval]
	: v=ID 						{retval = new AttribRef(type, $v.text);};
		
stringLiteral returns [StringLiteral retval]
	: v=QUOTED 					{retval = new StringLiteral($v.text);};
	
numberLiteral returns [NumberLiteral retval]
	: v=INT						{retval = new NumberLiteral(Integer.valueOf($v.text));};
		
intItemList returns [List<Object> retval]
@init {retval = Lists.newArrayList();}
	: item1=intItem {retval.add($item1.retval);} (COMMA item2=intItem {retval.add($item2.retval);})*;
	
strItemList returns [List<Object> retval]
@init {retval = Lists.newArrayList();}
	: item1=strItem {retval.add($item1.text);} (COMMA item2=strItem {retval.add($item2.text);})*;
	
columnList returns [List<String> retval]
@init {retval = Lists.newArrayList();}
	: column[retval] (COMMA column[retval])*;

qstringList returns [List<String> retval]
@init {retval = Lists.newArrayList();}
	: qstring[retval] (COMMA qstring[retval])*;

column [List<String> list]	
	: charstr=dottedValue 				{if (list != null) list.add($charstr.text);};

dottedValue	
	: ID ((DOT | COLON) ID)*;

qstring	[List<String> list]
	: QUOTED 					{if (list != null) list.add($QUOTED.text);};

INT	: DIGIT+;
ID	: CHAR (CHAR | DIGIT)*;
 
QUOTED		
@init {final StringBuffer sbuf = new StringBuffer();}	
	: DQUOTE (options {greedy=false;} : any=. {sbuf.append((char)$any);})* DQUOTE 	{setText(sbuf.toString());}
	| SQUOTE (options {greedy=false;} : any=. {sbuf.append((char)$any);})* SQUOTE	{setText(sbuf.toString());}
	;

fragment
DIGIT	: '0'..'9'; 

fragment
CHAR 	: 'a'..'z' | 'A'..'Z'; 

WS 	: (' ' |'\t' |'\n' |'\r' )+ {skip();} ;

keySELECT 	: {AntlrActions.isKeyword(input, "SELECT")}? ID;
keyDELETE 	: {AntlrActions.isKeyword(input, "DELETE")}? ID;
keyCREATE 	: {AntlrActions.isKeyword(input, "CREATE")}? ID;
keyDESCRIBE 	: {AntlrActions.isKeyword(input, "DESCRIBE")}? ID;
keySHOW 	: {AntlrActions.isKeyword(input, "SHOW")}? ID;
keyTABLE 	: {AntlrActions.isKeyword(input, "TABLE")}? ID;
keyTABLES 	: {AntlrActions.isKeyword(input, "TABLES")}? ID;
keyWHERE	: {AntlrActions.isKeyword(input, "WHERE")}? ID;
keyFROM 	: {AntlrActions.isKeyword(input, "FROM")}? ID;
keySET 		: {AntlrActions.isKeyword(input, "SET")}? ID;
keyIN 		: {AntlrActions.isKeyword(input, "IN")}? ID;
keyIS 		: {AntlrActions.isKeyword(input, "IS")}? ID;
keyLIKE		: {AntlrActions.isKeyword(input, "LIKE")}? ID;
keyTO 		: {AntlrActions.isKeyword(input, "TO")}? ID;
keyOR 		: {AntlrActions.isKeyword(input, "OR")}? ID;
keyAND 		: {AntlrActions.isKeyword(input, "AND")}? ID;
keyNOT 		: {AntlrActions.isKeyword(input, "NOT")}? ID;
keyTRUE 		: {AntlrActions.isKeyword(input, "TRUE")}? ID;
keyFALSE 	: {AntlrActions.isKeyword(input, "FALSE")}? ID;
keyBETWEEN 	: {AntlrActions.isKeyword(input, "BETWEEN")}? ID;
keyNULL 	: {AntlrActions.isKeyword(input, "NULL")}? ID;
keyLOWER 	: {AntlrActions.isKeyword(input, "LOWER")}? ID;
keyUPPER 	: {AntlrActions.isKeyword(input, "UPPER")}? ID;
keyTRIM 	: {AntlrActions.isKeyword(input, "TRIM")}? ID;
keyCONCAT 	: {AntlrActions.isKeyword(input, "CONCAT")}? ID;
keySUBSTRING 	: {AntlrActions.isKeyword(input, "SUBSTRING")}? ID;
keyLENGTH 	: {AntlrActions.isKeyword(input, "LENGTH")}? ID;
keyABS 		: {AntlrActions.isKeyword(input, "ABS")}? ID;
keyMOD	 	: {AntlrActions.isKeyword(input, "MOD")}? ID;
keyCURRENT_DATE	: {AntlrActions.isKeyword(input, "CURRENT_DATE")}? ID;
keyCURRENT_TIME : {AntlrActions.isKeyword(input, "CURRENT_TIME")}? ID;
keyCURRENT_TIMESTAMP : {AntlrActions.isKeyword(input, "CURRENT_TIMESTAMP")}? ID;
