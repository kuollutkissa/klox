package com.kuollutkissa.klox

class LoxFunction(private val declaration: FunctionStatement, private val closure: Environment, val isInitializer: Boolean) : LoxCallable {
    override fun arity(): Int = declaration.params.size

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any? {
        val environment = Environment(closure)
        for(i in 0..<declaration.params.size) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }
        try { interpreter.executeBlock(declaration.body, environment) } catch(ret: Return) {
            if(isInitializer) return closure.getAt(0, "this")
            return ret.value
        }
        if(isInitializer) return closure.getAt(0, "this")
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}
interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any?
    fun arity(): Int
}