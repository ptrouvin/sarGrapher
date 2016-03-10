<%-- 
    Document   : graph
    Created on : 5 sept. 2014, 15:09:34
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

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="com.o4s.sarGrapher.CSVs"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.nio.file.Path"%>
<%@page import="com.o4s.sarGrapher.CSV"%>
<%@page import="java.io.File"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    if(session==null || session.getAttribute("jsch")==null ){
        String referer=request.getHeader("Referer");
        if( referer==null )
            referer="index.jsp";
        response.sendRedirect(referer);
        return;
    }
    
    String hostname=(String)session.getAttribute("host");
    
    CSVs csvs=new CSVs();
    
    String message="";
    CSV hdr=csvs.get("__HEADER__.txt");
    hdr.open();

    CSV cpu=csvs.get("CPU.all.txt");
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="js/jquery.min.js"></script>
        
        <script type="text/javascript">
            $(function(){
                var obj=$('#cloneThisTextarea');
                $("img").after(obj.clone());
            });
        </script>
    </head>
    <body>
        <h1><%=hostname%></h1>
        <div style="color: red; font-weight: bold"><%=message%></div>
        <div>
            <p>Relev&eacute; du <%= hdr.get("date") %></p>
            <p><%=hdr.get("hostname")%> <%=hdr.get("OS")%> <%=hdr.get("OS_VERSION")%></p>
            <p>Architecture: <%=hdr.get("arch")%> <%=hdr.get("procs")%> processeurs</p>
            <textarea id="cloneThisTextarea" placeholder="Insert your comment here" rows="5" cols="80"></textarea>
        </div>
        <img src="graph?width=1024&type=LINE&title=CPU&ytitle=%25&cpu=CPU.all.txt:<%=cpu.hasField("_usr") ? "_usr" : "_user"%>,_iowait">
        <%
            if( csvs.exists("kbswpfree.txt") ){
                out.println("<img src='graph?width=1024&type=LINE&title=MEMORY&ytitle=%25&mem=kbmemfree.txt:_memused&swap=kbswpfree.txt:_swpused'>");
            } else {
                out.println("<img src='graph?width=1024&type=LINE&title=MEMORY&ytitle=%25&mem=kbmemfree.txt:_memused,_swpused'>");
            }
        %>    
        <%
            Pattern p=Pattern.compile("IFACE\\.(.*)\\.txt");
            for(Object o:csvs.list(p)){
                Matcher m=p.matcher(o.toString());
                if( m.matches() && !m.group(1).equals("lo") ){
                    CSV c=csvs.get(o.toString());
                    if( c.hasField("rxkB_s") ){
                        out.println("<img src='graph?width=1024&type=LINE&title=Network&ytitle=kb/s&"+m.group(1)+"="+o.toString()+":rxkB_s*8,txkB_s*8'>");
                    } else if( c.hasField("rxbyt_s") ){
                        out.println("<img src='graph?width=1024&type=LINE&title=Network&ytitle=kb/s&"+m.group(1)+"="+o.toString()+":rxbyt_s*8/1024,txbyt_s*8/1024'>");
                    }
                }
            }
        %>    
        <img src="graph?width=1024&type=LINE&title=Transactions IO&ytitle=IO/s&tps=tps.txt:rtps,wtps">
<%
// look for DEV.sd{bcdefg} SSD disks
String util="", tps="", rsp="", size="";
/*            
Pattern diskPattern=Pattern.compile("DEV.(sd[b-g]).txt");
for(Object e: csvs.list()) {
    String fn=e.toString();
    Matcher m=diskPattern.matcher(fn);
    if( m.matches() ){
        util += "&"+m.group(1)+"="+fn+":_util";
        tps += "&"+m.group(1)+"="+fn+":tps";
        rsp += "&"+m.group(1)+"="+fn+":await%2Bsvctm";
        size += "&"+m.group(1)+"="+fn+":rd_sec_s/2,wr_sec_s/2";
    }
}
if( !util.isEmpty() ){
    out.println("<img src='graph?width=1024&type=LINE&title=IO SSD - %25 utilisation&ytitle=%"+util+"'>");
    out.println("<img src='graph?width=1024&type=LINE&title=IO SSD - nombre IO/s&ytitle=IO/s"+tps+"'>");
    out.println("<img src='graph?width=1024&type=LINE&title=IO SSD - temps IO&ytitle=ms"+rsp+"'>");
    out.println("<img src='graph?width=1024&type=LINE&title=IO SSD - Taille IO&ytitle=k-octets"+size+"'>");
}
*/
        
        
// look for IO SAN (graphIt checked)
HashMap deviceGraph=(HashMap) session.getAttribute("deviceGraph"); // dev-nickname => graphTag
// reverse key, one graph per tag
HashMap tags=new HashMap(); // graphTag=>[devgraph key]
for(Object e: deviceGraph.keySet().toArray()) {
    String d=e.toString();
    
    String tag=(String)deviceGraph.get(d);
    ArrayList dgs=(ArrayList)tags.get(tag);
    if( dgs==null ) // key not found
        dgs=new ArrayList();
    
    dgs.add(d);
    tags.put(tag, dgs);
}

Boolean sadcDisplayed=false;

for(Object e: tags.keySet().toArray()) {
    String tag=e.toString();
    
    ArrayList dgs=(ArrayList)tags.get(tag);

    util="";
    tps="";
    size="";
    rsp="";

    int idx=1;
    for(Object d: dgs){
    
        CSV c=csvs.get("DEV."+d+".txt");
        if( c==null ){
            out.println("<p>Les informations sysstat pour '"+d+"' sont introuvables. ");
            if( ! sadcDisplayed )
                out.println("<br>V&eacute;rifier la configuration /etc/sysconfig/sysstat "
                        + "<br><b>SADC_OPTIONS=\"-S DISK\"</b>");
            out.println("</p>");
            sadcDisplayed=true;
            continue;
        }

        String varn=tag+idx;
        idx++;
        
        varn.replaceAll("[+\\-]", "");

        if( c!=null && c.hasField("_util") )
            util += "&"+varn+"=DEV."+d+".txt:_util";
        tps += "&"+varn+"=DEV."+d+".txt:tps";
        size += "&"+varn+"=DEV."+d+".txt:rd_sec_s/2,wr_sec_s/2";
        rsp += "&"+varn+"=DEV."+d+".txt:await%2Bsvctm";

    }
    if( !util.isEmpty() )
        out.println("<img src='graph?width=1024&type=LINE&title=IO "+tag+" - %25 utilisation&ytitle=%25"+util+"'>");
    if( !tps.isEmpty() ){
        out.println("<img src='graph?width=1024&type=LINE&title=IO "+tag+" - nombre IO/s&ytitle=IO/s"+tps+"'>");
        out.println("<img src='graph?width=1024&type=LINE&title=IO "+tag+" - temps IO&ytitle=ms"+rsp+"'>");
        out.println("<img src='graph?width=1024&type=LINE&title=IO "+tag+" - Taille IO&ytitle=k-octets"+size+"'>");
    }
}

%>        
        
    </body>
</html>
