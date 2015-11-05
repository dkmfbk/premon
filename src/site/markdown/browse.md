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

  * [http://premon.fbk.eu/resource/v-look](http://premon.fbk.eu/resource/v-look) - a lexical entry
  * [http://premon.fbk.eu/resource/pb17-look.01](http://premon.fbk.eu/resource/pb17-look.01) - a predicate
  * [http://premon.fbk.eu/resource/pb17-look.01-arg0](http://premon.fbk.eu/resource/pb17-look.01-arg0) - a semantic argument
  * [http://premon.fbk.eu/resource/pb17-look.01-example0](http://premon.fbk.eu/resource/pb17-look.01-example0) - a predicate usage example

This feature is powered by [OpenLink Virtuoso](https://github.com/openlink/virtuoso-opensource) and [LodView](https://github.com/dvcama/LodView).
