package com.abwd.parsers.resp.wrappers;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.abwd.utils.CommonUtil;

import java.util.Date;

/**
 * 拉取的消息体
 */
public class FetchMailWrapper {


    String from;

    String to;

    String date;

    String subject;

    String content;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        if(!StrUtil.isEmpty(html)){
            html=  CommonUtil.qpDecoding(html, CharsetUtil.GBK,CharsetUtil.UTF_8);
        }
        this.html = html;
    }

    String html;

    public String getFrom() {
        return from;
    }

    public FetchMailWrapper setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public FetchMailWrapper setTo(String to) {
        this.to = to;
        return this;
    }

    public String getDate() {
        return date;
    }

    public FetchMailWrapper setDate(String date) {
        this.date = date;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        if(!StrUtil.isEmpty(subject)){
            subject=  CommonUtil.qpDecoding(subject, CharsetUtil.GBK,CharsetUtil.UTF_8);
        }
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public FetchMailWrapper setContent(String content) {
        this.content = content;
        return this;
    }

    public String getBoundary() {
        return boundary;
    }

    public FetchMailWrapper setBoundary(String boundary) {
        this.boundary = boundary;
        return this;

    }

    public String boundary;




}
