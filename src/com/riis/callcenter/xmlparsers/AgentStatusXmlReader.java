package com.riis.callcenter.xmlparsers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.riis.callcenter.agentslist.AgentListItem.AgentStatus;

public class AgentStatusXmlReader {
	private String xmlString;
	
	public AgentStatusXmlReader(String xmlString) {
		this.xmlString = xmlString;
	}
	
	public AgentStatus parseAgentStatusFromXml() throws ParserConfigurationException, SAXException, IOException {
		XmlStringReader xmlStringReader = new XmlStringReader(xmlString);
		Document xmlDoc = xmlStringReader.readStringAsDocument();
		
		String agentState = xmlDoc.getDocumentElement().getElementsByTagName("agentACDState").item(0).getTextContent();
		AgentStatus status = AgentStatus.agentStatusForStatusText(agentState);
		return status;
	}
}
