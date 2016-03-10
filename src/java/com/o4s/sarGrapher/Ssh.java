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

import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Ssh{
    
    private JSch jsch=new JSch();
    private Session session=null;
    private ChannelExec channel=null;
    private String user="";
    private String host="";
    private String channelError="";
    private int channelExitStatus=0;
    private int timeout=5000; // ms
    
    public Ssh(String user, String host) throws JSchException{
        this.user=user;
        this.host=host;

        session = jsch.getSession(user, host);

    }

    public void setConfig(String key, String value){
        session.setConfig(key, value);
    }
    public void setTimeout(int timeout){
        this.timeout=timeout;
    }
    public void setTimeout(String timeout){
        int t=Integer.parseInt(timeout);
        if( t>0 )
            this.timeout=t;
    }

    public void addIdentity(String sshKeyFileName) throws JSchException{
        jsch.addIdentity(sshKeyFileName);
    }
    public void addIdentity(String sshKeyFileName, String passphrase) throws JSchException{
        jsch.addIdentity(sshKeyFileName, passphrase);
    }
    public void addPrivKey(String privkeyString) throws JSchException{
        //byte[] pk = Base64.decode(privkeyString.getBytes());
        //jsch.addIdentity(this.user+"@"+this.host,pk,null,null);
        jsch.addIdentity(this.user+"@"+this.host,privkeyString.getBytes(),null,null);
    }
    public void setKnownHosts(String filename) throws JSchException{
        jsch.setKnownHosts(filename);
    }

    
    public void setPassword(String password){
        session.setPassword(password);
    }
    
    public BufferedReader exec(String command) throws JSchException, IOException{
        if( !session.isConnected())
            session.connect(timeout);
        channel= (ChannelExec) session.openChannel("exec");
        channel.setPty(false);
        channel.setCommand(command);
        
        channelError="";
        channelExitStatus=0;
        
        channel.connect();
        
        return new BufferedReader(new InputStreamReader(channel.getInputStream()));
    }
    
    public void execEnd() throws IOException{
        channelExitStatus=channel.getExitStatus();
        BufferedReader be=new BufferedReader(new InputStreamReader(channel.getErrStream()));
        for(;be.ready();){
            String line=be.readLine();
            if( line==null )
                break;
            channelError += line+"\n";
        }
        
        channel.disconnect();
    }
    
    public ArrayList execComplete(String command) throws JSchException, IOException{
        BufferedReader bi=exec(command);
        ArrayList ret=new ArrayList();
        
        for(;;){
            String line=bi.readLine();
            if( line==null )
                break;
            ret.add(line);
        }
        execEnd();
        
        return ret;
    }
    
    public String execError(){
        return channelError;
    }
    public Boolean execHasError(){
        return !channelError.isEmpty();
    }
    public int execStatus(){
        return channelExitStatus;
    }
    
    public void disconnect() {
        session.disconnect();
    }

    public String getUser(){
        return user;
    }
    public String getHost(){
        return host;
    }
    
}
  

