# KLox
KLox is a kotlin-based implementation of a language almost entirely based on Robert Nystrom's Lox written in kotlin for the JVM. Runs on Java 21. 
Notable changes to Nystrom's specification are:
- use of `:` instead of `<` for inheritance
- the `print` statement being replaced by two native functions, `print()` and `println()`
- presence of the `readln()` function

Check out [Crafting Interpreters](http://craftinginterpreters.com)!
## Example code
```lox
class Foo {
	ee(p) {
		println(p + this.t);
	}
}

var x = Foo();
x.t = "se";
x.ee("aa");

```
