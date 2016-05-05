SPARQL access
===

PreMOn data can be programmatically queried using the SPARQL endpoint publicly available at [http://premon.fbk.eu/sparql](http://premon.fbk.eu/sparql).
The query interface below (powered by [YASQE](http://yasr.yasgui.org/) and [YASR](http://yasr.yasgui.org/)) can be used to query the endpoint using the browser; a [full-page](query-full.html) version of the interface is also available.
The following named graphs are available, with their content included by default in the query default graph (if no FROM clause is given):
 `pm:tbox` for TBox definitions;
 `pm:entries` for lexical entries, forms and WordNet links;
 `pm:pb17` for PropBank 1.7 data;
 `pm:pb215` for PropBank 2.1.5 data;
 `pm:nb10` for NomBank 1.0 data;
 `pm:vn32` for VerbNet 3.2 data;
 `pm:fn15` for FrameNet 1.5 data;
 `pm:fn16` for FrameNet 1.6 data;
 `pm:sl122c` for SemLink 1.2.2c data;
 `pm:pm13` for PredicateMatrix 1.3 data.

To run the query, hit the triangle button on the top right corner of the query form.

<link href='//cdn.jsdelivr.net/g/yasqe@2.2(yasqe.min.css),yasr@2.4(yasr.min.css)' rel='stylesheet' type='text/css'/>
<div id="yasqe"></div>
<div id="yasr"></div>
<script src='js/yasr.bundled.min.js'></script>
<script src='js/yasqe.bundled.min.js'></script>
<script src='js/init-yasgui.js'></script>
