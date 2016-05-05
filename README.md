# PREMON

[Premon](http://premon.fbk.eu) is a [Lemon model](http://lemon-model.net/index.php) Extension for Exposing Predicate Models as Linked Data.

## Installation

PREMON is released using the Maven paradigm. To install it, just type into the shell:
* `git clone https://github.com/dkmfbk/premon.git`
* `cd premon`
* `git checkout -b develop origin/develop` (to use the latest code from the `develop` branch)
* `mvn clean package -DskipTests -Prelease`
* `cd target`
* `tar xf premon-*-bin.tar.gz #where * refers to the version`
* `cd premon`

Now you can start the tool by simply use the `./premonitor` command.

## Usage

```
usage: ./premonitor [-b <PATH>] [-D] [-f <FMTS>] [-h] [-i <FOLDER>] [-m] [-p
       <FILE>] [-r] [-s <LEMMA>] [-V] [-v] [--wordnet <FILE>]
       [--wordnet-sensekeys <FILE>] [-x]

Transform linguistic resources into RDF

  -b,--output-base <PATH>        Output base path/name (default 'premon')
  -D,--verbose                   enable verbose output
  -f,--output-formats <FMTS>     Comma-separated list of output formats (default
                                 'tql.gz')
  -h,--help                      display this help message and terminate
  -i,--input <FOLDER>            input folder (default .)
  -m,--omit-filter-mappings      Omit filtering illegal mappings referring to
                                 non-existing conceptualizations (faster)
  -p,--properties <FILE>         Property file (default premonitor.properties)
  -r,--omit-owl2rl               Omit OWL2RL reasoning (faster)
  -s,--single <LEMMA>            Extract single lemma (apply to all resources)
  -V,--very verbose              enable very verbose output
  -v,--version                   display version information and terminate
     --wordnet <FILE>            WordNet RDF triple file (default:
                                 wordnet-3.1/wn31.nt.gz)
     --wordnet-sensekeys <FILE>  WordNet senseKey mapping
  -x,--omit-stats                Omit generation of statistics (faster)
```

All the options are optional and the default configuration, if option `-p` is omitted, is contained in the included file `premonitor.properties`. In general it suffices to populate the sub-directories under `resources` with the data that you want to convert, and then execute `./premonitor`.

Here is the list of available sub-directories of `resources` with the indication of what to place under each of them:

  * `framenet-1.5` - put here the contents of the `fndata-1.5` folder in the archive obtainable from this  [page](https://framenet.icsi.berkeley.edu/fndrupal/framenet_request_data)
  * `framenet-1.6` - put here the contents of the `fndata-1.6` folder in the archive obtainable from this [page](https://framenet.icsi.berkeley.edu/fndrupal/framenet_request_data)
  * `nombank-1.0` - put here the contents of the `nombank.1.0.tgz` archive downloadable from this [link](http://nlp.cs.nyu.edu/meyers/nombank/nombank.1.0.tgz) in this [page](http://nlp.cs.nyu.edu/meyers/NomBank.html)
  * `pm-1.3` - put here the content of the `PredicateMatrix.v1.3.tar.gz` archive downloadable from this [link](http://adimen.si.ehu.es/web/files/PredicateMatrix/PredicateMatrix.v1.3.tar.gz) in this [page](http://adimen.si.ehu.es/web/PredicateMatrix/)
  * `propbank-1.7` - put here the contents of the `propbank-1.7.tar.gz' archive downloadable from this [link](http://verbs.colorado.edu/verb-index/pb/propbank-1.7.tar.gz) in this [page](http://verbs.colorado.edu/verb-index/index.php)
  * `propbank-2.1.5` - put here the contents of the `v2.1.5.tar.gz' archive downloadable from this [link](https://github.com/propbank/propbank-frames/archive/v2.1.5.tar.gz) in this [page](https://github.com/propbank/propbank-frames/releases/tag/v2.1.5)
  * `semlink-1.2.2c` - put here the content of the `1.2.2c.zip` archive downloadable from this [link](https://verbs.colorado.edu/semlink/versions/1.2.2c.zip) in this [page](https://verbs.colorado.edu/semlink/)
  * `verbnet-3.2` - put here the contents of the `verbnet-3.2.tar.gz` archive downloadable from this  [link](http://verbs.colorado.edu/verb-index/vn/verbnet-3.2.tar.gz) in this [page](http://verbs.colorado.edu/verbnet_downloads/downloads.html)
  * `wordnet-3.0` - put here the contents of the `WordNet-3.0.tar.gz` archive downloadable from this [link](http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz) in this [page](https://wordnet.princeton.edu/wordnet/download/current-version/)
  * `wordnet-3.1` - put here the file `wn31.nt.gz` downloadable from this [link](http://wordnet-rdf.princeton.edu/wn31.nt.gz) in this [page](http://wordnet-rdf.princeton.edu/) (do not extract)
