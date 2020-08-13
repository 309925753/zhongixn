package com.vkzwbim.chat.util;

import com.cjt2325.cameralibrary.util.LogUtil;
import com.vkzwbim.chat.sp.CustomContext;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class XmlToMessage {

    //字符串转xml
    public static String formatXml(String inputXML) throws Exception {
        String xml = null;
        SAXReader reader = new SAXReader();
        XMLWriter writer = null;
        org.dom4j.Document document = reader.read(new StringReader(inputXML));
        try {
            if (document != null) {
                StringWriter stringWriter = new StringWriter();
                OutputFormat format = OutputFormat.createPrettyPrint();
                format.setNewLineAfterDeclaration(false);
                writer = new XMLWriter(stringWriter, format);
                writer.write(document);
                writer.flush();
                xml = stringWriter.getBuffer().toString();
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return xml;
    }
    public static String stringToXmlByDom4j(String mesageXml) {
        String messageContent="";
        try {
            SAXReader saxReader = new SAXReader();
            org.dom4j.Document docDom4j = saxReader.read(new ByteArrayInputStream(mesageXml.getBytes("utf-8")));
            org.dom4j.Element root = docDom4j.getRootElement();

            List<org.dom4j.Element> childElements = root.elements("received2");


            messageContent = childElements.get(0).attribute("content").getValue()+ CustomContext.SPLITEWORD +childElements.get(0).attribute("toUserId").getValue();


            LogUtil.e("cjh from "+messageContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageContent;
    }
}
