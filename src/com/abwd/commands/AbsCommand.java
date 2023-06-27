package com.abwd.commands;

public abstract class AbsCommand  {

    private String commandContent;

    public  String getCommand(){
        return commandContent;
    }

    protected void  setCommandContent(String commandContent){
        this.commandContent=commandContent;
    }

   public abstract String buildComand(Object param);

}
