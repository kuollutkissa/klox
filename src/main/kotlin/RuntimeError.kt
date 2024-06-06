package com.kuollutkissa.klox

class RuntimeError(val token: Token, message: String): RuntimeException(message)


