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
package com.o4s.sarGrapher;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author trouvin
 */
public class sshSA2CSV extends HttpServlet {
    
    static CSVs csvs=new CSVs();
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        
        HttpSession session=request.getSession();
        if( session==null || session.getAttribute("jsch")==null ){
            response.sendRedirect("index.jsp");
            return;
        }
        Ssh ssh=(Ssh) session.getAttribute("jsch");
        
        Boolean details=(Boolean) session.getAttribute("details");
        
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
            
        String error="";

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet sshSA2CSV</title>\n"
                + "<script type=\"text/javascript\" src=\"js/jquery.min.js\"></script>\n");  
        out.println("<style>#closeThis{"
                + " background-color: lightsalmon;"
                + " color: red;"
                + "}</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>R&eacute;p&eacute;ration des donn&eacute;es SAR brutes</h1>");
        out.println("<h2>"+(String)session.getAttribute("user")+"@"+(String)session.getAttribute("host")+"</h2>");

        out.println("<p id='closeThis'><big>PATIENTER JUSQU'&agrave; RECUPERATION COMPLETE DES DONNEES</big></p>");

        String saFn=request.getParameter("sa");

        BufferedReader bi;
        try {

            HashMap deviceInfo=(HashMap)session.getAttribute("deviceInfo");

            csvs.reset();

            bi = ssh.exec("sar -A -f "+saFn+" 2>&1");

            Boolean headerRead=false;
            String[] fields=null;
            String type="";
            Calendar docDate=null;
            Calendar lastDate=null;
            HashMap types=new HashMap();
            SimpleDateFormat spfDate=new SimpleDateFormat("YYYY/MM/dd");
            SimpleDateFormat spfHour=new SimpleDateFormat("HH:mm:SS");
            for(int lineno=0;; lineno++){
                String line=bi.readLine();
                if( line==null )
                    break;

                if( headerRead && !line.isEmpty() && !line.matches("^[0-9].*") )// to skip line like 'Moyenne: ...', but never ignore empty line, require for end of block detect
                    continue;
                if( ! headerRead ){
                    // Linux 2.6.32-431.23.3.el6.x86_64 (localhost.localdomain) 	01/09/2014 	_x86_64_	(2 CPU)
                    Matcher m=Pattern.compile("^(?<OS>\\S+)\\s+(?<OSVERSION>\\S+)\\s+\\((?<hostname>[^)]+)\\)\\s+(?<date>\\S+)\\s+(?<arch>\\S+)\\s+\\((?<procs>\\d+) CPU\\)").matcher(line);
                    if(!m.matches()){
                        // RHEL5.3
                        // Linux 2.6.18-348.el5 (ppldb203.bfbp.banquepopulaire.fr) 12/09/2014
                        m=Pattern.compile("^(?<OS>\\S+)\\s+(?<OSVERSION>\\S+)\\s+\\((?<hostname>[^)]+)\\)\\s+(?<date>\\S+)").matcher(line);
                        if(!m.matches()){
                            error +="<br />Header is invalid line:"+lineno+" : "+line;
                            break;
                        }
                        // reading date
                        String[] flds=m.group("date").split("[^0-9]");
                        docDate=new GregorianCalendar(Integer.parseInt(flds[2]), Integer.parseInt(flds[1]), Integer.parseInt(flds[0]));
                        Logger.getLogger(sshSA2CSV.class.getName()).info("Date read from header: "+spfDate.format(docDate.getTime()));

                        csvs.put("__HEADER__.txt", new String[]{"OS","OS_VERSION","hostname","date","arch","procs"});
                        csvs.put("__HEADER__.txt", m.group("OS")+","+m.group("OSVERSION")+","+m.group("hostname")+","+m.group("date")+",?,?");
                    } else {
                        // reading date
                        String[] flds=m.group("date").split("[^0-9]");
                        docDate=new GregorianCalendar(Integer.parseInt(flds[2]), Integer.parseInt(flds[1]), Integer.parseInt(flds[0]));
                        Logger.getLogger(sshSA2CSV.class.getName()).info("Date read from header: "+spfDate.format(docDate.getTime()));

                        csvs.put("__HEADER__.txt", new String[]{"OS","OS_VERSION","hostname","date","arch","procs"});
                        csvs.put("__HEADER__.txt", m.group("OS")+","+m.group("OSVERSION")+","+m.group("hostname")+","+m.group("date")+","+m.group("arch")+","+m.group("procs"));
                    }
                    headerRead=true;


                } else if( line.isEmpty() ){
                    // end of block
                    type="";
                    fields=null;
                    lastDate=null;
                } else if(type.isEmpty()){
//                        # lines with labels: date is repeated
//                        # 00:00:01        CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest     %idle
//                        # 00:00:01          DEV       tps  rd_sec/s  wr_sec/s  avgrq-sz  avgqu-sz     await     svctm     %util
//                        # 00:10:01       dev8-0      3,66      0,01     58,29     15,91      0,01      1,85      1,05      0,39
//                        # 00:10:01      dev11-0      0,00      0,00      0,00      0,00      0,00      0,00      0,00      0,00
                    line=line.replaceAll("[/%]+", "_");
                    fields=line.split("\\s+");
                    type=fields[1].replaceAll("[/]", "_"); // filtering char
                    if( types.containsKey(type) && types.get(type).toString().equals(fields[0]) ){
                        type +="1";
                    } else {
                        types.put(type, fields[0]);
                    }

                    String[] f=fields[0].split(":");
                    lastDate=(Calendar) docDate.clone(); 
                    lastDate.set(Calendar.HOUR, Integer.parseInt(f[0]));
                    lastDate.set(Calendar.MINUTE, Integer.parseInt(f[1]));
                    lastDate.set(Calendar.SECOND, Integer.parseInt(f[2]));
                } else {
                    line = line.replaceAll(",", "."); // decimal char , -> .
                    String[] values=line.split("\\s+");

                    String[] f=values[0].split(":");
                    Calendar dt=(Calendar) docDate.clone(); 
                    dt.set(Calendar.HOUR, Integer.parseInt(f[0]));
                    dt.set(Calendar.MINUTE, Integer.parseInt(f[1]));
                    dt.set(Calendar.SECOND, Integer.parseInt(f[2]));

                    String label="";
                    int firstField=1;

                    if( type.matches("^(CPU|TTY|DEV|IFACE)") )	{  //# line with label
                        label=values[1];
                        if( type.equals("DEV") && deviceInfo.containsKey(label) ){
                            label=(String) deviceInfo.get(label);
                        }
                        firstField=2;
                    }

                    //} else 					{ # line w/o label

                    String filename=type+(label.isEmpty() ? "":"."+label)+".txt";

                    ArrayList fl=new ArrayList();
                    if( !csvs.exists(filename) ){
                        fl.add("date");fl.add("heure");
                        for(int i=firstField; i<fields.length;i++)
                            fl.add(fields[i]);
                        csvs.put(filename, (String[]) fl.toArray(new String[fl.size()]));
                    }

                    StringBuilder b=new StringBuilder();
                    b.append("\"").append(spfDate.format(dt.getTime())).append("\",");
                    b.append("\"").append(spfHour.format(dt.getTime())).append("\"");
                    for(int i=firstField; i<values.length;i++)
                        b.append(",").append(values[i]);
                    csvs.put(filename, b.toString());
                }

            }
            ssh.execEnd();

        } catch (JSchException ex) {
            Logger.getLogger(sshSA2CSV.class.getName()).log(Level.SEVERE, null, ex);
            error += ex.getMessage();
        }

        out.println("<script>\n"
                + "$(function(){\n"
                + " $('.filename').each(function(){\n"
                + "     $(this).click(function(){\n"
                + "         var obj=$(this).find('.csvTable').get(0);\n"
                + "         var vis=$(obj).css('display')!='none';\n"
                + "         $('.csvTable').hide();\n"
                + "         if(vis){\n"
                + "             $(obj).hide();\n"
                + "         }else{\n"
                + "             $(obj).show();\n"
                + "         }\n"
                + "     });\n"
                + "     $(this).css('cursor','pointer');\n"
                + " });\n"
                + " $('.csvTable').hide().click(function(e){\n"
                + "     e.preventDefault();\n"
                + "     $(this).hide();\n"
                + "     return false;\n"
                + " });\n"
                + "});\n"
                + "function toggle(obj){\n"
                + " $(obj).find('.csvTable').toggle();\n"
                + "}\n"
                + "</script>");


        out.println("<div style=style=\"color: lightyellow; background-color: lightsalmon; font-weight: bold;\">"+error+"</div>");

        out.println("<div>CSV tables loaded:<br/>");

        out.println("<a href='graphSsh.jsp' target=_blank><button>Graphs</button></a>");
        out.println("<a href='index.jsp'><button>Retour</button></a>");

        out.println("<ul>");
        for(Object e: csvs.list() ) {
            String fn=e.toString();
            out.println("<li class='filename'>"+fn);
            out.println("<table class='csvTable'>\n");
            CSV c=csvs.get(fn);
            Boolean first=true;
            try {
                while(c.next()){
                    if( first ){
                        out.println("<tr>");
                        for(String f: c.getFields()){
                            out.println("<th>"+f+"</th>");
                        }
                        out.println("</tr>\n");
                        first=false;
                        
                        if( ! details && !fn.startsWith("__") )
                            // get out if details not wanted
                            break;
                        
                    }
                    out.println("<tr>");
                    for(String f: c.getFields()){
                        out.println("<td>"+c.get(f)+"</td>");
                    }
                    out.println("</tr>\n");
                }
            } catch (Exception ex) {
                Logger.getLogger(sshSA2CSV.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.println("</table>\n");
            out.println("</li>\n");
        };
        out.println("</ul>");
        out.println("</div>");

        out.println("<a href='graphSsh.jsp' target=_blank><button>Graphs</button></a>");
        out.println("<a href='index.jsp'><button>Retour</button></a>");

        out.println("<script>$('#closeThis').hide()</script>");

        out.println("</body>");
        out.println("</html>");
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(sshSA2CSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(sshSA2CSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
