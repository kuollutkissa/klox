package com.kuollutkissa.klox

abstract class Statement() {
    abstract fun <R> accept(vis: StatementVisitor<R>): R
}

class ExpressionStatement(val expression: Expression): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitExpressionStatement(this)
    }

}

class VarStatement(val name: Token, val initializer: Expression?): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitVarStatement(this)
    }

}
class BlockStatement(val statements: ArrayList<Statement>): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitBlockStatement(this)
    }

}
class IfStatement(val condition:Expression, val thenBranch: Statement, val elseBranch: Statement?) : Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitIfStatement(this)
    }
}

class WhileStatement(val condition: Expression, val body: Statement): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitWhileStatement(this)
    }
}
class FunctionStatement(val name: Token, val params: ArrayList<Token>, val body: ArrayList<Statement>): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitFunctionStatement(this)
    }
}
class ReturnStatement(val keyword: Token, val value: Expression?) : Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitReturnStatement(this)
    }
}
class ClassStatement(val name: Token, val superclass: VariableExpression?, val methods: ArrayList<FunctionStatement>): Statement() {
    override fun <R> accept(vis: StatementVisitor<R>): R {
        return vis.visitClassStatement(this)
    }
}
interface StatementVisitor<R> {
    fun visitExpressionStatement(stmt: ExpressionStatement): R
    fun visitVarStatement(stmt: VarStatement): R
    fun visitBlockStatement(stmt: BlockStatement): R
    fun visitIfStatement(stmt: IfStatement): R
    fun visitWhileStatement(stmt: WhileStatement): R
    fun visitFunctionStatement(stmt: FunctionStatement): R
    fun visitReturnStatement(stmt: ReturnStatement): R
    fun visitClassStatement(stmt: ClassStatement): R
}