package com.kuollutkissa.klox
import com.kuollutkissa.klox.TokenType.*

class Parser(private val tokens: ArrayList<Token>) {
    private var current = 0
    fun parse(): ArrayList<Statement> {
        val statements: ArrayList<Statement> = arrayListOf()
        while (!isAtEnd()) {
            statements.add(declaration()!!)
        }
        return statements
    }
    private fun declaration(): Statement? {
        try {
            if(match(VAR)) return varDeclaration()
            if(match(FUN)) return function("function")
            if(match(CLASS)) return classDeclaration()
            return statement()
        } catch(err: ParseError) {
            synchronise()
            return null
        }
    }
    private fun varDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expected identifier after 'var' keyword.")
        var initializer: Expression? = null
        if(match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expected ';' after variable declaration")
        return VarStatement(name, initializer)
    }
    private fun function(kind: String): FunctionStatement {
        val name = consume(IDENTIFIER, "Expected $kind name")
        consume(LEFT_PAREN, "Expected '(' after $kind name")
        val parameters: ArrayList<Token> = arrayListOf()
        if(!check(RIGHT_PAREN)) do {
            if(parameters.size > 254) error(peek(), "Can't have more than 254 parameters")
            parameters.add(consume(IDENTIFIER, "Expected parameter name."))
        } while(match(COMMA))
        consume(RIGHT_PAREN, "Expected ')' after $kind parameters")
        consume(LEFT_BRACE, "Expected $kind body.")
        val body = block()
        return FunctionStatement(name, parameters, body)
    }
    private fun classDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expected class name.")
        var superclass: VariableExpression? = null
        if(match(COLON)) {
            consume(IDENTIFIER, "Expected superclass name.")
            superclass = VariableExpression(previous())
        }
        consume(LEFT_BRACE, "Expected class body.")
        val methods = ArrayList<FunctionStatement>()
        while(!check(RIGHT_BRACE) && !isAtEnd())
            methods.add(function("method"))
        consume(RIGHT_BRACE, "Expected '}' after class body.")
        return ClassStatement(name, superclass, methods)
    }
    private fun statement(): Statement {
        if(match(LEFT_BRACE)) return BlockStatement(block())
        if(match(IF)) return ifStatement()
        if(match(WHILE)) return whileStatement()
        if(match(FOR)) return forStatement()
        if(match(RETURN)) return returnStatement()
        return expressionStatement()
    }

    private fun returnStatement(): Statement {
        val keyword = previous()
        var value: Expression? = null
        if(!check(SEMICOLON))  value = expression()
        consume(SEMICOLON, "Expected ';' after statement")
        return ReturnStatement(keyword, value)
    }
    private fun expressionStatement(): Statement {
        val expr = expression()
        consume(SEMICOLON, "Expected ';' after expression")
        return ExpressionStatement(expr)
    }
    private fun ifStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after condition")
        val thenBranch = statement()
        var elseBranch: Statement? = null
        if(match(ELSE)) {
            elseBranch = statement()
        }
        return IfStatement(condition, thenBranch, elseBranch)
    }
    private fun whileStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after condition")
        val body = statement()
        return WhileStatement(condition, body)
    }
    private fun forStatement(): Statement {
        consume(LEFT_PAREN, "Expected '(' after 'for'")
        val initializer: Statement? = if(match(SEMICOLON)) null
        else if(match(VAR)) varDeclaration()
        else expressionStatement()
        var condition: Expression? = null
        if(!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expected ';' after loop condition")
        var increment: Expression? = null
        if(!check(RIGHT_PAREN)) increment = expression()
        consume(RIGHT_PAREN, "Expected ')' after for clauses")
        var body = statement()
        if(increment != null) {
            body = BlockStatement (arrayListOf(body, ExpressionStatement(increment)))
        }
        if (condition == null) condition = LiteralExpression(true)
        body = WhileStatement(condition, body)
        if(initializer != null) body = BlockStatement(arrayListOf(initializer, body))
        return body
    }
    private fun expression(): Expression {
        return assignment()
    }
    private fun assignment(): Expression {
        val expr = or()
        if(match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if(expr is VariableExpression) {
                val name = expr.name
                return AssignmentExpression(name, value)
            } else if( expr is GetExpression) {
                return SetExpression(expr.obj, expr.name, value)
            }
            error(equals, "Invalid assignment target")
        }
        return expr
    }
    private fun or(): Expression {
        var expr = and()
        while(match(OR)) {
           val operator = previous()
           val right = and()
           expr = LogicalExpression(expr, operator, right)
        }
        return expr
    }
    private fun and(): Expression {
        var expr = equality()
        while(match(AND)) {
            val operator = previous()
            val right = equality()
            expr = LogicalExpression(expr, operator, right)
        }
        return expr
    }
    private fun equality(): Expression {
        var expr = comparison()
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = BinaryExpression(expr, operator, right)
        }
        return expr
    }
    private fun comparison(): Expression {
        var expr = term()
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = BinaryExpression(expr, operator, right)
        }
        return expr
    }
    private fun term(): Expression {
        var expr = factor()
        while(match(MINUS, PLUS)) {
            val operator = previous()
            val right = term()
            expr = BinaryExpression(expr, operator, right)
        }

        return expr
    }
    private fun factor(): Expression {
        var expr = unary()
        while(match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = BinaryExpression(expr, operator, right)
        }
        return expr
    }
    private fun unary(): Expression {
        if(match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return UnaryExpression(operator, right)
        }
        return call()
    }
    private fun call(): Expression {
        var expr = primary()
        while(true) {
            if(match(LEFT_PAREN)) expr = finishCall(expr)
            else if(match(DOT)) {
                val name = consume(IDENTIFIER, "Expected property name after '.'.")
                expr = GetExpression(expr, name)
            }
            else break
        }
        return expr
    }
    private fun finishCall(callee: Expression): Expression {
        val arguments: ArrayList<Expression> = arrayListOf()
        if(!check(RIGHT_PAREN)) {
            do {
                if(arguments.size > 254 ) error(peek(), "Can't have more than 255 arguments")
                arguments.add(expression())
            } while(match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expected ')' after function arguments")
        return CallExpression(callee, paren, arguments)
    }
    private fun primary() : Expression {
        if(match(FALSE)) return LiteralExpression(false)
        if(match(TRUE)) return  LiteralExpression(true)
        if(match(NIL)) return LiteralExpression(null)
        if(match(NUMBER, STRING)) return LiteralExpression(previous().literal)
        if(match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return GroupingExpression(expr)
        }
        if(match(IDENTIFIER)) return VariableExpression(previous())
        if(match(THIS)) return ThisExpression(previous())
        if(match(SUPER)) {
            val keyword = previous()
            consume(DOT, "Expected '.' after 'super'.")
            val method = consume(IDENTIFIER,"Expected method name.")
            return SuperExpression(keyword, method)
        }
        throw error(peek(), "Expected statement.")
    }
    private fun block(): ArrayList<Statement> {
        val statements:ArrayList<Statement> = arrayListOf()
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration()!!)
        }
        consume(RIGHT_BRACE, "Expected '}' after block")
        return statements
    }
    private fun match(vararg types: TokenType): Boolean {
        for(type in types) {
            if(check(type)) {
                advance()
                return true
            }
        }
        return false
    }
    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }
    private fun advance(): Token {
        if(!isAtEnd()) current++
        return previous()
    }
    private fun isAtEnd(): Boolean = peek().type == EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token = if(check(type)) advance() else throw error(peek(), message)

    private fun error(token: Token, message: String): ParseError {
        com.kuollutkissa.klox.error(token, message)
        return ParseError()
    }
    private fun synchronise() {
        advance()
        while(!isAtEnd()) if(previous().type == SEMICOLON) return
        when(peek().type) {
            CLASS -> {}
            FUN -> {}
            VAR -> {}
            FOR -> {}
            IF -> {}
            WHILE -> {}
            RETURN -> return
            else -> {}
        }
        advance()
    }
}

class ParseError : RuntimeException()