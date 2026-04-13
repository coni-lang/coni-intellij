package org.conilang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.conilang.psi.ConiTypes;
import com.intellij.psi.TokenType;

%%

%class ConiLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE=[\ \n\t\f\r]+
LINE_COMMENT=";".*
STRING=\"([^\"\\]|\\.)*\"
NUMBER=-?[0-9]+(\.[0-9]+)?

IDENTIFIER=[a-zA-Z0-9_\-\*\+\/\?\!\<\>\=\.]+
KEYWORD=(def|defn|defmacro|defn-|defmacro-|fn|let|if|do|loop|recur|quote|ns|true|false|nil)

%%

<YYINITIAL> {
  {WHITE_SPACE}      { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT}     { return ConiTypes.COMMENT; }
  {STRING}           { return ConiTypes.STRING; }
  {NUMBER}           { return ConiTypes.NUMBER; }
  {KEYWORD}          { return ConiTypes.KEYWORD; }
  {IDENTIFIER}       { return ConiTypes.IDENTIFIER; }
  "("                { return ConiTypes.LPAREN; }
  ")"                { return ConiTypes.RPAREN; }
  "["                { return ConiTypes.LBRACKET; }
  "]"                { return ConiTypes.RBRACKET; }
  "{"                { return ConiTypes.LBRACE; }
  "}"                { return ConiTypes.RBRACE; }
  "#"                { return ConiTypes.HASH; }
  ":"{IDENTIFIER}    { return ConiTypes.KEYWORD_SYM; }
}

[^] { return TokenType.BAD_CHARACTER; }
