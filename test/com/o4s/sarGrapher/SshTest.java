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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author trouvin
 */
public class SshTest {
    
    public SshTest() {
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
     * Test of setConfig method, of class Ssh.
     */
    @Test
    public void testSetConfig() throws JSchException {
        System.out.println("setConfig");
        String key = "compression_level";
        String value = "9";
        Ssh instance = new Ssh("root","ivlrhs01");
        instance.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
        instance.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
        instance.setConfig(key, value);
    }

    /**
     * Test of addIdentity method, of class Ssh.
     */
    @Test
    public void testAddIdentity_String() throws Exception {
        System.out.println("addIdentity");
        String sshKeyFileName = this.getClass().getClassLoader().getResource("resources/id_rsa").toString();
        Ssh instance = new Ssh("root","ivlrhs01");
        instance.addIdentity(sshKeyFileName.substring(6,sshKeyFileName.length()));
    }

    /**
     * Test of setCommand method, of class Ssh.
     */
    @Test
    public void testExec() throws JSchException {
        System.out.println("setCommand");
        String command = "hostname";
        Ssh instance = new Ssh("root","ivlrhs01");
        String sshKeyFileName = this.getClass().getClassLoader().getResource("resources/id_rsa").toString();
        instance.addIdentity(sshKeyFileName.substring(6,sshKeyFileName.length()));
        String sshKnownHosts = this.getClass().getClassLoader().getResource("resources/known_hosts").toString();
        instance.setKnownHosts(sshKnownHosts.substring(6,sshKnownHosts.length()));
        try {
            BufferedReader in=instance.exec(command);
            for(;;){
                String line=in.readLine();
                if( line==null )
                    break;
                System.out.println("<"+line);
            }
            instance.execEnd();
        } catch (IOException ex) {
            Logger.getLogger(SshTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("connected");
        instance.disconnect();
    }

}
