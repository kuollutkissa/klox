package com.kuollutkissa.klox

abstract class Expression {
    abstract fun <R> accept(vis: ExpressionVisitor<R>): R
}

class BinaryExpression(val left: Expression, val operator: Token, val right: Expression) : Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitBinaryExpression(this)
    }
}

class GroupingExpression(val expression: Expression) : Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitGroupingExpression(this)
    }
}

class LiteralExpression(val value: Any?) : Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitLiteralExpression(this)
    }
}

class UnaryExpression(val operator: Token, val right: Expression) : Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitUnaryExpression(this)
    }
}
class VariableExpression(val name: Token) : Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitVariableExpression(this)
    }
}
class AssignmentExpression(val name:Token, val value: Expression): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitAssignmentExpression(this)
    }
}
class LogicalExpression(val left: Expression, val operator: Token, val right: Expression): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitLogicalExpression(this)
    }
}
class CallExpression(val callee:Expression, val paren: Token, val arguments: ArrayList<Expression>): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitCallExpression(this)
    }
}
class GetExpression(val obj: Expression, val name: Token): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitGetExpression(this)
    }
}
class SetExpression(val obj: Expression, val name: Token, val value: Expression): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitSetExpression(this)
    }
}

class ThisExpression(val keyword: Token): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitThisExpression(this)
    }
}
class SuperExpression(val keyword: Token, val method: Token): Expression() {
    override fun <R> accept(vis: ExpressionVisitor<R>): R {
        return vis.visitSuperExpression(this)
    }
}
interface ExpressionVisitor<R> {
    fun visitBinaryExpression(expr: BinaryExpression): R
    fun visitGroupingExpression(expr: GroupingExpression): R
    fun visitLiteralExpression(expr: LiteralExpression): R
    fun visitUnaryExpression(expr: UnaryExpression):R
    fun visitVariableExpression(expr: VariableExpression): R
    fun visitAssignmentExpression(expr: AssignmentExpression):R
    fun visitLogicalExpression(expr: LogicalExpression): R
    fun visitCallExpression(expr: CallExpression): R
    fun visitGetExpression(expr: GetExpression): R
    fun visitSetExpression(expr: SetExpression): R
    fun visitThisExpression(expr: ThisExpression): R
    fun visitSuperExpression(expr: SuperExpression): R
}
