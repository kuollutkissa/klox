package com.kuollutkissa.klox

import java.util.*

class Resolver(private val interpreter: Interpreter): ExpressionVisitor<Unit>, StatementVisitor<Unit> {
    private val scopes: Stack<HashMap<String, Boolean>> = Stack()
    private var currentFunction = FunctionType.NONE
    private var currentClass = ClassType.NONE
    override fun visitBinaryExpression(expr: BinaryExpression) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitGroupingExpression(expr: GroupingExpression) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpression(expr: LiteralExpression) {
    }

    override fun visitUnaryExpression(expr: UnaryExpression) {
        resolve(expr.right)
    }

    override fun visitVariableExpression(expr: VariableExpression) {
        if(!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) error(expr.name, "Can't reference local variable in its initialiser.")
        resolveLocal(expr, expr.name)
    }

    override fun visitAssignmentExpression(expr: AssignmentExpression) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitLogicalExpression(expr: LogicalExpression) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpression(expr: CallExpression) {
        resolve(expr.callee)
        for(arg in expr.arguments) {
            resolve(arg)
        }
    }

    override fun visitGetExpression(expr: GetExpression) {
        resolve(expr.obj)
    }

    override fun visitSetExpression(expr: SetExpression) {
        resolve(expr.value)
        resolve(expr.obj)
    }

    override fun visitThisExpression(expr: ThisExpression) {
        if(currentClass == ClassType.NONE) error(expr.keyword, "Can't use 'this' outside a class")
        resolveLocal(expr, expr.keyword)
    }

    override fun visitSuperExpression(expr: SuperExpression) {
        if(currentClass == ClassType.NONE) error(expr.keyword, "Can't use 'super' outside a class.")
        if(currentClass != ClassType.SUBCLASS) error(expr.keyword, "Can't use super in a class w/o a superclass.")
        resolveLocal(expr, expr.keyword)
    }

    override fun visitExpressionStatement(stmt: ExpressionStatement) {
        resolve(stmt.expression)
    }

    override fun visitVarStatement(stmt: VarStatement) {
        declare(stmt.name)

        if(stmt.initializer != null) resolve(stmt.initializer)
        define(stmt.name)
    }

    override fun visitBlockStatement(stmt: BlockStatement) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitIfStatement(stmt: IfStatement) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if(stmt.elseBranch != null) resolve(stmt.elseBranch)
    }

    override fun visitWhileStatement(stmt: WhileStatement) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visitFunctionStatement(stmt: FunctionStatement) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitReturnStatement(stmt: ReturnStatement) {
        if(currentFunction == FunctionType.NONE) error(stmt.keyword, "Can't return from top-level code.")
        if(stmt.value != null) {
            if(currentFunction == FunctionType.INITIALIZER) error(stmt.keyword, "Can't return a value from an init function.")
            resolve(stmt.value)
        }
    }

    override fun visitClassStatement(stmt: ClassStatement) {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS
        declare(stmt.name)
        define(stmt.name)
        if(stmt.superclass != null && stmt.name.lexeme == stmt.superclass.name.lexeme) error(stmt.superclass.name, "A class cannot inherit from itself")
        if(stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS
            resolve(stmt.superclass)
        }
        if(stmt.superclass != null) {
            beginScope()
            scopes.peek()["super"] = true
        }
        beginScope()
        scopes.peek()["this"] = true
        for(met in stmt.methods) {
            var decl = FunctionType.METHOD
            if(met.name.lexeme == "init") decl = FunctionType.INITIALIZER
            resolveFunction(met, decl)
        }
        currentClass = enclosingClass
        endScope()
        if(stmt.superclass != null) endScope()
    }

    fun resolve(statements: ArrayList<Statement>) {
        for(stmt in statements) {
            resolve(stmt)
        }
    }
    private fun resolve(statement: Statement) {
        statement.accept(this)
    }
    private fun resolve(expression: Expression) {
        expression.accept(this)
    }
    private fun beginScope() {
        scopes.push(HashMap())
    }
    private fun endScope() {
        scopes.pop()
    }
    private fun declare(name: Token) {
        if(scopes.isEmpty()) return
        val scope: HashMap<String, Boolean> = scopes.peek()
        if(scope.containsKey(name.lexeme)) error(name, "A variable with this name already exists in this scope.")
        scope[name.lexeme] = false
    }
    private fun define(name: Token) {
        if(scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }
    private fun resolveLocal(expr: Expression, name: Token) {
        for(i in 0..<scopes.size) {
            if(scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
        return
    }
    private fun resolveFunction(function: FunctionStatement, type: FunctionType) {
        beginScope()
        val enclosingFn = currentFunction
        currentFunction = type
        for(param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFn
    }
}


private enum class FunctionType {
    NONE,
    FUNCTION,
    METHOD,
    INITIALIZER
}
private enum class ClassType {
    NONE,
    CLASS,
    SUBCLASS
}