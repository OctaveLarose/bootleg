# code_generator
Code generation side of my MsC thesis for the University of Kent (2021).

The actual AST manipulation/code generation/exports rely on [JavaParser](https://github.com/javaparser/javaparser).

# Usage
`--test`, or no flags, will generate a proof of concept program comprised of a few classes
doing basic operations.

`--ct-file FILENAME` will generate a program based off the calltrace file it received as input. 
Examples of the expected calltrace file format are present in `input_data/`, 
and is not specified explicitly anywhere as of right now as it's subject to change.

The exported code is generated in the `code_output/` directory. 
While there is a `build.gradle` file in it, it should need to be modified to be able to run the generated program. 
Allowing it to run more easily needs to be possible in the future, 
but isn't a priority as I have several features I need to implement beforehand.

# Current features

TODO. Well underway and can generate programs from calltrace files, but there's much to improve and implement before
writing this list.
