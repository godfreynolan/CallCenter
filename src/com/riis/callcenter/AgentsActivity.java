package com.riis.callcenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.riis.callcenter.agentslist.AgentItemViewFactory;
import com.riis.callcenter.agentslist.AgentListItem;
import com.riis.callcenter.agentslist.AgentListItem.AgentStatus;
import com.riis.callcenter.broadsoftrequest.BroadsoftRequestRunner;
import com.riis.callcenter.broadsoftrequest.BroadsoftRequests.BroadsoftRequest;
import com.riis.callcenter.broadsoftrequest.BroadsoftResponseListener;
import com.riis.callcenter.dialogs.AlertDialogs;
import com.riis.callcenter.genericlistadapter.GenericListAdapter;
import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.xmlparsers.AgentListXmlParser;
import com.riis.callcenter.xmlparsers.AgentStatusXmlReader;
import com.riis.callcenter.xmlparsers.XmlStringReader;
import com.riis.callcenter.R;

public class AgentsActivity extends ListActivity {
	private GenericListAdapter agentsAdapter;

	private Button settingsButton;
	private String callCenterName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.CustomTheme);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.queues_screen);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_titlebar_with_settings);
		((TextView) findViewById(R.id.title)).setText("Call Center Details");

		findViewById(R.id.refreshButton).setVisibility(View.INVISIBLE);

		setUpTheSettingsButton();

		agentsAdapter = new GenericListAdapter(new AgentItemViewFactory());
		setListAdapter(agentsAdapter);

		callCenterName = getIntent().getStringExtra("callCenterName");

		populateListFromResponse(getIntent().getStringExtra("response"));
		loadAgentStatuses();
	}

	private void setUpTheSettingsButton() {
		settingsButton = (Button) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AgentsActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	private void populateListFromResponse(String response) {
		try {
			XmlStringReader xmlReader = new XmlStringReader(response);
			parseAgentsAndPopulateList(xmlReader.readStringAsDocument(), callCenterName);
		} catch (Exception e) {
			e.printStackTrace();
			AlertDialogs.showLoadingFailedAlertDialog(this, "Couldn't load agents.");
		}
	}

	private void parseAgentsAndPopulateList(Document xmlResponse, String callCenterName) throws UnsupportedEncodingException, TransformerException {
		AgentListXmlParser agentsParser = new AgentListXmlParser(xmlResponse, callCenterName);
		ArrayList<GenericListItem> agents = agentsParser.parseAgentsFromXmlDoc();

		addAgentsToList(agents);
	}

	private void addAgentsToList(ArrayList<GenericListItem> agents) {
		for (int i = 0; i < agents.size(); i++) {
			agentsAdapter.addItem(agents.get(i), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AgentListItem agentClicked = (AgentListItem) agentsAdapter.getItemAssociateWithClickListener(this);

					if (agentClicked != null) {
						createChangeStateAlertDialog(agentClicked);
					}
				}
			});
		}
	}

	private void createChangeStateAlertDialog(final AgentListItem agentClicked) {
		Set<String> listItemsSet = new HashSet<String>();
		listItemsSet.add("Available");
		listItemsSet.add("Unavailable");
		listItemsSet.add("Sign-In");
		listItemsSet.add("Sign-Out");
		listItemsSet.remove(agentClicked.status.getStatusText());
		final String[] listItems = listItemsSet.toArray(new String[listItemsSet.size()]);
		AlertDialog.Builder builder = new AlertDialog.Builder(AgentsActivity.this);

		builder.setTitle("Change status of " + agentClicked.name);
		builder.setItems(listItems, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				BroadsoftRequestRunner agentStatusChangeRunner = new BroadsoftRequestRunner();

				String xmlPutData = makeStatusChangeXml(listItems[which]);

				// Run the status change request for the agent, with the proper
				// XML header
				agentStatusChangeRunner.runRequest(BroadsoftRequest.AGENT_STATUS, new BroadsoftResponseListener() {
					@Override
					public void onRequestCompleted(String response, boolean success, String failureMessage) {
						if (success) {
							agentClicked.status = AgentStatus.agentStatusForStatusText(listItems[which]);
							agentsAdapter.forceRefresh();
						} else {
							Toast.makeText(AgentsActivity.this, "Couldn't update status", Toast.LENGTH_LONG).show();
						}

						dialog.cancel();
					}
				}, agentClicked.userId, xmlPutData);

			}
		});

		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.create().show();
	}

	private String makeStatusChangeXml(String statusToInsert) {
		String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "<CallCenter xmlns=\"http://schema.broadsoft.com/xsi\">" + "<agentACDState>" + statusToInsert + "</agentACDState>" + "<agentUnavailableCode>1234</agentUnavailableCode>" + "<callCenterList>" + "<callCenterDetails>" + "<serviceUserId>" + callCenterName + "</serviceUserId>" + "<available>false</available>" + "</callCenterDetails>" + "</callCenterList>" + "</CallCenter>";

		return xmlString;
	}
	
	private void loadAgentStatuses() {
		for (int i = 0; i < agentsAdapter.getCount(); i++) {
			GenericListItem agent = (GenericListItem) agentsAdapter.getItem(i);
			loadAgentStatus((AgentListItem) agent);
		}
	}
	
	private void loadAgentStatus(final AgentListItem agent) {
		BroadsoftRequestRunner statusRunner = new BroadsoftRequestRunner();
		statusRunner.runRequest(BroadsoftRequest.AGENT_STATUS, new BroadsoftResponseListener() {
			@Override
			public void onRequestCompleted(String response, boolean success, String failureMessage) {
				AgentStatusXmlReader statusReader = new AgentStatusXmlReader(response);
				
				try {
					agent.status = statusReader.parseAgentStatusFromXml();
				} catch (Exception e) {
					agent.status = AgentStatus.AVAILABLE;
				}
				
				agentsAdapter.forceRefresh();
			}
		}, agent.userId);
	}
}
