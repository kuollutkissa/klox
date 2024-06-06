package com.kuollutkissa.klox

import java.io.File
import kotlin.system.exitProcess

var hadError: Boolean = false
var hadRuntimeError: Boolean = false
val interpreter = Interpreter()
fun main(args: Array<String>) {
    if(args.size > 1) {
        println("Usage: klox [script.lox]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

private fun runFile(path: String) {
    val source = File(path).readText()
    run(source)
    if(hadError) exitProcess(65)
    if(hadRuntimeError) exitProcess(70)
}
private fun runPrompt() {
    while(true) {
        print("~klox> ")
        val line: String = readLine() ?: break
        if(line == "") break
        run(line)
        hadError = false
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: ArrayList<Token> = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()
    if(hadError) return
    val resolver = Resolver(interpreter)
    resolver.resolve(statements)
    if(hadError) return
    interpreter.interpret(statements)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}
private fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error $where: $message")
}

fun error(token: Token, message: String) {
    if(token.type == TokenType.EOF) {
        report(token.line, "at end", message)
    } else {
        report(token.line, "at ${token.lexeme}", message)
    }
}
fun runtimeError(error: RuntimeError) {
    System.err.println("${error.message}\n[${error.token.line}]")
    hadRuntimeError = true
}