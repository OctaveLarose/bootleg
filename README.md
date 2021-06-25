# code_generator
Code generation side of my MsC thesis for the University of Kent (2021).

Aims to generate benchmarks automatically, ideally focusing specifically on creating huge codebases.
The current focus (as of 25/06/21) is simulating the [AWFY benchmarks](https://github.com/smarr/are-we-fast-yet), though,
which aren't very large.

AST manipulation/code generation/exports in my code all rely on [JavaParser](https://github.com/javaparser/javaparser).

Data fetching is done using [DiSL](https://gitlab.ow2.org/disl/disl), with my other project 
[disl_metrics_fetcher](https://github.com/OctaveLarose/disl_metrics_fetcher). 
Since it relies on the [ASM framework](https://asm.ow2.io/), the formats defined in the latter's documentation
tend to be the ones I use (for instance, for defining method signatures as human readable strings).

# Usage
`--test`, or no flags, will generate a proof of concept program comprised of a few classes
doing basic operations.

`--ct-file FILENAME` will generate a program based off the calltrace file it received as input. 
Examples of the expected calltrace file format are present in `input_data/`, 
and is not specified explicitly anywhere as of right now as it's subject to change.

The exported code is generated in the `code_output/` directory. 
While there is a `build.gradle` file in it, it should need to be modified to be able to run the generated program. 
Allowing it to run more easily needs to be possible in the future, 
but isn't a priority for me right now.

# Current features

TODO. Well underway and can generate programs from calltrace files, but there's much to improve and implement before
writing this list.
