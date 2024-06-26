package com.kuollutkissa.klox

class LoxInstance(private val clazz: LoxClass) {
    private val fields = HashMap<String, Any?>()
    override fun toString(): String = "${clazz.name} instance"
    fun get(name: Token): Any? {
        if(fields.containsKey(name.lexeme)) return fields[name.lexeme]
        val method = clazz.findMethod(name.lexeme)
        if(method != null) return method.bind(this)
        throw RuntimeError(name, "Undefined property ${name.lexeme}.")
    }
    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}