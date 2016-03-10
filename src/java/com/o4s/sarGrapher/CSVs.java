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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author trouvin
 */
public class CSVs {
    
    private static HashMap csv=new HashMap();
    
    public CSVs(){
        
    }
    
    public void reset(){
        csv.clear();
    }
    
    public void put(String filename, String[] header) throws Exception{
        CSV c=null;
        if( !csv.containsKey(filename) ){
            c=new CSV(filename,header);
            csv.put(filename, c);
        } else {
            throw new Exception(filename+" Already exists");
        }
    }
    public void put(String filename, String line) throws Exception{
        CSV c=null;
        if( !csv.containsKey(filename) ){
            throw new Exception("Create before using: "+filename);
        } else {
            c=(CSV) csv.get(filename);
        }
        c.put(line);
    }
    
    public boolean exists(String filename){
        return csv.containsKey(filename);
    }
    
    public CSV get(String filename){
        return (CSV) csv.get(filename);
    }
    
    public Set list(){
        return csv.keySet();
    }
    
    public Set list(Pattern pat){
        HashMap ret=new HashMap();
        for(Object e:csv.keySet() ){
            Matcher m=pat.matcher(e.toString());
            if( m.matches() && ! ret.containsKey(e.toString()) )
                ret.put(e.toString(),1);
        }
        return ret.keySet();
    }
}
