<%-- 
    Document   : sshGet
    Created on : 17 sept. 2014, 12:37:40
    Author     : trouvin
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
--%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.o4s.sarGrapher.Ssh"%>
<%
    if(session==null || session.getAttribute("jsch")==null ){
        String referer=request.getHeader("Referer");
        if( referer==null )
            referer="index.jsp";
        response.sendRedirect(referer);
        return;
    }
    
    Ssh ssh=(Ssh)session.getAttribute("jsch");
    
    // store deviceInfo to hashMap
    HashMap deviceInfo=new HashMap();
    // GRAPH[devMajour-Minor]=graphTag
    HashMap deviceGraph=new HashMap();
    Pattern pat=Pattern.compile("DEV\\[([^\\]]+)\\]");
    for(Enumeration e=request.getParameterNames(); e.hasMoreElements();){
        String n=e.nextElement().toString();
        Matcher m=pat.matcher(n);
        if( m.matches() ){
            String v=request.getParameter(n);
            deviceInfo.put(m.group(1), v);
            
            String graphTag=request.getParameter("GRAPH["+m.group(1)+"]");
            if( graphTag!=null && ! graphTag.isEmpty() )
                deviceGraph.put(v, graphTag);
        }
    }
    // save to session
    session.setAttribute("deviceInfo", deviceInfo);
    session.setAttribute("deviceGraph", deviceGraph);
    
    ArrayList l = ssh.execComplete("find /var/log/sa/sa*");
        
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SarGrapher</title>
    </head>
    <body>
        <h1>sa log file selection</h1>
        
        <h1><%=ssh.getUser()%>@<%=ssh.getHost()%></h1>
        
        Choisissez un fichier dans la liste:
        <ul>
            <%
                Calendar cal=Calendar.getInstance();
                int todayD=cal.get(Calendar.DAY_OF_MONTH);
                int todayM=cal.get(Calendar.MONTH);
                Pattern p=Pattern.compile("^/var/log/sa/sa(\\d+)$");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
                
                for(Iterator it=l.iterator(); it.hasNext();){
                    String fn=it.next().toString();
                    
                    Matcher m=p.matcher(fn);
                    if( m.matches() ){
                        int d=Integer.parseInt(m.group(1));
                        cal=Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_MONTH, d);
                        if( todayD<d ){
                            cal.set(Calendar.MONTH,todayM-1);
                        }
                        out.println("<li><a href=\"sshSA2CSV?sa="+fn+"\">"+sdf.format(cal.getTime())+"</a></li>\n");
                    }
                }
            %>
        </ul>
    </body>
</html>
