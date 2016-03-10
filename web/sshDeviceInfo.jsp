<%@page import="java.util.HashMap"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="com.jcraft.jsch.JSchException"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="com.o4s.sarGrapher.Ssh"%>
<%@page import="java.io.File"%>
<%@page import="java.io.IOException"%>
<%@page import="com.o4s.sarGrapher.CSV"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.o4s.sarGrapher.FindFiles"%>
<!DOCTYPE html>
<!--
/*
Copyright [2014] [Pascal TROUVIN, O4S France]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
-->
<html>
    <head>
        <title>sarGrapher</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script type="text/javascript" src="js/jquery.min.js"></script> 
        <link href="css/bootstrap.min.css" rel="stylesheet">
        <script src="js/bootstrap.min.js"></script>
        <script>
            function insertNew(){
                
            }
        </script>
    </head>
    <body>
        <%
    if(session==null || session.getAttribute("jsch")==null ){
        String referer=request.getHeader("Referer");
        if( referer==null )
            referer="index.jsp";
        response.sendRedirect(referer);
        return;
    }
    
    Ssh ssh=(Ssh)session.getAttribute("jsch");
    
    String user=(String)session.getAttribute("user");
    String host=(String)session.getAttribute("host");
    
    Boolean debug=(request.getParameter("debug")!=null);
    String graphPattern=(String)session.getAttribute("graphPattern");
    
    String error="";
    ArrayList l=null;
    String command="stat -Lc \"'%n %t %T'\" /dev/mapper/*";
    try {
        l=ssh.execComplete(command);
//	# /dev/mapper/control a 3a
//	# /dev/mapper/DATA0 fd 2
//	# /dev/mapper/DATA1 fd 7
//	# /dev/mapper/DATA2 fd 3
//	# /dev/mapper/DATA3 fd b
//	# /dev/mapper/DATA4 fd 4
//	# /dev/mapper/DATA5 fd 6
//	# /dev/mapper/DATA6 fd 5
//	# /dev/mapper/DATA7 fd 9
//	# /dev/mapper/DATA8 fd 8
//	# /dev/mapper/DATA9 fd a
        error +="RC("+ssh.execStatus()+") - "+ssh.execError();
        if( l.size()==0 && ssh.execStatus()!=0 ){
            error += "Unable to successfully exec(ssh "+command+") on this host";
        }
        if( debug && l.size()>0 )
            for(Iterator it=l.iterator(); it.hasNext();){
                error += "<br/>"+it.next().toString()+"\n";
            }

    } catch(JSchException e){
        error += e.getMessage();
    }
                                
             
%>
        <div>On <%=user%>@<%=host%></div>
        <div id="error" style="color: lightyellow; background-color: lightsalmon; font-weight: bold;"><%=error%></div>
        
        <h1>Renommage des p&eacute;riph&eacute;riques</h1>
        <form method="POST" action="sshSelectSAlog.jsp">
            <table border="0">
                <tr><th>Nom physique</th><th>Nom logique</th><th>Graph it</th></tr>
<%
        Pattern p=Pattern.compile("^[']?/dev/mapper/([^ ]+) ([^ ]+) ([^ ']+)[']?$");
        
        // Pattern to auto-set graph this device or not
        Pattern pdev=Pattern.compile(graphPattern); // , Pattern.CASE_INSENSITIVE); // 2014-11-03
        
        for(Iterator it=l.iterator(); it.hasNext();){
            String line=it.next().toString();
            Matcher m=p.matcher(line);
            if( m.matches()){
                int major=Integer.parseInt(m.group(2), 16);
                int minor=Integer.parseInt(m.group(3), 16);
                
                Matcher md=pdev.matcher(m.group(1));
                Boolean graphIt=md.find();
                
                %>
                <tr>
                    <td>dev<%=major%>-<%=minor%></td>
                    <td><input type="text" name="DEV[dev<%=major%>-<%=minor%>]" value="<%=m.group(1)%>"></td>
                    <td><input type="text" name="GRAPH[dev<%=major%>-<%=minor%>]" value="<%= graphIt ? md.group(1):"" %>"></td>
                </tr>
                <%
            }
        }
        if( host.equalsIgnoreCase("pplaed03") ){
            %>
                <tr>
                    <td>dev8-16</td>
                    <td><input type="text" name="DEV[dev8-16]" value="sdb"></td>
                    <td><input type="text" name="GRAPH[dev8-16]" value="SSD"></td>
                </tr>
                <tr>
                    <td>dev8-32</td>
                    <td><input type="text" name="DEV[dev8-32]" value="sdc"></td>
                    <td><input type="text" name="GRAPH[dev8-32]" value="SSD"></td>
                </tr>
                <tr>
                    <td>dev8-48</td>
                    <td><input type="text" name="DEV[dev8-48]" value="sdd"></td>
                    <td><input type="text" name="GRAPH[dev8-48]" value="SSD"></td>
                </tr>
                <tr>
                    <td>dev8-64</td>
                    <td><input type="text" name="DEV[dev8-64]" value="sde"></td>
                    <td><input type="text" name="GRAPH[dev8-64]" value="SSD"></td>
                </tr>
                <tr>
                    <td>dev8-80</td>
                    <td><input type="text" name="DEV[dev8-80]" value="sdf"></td>    
                    <td><input type="text" name="GRAPH[dev8-80]" value="SSD"></td>
               </tr>
                <tr>
                    <td>dev8-96</td>
                    <td><input type="text" name="DEV[dev8-96]" value="sdg"></td>
                    <td><input type="text" name="GRAPH[dev8-66]" value="SSD"></td>
                </tr>
            <%
        }


%>
                <!--tr><td colspan="2" id="insertBefore"><a href="#" onClick="insertNew()" class="glyphicon glyphicon-plus"></a></td></tr-->
                <tr style="display: none"><td>dev<input type="text" disabled class="major" placeholder="num&eacute;ro majeur du device">-<input type="text" disabled class="minor" placeholder="num&eacute;ro mineur du device"></td><td><input type="text" disabled name="value" placeholder="Nom logique"></td><tr>
            </table>
            <input type="submit" value="Etape suivante">
        </form>
    </body>
</html>
