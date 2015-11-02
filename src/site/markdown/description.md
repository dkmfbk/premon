PreMOn in a nutshell
===

#### What is PreMOn?

PreMOn (Predicate Model for Ontologies) provides an [OWL ontology](ontology/core.html) for modelling predicates with their arguments, selectional constraints, predicate and argument relations and the alignments between predicates and arguments in different resources. This ontology extends [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) (Lexicon Model for Ontologies) in its latest version proposed by the [Ontology-Lexica Community Group at W3C](https://www.w3.org/community/ontolex/), which provides the backbone for relating predicates to the lexical entries evoking them (as well to their subcategorization frames).

PreMON provides also a number of freely-available, interlinked [RDF datasets](download.html) for [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html) and [NomBank](http://nlp.cs.nyu.edu/meyers/NomBank.html) ([VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html) and [FrameNet](https://framenet.icsi.berkeley.edu/fndrupal/) under construction) predicates described according to the PreMOn ontology, which are published online as Linked Open Data.


#### Benefits

Compared to the current situation where each predicate model has its own proprietary XML format, the use of a common RDF vocabulary such as PreMOn brings several benefits to the tasks relying on those models, such as information extraction, question answering and natural language generation.

  * possibility to represent the peculiarities of each resource, while providing at the same time (using RDFS/OWL subclass/subproperty primitives) an homogeneous representation that abstracts from them;
  * easy data integration, leveraging the use of stable URIs for denoting predicates, arguments and other model elements;
  * standardized, expressive query support using SPARQL, e.g., for retrieving and analyzing the predicates for a certain entry or the alignments for a given predicate;
  * support for automated reasoning (RDFS/OWL or rule-based);
  * publication as Linked Open Data (LOD) for ease of access and interlinking with other resources, and for enabling third parties to contribute and extend predicate resources in a decentralized way (e.g., with new alignments).

These capabilities are particulary relevant in the context of Semantic Web (SW), where predicate models are increasingly used for information extraction, e.g., in tools such as [FRED](http://stlab.istc.cnr.it/stlab/FRED), [PIKES](http://pikes.fbk.eu/), or as the starting point from which to derive ontologies of extracted knowledge, such as [FrameBase](http://framebase.org/) and the [ESO ontology](http://www.newsreader-project.eu/results/event-and-situation-ontology/) both derived from FrameNet.
