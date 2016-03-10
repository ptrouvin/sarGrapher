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
        <style>
            .top {
                vertical-align: top;
            }
            #licenses {
                float: right;
            }
        </style>
    </head>
    <body>
        <%
            String privkey=request.getParameter("privkey");
            String host=request.getParameter("host");
            String user=request.getParameter("user");
            String password=request.getParameter("password");
            String timeout=request.getParameter("timeout");
            Boolean details=request.getParameter("details")!=null;
            String graphPattern=request.getParameter("graphPattern");

            StringBuffer buf=new StringBuffer();
            
            String error="";
                        
            if( user!=null && host!=null ){
                Ssh ssh=new Ssh(user, host);
                if(password!=null)
                    ssh.setPassword(password);
                if( privkey==null ){
                    privkey = request.getParameter("privkeyFile");
                }
                if( timeout!=null ){
                    ssh.setConfig("timeout", timeout);
                }

                ssh.setConfig("StrictHostKeyChecking", "no");
                
                if( privkey!=null && !privkey.isEmpty() ){
                    ssh.addPrivKey(privkey);
                }

                
                ArrayList l;
                try {
                    l=ssh.execComplete("hostname");
                    for(Iterator it=l.iterator(); it.hasNext();)
                    buf.append(it.next().toString()+"\n");
                
                    error +="RC("+ssh.execStatus()+") - "+ssh.execError();
                    if( ssh.execStatus()==0 ){
                        session.setAttribute("jsch", ssh);
                        session.setAttribute("user", user);
                        session.setAttribute("host", host);
                        session.setAttribute("privkey", privkey);
                        session.setAttribute("password", password);
                        session.setAttribute("details",details);
                        session.setAttribute("graphPattern",graphPattern);
                        
                        response.sendRedirect("sshDeviceInfo.jsp");
                        return;
                    }

                    error += "Unable to successfully exec(ssh hostname) on this host";
                    
                } catch(JSchException e){
                    error += e.getMessage();
                }
                                
                
        
            }
            
            if( host == null ){
                host=(String)session.getAttribute("host");
                if( host == null )
                    host="ivlrhs01";
            }
            if( user==null ){
                user=(String)session.getAttribute("user");
                if( user == null )
                    user="root";
            }
            if( privkey==null ){
                privkey=(String)session.getAttribute("privkey");
                if( privkey == null )
                    privkey="";
            }
            if( password==null ){
                password=(String)session.getAttribute("password");
                if( password == null )
                    password="";
            }
            if( timeout==null ){
                timeout="30000";
            }
            if( graphPattern==null ){
                graphPattern="(DATA)";
            }
            
        %>
        <h1>sarGrapher</h1> <span id="licenses" class="glyphicon glyphicon-lock"></span>
        <h2>Outil de repr&eacute;sentation graphique des statistiques linux (sar)</h2>
        <div>On <%=user%>@<%=host%><br/>
            <%=buf.toString()%>
        </div>
        <div id="error" style="color: lightyellow; background-color: lightsalmon; font-weight: bold;"><%=error%></div>
        
        <form method="POST">
            <input type="text" required id="user" name="user" placeholder="username" value="<%=user%>">@<input type="text" required id="host" name="host" placeholder="host" value="<%=host%>">
            <br><input type="password" class="top" id="password" name="password" placeholder="enter password"> <span class="top">et/ou</span>
            <textarea placeholder="Insert your ssh private key" name="privkey" id="privkeyString" rows="5" cols="80"><%=privkey%></textarea>
            <br>Temps maximal d'attente de la connexion <input type="number" name="timeout" value="<%=timeout%>" title="en milli-secondes">
            <br>D&eacute;tails <input type="checkbox" name="details">
            <br>Device graph pattern <input type="text" name="graphPattern" value="<%=graphPattern%>">
            <br><input type="submit">
        </form>
            
    </body>
</html>
