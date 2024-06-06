package com.kuollutkissa.klox

object Clock: LoxCallable {
    override fun arity(): Int = 0
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Double {
        return System.currentTimeMillis()/ 1000.0
    }

    override fun toString(): String = "<native fn:clock>"

}

object Print: LoxCallable {
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any? {
        val arg = arguments[0]
        print(stringify(arg))
        return null
    }

    override fun arity(): Int = 1

    override fun toString(): String = "<native fn:print>"
}

object Println: LoxCallable {
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any? {
        val arg = arguments[0]
        println(stringify(arg))
        return null
    }

    override fun arity(): Int = 1

    override fun toString(): String = "<native fn:print>"

}

private fun stringify(any: Any?): String {
    if(any == null) return "nil"
    if(any is Double) {
        var text = any.toString()
        if(text.endsWith(".0")) text = text.substring(0,text.length-2)
        return text
    }
    return any.toString()
}

object ReadLn: LoxCallable {
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Any {
        return readln()
    }

    override fun arity(): Int = 0

    override fun toString(): String = "<native fn:readln>"
}

object ParseNum: LoxCallable {
    override fun call(interpreter: Interpreter, arguments: ArrayList<Any?>): Double {
        return (arguments[0] as String).toDouble()
    }

    override fun arity(): Int = 1
    
    override fun toString(): String = "<native fn:parseNum>"
}