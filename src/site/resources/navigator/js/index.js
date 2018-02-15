var timer;
var accordions;
var nTable = 0;
var nTableActive = 0;
var nTableVis = 0;
var tableTitle = [];
var mainUrl = getParameterByName('nav');
if(window.location.href.indexOf("?")>=1){
    window.history.pushState("Navigator", "Navigator", window.location.href.substr(0, window.location.href.indexOf("?")));
}

$(document).ready(function() {
    if(mainUrl!=null){
        document.getElementById("frame").contentWindow.location.href = mainUrl;
    }

    document.getElementById("frame").contentWindow.location.href = "filler.html";

    createTable("Lexical Entry");
    createTable("FrameNet 1.5");
    createTable("FrameNet 1.6");
    createTable("FrameNet 1.7");    
    createTable("NomBank 1.0");
    createTable("PropBank 1.7");
    createTable("PropBank 2.1.5");
    createTable("PropBank 3.1");    
    createTable("VerbNet 3.2");
    createTable("VerbNet 3.3");

    var file = [
        "data/lexEnt.json",
        "data/fn15semCla.json",
        "data/fn16semCla.json",
        "data/fn17semCla.json",        
        "data/nb10semCla.json",
        "data/pb17semCla.json",
        "data/pb215semCla.json",
        "data/pb31semCla.json",        
        "data/vn32semCla.json",
        "data/vn33semCla.json"
    ];

    var uri = "https://premon.fbk.eu/resource/";

    var prefix = "pm:";

    var height = $("#master").height();

    height = height-nTable*34-80;

    for(var i = 0; i < nTable; i++){
        initTable(i, height>1000?height:1000, uri, prefix, file);
    }

    openFirstAccordion();
    resize();
});

window.addEventListener("resize", resize);

function resize() {
    var height = $("#master").height();
    height = height-nTableActive*34-81;
    for(var l=0; l<nTable; l++){
        var size = $("#table"+l).DataTable().page.info().recordsDisplay*35;
        $('#table'+l+'_wrapper div.dataTables_scrollBody').height(height<size?height:size);
    }
}

function createTable(title){
    addTable(title);
    tableTitle[nTable-1] = title;
}

function addTable(title) {

    var master = document.getElementById("master");

    master.innerHTML = master.innerHTML + "<button class='accordion'>"+title+"</button> " +
        "<div class='acc'>"+
            "<table id='table"+nTable+"' class='aaTable display nowrap' cellspacing='0' width='100%'>"+
                "<thead class='tHead'>"+
                    "<tr>"+
                        "<th>col1</th>"+
                        "<th>col2</th>"+
                    "</tr>"+
                "</thead>"+
            "</table>"+
        "</div>";

    nTable++;
}

function initTable(n, height, uri, prefix, file){
    $("#table"+n).DataTable({
        ajax: file[n],
        bInfo: false,
        "columnDefs": [
            {
                "targets": [0],
                "visible": false
            }
        ],
        "createdRow" : function ( row, data, index ) {
            var val = $('td', row).eq(0).html();
            $('td', row).eq(0).html('<a href="'+uri+val+'" target="iframe1">'+prefix+val+'</a>');
        },
        deferRender: true,
        scrollCollapse: true,
        scroller: true,
        scrollY: height
    });
    $("#table"+n).DataTable().on( 'draw.dt', function (e, settings, data) {
        updateAll();
    });
    $("#table"+n).DataTable().on('init.dt', function (e, settings, data) {
        openFirstAccordion();
    })
}

function keyUp() {
    if(timer){
        clearTimeout(timer);
    }
    timer = setInterval(function(){
        clearTimeout(timer);
        for(var u=0; u<nTable; u++){
            //$("#table"+u).DataTable().draw();
            $("#table"+u).DataTable().search($("#filter").val()).draw(false);
        }
        updateAccordion();
    },500);
}

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function openFirstAccordion(){
    accordions = document.getElementsByClassName("accordion");
    accordions[0].classList.add("active");
    accordions[0].nextElementSibling.classList.add("show");
}

function updateAll(){
    accordions = document.getElementsByClassName("accordion");
    updateAccordion();
    updateAccordionListener();
}

function updateAccordion(){
    nTableActive = 0;
    for(var l=0; l<nTable; l++){
        var size = $("#table"+l).DataTable().page.info().recordsDisplay;
        var rendered = $("#table"+l).dataTable()._('tr', {"filter": "applied"}).length;
        var total = $("#table"+l).DataTable().column(0).data().length;

        if(size == 0){
            hideAccordion(accordions[l]);
        }else {
            showAccordion(accordions[l], tableTitle[l], size, total, rendered);
            nTableActive++;
        }
    }
    resize();
}

function showAccordion(acco, title, size, total, rendered){
    acco.style.display = "block";
    acco.nextElementSibling.style.display = "block";
    //var render = rendered==size?"":"("+rendered+" rendered)";
    var render = "";
    acco.innerHTML = title+" ("+size+" / "+total+") "+render;
}

function hideAccordion(acco) {
    acco.classList.remove("active");
    acco.nextElementSibling.classList.remove("show");
    acco.style.display = "none";
    acco.nextElementSibling.style.display = "none";
}

function updateAccordionListener(){
    for (var i = 0; i < accordions.length; i++) {
        accordions[i].onclick = function(){
            for(var j = 0; j < accordions.length; j++){
                if(accordions[j] != this){
                    accordions[j].classList.remove("active");
                    accordions[j].nextElementSibling.classList.remove("show");
                }
            }
            this.classList.toggle("active");
            this.nextElementSibling.classList.toggle("show");
        }
    }
}