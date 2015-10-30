var yasqe = YASQE(document.getElementById("yasqe"), {
    sparql: {
        endpoint: "https://premon.fbk.eu/sparql",
        showQueryButton: true
    },
    value: "SELECT (COUNT(*) AS ?n) WHERE { ?s ?p ?o }",
    backdrop: true,
    showQueryButton: true
});

var yasr = YASR(document.getElementById("yasr"), {
    getUsedPrefixes: yasqe.getPrefixesFromQuery
});

yasqe.options.sparql.callbacks.complete = yasr.setResponse;
