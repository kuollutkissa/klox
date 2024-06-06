package com.kuollutkissa.klox

class Environment(val enclosing: Environment?) {
    private val values: HashMap<String, Any?> = hashMapOf()
    constructor() : this(null)

    fun define(name: String, value: Any?) {
        values[name] = value
    }
    fun get(name: Token): Any? {
        if(values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
    fun assign(name: Token, value: Any?) {
        if(values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        if(enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }
    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }
    fun ancestor(distance:Int): Environment {
        var env: Environment = this
        for(i in 0..<distance) {
            env = env.enclosing!!
        }
        return env
    }
}