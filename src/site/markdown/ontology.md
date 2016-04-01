Ontology overview
===

The PreMOn ontology is organized in modules (see figure):

  * the [core module](ontology/core.html) extends [lemon](http://www.w3.org/community/ontolex/wiki/Final_Model_Specification) (Lexical Model for Ontologies) by the [Ontology-Lexica Community Group](https://www.w3.org/community/ontolex/) at W3C and defines the main abstractions for modeling semantic classes with their semantic roles, their relations, mappings between different predicate models, and annotations (e.g., examples);
  * the [PropBank module](ontology/pb.html) extends the core module for representing concepts specific to [PropBank](https://verbs.colorado.edu/~mpalmer/projects/ace.html) (e.g., numbered/modifier/agentive arguments, function tags);
  * the [NomBank module](ontology/nb.html) extends the core module for representing concepts specific to [NomBank](http://nlp.cs.nyu.edu/meyers/NomBank.html) (e.g., numbered/modifier arguments, tags);
  * the [VerbNet module](ontology/vn.html) extends the core module for representing concepts specific to [VerbNet](https://verbs.colorado.edu/~mpalmer/projects/verbnet.html) (e.g., thematic roles, restrictions, syntactic frames);
  * the [FrameNet module](ontology/fn.html) extends the core module for representing concepts specific to [FrameNet](https://framenet.icsi.berkeley.edu/) (e.g., lexical units, semantic types).

To open PreMOn and all it's module in [Protégé](http://protege.stanford.edu/), download and extract this [zip package](ontology/PreMOn-all.zip), and open the `all.owl` file.

<div style="text-align: center; padding-top: 20px; padding-bottom: 20px">
<object type="image/svg+xml" data="images/modules.svg"></object>
</div>
