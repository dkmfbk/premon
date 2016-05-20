PreMOn overview
===


### What is PreMOn?


**PreMOn** (*PRedicate Model for ONtologies*) is a linguistic resource for representing predicate models (i.e., [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html), [NomBank](http://nlp.cs.nyu.edu/meyers/NomBank.html), [VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html), [FrameNet](https://framenet.icsi.berkeley.edu/fndrupal/)), their annotations, the mappings between them (e.g, [SemLink](https://verbs.colorado.edu/semlink/), [PredicateMatrix](http://adimen.si.ehu.es/web/PredicateMatrix)), and the mappings to frame-based ontologies in RDF/OWL. PreMOn consists of two components:

1. the [PreMOn Ontology](ontology.html), an OWL 2 ontology that extends [lemon](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification) (by the W3C Ontology Lexicon Community Group) for modeling the core concepts of semantic class (i.e., roleset in PropBank and NomBank, verb class in VerbNet, and frame in FrameNet), semantic role, mapping, and annotation common to all predicate models; and,

2. the [PreMOn Dataset](download.html), a freely-available, interlinked RDF dataset containing the PropBank, NomBank, VerbNet, and FrameNet predicate model data (in various versions), the examples provided with the original resources, and the SemLink and PredicateMatrix mappings, published online as Linked Open Data according to the PreMOn Ontology.

Compared to the current situation where each predicate model has its own proprietary XML format, PreMOn brings several benefits to users of predicate models:

1. ease of access and reuse of predicate model data, due to the adoption of a common RDF format, stable URIs, and LOD best practices;
2. possibility to abstract and capture the aspects common to different predicate models, while at the same time keeping track of the peculiarities of each model (using RDFS/OWL subclass/subproperty primitives);
3. possibility to apply SW technologies to predicate model data, such as automated reasoning and SPARQL querying, e.g., for retrieving the semantic classes of a lexical entry and the associated mappings;
4. possibility to combine PreMOn with other linguistic ontologies, e.g., for providing the SRL annotations of a text according to the [NLP Interchange Format](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/) (NIF);
5. possibility for third parties to publish and interlink their datasets with PreMOn, extending it in a decentralized way (e.g., with new mappings).

These capabilities are particulary relevant in the context of Semantic Web (SW), where predicate models are increasingly used for information extraction, e.g., in tools such as [NewsReader](http://www.newsreader-project.eu/), [PIKES](http://pikes.fbk.eu/), or as the starting point from which to derive ontologies of extracted knowledge, such as [FrameBase](http://framebase.org/) and the [ESO ontology](http://www.newsreader-project.eu/results/event-and-situation-ontology/) both derived from FrameNet.


### PreMOn ontology

The [PreMOn ontology](ontology.html) consists of a [core](http://premon.fbk.eu/ontology) module defining the main abstractions for representing predicate model, and of additional modules ([PropBank](http://premon.fbk.eu/ontology/pb), [NomBank](http://premon.fbk.eu/ontology/nb), [VerbNet](http://premon.fbk.eu/ontology/vn), and [FrameNet](http://premon.fbk.eu/ontology/vn)) specific to each predicate model included in PreMOn.

The core module (see figure) extends [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) and thus inherits the capability to represent lexical entries (class [`LexicalEntry`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry)) with their associated forms, and to relate lexical entries to the ontological entities they denote (classes, properties, individuals) using the [`LexicalSense`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#LexicalSense) reified relation.
lemon supports mapping entries to [`LexicalConcept`](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept)s (subclass of SKOS [`Concept`](http://www.w3.org/TR/skos-reference/#concepts)), each denoting an intensional (∼informal) meaning evoked by a set of lexical entries.
Example of lexical concepts are WordNet synsets, whose semantics is not formally encoded in an ontology.

<div style="text-align: center; padding-top: 20px; padding-bottom: 20px">
<img src="images/core.svg" alt="PreMOn ontology, core module" style="width: 70%"/>
</div>

#### Semantic Classes and Roles

<sub>[in green in the figure]</sub>

The *PreMOn Ontology Core Module* extends *lemon* by introducing classes [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass) and [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole). [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass) homogeneously represents the semantic classes from the various predicate models. That is, individuals of this class correspond to rolesets in PropBank and NomBank (e.g., *pm:nb10-seller.01* and *pm:pb17-sell.01*), verb classes in VerbNet (e.g., *pm:vn32-give-13.1-1*), and frames in FrameNet (e.g., *pm:fn15-commerce sell*). An instance of [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass) typically has (via property [`pmo:semRole`](http://premon.fbk.eu/ontology/core#semRole)) a number of [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole)s, representing, from a semantic point of view, the roles the arguments of that [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass) can play.

Semantic roles are defined locally to semantic classes, so VerbNet *agent* is represented as multiple semantic roles, one for each verb class it occurs in, and with each semantic role linked to its specific selectional restrictions (if any). Note that [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass) is defined as subclass of [`ontolex:LexicalConcept`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept), as we see [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass)es as essentially informal concepts rather than well defined concepts of a formal ontology (although an ontology can be derived from them, cf., FrameBase and ESO). Being [`ontolex:LexicalConcept`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept)s, [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass)es inherit the link to lexical entries as well as the link (via [`ontolex:isConceptOf`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Concept)) to the ontological entities formalizing them, typically event classes.

Properties [`pmo:classRel`](http://premon.fbk.eu/ontology/core#classRel) and [`pmo:roleRel`](http://premon.fbk.eu/ontology/core#roleRel), and their resource-specific subproperties, are introduced to express the relations between elements at each level, such as subtyping, and predicate and role inheritance (e.g., [`pmofn:inheritsFrom`](http://premon.fbk.eu/ontology/fn#inheritsFrom) and [`pmofn:inheritsFromFER`](http://premon.fbk.eu/ontology/fn#inheritsFromFER) for FrameNet). Additional resource-specific classes (e.g., [`pmovn:ThematicRole`](http://premon.fbk.eu/ontology/vn#ThematicRole)) and properties (e.g., [`pmovn:thematicRole`](http://premon.fbk.eu/ontology/vn#thematicRole)) further characterize important aspects of each predicate model, like commonalities between semantic roles.

#### Mappings

<sub>[in red in the figure]</sub>

Mappings between different predicate models are practically relevant but cannot be expressed using only the classes above, as they are often defined (e.g., in SemLink and PredicateMatrix) in terms of <[`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass), [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry)> pairs. To model these pairs, one could reuse the notion of **ontolex:LexicalSense**. However, its formalization in lemon as reified relation depends on the existence of (exactly) one ontological entity for each <[`ontolex:LexicalConcept`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept), [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry)> pair, a strong constraint that we do not necessarily need for our purposes. Therefore, we introduce the [`pmo:Conceptualization`](http://premon.fbk.eu/ontology/core#Conceptualization) class. Structurally, a [`pmo:Conceptualization`](http://premon.fbk.eu/ontology/core#Conceptualization) can be seen as the reification of the [`ontolex:evokes`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Evokes) relation between [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry) and [`ontolex:LexicalConcept`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept). Semantically, it can be seen as a very specific intensional concept (among many, in case of polysemy) evoked by a single [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry), which can be generalized to a [`ontolex:LexicalConcept`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Concept) when multiple entries are considered but with a possible loss of information that prevents precise alignments to be represented.

Mappings are explicitly represented as individuals of class [`pmo:Mapping`](http://premon.fbk.eu/ontology/core#Mapping), and can be seen as sets of (or n-ary relations between) either (i) [`pmo:Conceptualization`](http://premon.fbk.eu/ontology/core#Conceptualization)s, (ii) [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass)es, and (iii) [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole)s, with role mappings anchored to conceptualization or class mappings via property [`pmo:semRoleMapping`](http://premon.fbk.eu/ontology/core#semRoleMapping). We rely on this set-like modeling, since mappings are not necessarily represented as binary relations in predicate mapping resources: e.g., in the PredicateMatrix, each row represents the mapping of a semantic role / lexical entry pair over the different resources (e.g., <*13.1-1-agent*, *deal*> in VerbNet, <*sell.01-arg0*, *sell*> in PropBank, <*Commerce Sell-seller*, *sell*> in FrameNet) as well as the corresponding WordNet verb sense. Reifying the n-ary mapping relation also allows us, if needed, to further characterize each single mapping, asserting additional information such as confidence and reliability. Moreover, it is possible to further specialize mappings
(e.g., to model mappings holding only in one direction, from a resource to another one, or to represent different types of relationships among the members of the mapping) by subtyping the [`pmo:Mapping`](http://premon.fbk.eu/ontology/core#Mapping) class or the property (pmo:item) relating a [`pmo:Mapping`](http://premon.fbk.eu/ontology/core#Mapping) to its members.

#### Annotations

<sub>[in yellow in the figure]</sub>

Predicate models are typically complemented by examples showing concrete occurrences of semantic classes and roles in text. More generally, a text can be annotated with semantic classes and roles as a result of manual or automatic SRL.

The PreMOn Ontology provides some common primitives, based on the [NLP Interchange Format](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/nif-core.html) (NIF), which aim at properly modeling the heterogeneous annotations of a text for different predicate models. NIF introduces the general notion of [`nif:String`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String) to represent arbitrary text strings. [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) is a particular subclass of [`nif:String`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String), representing a whole string of text. Any substring (itself a [`nif:String`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String)) has a [`nif:referenceContext`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext) relation to the [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) individual representing the whole text containing it.

To specifically model the aforementioned examples complementing predicate models, we introduce [`pmo:Example`](http://premon.fbk.eu/ontology/core#Example), subclass of [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context), to represent the string associated with the example. The occurrence of a [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry), [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass), or [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole) in a [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) is denoted by an instance of [`nif:Annotation`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Annotation), related to the given [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry), [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass), or [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole) via property [`pmo:valueObj`](http://premon.fbk.eu/ontology/core#valueObj) (the value attached to the annotation), and to the [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) instance via property [`nif:annotation`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#annotation). If detailed information on the specific span of text (i.e., substring) denoting the [`ontolex:LexicalEntry`](https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Lexical_Entry), [`pmo:SemanticClass`](http://premon.fbk.eu/ontology/core#SemanticClass), or [`pmo:SemanticRole`](http://premon.fbk.eu/ontology/core#SemanticRole) is available (e.g., FrameNet provides the specific offsets of lexical units, frames, and frame elements, in the example text) an additional instance of [`pmo:Markable`](http://premon.fbk.eu/ontology/core#Markable), subclass of [`nif:String`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String), is created and linked to the specific [`nif:Annotation`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Annotation) and [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) via properties [`nif:annotation`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#annotation) and [`nif:referenceContext`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext), respectively. As the same [`nif:Context`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context) may contain multiple [`nif:Annotation`](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Annotation)s referring to one or more semantic classes and their corresponding roles, an additional [`pmo:AnnotationSet`](http://premon.fbk.eu/ontology/core#AnnotationSet) instance is created to cluster annotations from the same predicate structure.

Below you can find an image illustrating the instantiation of the PreMOn Ontology with some semantic classes, semantic roles, mappings, and examples, with predicate model data.

<div style="text-align: center; padding-top: 20px; padding-bottom: 20px">
<img src="images/example.png" alt="Example of predicates representation using the PreMOn ontology" style="width: 90%"/>
</div>

Additional classes and properties are defined in the specific submodule for each predicate model ([PropBank](http://premon.fbk.eu/ontology/pb), [NomBank](http://premon.fbk.eu/ontology/nb), [VerbNet](http://premon.fbk.eu/ontology/vn), and [FrameNet](http://premon.fbk.eu/ontology/vn)).


### PreMOn datasets

To populate PreMOn with content from the various resources (predicate models, mappings), we developed an [open-source Java command-line tool](premonitor.html) available. The tool applies pluggable, resource-specific converters to the original distribution files of each resource, instantiating the proper individuals and assertions according to the PreMOn Ontology. If available, mappings to additional resources (e.g., WordNet synsets, OntoNotes groupings) are also extracted. OWL 2 RL inference, statistics extraction and some cross-resource cleanup (e.g., for dropping inconsistent mappings) are applied to extracted triples, leveraging [RDFpro](http://rdfpro.fbk.eu) for RDF processing.

Specific conversion strategies had to be implemented for each predicate model. E.g., in VerbNet, semantic roles (with selectional constraints) and frames have to be propagated from a class to its subclasses, unless redefined in the latter. In PropBank (and NomBank), the instantiation of [`pmopb:SemanticRole`](http://premon.fbk.eu/ontology/pb#SemanticRole)s requires creating an individual for each ⟨[`pmopb:Roleset`](http://premon.fbk.eu/ontology/pb#Roleset), [`pmopb:Argument`](http://premon.fbk.eu/ontology/pb#Argument)⟩ pair, as no information is provided on which arguments a predicate may have (besides explicit occurrence in frame files, in which case semantic role attributes [`pmopb:core`](http://premon.fbk.eu/ontology/pb#core)/[`pmonb:core`](http://premon.fbk.eu/ontology/nb#core) are set to “true”).
We applied the conversion suite on a large collection of resources, producing a comprehensive dataset, namely the [PreMOn Dataset](download.html), containing:

* PropBank v1.7 (pb17)
* PropBank v2.1.5 released with OntoNotes v5 (pb215)
* NomBank v1.0 (nb10)
* VerbNet v3.2 (vn32)
* FrameNet v1.5 (fn15)
* FrameNet v1.6 (fn16)
* SemLink 1.2.2c (sl122c)
* PredicateMatrix 1.3 (pm13)

The PreMOn Dataset contains the mappings between semantic classes and roles provided by each predicate model, SemLink and the PredicateMatrix, as well as the mappings between VerbNet classes and lexical senses in WordNet 3.1 (wn31) and OntoNotes 5 groupings.

By adopting an homogeneous schema for heterogeneous predicate models, PreMOn facilitates the joint querying of content from different resources. For instance, a query like

```
SELECT DISTINCT ?lexEnt (COUNT(?resource) as ?n)
WHERE {
	{SELECT DISTINCT ?lexEnt ?resource
	WHERE {
		GRAPH ?resource
			{?lexEnt ontolex:evokes ?semCla .
			?semCla a pmo:SemanticClass . }
	}} FILTER NOT EXISTS {
		?conc pmo:evokingEntry ?lexEnt .
		?mapping a pmo:Mapping ; pmo:item ?conc }
} GROUP BY ?lexEnt ORDER BY DESC(?n) ?lexEnt
```

looks for lexical entries (`?lexEnt`) evoking semantic classes in different resources (`?resource`), for which no mappings are defined ([try this query](http://bit.ly/premon-example) on our SPARQL endpoint). Results are ordered by decreasing number of resources defining the lexical entry. This query hints a way to exploit PreMOn to investigate, and possibly extend, mappings between predicate models.



### What's next?

  * read the [LREC2016 paper](https://dkm-static.fbk.eu/people/rospocher/files/pubs/2016lrec1.pdf);
  * explore the [PreMOn ontology](ontology.html);
  * download [PreMOn datasets](download.html) for various predicate models;
  * [navigate](navigator) all the `ontolex:LexicalEntry` and `pmo:SemanticClass` instances;
  * query datasets using the [PreMOn SPARQL endpoint](query.html);
  * check out the [URI dereferencing](browse.html) facilities offered by PreMOn;
  * download and use yourself the [premonitor](premonitor.html) conversion tool.
