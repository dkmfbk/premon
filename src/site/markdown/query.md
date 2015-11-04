SPARQL access
===

PreMOn data can be programmatically queried using the SPARQL endpoint publicly available at [http://premon.fbk.eu/sparql](http://premon.fbk.eu/sparql).
The query interface below (powered by [YASQE](http://yasr.yasgui.org/) and [YASR](http://yasr.yasgui.org/) can be used to submit your queries to the endpoint using your browser; a [full-page](query-full.html) version of the interface is also available.
The following named graphs are used:
 `pm:tbox` for TBox definitions;
 `pm:entries` for lexical entries, forms and WordNet links;
 `pm:nb10` for NomBank 1.0;
 `pm:pb17` for PropBank 1.7;
 `pm:pbon5` for PropBank Ontonote 5.

<link href='//cdn.jsdelivr.net/g/yasqe@2.2(yasqe.min.css),yasr@2.4(yasr.min.css)' rel='stylesheet' type='text/css'/>
<div id="yasqe"></div>
<div id="yasr"></div>
<script src='//cdn.jsdelivr.net/yasr/2.4/yasr.bundled.min.js'></script>
<script src='//cdn.jsdelivr.net/yasqe/2.2/yasqe.bundled.min.js'></script>
<script src='js/init-yasgui.js'></script>
