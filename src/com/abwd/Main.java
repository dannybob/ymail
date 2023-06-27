package com.abwd;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.SystemPropsUtil;
import cn.hutool.json.JSONUtil;
import com.abwd.commands.LoginCommand;
import com.abwd.parsers.resp.RespParser;
import com.abwd.parsers.resp.wrappers.FetchMailWrapper;
import com.abwd.smtp.mime.MimeBuilder;
import com.abwd.smtp.mime.MimeDomain;
import com.abwd.utils.CommonUtil;
import com.sun.org.apache.xml.internal.security.Init;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

    private static Connection imapConnection=null;
    private static Connection smtpConnection=null;

    public static void main(String[] args) {
            testImap();
           // testSmtp();
    }


    static void testSmtp(){
        try{
            connectSMTPServer();
            //EHLO LAPTOP-KV9SHLP4\
            RespMessage respMessage=  sendMessage(Constant.PROTOCOL_SMTP,"EHLO "+ System.getenv("USERDOMAIN_ROAMINGPROFILE"));
            resetRespMessage(respMessage);

            respMessage=  sendMessage(Constant.PROTOCOL_SMTP,"AUTH LOGIN");
            resetRespMessage(respMessage);

            respMessage=  sendMessage(Constant.PROTOCOL_SMTP,Base64.encode("xxxxxxx@qq.com"));
            resetRespMessage(respMessage);

            respMessage= sendMessage(Constant.PROTOCOL_SMTP,Base64.encode("ypxwdozxldbabaia"));
            resetRespMessage(respMessage);
            List<String> mailHeader=buildMailHeader();
            List<String> mailContent=buildMailContent();
           // MAIL FROM: <danny@hmail.com> SIZE=1336
            respMessage= sendMessage(Constant.PROTOCOL_SMTP,"MAIL FROM: <xxxx@qq.com> SIZE="+ (CommonUtil.countBytesWithFeedLine(mailHeader)+CommonUtil.countBytesWithFeedLine(mailContent)));
            resetRespMessage(respMessage);

            //RCPT TO: <danny@hmail.com>
            respMessage= sendMessage(Constant.PROTOCOL_SMTP,"RCPT TO: <xxxx@qq.com>");
            resetRespMessage(respMessage);

            respMessage= sendMessage(Constant.PROTOCOL_SMTP,"DATA");
            resetRespMessage(respMessage);
            List<String> mail=new ArrayList<>();
            mail.addAll(mailHeader);
            mail.addAll(mailContent);
            mail.add("");
            mail.add(".");
            System.out.println("总字节数："+CommonUtil.countBytesWithFeedLine(mail));
            System.out.println("邮件头："+CommonUtil.countBytesWithFeedLine(mailHeader));
            System.out.println("邮件正文："+CommonUtil.countBytesWithFeedLine(mailContent));

            respMessage= sendMessage(Constant.PROTOCOL_SMTP,mail);

            resetRespMessage(respMessage);

            respMessage= sendMessage(Constant.PROTOCOL_SMTP,"QUIT");
            resetRespMessage(respMessage);




        }catch (Exception e){
            e.printStackTrace();

        }

    }

    static void testImap(){
        try{
            connectIMAPServer();
            RespMessage respMessage=  sendMessage(Constant.PROTOCOL_IMAP,"C1 CAPABILITY");
            System.out.println(respMessage);
            resetRespMessage(respMessage);
            Map<String,String> loginParam= MapUtil.newHashMap();
            loginParam.put("account","danny@hmail.com");
            loginParam.put("pwd","123456");
            respMessage=sendMessage(Constant.PROTOCOL_IMAP,new LoginCommand().buildComand(loginParam));
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C4 LIST \"\" *");
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C5 LSUB \"\" *");
            List<String> mailDirs=  RespParser.LSUB(respMessage.lines);
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C6 NOOP");
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C7 STATUS "+mailDirs.get(0)+" (MESSAGES RECENT UIDVALIDITY)");
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C8 NOOP");
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C9 CAPABILITY");
            resetRespMessage(respMessage);

            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C10 SELECT "+mailDirs.get(0)+"");
            int exists= RespParser.SELECT(respMessage.lines);
            resetRespMessage(respMessage);

            //C11 FETCH 1:5 (UID)
            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C11 FETCH 1:"+exists+" (UID)");
            resetRespMessage(respMessage);
            //C12 UID FETCH 1:5 (UID RFC822.SIZE FLAGS BODY.PEEK[HEADER])
            respMessage=sendMessage(Constant.PROTOCOL_IMAP,"C12 UID FETCH 1:"+exists+" (UID RFC822.SIZE FLAGS BODY.PEEK[])");
            List<FetchMailWrapper> fetchMailWrappers=  RespParser.UID_FETCH(respMessage.lines);

            resetRespMessage(respMessage);

            System.out.println(respMessage);
        }catch (Exception e){

            e.printStackTrace();
        }
    }
    static void resetRespMessage(RespMessage respMessage){
        respMessage.setCode("ok");
        respMessage.getLines().clear();
    }
    static void connectIMAPServer(){
        try {
            Socket client =new Socket("127.0.0.1",143);
            imapConnection=new Connection(client,Constant.PROTOCOL_IMAP);
            System.out.println("IMAPServer connect success!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IMAPServer connect failed:"+e.getMessage());
        }
    }

    static void connectSMTPServer(){
        try {

            Socket client =new Socket("smtp.qq.com",25);
            smtpConnection=new Connection(client,Constant.PROTOCOL_SMTP);
            System.out.println("SMTPServer connect success");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("SMTPServer connect failed:"+e.getMessage());
        }
    }
    public static  RespMessage sendMessage(String protocol, String message) throws IOException{
        List<String> list=new ArrayList<>();
        list.add(message);
        return sendMessage(protocol,list,null);
    }
   public static  RespMessage sendMessage(String protocol, List<String> messages,List<String> ...fragments) throws IOException {
        System.out.println("send command:"+JSONUtil.toJsonPrettyStr(messages));
        Connection connection=protocol.equals(Constant.PROTOCOL_IMAP)?imapConnection:smtpConnection;
        RespMessage respMessage=new RespMessage();
       for (int i=0;i<messages.size();i++){
           connection.sendWriter.write(messages.get(i)+"\r\n");
        }
        connection.sendWriter.flush();
        if(fragments!=null){
           for(List<String> fragment:fragments){
               if(fragment!=null&&fragment.size()>0){
                   System.out.println("send fragment:"+JSONUtil.toJsonPrettyStr(fragment));

                   for (int i=0;i<fragment.size();i++){
                       connection.sendWriter.write(fragment.get(i)+"\r\n");
                   }
                   connection.sendWriter.flush();
               }
           }
        }
        String line=connection.receivReader.readLine();
        respMessage.getLines().add(line);
        while (!finishRead(line,protocol)){
            line=connection.receivReader.readLine();
            respMessage.getLines().add(line);
        }
        System.out.println("receiv message:"+respMessage.toString());

       return respMessage;
    }
    static  boolean finishRead(String line,String protocol){
        return protocol.equals(Constant.PROTOCOL_IMAP)?line.toLowerCase().endsWith("completed"):
                (line.toLowerCase().endsWith(" ok")
                        ||line.toLowerCase().endsWith(" help")
                        ||line.toLowerCase().startsWith("334 ")
                        ||line.toLowerCase().startsWith("235 ")
                        ||line.toLowerCase().startsWith("535 ")    //失败码
                        ||line.toLowerCase().startsWith("503 ")
                        ||line.toLowerCase().startsWith("354 ")
                        ||line.toLowerCase().startsWith("250 ")
                        ||line.toLowerCase().startsWith("221 ")


                );
    }

   static class RespMessage{
        String code="ok";

       public String getCode() {
           return code;
       }

       public RespMessage setCode(String code) {
           this.code = code;
           return this;
       }

       public String getMsg() {
           return msg;
       }

       public RespMessage setMsg(String msg) {
           this.msg = msg;
           return this;
       }

       public List<String> getLines() {
           return lines;
       }

       public RespMessage setLines(List<String> lines) {
           this.lines = lines;
           return this;
       }

        String msg;
        List<String> lines=new ArrayList<>();

       public RespMessage(String code, String msg, List<String> lines) {
           this.code = code;
           this.msg = msg;
           this.lines = lines;
       }
       public RespMessage() {

       }

       @Override
       public String toString() {
           return JSONUtil.toJsonPrettyStr(this);
       }
   }
    static class Connection{
        Socket client;
        BufferedWriter sendWriter;
        BufferedReader receivReader=null;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        String protocol;
        public Connection(Socket client,String protocol) throws IOException {
            this.client = client;
            this.sendWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            this.receivReader =new BufferedReader(new InputStreamReader(client.getInputStream(), CharsetUtil.CHARSET_ISO_8859_1) );
            this.protocol=protocol;
        }




    }

    static List<String> buildMailHeader(){
        MimeBuilder mimeBuilder=new MimeBuilder();
        List<String> lines= mimeBuilder

                .addMimeDomain(new MimeDomain("From","\"772291304@qq.com\" <772291304@qq.com>",mimeBuilder)).completeDomain()
                .addMimeDomain(new MimeDomain("To","772291304 <772291304@qq.com>",mimeBuilder)).completeDomain()
                .addMimeDomain(new MimeDomain("Date","Fri, 24 Mar 2023 07:57:46 +0800",mimeBuilder)).completeDomain()
                .addMimeDomain(new MimeDomain("Subject","myfirstsend",mimeBuilder)).completeDomain()
                .addMimeDomain(new MimeDomain("X-Priority","3",mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("X-GUID", UUID.fastUUID().toString(),mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("X-Has-Attach","no",mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("X-Mailer","Foxmail 7.2.25.213[cn]",mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("Mime-Version","1.0",mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("Message-ID","<202103240757467925400@hmail.com>",mimeBuilder)).completeDomain()

                .addMimeDomain(new MimeDomain("Content-Type","multipart/alternative;",mimeBuilder))
                .appendNewLine("boundary=\"----=_001_NextPart000854588276_=----").completeDomain()
                .addSpaceLine()

                .getLines();
        return lines;
    }
    static List<String> buildMailContent(){
        MimeBuilder mimeBuilder=new MimeBuilder();
        List<String> lines= mimeBuilder
                .addLine("qianyan")
                .addSpaceLine()
                .addLine("------=_001_NextPart000854588276_=----")
                .addMimeDomain(new MimeDomain("Content-Type","text/plain;",mimeBuilder).appendNewLine("charset=\"us-ascii\"")).completeDomain()
                .addMimeDomain(new MimeDomain("Content-Transfer-Encoding","base64",mimeBuilder)).completeDomain()
                .addSpaceLine()
                .addLine("aGVsbG93b3JsZA==")
                .addSpaceLine()
                .addLine("------=_001_NextPart000854588276_=----")
                .addLine("Content-Type: text/html;")
                .addLine("\tcharset=\"us-ascii\"")
                .addLine("Content-Transfer-Encoding: quoted-printable")
                .addSpaceLine()
                .addLine("<html><body>wo nai ying xiong</body></html>")
                .addLine("------=_001_NextPart000854588276_=----")

                /*   .addSpaceLine()
                .addLine(".")*/
                .getLines();
        return lines;
    }
}
