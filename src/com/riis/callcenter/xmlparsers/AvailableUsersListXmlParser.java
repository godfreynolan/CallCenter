package com.riis.callcenter.xmlparsers;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.riis.callcenter.agentslist.AgentListItem;
import com.riis.callcenter.agentslist.AgentListItem.AgentStatus;
import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.utils.XmlNodeUtils;

public class AvailableUsersListXmlParser {
	private String xmlString;
	private ArrayList<GenericListItem> agents;
	private String[] namesToIgnore;

	public AvailableUsersListXmlParser(String xmlString, String[] namesToIgnore) {
		this.xmlString = xmlString;
		this.agents = new ArrayList<GenericListItem>();
		this.namesToIgnore = namesToIgnore;
	}

	public ArrayList<GenericListItem> parseAgentsFromXmlDoc() throws ParserConfigurationException, SAXException, IOException {
		XmlStringReader xmlStringReader = new XmlStringReader(xmlString);
		Document xmlDoc = xmlStringReader.readStringAsDocument();
		
		NodeList availableUsersList = xmlDoc.getDocumentElement().getElementsByTagName("groupDirectory").item(0).getChildNodes();

		for (int i = 0; i < availableUsersList.getLength(); i++) {
			String potentialUserId = XmlNodeUtils.getChildTagValue(availableUsersList.item(i), "userId");

			if (!userIdIsInIgnoreList(potentialUserId)) {
				Node agentToAdd = availableUsersList.item(i);

				if (agentToAdd != null) {
					String userId = XmlNodeUtils.getChildTagValue(agentToAdd, "userId");
					String agentName = XmlNodeUtils.getChildTagValue(agentToAdd, "firstName");
					agentName += " " + XmlNodeUtils.getChildTagValue(agentToAdd, "lastName");
					
					AgentListItem newAgent = new AgentListItem(agentName, userId);
					newAgent.status = AgentStatus.DISABLED;
					
					agents.add(newAgent);
				}
			}
		}

		return agents;
	}

	private boolean userIdIsInIgnoreList(String userId) {
		for (String nameToCheck : namesToIgnore) {
			if (nameToCheck.equals(userId)) {
				return true;
			}
		}

		return false;
	}
}
