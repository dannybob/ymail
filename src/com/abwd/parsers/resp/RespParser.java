package com.abwd.parsers.resp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.MultipartOutputStream;
import com.abwd.parsers.resp.wrappers.FetchMailWrapper;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.internal.ws.encoding.MimeMultipartParser;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

  public static List<String> LSUB(List<String> lines){
       List<String> list= CollectionUtil.newArrayList();
       for(int i=0;i<lines.size()-1;i++){
           String[] arr= lines.get(i).split(" ");
           list.add(arr[arr.length-1]);
       }
        return list;
  }
    public static int SELECT(List<String> lines){
            String[] arr= lines.get(0).split(" ");
        return Integer.parseInt(arr[1]);
    }
    public static List<FetchMailWrapper> UID_FETCH(List<String> lines){
       // MimeMultipartParser mimeMultipartParser=new MimeMultipartParser();
        List<FetchMailWrapper>fetchMailWrapperlist=new ArrayList<>();
        FetchMailWrapper fetchMailWrapper=null;
        String boundary=null;
        StringBuffer content=null;
        boolean boundaryStart=false;
        for(int i=0;i<lines.size()-1;i++){
            if(lines.get(i).startsWith("* ")){
                fetchMailWrapper=new FetchMailWrapper();
                content=new StringBuffer();

            }else if(!lines.get(i).startsWith(")")){
                content.append(lines.get(i).endsWith("=")?lines.get(i).substring(0,lines.get(i).length()-1):(i<=(lines.size()-2)&&lines.get(i+1).startsWith("\t"))?lines.get(i):lines.get(i)+"\r\n");
            }
            if(lines.get(i).startsWith("From:")){
                fetchMailWrapper.setFrom(lines.get(i).substring(5));
            }
            if(lines.get(i).startsWith("To:")){
                fetchMailWrapper.setTo(lines.get(i).substring(3));
            }
            if(lines.get(i).startsWith("Date:")){
                fetchMailWrapper.setDate(lines.get(i).substring(5));
            }
            if(lines.get(i).startsWith("\tboundary=")){
                 boundaryStart=true;
                 fetchMailWrapper.setBoundary(boundary);
            }
            if(lines.get(i).startsWith(")")){
                boundaryStart=false;
                fetchMailWrapper.setContent(content.toString().replaceAll("=3D","=").replaceAll("=0A",""));
                List<String> htmls= ReUtil.findAllGroup0("<html>[\\s\\S]*?</html>",fetchMailWrapper.getContent());
                if(!CollectionUtil.isEmpty(htmls)){
                    fetchMailWrapper.setHtml(htmls.get(0));
                }
                fetchMailWrapperlist.add(fetchMailWrapper);
            }
        }

        return fetchMailWrapperlist;
    }


}
