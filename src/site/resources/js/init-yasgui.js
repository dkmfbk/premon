var yasqe = YASQE(document.getElementById("yasqe"), {
    sparql: {
        endpoint: "https://premon.fbk.eu/sparql",
        showQueryButton: true
    },
    value: "SELECT (COUNT(*) AS ?n) WHERE { ?s ?p ?o }",
    backdrop: true,
    showQueryButton: true,
    collapsePrefixesOnLoad: true
});

var yasr = YASR(document.getElementById("yasr"), {
    getUsedPrefixes: function() {
        var map = yasqe.getPrefixesFromQuery();
        map = Object.create(map);
        map.rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        map.rdfs = "http://www.w3.org/2000/01/rdf-schema#";
        map.owl = "http://www.w3.org/2002/07/owl#";
        map.xsd = "http://www.w3.org/2001/XMLSchema#";
        map.dc = "http://purl.org/dc/elements/1.1/";
        map.void = "http://rdfs.org/ns/void#";
        map.dcterms = "http://purl.org/dc/terms/";
        map.ontolex = "http://www.w3.org/ns/lemon/ontolex#";
        map.decomp = "http://www.w3.org/ns/lemon/decomp.owl#";
        map.pm = "http://premon.fbk.eu/resource/";
        map.pmo = "http://premon.fbk.eu/ontology/core#";
        map.pmopb = "http://premon.fbk.eu/ontology/pb#";
        map.pmonb = "http://premon.fbk.eu/ontology/nb#";
        map.pmofn = "http://premon.fbk.eu/ontology/pb#";
        map.pmovn = "http://premon.fbk.eu/ontology/nb#";
        map.nif = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
        map.semiotics = "http://www.ontologydesignpatterns.org/cp/owl/semiotics.owl#";
        map.skos = "http://www.w3.org/2004/02/skos/core#";
        return map;
    }
});

yasqe.addPrefixes({

    ontolex: "http://www.w3.org/ns/lemon/ontolex#",
    decomp: "http://www.w3.org/ns/lemon/decomp.owl#",
    nif: "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#",
    wn31: "http://wordnet-rdf.princeton.edu/wn31/",
    lexinfo: "http://www.lexinfo.net/ontology/2.0/lexinfo#",
    lime: "http://www.w3.org/ns/lemon/lime#",
    pmo: "http://premon.fbk.eu/ontology/core#",
    pmopb: "http://premon.fbk.eu/ontology/pb#",
    pmonb: "http://premon.fbk.eu/ontology/nb#",
    pmofn: "http://premon.fbk.eu/ontology/fn#",
    pmovn: "http://premon.fbk.eu/ontology/vn#",
    pm: "http://premon.fbk.eu/resource/"

});
yasqe.collapsePrefixes = true;
yasqe.options.sparql.callbacks.complete = yasr.setResponse;
