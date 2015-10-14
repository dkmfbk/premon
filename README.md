# PropLem

PropLem is a tool to convert PropBank-style resources to RDF, using the [Lemon model](http://lemon-model.net/index.php).

```
usage: ./proplem [-D] [-e] [--framenet <FILE>] [-h] [-i <FOLDER>] [-l
       <ISO-CODE>] [--namespace <URI>] [-o] [-s <LEMMA>] [--use-wn-lex] [-v]
       [-V] [--verbnet <FILE>] [-w <FILE>] [--wordnet <FILE>]

Transform a ProbBank instance into RDF

  -D,--verbose          enable verbose output
  -e,--examples         Extract examples
     --framenet <FILE>  FrameNet RDF triple file
  -h,--help             display this help message and terminate
  -i,--input <FOLDER>   input folder
  -l,--lang <ISO-CODE>  Language for literals, default en
     --namespace <URI>  Namespace, default http://pb2rdf.org/
  -o,--ontonotes        Specify that this is an OntoNotes version of ProbBank
  -s,--single <LEMMA>   Extract single lemma
     --use-wn-lex       Use WordNet LexicalEntries when available
  -v,--version          display version information and terminate
  -V,--very verbose     enable very verbose output
     --verbnet <FILE>   VerbNet RDF triple file
  -w,--output <FILE>    Output file
     --wordnet <FILE>   WordNet RDF triple file
```

Description of the options:

* `--input` (mandatory) the input folder, containing the PropBank XMLs files
* `--output` (mandatory) the output file; the format will depend on the extension (see the `@read` processor from the [RDFpro documentation page](http://rdfpro.fbk.eu/usage.html) for a list of output/compression formats)
* `--examples` performs the examples extraction too
* `--framenet` specify the FrameNet RDF file (downloadable from the [Lemon Uby lexica page](http://lemon-model.net/lexica/uby/))
* `--verbnet` specify the VerbNet RDF file (downloadable from the [Lemon Uby lexica page](http://lemon-model.net/lexica/uby/))
* `--wordnet` specify the WordNet RDF file (downloadable from the [Lemon Uby lexica page](http://lemon-model.net/lexica/uby/))
* `--lang` the language for the literal strings (such as labels and examples), default is `@en`
* `--namespace` namespace used by the tool, default `http://pb2rdf.org/`
* `--single` extracts RDF information from a single PropBank lemma (look, eat, be, ...)
* `--use-wn-lex` do not create from scratch the WordNet entries and forms, but use the ones from the `--wordnet` option when available (when the entry does not exist in the resource, it is created anyway)
* `--ontonotes` specify to the tool that you are using the PropBank version included in the OntoNotes release
