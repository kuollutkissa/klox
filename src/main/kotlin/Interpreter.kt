package com.kuollutkissa.klox


class Interpreter: ExpressionVisitor<Any?>, StatementVisitor<Unit> {
    private val globals = Environment()
    private val locals: HashMap<Expression, Int> = HashMap()
    private var environment = globals
    init {
        globals.define("clock", Clock)
        globals.define("print", Print)
        globals.define("println", Println)
        globals.define("readln", ReadLn)
        globals.define("parseNum", ParseNum)
    }
    fun interpret(statements: ArrayList<Statement>) {
        try {
            for(stmt in statements) {
                execute(stmt)
            }
        } catch(err: RuntimeError) {
            runtimeError(err)
        }
    }
    fun resolve(expr: Expression, depth: Int) {
        locals[expr] = depth
    }

    private fun lookUpVariable(name: Token, expr: Expression): Any? {
        val distance = locals[expr]
        return if(distance != null)
                environment.getAt(distance, name.lexeme)
                else return globals.get(name)

    }
    private fun execute(statement: Statement) {
        statement.accept(this)
    }
    override fun visitBinaryExpression(expr: BinaryExpression): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when(expr.operator.type) {
            TokenType.PLUS -> {
                if(left is Double && right is Double) {
                    left + right
                } else if(left is String && right is String) {
                    left + right
                } else throw RuntimeError(expr.operator, "Operands must be either two numbers or two strings") // unreachable
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            else -> null // unreachable
        }
    }

    override fun visitGroupingExpression(expr: GroupingExpression): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpression(expr: LiteralExpression): Any? {
        return expr.value
    }

    override fun visitUnaryExpression(expr: UnaryExpression): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, expr.right)
                -(right as Double)
            }
            TokenType.BANG -> !(isTruthy(right))
            else -> null // unreachable
        }
    }

    override fun visitVariableExpression(expr: VariableExpression): Any? {
        return lookUpVariable(expr.name, expr)
    }

    override fun visitAssignmentExpression(expr: AssignmentExpression): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if(distance != null)
            environment.assignAt(distance, expr.name, value)
        else
            globals.assign(expr.name, value)
        return value
    }

    override fun visitLogicalExpression(expr: LogicalExpression): Any? {
        val left = evaluate(expr.left)
        if(expr.operator.type == TokenType.OR) {
            if(isTruthy(left))  return left
        } else if(!isTruthy(left)) return left
        return evaluate(expr.right)
    }

    override fun visitCallExpression(expr: CallExpression): Any? {
        val callee = evaluate(expr.callee)
        val arguments:ArrayList<Any?>  = arrayListOf()
        for(arg in expr.arguments) {
            arguments.add(evaluate(arg))
        }
        if(callee !is LoxCallable)
            throw RuntimeError(expr.paren,  "Object is not callable")
        if(callee.arity() != arguments.size) throw RuntimeError(expr.paren,
            "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        return callee.call(this, arguments)
    }

    override fun visitGetExpression(expr: GetExpression): Any? {
        val obj = evaluate(expr.obj)
        if(obj is LoxInstance) {
            return obj.get(expr.name)
        }
        throw RuntimeError(expr.name, "Not an object.")
    }

    override fun visitSetExpression(expr: SetExpression): Any? {
        val obj = evaluate(expr.obj)
        if(obj !is LoxInstance) throw RuntimeError(expr.name, "Not an object.")
        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }
    private fun evaluate(expr: Expression): Any? {
        return expr.accept(this)
    }
    private fun isTruthy(any: Any?): Boolean {
        if (any == null) return false
        if (any is Boolean) return any
        return true
    }
    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if(a == null) return false
        return a == b
    }
    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if(operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }
    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if(left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers")
    }

    override fun visitThisExpression(expr: ThisExpression): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitSuperExpression(expr: SuperExpression): Any {
        val dist = locals[expr]!!
        val superclass = environment.getAt(dist, "this") as LoxClass
        val obj = environment.getAt(dist-1, "this") as LoxInstance
        val method = superclass.findMethod(expr.method.lexeme)
            ?: throw RuntimeError(expr.method, "Undefined property ${expr.method.lexeme}.")
        return method.bind(obj)
    }

    override fun visitExpressionStatement(stmt: ExpressionStatement) {
        evaluate(stmt.expression)
    }

    override fun visitVarStatement(stmt: VarStatement) {
        var value: Any? = null
        if(stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlockStatement(stmt: BlockStatement) {
        executeBlock(stmt.statements, Environment(environment))
    }
    fun executeBlock(stmt: ArrayList<Statement>, env: Environment)  {
        val containing = environment
        environment = env
        try {
            for(st in stmt) {
                execute(st)
            }
        } finally {
            environment = containing
        }

    }
    override fun visitIfStatement(stmt: IfStatement) {
        if(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhileStatement(stmt: WhileStatement) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitFunctionStatement(stmt: FunctionStatement) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitReturnStatement(stmt: ReturnStatement) {
        var value: Any? = null
        if(stmt.value != null) value = evaluate(stmt.value)
        throw Return(value)
    }

    override fun visitClassStatement(stmt: ClassStatement) {
        var superclass: Any? = null
        if(stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if(superclass !is LoxClass) throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
        }
        environment.define(stmt.name.lexeme, null)
        if(stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }
        val methods = HashMap<String, LoxFunction>()
        for(met in  stmt.methods) {
            val func = LoxFunction(met, environment, met.name.lexeme == "init")
            methods[met.name.lexeme] = func
        }

        val clazz = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)
        if(superclass != null) {
            environment = environment.enclosing!!
        }
        environment.assign(stmt.name, clazz)
    }

}