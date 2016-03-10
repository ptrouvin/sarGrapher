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

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author trouvin
 */
public class graph extends HttpServlet {

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
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        String title="";
        String ytitle="";
        int width=400;
        int height=300;
        String graphType="LINE";
        Boolean useSSH=false;
        HttpSession session=request.getSession();
        if( session!=null && session.getAttribute("jsch")!=null ){
            useSSH=true;
        }
        
        for(Enumeration e=request.getParameterNames(); e.hasMoreElements();){
            String name=e.nextElement().toString();
            String val=request.getParameter(name);
            if( val==null )
                continue; // seems unnecessary but for code clarity
            else if( name.equals("title") )
                title=val;
            else if( name.equals("ytitle") )
                ytitle=val;
            else if( name.equals("width") )
                width=Integer.parseInt(val);
            else if( name.equals("height") )
                height=Integer.parseInt(val);
            else if( name.equals("useSSH") )
                useSSH=true;
            else if( name.equals("type") )
                graphType=val;
            else {
                String[] fflds=val.split(":");
                String fn=fflds[0];
                String[] flds=fflds[1].split(",");
                
                if( fn==null ){
                    Logger.getLogger(graph.class.getName()).log(Level.SEVERE, "Unable to find filename from: {0}={1}", new Object[]{name, val});
                    return;
                }

                CSV c=null;
                if( useSSH ){
                    CSVs cs=new CSVs();
                    c=cs.get(fn);
                }
                if( c==null ){
                    Logger.getLogger(graph.class.getName()).log(Level.SEVERE, "Unable to find CSV: {0}", fn);
                    return;
                }
                
                ArrayList ds=new ArrayList();
                
                for(int i=0; i<flds.length; i++)
                    ds.add(new TimeSeries(name+"."+flds[i]));
        
                try {
                    c.open();
                    do {
                        try {
                            if( !c.next() )
                                break;
                        } catch(Exception ex){ // Missing field
                            if( ex.getMessage().startsWith("Missing fields") )
                                break;
                            Logger.getLogger(graph.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        
                        String heure=(String)c.get("heure");
                        for(int i=0; i<flds.length; i++){
                            Double fv;
                            
                            val=(String)c.get(flds[i]);
                            if( val!=null ){
                                fv=Double.parseDouble(val);
                            } else {
                                // try to replace variable
                                String str=flds[i];
                                for(String s: c.getFields()){
                                    str=str.replaceAll(s, c.get(s).toString());
                                }
                                Expression exp= new ExpressionBuilder(str).build();
                                ValidationResult v=exp.validate();
                                String errorMsg="";
                                if( !v.isValid() ){
                                    for(String s :v.getErrors()){
                                        errorMsg += (errorMsg.isEmpty() ? "":"," )+s;
                                    }
                                    if( !errorMsg.isEmpty() ){
                                        Logger.getLogger(graph.class.getName()).log(Level.SEVERE, "Syntax ERROR or missing variables: {0}", errorMsg);
                                    }
                                }
                                fv=exp.evaluate();
                                
                            }
                            //dataset.addValue(fv,flds[i],heure);
                            Date dt = new SimpleDateFormat("HH:mm:ss").parse(heure);
                            ((TimeSeries)ds.get(i)).addOrUpdate(new Minute(dt), fv);
                        }
                    } while(true);
                } catch (Exception ex) {
                    Logger.getLogger(graph.class.getName()).log(Level.SEVERE, null, ex);
                }                
                
                for(int i=0; i<flds.length; i++)
                    dataset.addSeries((TimeSeries)ds.get(i));
        
            }
        }
        //JFreeChart barChart = ChartFactory.createBarChart(title, "", "Unité vendue", dataset, PlotOrientation.VERTICAL, true, true, false); 
        //JFreeChart barChart = ChartFactory.createTimeSeriesChart(title, title, dataDir, dataset);// .createStackedBarChart(title, "", "", dataset); 
//        JFreeChart timechart = ChartFactory.createTimeSeriesChart(  
//            title, // Title  
//            "Heure",         // X-axis Label  
//            "",       // Y-axis Label  
//            dataset,        // Dataset  
//            true,          // Show legend  
//            true,          // Use tooltips  
//            false          // Generate URLs  
//        );  
//        Plot p=timechart.getPlot();
        
        XYItemRenderer r;
        
        if( graphType.equals("AREA") ){
            r = new XYAreaRenderer();
        } else if( graphType.equals("BAR") ){
            r = new XYBarRenderer();
        } else if( graphType.equals("STACK") ){
            r = new StackedXYAreaRenderer();
        } else {
            r = new StandardXYItemRenderer();        
        }
        
        final DateAxis domainAxis = new DateAxis("Heure");
        domainAxis.setVerticalTickLabels(false);
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));                        
        domainAxis.setLowerMargin(0.01);
        domainAxis.setUpperMargin(0.01);
        final ValueAxis rangeAxis = new NumberAxis(ytitle);        
        final XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, r);      
        final JFreeChart chart = new JFreeChart(title, plot);             

          
//        JFreeChart timechart=null;
//        if( graphType.equals("AREA") ){
//            timechart = ChartFactory.createXYAreaChart(title, "Heure", "", dataset, PlotOrientation.VERTICAL, true, true, false); 
//        } else if( graphType.equals("BAR") ){
//            timechart = ChartFactory.createXYBarChart(title, "Heure", true, "", dataset, PlotOrientation.VERTICAL, true, true, false); 
//        } else {
//            timechart = ChartFactory.createXYLineChart(title, "Heure", "", dataset, PlotOrientation.VERTICAL, true, true, false); 
//        }
        
        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        ChartUtilities.writeChartAsPNG(out, chart, width, height); 
        
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
