package com.riis.callcenter.queueslist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.genericlistadapter.GenericListItemViewFactory;
import com.riis.callcenter.R;

public class QueueItemViewFactory implements GenericListItemViewFactory {

	@Override
	public View generateListItemViewFromListItem(Context context, GenericListItem queueItem, OnClickListener clickListener) {
		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View queueItemView = inflator.inflate(R.layout.queue_list_item, null);

		ImageView statusView = (ImageView) queueItemView.findViewById(R.id.activeIndicator);
		TextView nameView = (TextView) queueItemView.findViewById(R.id.queueTitle);
		TextView callsInQueueView = (TextView) queueItemView.findViewById(R.id.callsInQueueNumber);
		TextView callsInQueueSeparatorView = (TextView) queueItemView.findViewById(R.id.callsInQueueSeparator);
		TextView maxCallsInQueueView = (TextView) queueItemView.findViewById(R.id.maxCallsInQueueNumber);

		nameView.setText(((QueueListItem) queueItem).name);

		int queueStatusIndex = 0;

		int callsInQueue = ((QueueListItem) queueItem).callsInQueue;
		int maxCallsInQueue = ((QueueListItem) queueItem).maxCallsInQueue;
		float percentOfQueueFull = 0.0f;
		if (maxCallsInQueue > 0) {
			percentOfQueueFull = callsInQueue / maxCallsInQueue;
		}

		callsInQueueView.setText(Integer.toString(callsInQueue));

		if (percentOfQueueFull > 0.50) {
			callsInQueueView.setTextColor(Color.RED);
			callsInQueueSeparatorView.setTextColor(Color.RED);
			maxCallsInQueueView.setTextColor(Color.RED);
			queueStatusIndex = 2;
		} else if (percentOfQueueFull > 0.1) {
			callsInQueueView.setTextColor(Color.YELLOW);
			callsInQueueSeparatorView.setTextColor(Color.YELLOW);
			maxCallsInQueueView.setTextColor(Color.YELLOW);
		}

		maxCallsInQueueView.setText(Integer.toString(maxCallsInQueue));

		if (queueStatusIndex == 1) {
			statusView.setImageResource(android.R.drawable.presence_away);
		} else if (queueStatusIndex == 2) {
			statusView.setImageResource(android.R.drawable.presence_busy);
		}

		queueItemView.setOnClickListener(clickListener);

		return queueItemView;
	}
}
