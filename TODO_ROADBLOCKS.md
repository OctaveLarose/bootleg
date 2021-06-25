# Obstacles to getting it to run with any calltrace file

## 1.
Inheritance not taken into account, and can't be inferred from just a CT file.
Sure, I can detect a super() constructor call if a constructor is called from the context of another constructor, but that doesn't necessarily mean
the caller extends the callee (it could also just be having the class be one of its fields / necessary for the caller's setup)


- Consequence 1: no class inherits from any other class.

- Cons. 2: the parent class will be empty, as all its methods will be considered to belong only to its subclasses who actually call them.

- Cons. 3: if the code generator needs to take a class as parameter and call a method from it (a common case, I believe),
it'll fail since it won't realize the class has

Bandage solution: Ignoring inheritance entirely and for cons. 3, just instantiating a new subclass.

Possible long-term solution: No way around integrating more data about the program's structure, which I guess always was inevitable. Most importantly, a tree hierarchy for the classes.
This means finding new tools / making one ourselves, and finding an adequate file format.

---

## 2.
The system doesn't know whether it should instantiate a new class, or use a method parameter/field when it needs to call another class' method.

- Consequence: Can't emulate a method's behaviour very accurately if I can't know which class to call, or if one needs to be instantiated.

Note: this may be a minor problem we can ignore, like it being easily inferred from parameters/fields 95% of the time.

Bandage solution: Checking if some arguments have the wanted type and call the methods from them then, then checking fields.
BUT arguments / maybe fields will often call a parent class which I can't handle right now...
So extra bandage solution: roadblock 1's bandage solution

Possible long-term solution: ??? Probably ignoring it, I can't think of any good solutions. Oops.
But probably yet more info about the code, but... which, and with what format?

## 3.
No accounting for overloaded methods. Only the method name is taken into account, not the method's full signature.

- Consequence : the system probably will get confused if it encounters overloaded methods, and probably ignore one altogether
if it doesn't straight up doesn't work. Same for several constructors.


Solution: Frankly, it's not a hard fix, it just means modifying the system to fetch methods from ClassBuilder objects
using more than just their names / if using just their names, returning a list of methods that all have said name.
This means relying on their signatures instead of just their names and tweaking the system accordingly.

However, that means finding a format to represent a method (most likely the ASM lib's, like `(Ljava/lang/Object;)Z Sieve.verifyResult`)
and do a bunch of tweaking that isn't needed right now as the programs I test my system with don't use
overloaded methods. That's more a TODO for later than an actual complex question.

# Obstacles to a realistic program

- No information about the classes' packages means they all end up in the same package.