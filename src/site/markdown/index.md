<div class="well sidebar" id="well-home">
    <h1>
        <img src='images/premon-big.png' alt='PreMOn' title='PreMOn' />
    </h1>

    <p class='title2'>
        PREdicate Model for ONtologies
    </p>

    <p class='title2'>
        <a class="btn btn-primary btn-large" href="ontology.html">Ontology reference</a>
        <a class="btn btn-primary btn-large" href="download.html">Data download</a>
        <a class="btn btn-primary btn-large" href="navigator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Navigator&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>
        <a class="btn btn-primary btn-large" href="query.html">SPARQL endpoint</a>
    </p>
</div>

---------------------------------------

### About

**PreMOn** (Predicate Model for Ontologies) is a linguistic Linked Data resource representing **predicate** models such as PropBank, NomBank, VerbNet and FrameNet.
PreMOn consists of an **OWL ontology** for modelling **semantic classes** with their **roles**, mappings across different predicate models, and annotations, based on **lemon** (Lexicon Model for Ontologies); and of a set of **RDF datasets** for the main predicate models described using this ontology.

[learn more...](overview.html)

### Features

- modular [OWL ontology](ontology.html) (core module with extension for each predicate model)
- based on [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) by the [Ontology-Lexica Community Group](https://www.w3.org/community/ontolex/) at W3C
- [dataset](download.html) with [VOID](http://www.w3.org/TR/void/) statistics available for [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html) (1.7 and 2.1.5), [NomBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html) (1.0), [VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html) (3.2), [FrameNet](https://framenet.icsi.berkeley.edu/fndrupal/) (1.5 and 1.6), [SemLink](https://verbs.colorado.edu/semlink/) (1.2.2c), and [PredicateMatrix](http://adimen.si.ehu.es/web/PredicateMatrix) (1.3)
- SPARQL [endpoint](query.html) with web interface
- URI dereferencing and navigation using [lodview](https://github.com/dvcama/LodView)
- [premonitor](premonitor.html) software for converting original resources

### News

- 2016-06-20 Browse PreMOn with the [Navigator](navigator)
- 2016-05-04 PreMOn 2016.b released
- 2016-05-02 Added PredicateMatrix 1.3
- 2016-04-08 PreMOn is on [DataHub](https://datahub.io/dataset/premon)
- 2016-03-10 [Manuscript](https://dkm-static.fbk.eu/people/rospocher/files/pubs/2016lrec1.pdf) of the LREC2016 paper available
- 2016-03-10 Added FrameNet, VerbNet, SemLink
- 2016-01-26 Paper accepted at [LREC2016](http://lrec2016.lrec-conf.org/en/).
- 2015-11-12 Updated datasets (added missing inferences)
- 2015-10-28 Published ontology, PropBank and NomBank datasets
