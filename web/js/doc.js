function getDocs(){
    $.get("doc",{},displayDocs,"json");
}

function displayDocs(tab){
    if( tab.error ){
        $("docMessage").removeClass("STATUS0").addClass("STATUS2").html(tab.error);
    }
    $("#docList").html("<ul id='docListItems'></ul>");
    for(var i=0; i<tab.length; i++){
        var n=tab[i].name;
        var t=tab[i].title;
        $('#docListItems').append("<li><a href='#' onClick='getDoc(\""+n+"\")'>"+n+" : "+t+"</a> <a href='#' onClick='delDoc(\""+n+"\",this)' class='close' >&times;</a></li>\n");
    }
}

function getDoc(name){
    $.get("doc",{"name":name},displayDoc,"json");
}

function displayDoc(tab){
    
    if( tab.error ){
        $("docMessage").removeClass("STATUS0").addClass("STATUS2").html(tab.error);
    }
    $('#docName').val(tab.name);
    $('#docTitle').val(tab.title);
    var data=tab.content.replace(/%2B/g,"+");
    $('#docContent').tinymce().setContent(data, {format: 'raw'});
    //$('#docContent').tinymce().setContent(tab.content);
}

function delDoc(name,obj){
    $.ajax({
        url:"doc?name="+name,
        type:"DELETE",
        data: {},
        success: checkTab,
        dataType:"json"
    });
    $(obj).parents("li").remove();
}

