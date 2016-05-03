URI dereferencing
===

All PreMOn URIs can be dereferenced, obtaining different representations based on [HTTP content negotiation](https://en.wikipedia.org/wiki/Content_negotiation) as per LOD publishing [best practices](http://www.w3.org/TR/cooluris/).

Clients requesting `text/html` (e.g., a browser sending HTTP header `Accept: text/html`) are redirected to a human-readable HTML representation of the requested URI that lists all the RDF triples it participates to and supports navigation to related resources.

Clients requesting an RDF MIME type are answered with an RDF representation in the requested format.
The following RDF formats are supported:
 [RDF/XML](http://www.w3.org/TR/rdf-syntax-grammar/) (`application/rdf+xml`),
 [NTriples](http://www.w3.org/TR/n-triples/) (`text/plain`),
 [Turtle](http://www.w3.org/TR/turtle/) (`text/turtle`),
 [RDF/JSON](https://jena.apache.org/documentation/io/rdf-json.html) (`application/rdf+json`),
 [JSON-LD](http://www.w3.org/TR/json-ld/) (`application/ld+json`).
No quad format is currently supported (e.g.,
 [TriG](http://www.w3.org/TR/trig/),
 [TQL](http://rdfpro.fbk.eu/tql.html)).

Here are some URIs you can retrieve with your browser or client (e.g., `wget`) to try the URI dereferencing mechanism:

  * Lexical Entry: [http://premon.fbk.eu/resource/v-look](http://premon.fbk.eu/resource/v-look)
  * Semantic Class
      * PropBank Roleset: [http://premon.fbk.eu/resource/pb17-look.01](http://premon.fbk.eu/resource/pb17-look.01)
      * NomBank Roleset: [http://premon.fbk.eu/resource/nb10-look.01.html](http://premon.fbk.eu/resource/nb10-look.01.html)
      * VerbNet Class: [http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4](http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4)
      * FrameNet Frame: [http://premon.fbk.eu/resource/fn15-seeking.html](http://premon.fbk.eu/resource/fn15-seeking.html)
  * Semantic Role
      * PropBank Semantic Role: [http://premon.fbk.eu/resource/pb17-look.01-arg0](http://premon.fbk.eu/resource/pb17-look.01-arg0)
      * NomBank Semantic Role: [http://premon.fbk.eu/resource/nb10-look.01-arg0](http://premon.fbk.eu/resource/nb10-look.01-arg0)
      * VerbNet Semantic Role: [http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4-experiencer](http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4-experiencer)
      * FrameNet Frame Element: [http://premon.fbk.eu/resource/fn15-scrutiny@cognizer](http://premon.fbk.eu/resource/fn15-scrutiny@cognizer)
  * Mapping:
      * (SemLink) VerbNet - FrameNet: [conceptualization](http://premon.fbk.eu/resource/con-A-0q7lhtJIGF4tIh8fcSF9) and [semantic role](http://premon.fbk.eu/resource/arg-EguUonKJcWUBcGcppTJPNU)
      * (SemLink) VerbNet - PropBank: [conceptualization](http://premon.fbk.eu/resource/con-A147cm2rrYvJ7FrPP1_abL) and [semantic role](http://premon.fbk.eu/resource/arg-Cm29KepxlJ-PJ7gxposV-e)
      * (PredicateMatrix) VerbNet - FrameNet - PropBank (and WordNet): [conceptualization](http://premon.fbk.eu/resource/con-ChMADAVoodfK1Vr3b_yTA0), [semantic class](http://premon.fbk.eu/resource/pred-PuSNb4psBxuDFWKHW82rlo) and [semantic role](http://premon.fbk.eu/resource/arg-OfQZSz4AEb9OqMIRZNx23E)
      * (PropBank) PropBank - VerbNet: [conceptualization](http://premon.fbk.eu/resource/con-EnGvYz0-JxAEgioTU_Dx7B) and [semantic role](http://premon.fbk.eu/resource/arg-HCBxgS1GmR_IKKXdogxkWN)
      * (PropBank) PropBank - FrameNet: [conceptualization](http://premon.fbk.eu/resource/con-BZIGuFUnTUFNv94IiSTzh2)
      * (NomBank) NomBank - PropBank: [conceptualization](http://premon.fbk.eu/resource/con-A-GMtLZb2HgJdwIkW2QJXe) and [semantic role](http://premon.fbk.eu/resource/arg-IwjnkbGi2ESF-uzgHVXDnZ)
      * VerbNet - WordNet - OntoNote Groupings: on a [VN member](http://premon.fbk.eu/resource/sense-DSyxMNFyzdJBSEQeV1YyB8)
      * FrameNet 1.5 - FrameNet 1.6: [semantic classes](http://premon.fbk.eu/resource/pred-A2wgvkV1GF9FhCqCjrMu3W) and [semantic role](http://premon.fbk.eu/resource/arg-Eqjd_xCZg9nNhuU7KzC9hX)
  * Example:
      * PropBank: [http://premon.fbk.eu/resource/pb17-look.01-example_LJfSq7Uz](http://premon.fbk.eu/resource/pb17-look.01-example_LJfSq7Uz)
      * NomBank: [http://premon.fbk.eu/resource/nb10-look.01-example_DNhmvHaf](http://premon.fbk.eu/resource/nb10-look.01-example_DNhmvHaf)
      * VerbNet: [http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4_frame_1_ex](http://premon.fbk.eu/resource/vn32-stimulus_subject-30.4_frame_1_ex)
      * FrameNet: [http://premon.fbk.eu/resource/fn15-example_779332.html](http://premon.fbk.eu/resource/fn15-example_779332.html)

This feature is powered by [OpenLink Virtuoso](https://github.com/openlink/virtuoso-opensource) and [LodView](https://github.com/dvcama/LodView).
