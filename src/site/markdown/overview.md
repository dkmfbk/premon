PreMOn overview
===


### What is PreMOn?

PreMOn (Predicate Model for Ontologies) provides an [OWL ontology](ontology.html) for modelling predicates with their arguments, selectional constraints, predicate and argument relations and the alignments between predicates and arguments in different resources. This ontology extends [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) (Lexicon Model for Ontologies) in its latest version proposed by the [Ontology-Lexica Community Group at W3C](https://www.w3.org/community/ontolex/), which provides the backbone for relating predicates to the lexical entries evoking them (as well to their subcategorization frames).

PreMON provides also a number of freely-available, interlinked [RDF datasets](download.html) for [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html) and [NomBank](http://nlp.cs.nyu.edu/meyers/NomBank.html) ([VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html) and [FrameNet](https://framenet.icsi.berkeley.edu/fndrupal/) under construction) predicates described according to the PreMOn ontology, which are published online as Linked Open Data.

Compared to the current situation where each predicate model has its own proprietary XML format, the use of a common RDF vocabulary such as PreMOn brings several benefits to the tasks relying on those models, such as information extraction, question answering and natural language generation.

  * possibility to represent the peculiarities of each resource, while providing at the same time (using RDFS/OWL subclass/subproperty primitives) an homogeneous representation that abstracts from them;
  * easy data integration, leveraging the use of stable URIs for denoting predicates, arguments and other model elements;
  * standardized, expressive query support using SPARQL, e.g., for retrieving and analyzing the predicates for a certain entry or the alignments for a given predicate;
  * support for automated reasoning (RDFS/OWL or rule-based);
  * publication as Linked Open Data (LOD) for ease of access and interlinking with other resources, and for enabling third parties to contribute and extend predicate resources in a decentralized way (e.g., with new alignments).

These capabilities are particulary relevant in the context of Semantic Web (SW), where predicate models are increasingly used for information extraction, e.g., in tools such as [FRED](http://stlab.istc.cnr.it/stlab/FRED), [PIKES](http://pikes.fbk.eu/), or as the starting point from which to derive ontologies of extracted knowledge, such as [FrameBase](http://framebase.org/) and the [ESO ontology](http://www.newsreader-project.eu/results/event-and-situation-ontology/) both derived from FrameNet.


### PreMOn ontology

PreMOn ontology consists of a core module defining the main abstractions for modelling predicates, and of additional modules specific to each predicate model included in PreMOn.

The core module (see figure) extends [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) and thus inherits the capability to represent lexical entries (class [`LexicalEntry`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry) with their associated forms, and to relate lexical entries to the ontological entities they denote (classes, properties, individuals) using the [`LexicalSense`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#LexicalSense) reified relation.
Using classes [`SyntacticFrame`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Syntactic_Frame) and [`SyntacticArgument`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Syntactic_Argument), lemon also allows modelling the subcategorization frames of a lexical entry (e.g., [VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html) frames) and mapping syntactic arguments (e.g., subject, direct/indirect object) to the subject/object of an ontological property via [`OntoMap`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#OntoMap) instances.
Apart from mapping to an ontology, which provide the extensional (formal) interpretation of lexical entries, lemon supports mapping entries to [`LexicalConcept`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept)s (subclass of SKOS [`Concept`](http://www.w3.org/TR/skos-reference/#concepts)), each denoting an intensional (∼informal) meaning evoked by a set of lexical entries.
Example of lexical concepts are WordNet synsets, whose semantics is not formally encoded in an ontology.

<div style="text-align: center; padding-top: 20px; padding-bottom: 20px">
<img src="images/core.svg" alt="PreMOn ontology, core module" style="width: 70%"/>
</div>

PreMOn extends lemon by introducing classes [`Predicate`](http://premon.fbk.eu/ontology/core#Predicate), [`SemanticArgument`](http://premon.fbk.eu/ontology/core#SemanticArgument) and [`SemanticType`](http://premon.fbk.eu/ontology/core#SemanticType) as subclasses of `LexicalConcept`, as they are seen as informal concepts rather than well defined classes and properties of a formal ontology.
A `Predicate` has a number of `SemanticArgument`s and both `Predicate` and `SemanticArgument` are associable to [`SemanticType`](http://premon.fbk.eu/ontology/core#SemanticType)s expressing selectional constraints and other semantic restrictions in predicate models.
`SemanticArgument`s are defined locally to predicates, so VerbNet ‘agent’ is represented as multiple arguments, one for each predicate it occurs in and with each argument linked to its specific selectional constraints (if any);
property role is used to link these arguments to the same VerbNet [`SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole) constant.
Properties [predicateRel](http://premon.fbk.eu/ontology/core#predicateRel), [argumentRel](http://premon.fbk.eu/ontology/core#argumentRel), [typeRel](http://premon.fbk.eu/ontology/core#typeRel), and their resource-specific sub-properties, are introduced to express the relations between elements as each level, such as sub-typing and predicate and argument inheritance.
Being `LexicalConcept`s, `SemanticType`s, `Predicate`s, and `SemanticArgument`s inherit the link to lexical entries as well as the link (via [`isConceptOf`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Concept)) to the ontological entities formalizing them.

Alignments between different predicate models are generally defined in terms of `Predicate`/`LexicalEntry` pairs and `SemanticArgument`/`LexicalEntry` pairs. To model these pairs we introduce a [`Conceptualization`](http://premon.fbk.eu/ontology/core#Conceptualization) class, whose instances can be aligned using SKOS [exactMatch](http://www.w3.org/TR/skos-reference/#exactMatch) (strong relation, transitive) and SKOS [closeMatch](http://www.w3.org/TR/skos-reference/#closeMatch) (weak relation, non transitive) links.
Structurally, a `Conceptualization` can be seen as the reification of the [`evokes`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Evokes) relation between `LexicalEntry` and `LexicalConcept`.
Semantically, it can be seen as a very specific intensional concept (among many, in case of polysemy) evoked by a single `LexicalEntry`, which can be generalized to a `LexicalConcept` when multiple entries are considered but with a loss of information that prevents precise alignments to be represented.
Conceptualization can be seen as a generic extension of lemon.

An example of predicate modelling according to PreMOn, including an example of alignments, is shown below.

<div style="text-align: center; padding-top: 20px; padding-bottom: 20px">
<img src="images/example.svg" alt="Example of predicates representation using the PreMOn ontology" style="width: 70%"/>
</div>


### PreMOn datasets

The goal of PreMOn is to provide also a set of RDF datasets describe according to the PreMOn ontology for the most relevant predicate models - [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html), [NomBank](http://nlp.cs.nyu.edu/meyers/NomBank.html), [VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html), [FrameNet](https://framenet.icsi.berkeley.edu/fndrupal/) - including the alignment between different models provided by [SemLink](https://verbs.colorado.edu/semlink/) and by the [Predicate Matrix](http://adimen.si.ehu.es/web/PredicateMatrix).
Datasets for PropBank, NomBank and their alignment is already complete, while work is still ongoing for the remaining datasets (stay tuned!).

All PreMOn dataset (as well as the PreMOn ontology) are published online as Linked Open Data on this web site. Data can be accessed in three ways:

  * [bulk dataset download](download.html), with dataset [VOID](http://www.w3.org/TR/void/) statistics and datasets including OWL 2 RL inferences also available;
  * [SPARQL access](query.html), through a publicly available endpoint that provides access to all PreMOn data;
  * [URI dereferencing](browse.html), with proper support of content negotiation and HTML rendering of PreMOn resources.

In line with Linked Data principles, PreMOn datasets include `owl:sameAs` links between PreMOn lexical entries and corresponding lexical entries in the Princeton WordNet 3.1 [RDF/lemon dataset](http://wordnet-rdf.princeton.edu/); additional `owl:sameAs`/`rdfs:seeAlso` links between PreMOn resources and corresponding [lemonUby](http://lemon-model.net/lexica/uby/) resources are planned as a future extension.

The generation of PreMON datasets is supported by a Java command-line tool - the [premonitor](premonitor.html) - that takes in input the files of the original predicate resources and emits the corresponding RDF/PreMOn files based
on few configurable parameters. The tool is available for download and can be used by anyone to convert predicate resources in the PreMOn model.

Inside PreMOn datasets, named graphs are used to track provenance at a coarse-grained level, by placing each triple in a graph annotated with the original resource(s) (e.g., PropBank 1.7 frame files) the triple has been extracted from. This mechanism allows to query only for data of specific resources (e.g., PropBank and NomBank), as shown in the SPARQL query below, and can be exploited for managing different versions of the same resource.

```
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX ol:   <http://www.w3.org/ns/lemon/ontolex#>
PREFIX :     <http://premon.fbk.eu/ontology/core#>

SELECT ?p
FROM :verbnet                              # we include VerbNet and FrameNet data
FROM :framenet                             # (as we are mapping between them),
FROM :semlink                              # with alignments only from SemLink
WHERE {
  ?ec rdf:type :Conceptualization ;        # from the Conceptualization that reifies
      :evokingEntry :sell-v ;              # the relation between entry :sell-v
      :evokedConcept :vn.13.1-1 ;          # and predicate :vn.13.1-1 we look for
      skos:closeMatch*/:evokedConcept ?p . # predicates ?p aligned to it;
  ?p  skos:inScheme :framenet .            # predicates ?p must belong to FrameNet
}
```


### What's next?

  * explore the [PreMOn ontology](ontology.html);
  * download [PreMOn datasets](download.html) for various predicate models;
  * query datasets using the [PreMOn SPARQL endpoint](query.html);
  * check out the [URI dereferencing](browse.html) facilities offered by PreMOn;
  * download and use yourself the [premonitor](premonitor.html) conversion tool.
