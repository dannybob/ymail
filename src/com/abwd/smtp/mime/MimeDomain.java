package com.abwd.smtp.mime;

import sun.security.util.Length;

import java.util.ArrayList;
import java.util.List;

public class MimeDomain {
    MimeBuilder host;

    String newLineStartChar="\t";
    public MimeDomain(String name, String value,MimeBuilder host) {
        this.name = name;
        this.value = value;
        this.host=host;
        lines.add(name+": "+value);
    }

    String name;

    String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MimeDomain appendNewLine(String line){

        lines.add(newLineStartChar+line);
        return this;
    }


    public List<String> getLines() {
        return lines;
    }

    List<String> lines=new ArrayList<>();

   public MimeBuilder completeDomain(){
        this.host.getLines().addAll(lines);
        return  host;
    }

}
