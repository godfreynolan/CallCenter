package com.riis.callcenter.xmlparsers;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.queueslist.QueueListItem;
import com.riis.callcenter.utils.XmlNodeUtils;

public class QueuesListXmlParser {
	private String xmlString;
	private ArrayList<GenericListItem> queues;
	
	public QueuesListXmlParser(String xmlString) {
		this.xmlString = xmlString;
		this.queues = new ArrayList<GenericListItem>();
	}
	
	public ArrayList<GenericListItem> parseQueuesFromXmlDoc() throws ParserConfigurationException, SAXException, IOException {
		XmlStringReader xmlStringReader = new XmlStringReader(xmlString);
		Document xmlDoc = xmlStringReader.readStringAsDocument();
		
		NodeList callCenters = xmlDoc.getDocumentElement().getElementsByTagName("callCenter");

		for(int i = 0; i < callCenters.getLength(); i++) {
			Node callCenterNode = callCenters.item(i);
			String callCenterName = XmlNodeUtils.getChildTagValue(callCenterNode, "serviceUserID");

			QueueListItem newQueue = new QueueListItem(callCenterName);
			queues.add(newQueue);
		}
		
		return queues;
	}
}
