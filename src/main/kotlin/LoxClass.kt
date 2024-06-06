package com.kuollutkissa.klox

class LoxClass(val name: String, val superclass: LoxClass?, private val methods: HashMap<String, LoxFunction>): LoxCallable {
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any  {
        val instance =  LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity(): Int = 0
    override fun toString(): String {
        return name
    }
    fun findMethod(name: String): LoxFunction? {
        if(methods.containsKey(name)) return methods[name]
        return superclass?.findMethod(name)
    }
}