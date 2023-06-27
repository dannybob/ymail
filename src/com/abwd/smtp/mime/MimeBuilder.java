package com.abwd.smtp.mime;

import cn.hutool.core.collection.ListUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * mime构建
 */
public class MimeBuilder {
    public MimeBuilder(){

    }
    public MimeBuilder(List<String>lines){
        this.lines= ListUtil.toList(lines);
    }
    private List<String>lines=new ArrayList<>();
    public  MimeDomain addMimeDomain(MimeDomain mimeDomain){
       return mimeDomain;
    }
    public List<String> getLines() {
        return lines;
    }

    protected void setLines(List<String> lines) {
        this.lines = lines;
    }

    public MimeBuilder addSpaceLine(){
        this.lines.add("");
        return this;
    }

    public MimeBuilder addLine(String line){
        this.lines.add(line);
        return this;
    }

}
