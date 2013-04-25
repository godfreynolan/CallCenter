package com.riis.callcenter;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.riis.callcenter.broadsoftrequest.BroadsoftRequestRunner;
import com.riis.callcenter.broadsoftrequest.BroadsoftRequests.BroadsoftRequest;
import com.riis.callcenter.broadsoftrequest.BroadsoftResponseListener;
import com.riis.callcenter.dialogs.AlertDialogs;
import com.riis.callcenter.genericlistadapter.GenericListAdapter;
import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.queueslist.QueueItemViewFactory;
import com.riis.callcenter.queueslist.QueueListItem;
import com.riis.callcenter.xmlparsers.CallsXmlReader;
import com.riis.callcenter.xmlparsers.QueueDetailsXmlParser;
import com.riis.callcenter.xmlparsers.QueuesListXmlParser;
import com.riis.callcenter.R;

public class QueuesActivity extends ListActivity {
	private GenericListAdapter queuesAdapter;

	private Button refreshButton;
	private Button settingsButton;
	private ProgressDialog loadingDialog;
	private BroadsoftRequestRunner queuesRunner;
	private BroadsoftRequestRunner callsRunner;
	private BroadsoftRequestRunner detailsRunner;

	private SharedPreferences sharedPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setTheme(R.style.CustomTheme);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.queues_screen);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_titlebar_with_settings);
		((TextView)findViewById(R.id.title)).setText("Call Centers");

		sharedPrefs = getSharedPreferences(SettingsActivity.SHARED_PREF_NAME, MODE_PRIVATE);
		
		setUpTheListAdapter();
		setUpTheRefreshButton();
		setUpTheSettingsButton();

		createLoadingDialog();
		populateListFromResponse(getIntent().getStringExtra("response"));
	}
	
	private void createLoadingDialog() {
		loadingDialog = AlertDialogs.createAndShowLoadingDialog(QueuesActivity.this, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(queuesRunner != null)
					queuesRunner.cancelRequest();
				if(callsRunner != null)
					callsRunner.cancelRequest();
				if(detailsRunner != null)
					detailsRunner.cancelRequest();
			}
		});
	}
	
	private void setUpTheListAdapter() {
		queuesAdapter = new GenericListAdapter(new QueueItemViewFactory());
		setListAdapter(queuesAdapter);
	}

	private void setUpTheRefreshButton() {
		refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setText("Refresh | Last updated: " + sharedPrefs.getString(SettingsActivity.LAST_UPDATED_KEY, "Never"));
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				queuesRunner = new BroadsoftRequestRunner();
				createLoadingDialog();
				queuesRunner.runRequest(BroadsoftRequest.QUEUES_REQUEST, responseListener);
			}
		});
	}
	
	private void setUpTheSettingsButton() {
		settingsButton = (Button) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(QueuesActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	private BroadsoftResponseListener responseListener = new BroadsoftResponseListener() {
		@Override
		public void onRequestCompleted(String response, boolean success, String failureMessage) {

			if(!success) {
				AlertDialogs.showLoadingFailedAlertDialog(QueuesActivity.this, failureMessage);
			} else {
				populateListFromResponse(response);
				
				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString(SettingsActivity.LAST_UPDATED_KEY, df.format(Calendar.getInstance().getTime()));
				editor.commit();
				refreshButton.setText("Refresh | Last updated: " + sharedPrefs.getString(SettingsActivity.LAST_UPDATED_KEY, "Never"));
			}
		}
	};

	private void populateListFromResponse(String response) {
		try {
			loadQueuesAndPopulateList(response);
		} catch(Exception e) {
			AlertDialogs.showLoadingFailedAlertDialog(this, "Couldn't load queues.");
		}
	}

	private void loadQueuesAndPopulateList(String response) throws TransformerException, ParserConfigurationException, SAXException, IOException {
		QueuesListXmlParser queuesParser = new QueuesListXmlParser(response);
		ArrayList<GenericListItem> queues = queuesParser.parseQueuesFromXmlDoc();
		
		loadCallsInQueueForList(queues);
		
		queuesAdapter.setDataSetWithClickListenerForAll(queues, queueClickListener);
	}
	
	private void loadCallsInQueueForList(ArrayList<GenericListItem> queues) {
		for(int i = 0; i < queues.size()-1; i++) {
			loadCallsInQueueForQueue((QueueListItem)queues.get(i), false);
		}
		loadCallsInQueueForQueue((QueueListItem)queues.get(queues.size()-1), true);
	}
	
	private void loadCallsInQueueForQueue(final QueueListItem queue, final boolean isLastItemInQueue) {
		callsRunner = new BroadsoftRequestRunner();
		callsRunner.runRequest(BroadsoftRequest.CALLS_REQUEST, new BroadsoftResponseListener() {
			@Override
			public void onRequestCompleted(String response, boolean success, String failureMessage) {
				CallsXmlReader callsReader = new CallsXmlReader(response);
				
				try {
					queue.callsInQueue = callsReader.parseNumberOfCalls();
				} catch(Exception e) {
					queue.callsInQueue = -1;
				}
				
				loadMaxCallsInQueueForQueue(queue, isLastItemInQueue);
			}
		}, queue.name);
	}
	
	private void loadMaxCallsInQueueForQueue(final QueueListItem queue, final boolean isLastItemInQueue) {
		detailsRunner = new BroadsoftRequestRunner();
		detailsRunner.runRequest(BroadsoftRequest.QUEUE_DETAILS_REQUEST, new BroadsoftResponseListener() {
			@Override
			public void onRequestCompleted(String response, boolean success, String failureMessage) {
				QueueDetailsXmlParser detailsParser = new QueueDetailsXmlParser(response);
				
				try {
					queue.maxCallsInQueue = detailsParser.parseMaxQueuesFromXmlDocForQueue();
				} catch(Exception e) {
					queue.maxCallsInQueue = -1;
				}
				
				queuesAdapter.forceRefresh();
				if(isLastItemInQueue) {
					loadingDialog.dismiss();
				}
			}
		}, queue.name);
	}
	

	private View.OnClickListener queueClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView queueNameView = (TextView) v.findViewById(R.id.queueTitle);

			loadAndShowAgentListForQueue(queueNameView.getText().toString());
		}
	};

	private void loadAndShowAgentListForQueue(final String queueName) {
		loadingDialog = AlertDialogs.createAndShowLoadingDialog(QueuesActivity.this, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) { }
		});
		
		BroadsoftRequestRunner agentsRunner = new BroadsoftRequestRunner();
		agentsRunner.runRequest(BroadsoftRequest.AGENTS_REQUEST, new BroadsoftResponseListener() {
			@Override
			public void onRequestCompleted(String response, boolean success, String failureMessage) {
				loadingDialog.dismiss();

				if(!success) {
					AlertDialogs.showLoadingFailedAlertDialog(QueuesActivity.this, failureMessage);
				} else {
					Intent intent = new Intent(QueuesActivity.this, AgentsActivity.class);
					intent.putExtra("response", response);
					intent.putExtra("callCenterName", queueName);
					intent.putExtra("queueNames", getListOfQueueNames());
					startActivity(intent);
				}
			}
		}, queueName);
	}
	
	private String[] getListOfQueueNames() {
		String[] queueNames = new String[queuesAdapter.getCount()];
		
		for(int i = 0; i < queueNames.length; i++) {
			queueNames[i] = ((QueueListItem)queuesAdapter.getItem(i)).name;
		}
		
		return queueNames;
	}
	
	@Override
	public void onBackPressed() {
	
	}
}
