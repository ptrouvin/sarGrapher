<%-- 
    Document   : doc
    Created on : Sep 25, 2014, 3:01:05 PM
    Author     : pascal
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script src="js/jquery.min.js"></script>
        <!-- Bootstrap -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
          <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
          <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
        <![endif]-->
        <script src="js/bootstrap.min.js"></script>
        
        <script src="js/tinymce/jquery.tinymce.min.js"></script>
        <script type="text/javascript" src="js/tinymce/tinymce.min.js"></script>
        <script src="js/pasteimage/pasteimage.js"></script>
        
        <link href="css/monitoring.css" rel="stylesheet">
        <script src="js/doc.js"></script>
        
        <script>
            $(function(){
                tinymce.init({
                    selector: "#docContent",
                    inline: true,
                    plugins: [
                         "advlist autolink lists link image charmap print preview anchor",
                         "searchreplace visualblocks code fullscreen",
                         "insertdatetime media table contextmenu paste save"
                    ],
                    toolbar: "insertfile undo redo save | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link",
                    save_enablewhendirty: true,
                    //readonly:true,
                    save_onsavecallback: function() {
                        var data = tinyMCE.get('docContent').getContent();
                        /*data=data.replace(/(src=["'])([^"']+)/g,
                            function(match, p1, p2, p3, offset, string){
                                return p1+p2.replace(/\+/g,"%2B");
                            });*/
                        var dsp=$('#source').css('display');
                        $('#source').text(data).show();
                        if( dsp == "none" ){
                            $('#docContent').hide();
                            alert("Copy & paste the content");
                        }
                        
                    },
                    autosave_interval: "120s"
                });
                
                $.pasteimage(pasteImageCallback);
                
            });
            
            /* Creates a new image from a given source */
            function pasteImageCallback(source) {
                var src=source.replace(/([\r\n ]+)/,"");
                // for IE9
                //tinymce.activeEditor.execCommand('mceInsertContent', false, "<br/>\n"+src+"<img src=\""+src+"\" />");
                //
                // for chrome
                var ed = tinyMCE.activeEditor;                // get editor instance
                var range = ed.selection.getRng();                  // get range
                var newNode = ed.getDoc().createElement ( "img" );  // create img node
                newNode.src=source;                           // add src attribute
                range.insertNode(newNode); 
            }
            
        </script>
        
    </head>
    <body>
        <h1>Documentation <a href="index.jsp" class="glyphicon glyphicon-home"></a></h1>
        
        <div id="docMessage"></div>
                
        <div id="docContent" style="width:100%; height:100%">
            <p>La documentation de sarGrapher.</p> <h1>sarGrapher</h1> <p>&nbsp;</p> <h2>Installation</h2> <h3>&nbsp;Sans tomcat</h3> <p>D&eacute;zipper le contenu du zip, qui contient &agrave; la fois un apache tomcat et l'application sarGrapher pr&eacute;install&eacute;e.</p> <p>Puis d&eacute;marrer tomcat, bin/startup.cmd ou bin/startup.sh (selon que vous &ecirc;tes sous windows ou linux)</p> <p>&nbsp;</p> <p>sarGrapher est ensuite disponible &agrave; l'URL: <a href="http://localhost:8080/sarGrapher/">http://localhost:8080/sarGrapher/</a></p> <p>&nbsp;</p> <h3>Avec un tomcat existant</h3> <p>Copier le fichier sarGrapher.war dans le r&eacute;pertoire de votre tomcat/webapps</p> <p>Tomcat va l'instancier automatiquement</p> <p>&nbsp;</p> <p>sarGrapher est ensuite disponible &agrave; l'URL: <a href="http://localhost:8080/sarGrapher/">http://localhost:8080/sarGrapher/</a></p> <p>&nbsp;Le port peut &ecirc;tre diff&eacute;rent, cel&agrave; d&eacute;pend de la configuration de votre tomcat.</p> <p><br />&nbsp;</p> <p>&nbsp;</p>
        </div>
        
        <div id="source" style="display:none"></div>
    </body>
</html>
