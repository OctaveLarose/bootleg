# bootleg
Creates a program with similar behaviour and architecture as an input one, given a calltrace of the latter 
and additional information about its contents.

Code generation side of my MsC thesis for the University of Kent (2021).
Aims to generate benchmarks automatically, ideally focusing specifically on creating huge codebases.
The current focus is simulating the [AWFY benchmarks](https://github.com/smarr/are-we-fast-yet), though,
which aren't very large.

AST manipulation/code generation/exports in my code all rely on [JavaParser](https://github.com/javaparser/javaparser).

Data fetching is done using [DiSL](https://gitlab.ow2.org/disl/disl), with my other project 
[disl_metrics_fetcher](https://github.com/OctaveLarose/disl_metrics_fetcher). 
Since it relies on the [ASM framework](https://asm.ow2.io/), the formats defined in the latter's documentation
tend to be the ones I use (for instance, for defining method signatures as human readable strings).

# Usage
`--test` will generate a proof of concept program for the code generation, comprised of a few classes
doing basic operations.

`--ct-file FILENAME` will generate a program based off the calltrace file it received as input. 
Examples of the expected calltrace file format are present in `input_data/`, 
and is not specified explicitly anywhere as of right now as it's susceptible to change in the near future.

`--op-file FILENAME` ("operations file") can be used to provide additional information about the program.
As of now (08/06/21), this information corresponds to arithmetic bytecode operators called in order in each function. 

`--no-print-method-names` disables the default behaviour of adding a print statement to every method, 
to show they're actually being called and in which order they are.

---

The exported code is generated in the `code_output/` directory. 
While there is a `build.gradle` file in it, it should need to be modified to be able to run the generated program. 
Allowing it to run more easily needs to be possible in the future, 
but isn't a priority for me right now.

# Current features (last updated on 06/08/21)

- Can generate executable programs given a file containing a calltrace as input.
- Can take additional information about the methods' content in an auxiliary file.
- Method calls are handled in the same order as in the calltrace.
- Public/private/protected modifiers, static methods and calls to static methods accounted for.
- Generates dummy/random argument values depending on the primitive/object type.
- Method context aware, i.e uses local variables / parameter values as arguments whenever possible.
- Instantiates classes as needed to call public non-static methods.
- Exports all generated classes to `.java` files.
- Package structure is taken into account, including during the code exportation.
- Generates executable programs with no `NullPointerException` thrown.


# To be handled better (last updated on 06/08/21)

- inner classes. (currently instantiated as actual classes)
- static initializers. (currently replaced with static public methods)
- loading existing classes from existing files and modifying them. (not useful right now, but could be in the future)
- usage of fields, to some degree
- import statements instead of object full paths.
- the contents of the [TODO.md](TODO.md) file, in general.

