package com.abwd.commands;

import java.util.Map;

public class LoginCommand extends  AbsCommand{
    @Override
    public String buildComand(Object param) {
        Map<String,String> map=(Map<String, String>)param;
        String account=map.get("account");
        String pwd=map.get("pwd");
        setCommandContent("C2 LOGIN "+account+" "+pwd);
        return  getCommand();
    }
}
