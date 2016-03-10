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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 *
 * @author trouvin
 */

public class FindFiles {
    
    private Finder finder=null;
    private Path startingDir=Paths.get("data");
    private String pattern="*";

    public FindFiles(){
        
    }
    public FindFiles(String dir, String pattern){
        startingDir=Paths.get(dir);
        this.pattern=pattern;
    }
    
    public FindFiles(String pattern) throws IOException{
     
        this.pattern=pattern;
    }
    
    public ArrayList walk() throws IOException{
        finder = new Finder(pattern);
        Files.walkFileTree(startingDir, finder);
        return finder.done();
    }
    
}

class Finder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    //private int numMatches = 0;
    private ArrayList fileList=new ArrayList();

    Finder(String pattern) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    // Compares the glob pattern against
    // the file or directory name.
    void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(name) && file.toFile().isFile() && file.toFile().exists() ){
            //numMatches++;
            //System.out.println(file);
            fileList.add(file);
        }
    }

    // Prints the total number of
    // matches to standard out.
    ArrayList  done() {
        //System.out.println("Matched: " + numMatches);
        return fileList;
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}


