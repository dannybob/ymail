package com.abwd.utils;

import cn.hutool.core.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class CommonUtil {
    /**
     *  Quoted-printable编码解码
     * @param str
     * @param ordinaryCharset
     * @param dstCharset
     * @return
     */
    public final static String qpDecoding(String str,String ordinaryCharset,String dstCharset)
    {
        if (str == null)
        {
            return "";
        }
        try
        {
            StringBuffer sb = new StringBuffer(str);
            for (int i = 0; i < sb.length(); i++)
            {
                if (sb.charAt(i) == ' ' && sb.charAt(i - 1) == '=')
                {
                    // 解码这个地方也要修改一下
                    // sb.deleteCharAt(i);
                    sb.deleteCharAt(i - 1);
                }
            }
            str = sb.toString();
            byte[] bytes = str.getBytes("US-ASCII");
            for (int i = 0; i < bytes.length; i++)
            {
                byte b = bytes[i];
                if (b != 95)
                {
                    bytes[i] = b;
                }
                else
                {
                    bytes[i] = 32;
                }
            }
            if (bytes == null)
            {
                return "";
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int i = 0; i < bytes.length; i++)
            {
                int b = bytes[i];
                if (b == '=')
                {
                    try
                    {
                        int u = Character.digit((char) bytes[++i], 16);
                        int l = Character.digit((char) bytes[++i], 16);
                        if (u == -1 || l == -1)
                        {
                            continue;
                        }
                        buffer.write((char) ((u << 4) + l));
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    buffer.write(b);
                }
            }
            String ordinaryStr=new String(buffer.toByteArray(), ordinaryCharset);
           // return CharsetUtil.convert(ordinaryStr,ordinaryCharset,dstCharset);
            return ordinaryStr;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

   public static int  countBytesWithFeedLine(List<String> lines){
       int count=0;
       for(String line: lines){
          count=count+line.getBytes().length+2;
       }
       return count;
   }

}
