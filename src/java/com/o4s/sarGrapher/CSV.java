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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author trouvin
 */
public class CSV {
    
    private String filename=null;
    private BufferedReader f = null;
    private HashMap headers=null;
    private String[] fields=null;
    private int lineno=0;
    private String sep=",";
    
    private ArrayList lines=null; // to store in memory table lines
    private boolean inMemory=false;
    
    /**
     *
     * @param filename
     * @param sep
     */
    public CSV(String filename, String sep){
        this.sep=sep;
        this.filename=filename;
    }
    
    /**
     *
     * @param filename
     */
    public CSV(String filename){
        this.filename=filename;
    }
    
    /**
     * Use this form when you want to create CSV table in memory
     * 
     * @param filename
     * pseudo to retrieve the CSV table
     * 
     * @param header
     * A list of fields
     */
    public CSV(String filename, String[] header){
        this.filename=filename;
        this.fields=header;
        inMemory=true;
        lines=new ArrayList();
    }
    
    /**
     *
     * @param in
     * @return
     * @throws IOException
     * @throws Exception
     */
    public Boolean open(InputStream in) throws IOException, Exception{
        f=new BufferedReader(new InputStreamReader(in));
        String line=f.readLine();
        if( line.isEmpty() ){
            throw new IOException("No Header found, file is empty");
        }
        fields=line.split(sep);
        
        return next();
    }
    
    /**
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public Boolean open() throws FileNotFoundException, IOException, Exception{
        if( fields==null ){
            this.filename=filename;
            f=new BufferedReader(new FileReader(filename));

            String line=f.readLine();
            if( line.isEmpty() ){
                throw new IOException("No Header found, file is empty");
            }
            fields=line.split(sep);
        } else {
            rewind();
        }
        
        return next();
    }
    
    /**
     *
     * @throws IOException
     */
    public void rewind() throws IOException{
        if( f!=null )
            f.reset();
        lineno=0;
    }
    
    /**
     *
     * @return
     */
    public String filename(){
        return filename;
    }
    
    /**
     *
     * @return
     */
    public String sep(){
        return sep;
    }

    /**
     *
     * @param sep
     * @return
     */
    public String sep(String sep){
        this.sep=sep;
        return sep;
    }
    
    /**
     *
     * @return
     * @throws IOException
     * @throws Exception
     */
    public Boolean next() throws IOException, Exception{
        if( headers==null ){
            headers=new HashMap();
        }
        if( f==null && !inMemory ){
            this.open();
        }
        String line;
        do {
            if( inMemory ){
                if( lineno<lines.size() )
                    line=(String) lines.get(lineno);
                else
                    line=null;
            } else {
                line=f.readLine();
            }
            lineno++;
        } while( line!=null && line.isEmpty() );
        if( line==null ){
            // end of file reached
            return false;
        }
        String[] values=line.split(sep);
        if(values.length != fields.length){
            throw new Exception("Missing fields at line "+filename+":"+lineno);
        }
        for(int i=0; i<fields.length; i++){
            String v=values[i];
            if( v.startsWith("\"") && v.endsWith("\"") )
                v=v.substring(1, v.length()-1 );
            headers.put(fields[i], v);
        }
        
        return true;
    }
    
    /**
     *
     * @param fieldName
     * @return
     */
    public Object get(String fieldName){
        return headers.get(fieldName);
    }
    
    /**
     *
     * @return
     */
    public String[] getFields(){
        return fields;
    }
    
    
    public void put(String line){
        lines.add(line);
        
    }
    
    public Boolean hasField(String name) throws Exception{
        if( headers==null )
            next();
        return headers.containsKey(name);
    }
}
