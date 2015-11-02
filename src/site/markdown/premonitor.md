PreMOnitor tool
===

**PreMOnitor** is a Java command line tool for converting the frame files distributed with various predicate resources in the premon RDF representation.


Download the last stable version (easy way)
---

The tool is distributed using the git/Maven paradigms. This means that you can easily download and install the last version (stable or unstable).
To install the last stable version of the tool, just download it from here (missing link).


Install from sources (not-so-easy way)
---

To compile the tool from sources, just open a terminal (for Linux/Mac OS) and type these commands:


```
git checkout https://github.com/dkmfbk/premon.git
cd premon

# Uncomment the following command to use the last/unstable version of PreMOn
# git checkout -b develop origin/develop

mvn clean package -DskipTests -Prelease
cd target
tar xzf premon-*-bin.tar.gz # the * refers to the version
cd premon

```

You can now start the program by using the `./premon` executable.


Run the tool
---

Starting from a downloaded or compiled version, you should use the `./premon` executable to run the tool.
The output file location (where the tool would save the triples) is the only mandatory parameter.

The programm will then look for the `pb` folder (containing the PropBank XMLs files).
You can overwrite this behavior by setting the `pb-folder` and `input` options.

This is the complete list of options (that the tool print when the `-h` option is set):

```
usage: ./premonitor [-D] [-e] [-h] [-i <FOLDER>] [-l <ISO-CODE>] [-o]
       [--pb-folder <FOLDER>] [-s <LEMMA>] [-v] [-V] [-w <FILE>]

Transform linguistic resources into RDF

  -D,--verbose              enable verbose output
  -e,--examples             Extract examples
  -h,--help                 display this help message and terminate
  -i,--input <FOLDER>       input folder (default .)
  -l,--language <ISO-CODE>  Language for literals, default en
  -o,--ontonotes            Specify that this is an OntoNotes version of
                            ProbBank
     --pb-folder <FOLDER>   PropBank frames folder
  -s,--single <LEMMA>       Extract single lemma
  -v,--version              display version information and terminate
  -V,--very verbose         enable very verbose output
  -w,--output <FILE>        Output file (mandatory)
```