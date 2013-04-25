package com.riis.callcenter.xmlparsers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class QueueDetailsXmlParser {
	private String xmlString;
	
	public QueueDetailsXmlParser(String xmlString) {
		this.xmlString = xmlString;
	}
	
	public int parseMaxQueuesFromXmlDocForQueue() throws ParserConfigurationException, SAXException, IOException {
		XmlStringReader xmlStringReader = new XmlStringReader(xmlString);
		Document xmlDoc = xmlStringReader.readStringAsDocument();
		
		String queueLength = xmlDoc.getDocumentElement().getElementsByTagName("queueLength").item(0).getTextContent();
		return Integer.parseInt(queueLength);
	}
}
