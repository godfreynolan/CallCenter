package com.riis.callcenter.agentslist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.genericlistadapter.GenericListItemViewFactory;
import com.riis.callcenter.R;

public class AgentItemViewFactory implements GenericListItemViewFactory {

	@Override
	public View generateListItemViewFromListItem(Context context, GenericListItem agentItem, OnClickListener clickListener) {
		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View agentItemView = inflator.inflate(R.layout.agent_list_item, null);

		TextView nameView = (TextView) agentItemView.findViewById(R.id.agentName);
		Button statusButton = (Button) agentItemView.findViewById(R.id.agentStatusButton);

		nameView.setText(((AgentListItem) agentItem).name);

		ImageView statusIcon = (ImageView) agentItemView.findViewById(R.id.agentStatusImage);
		statusIcon.setImageResource(((AgentListItem) agentItem).status.getDrawableId());
		
		statusButton.setText(((AgentListItem) agentItem).status.getStatusText());
		statusButton.setBackgroundResource(((AgentListItem) agentItem).status.getBackgroundDrawableId());
		statusButton.setOnClickListener(clickListener);

		return agentItemView;
	}
}
