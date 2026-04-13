package org.conilang.lexer

import com.intellij.lexer.FlexAdapter

class ConiLexerAdapter : FlexAdapter(ConiLexer(null))
