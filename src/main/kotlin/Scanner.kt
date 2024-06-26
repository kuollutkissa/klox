package com.kuollutkissa.klox
import com.kuollutkissa.klox.TokenType.*

class Scanner(private val source: String) {
    private val tokens = ArrayList<Token>()
    private var start = 0
    private var current = 0
    private var line = 1
    fun scanTokens(): ArrayList<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }
    private fun scanToken() {
        when(val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            ':' -> addToken(COLON)
            '!' -> addToken(if(match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if(match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if(match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if(match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if(match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance()
                } else {
                    return addToken(SLASH)
                }
            }
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()

            else -> {
                if(c.isDigit()) number()
                else if(c.isAlpha()) identifier()
                else error(line, "Unexpected character.")
            }
        }
    }
    private fun advance(): Char {
        return source[current++]
    }
    private fun addToken(type: TokenType) {
        addToken(type, null)
    }
    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start..<current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if(isAtEnd()) return false
        if(source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if(isAtEnd()) return '\u0000'
        return  source[current]
    }
    private fun peekNext(): Char {
        if(current+1>= source.length) return '\u0000'
        return  source[current+1]
    }
    private fun Char.isAlpha(): Boolean {
        return this.isLowerCase() || this.isUpperCase() || this == '_'
    }
    private fun Char.isAlphaNum() : Boolean {
        return this.isAlpha() || this.isDigit()
    }
    private fun string() {
        while(peek() != '"' && !isAtEnd()) {
            advance()
        }
        if(isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }
        advance()
        val value = source.substring(start + 1 ..<current-1)
        addToken(STRING, value)
    }
    private fun number() {
        while(peek().isDigit()) advance()
        if(peek() == '.' && peekNext().isDigit()) {
            advance()
            while(peek().isDigit()) advance()
        }
        addToken(NUMBER, source.substring(start..<current).toDouble())
    }
    private fun identifier() {
        while(peek().isAlphaNum()) advance()
        val text = source.substring(start..<current)
        var type = keywords[text]
        if(type == null) type = IDENTIFIER
        addToken(type)
    }
    companion object {
        val keywords = hashMapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE,
        )
    }
 }