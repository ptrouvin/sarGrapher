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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author trouvin
 */
public class FindFilesTest {
    
    public FindFilesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of walk method, of class FindFiles.
     */
    @Test
    public void testWalk() throws Exception {
        String r=this.getClass().getClassLoader().getResource("resources/__HEADER__.txt").toString();
        FindFiles instance = new FindFiles(r.substring(6,r.length()-15),"*");
        ArrayList result = instance.walk();
        if( result.size()!=2 )
            fail(result.size()+" entries while 2 attented");
        for(Iterator it=result.iterator(); it.hasNext();){
            File f=new File(it.next().toString());
            String fn=f.getName();
            if( ! fn.equals("__HEADER__.txt") && ! fn.equals("CPU.all.txt") )
                fail("Unknown file "+fn);
        }
    }
    
}
