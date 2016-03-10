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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class CSVTest {
    
    String header_txt="resources/__HEADER__.txt";
    String cpu_all_txt="resources/CPU.all.txt";
    
    public CSVTest() {
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
     * Test of open method, of class CSV.
     */
    @Test
    public void testOpen() throws Exception {
        System.out.println("open");
        CSV instance = new CSV(header_txt);
        //System.out.println(this.getClass().getClassLoader().getResource(header_txt));
        instance.open(this.getClass().getClassLoader().getResourceAsStream(header_txt));
        assertEquals(header_txt, instance.filename());

        instance = new CSV("__Does_not_exists__");
        try {
            instance.open();
        } catch(FileNotFoundException e){
            assertTrue(true);
        }
    }

    /**
     * Test of sep method, of class CSV.
     */
    @Test
    public void testSep_String() {
        System.out.println("sep");
        String sep = ";";
        CSV instance = new CSV(header_txt);
        try {
            instance.open(this.getClass().getClassLoader().getResourceAsStream(header_txt));
        } catch (IOException ex) {
            Logger.getLogger(CSVTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException ");
        } catch (Exception ex) {
            Logger.getLogger(CSVTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception ");
        }
        assertEquals(",", instance.sep());
        String expResult = ";";
        String result = instance.sep(sep);
        assertEquals(expResult, result);
    }

    /**
     * Test of next method, of class CSV.
     */
    @Test
    public void testNext() throws Exception {
        System.out.println("next");
        CSV instance = new CSV(header_txt);
        assertTrue(instance.open(getClass().getClassLoader().getResourceAsStream(header_txt)));
        assertFalse(instance.next());
        instance = new CSV("CPU.all.txt");
        instance.open(getClass().getClassLoader().getResourceAsStream(cpu_all_txt));
        assertTrue(instance.next());
        assertTrue(instance.next());
    }

    /**
     * Test of get method, of class CSV.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        CSV instance = new CSV(header_txt);
        try {
            assertTrue(instance.open(getClass().getClassLoader().getResourceAsStream(header_txt)));
        } catch (IOException ex) {
            Logger.getLogger(CSVTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException ");
        } catch (Exception ex) {
            Logger.getLogger(CSVTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Exception ");
        }
        assertEquals("Linux", instance.get("OS"));
    }
    
}
