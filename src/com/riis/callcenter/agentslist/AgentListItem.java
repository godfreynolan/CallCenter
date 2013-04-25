package com.riis.callcenter.agentslist;

import com.riis.callcenter.genericlistadapter.GenericListItem;
import com.riis.callcenter.R;

public class AgentListItem extends GenericListItem {
	public enum AgentStatus {
		AVAILABLE(R.drawable.btn_ok_large, "Available", R.drawable.btn_green_selector),
		UNAVAILABLE(R.drawable.no_large, "Unavailable", R.drawable.btn_red_selector),
		SIGN_IN(R.drawable.no_large, "Sign-In", R.drawable.btn_red_selector),
		SIGN_OUT(R.drawable.no_large, "Sign-Out", R.drawable.btn_red_selector),
		DISABLED(0, null, 0);

		private int drawableId;
		private String statusText;
		private int backgroundDrawableId;

		private AgentStatus(int drawableId, String statusText, int backgroundDrawableId) {
			this.drawableId = drawableId;
			this.statusText = statusText;
			this.backgroundDrawableId = backgroundDrawableId;
		}

		public int getDrawableId() {
			return drawableId;
		}
		
		public String getStatusText() {
			return statusText;
		}
		
		public int getBackgroundDrawableId() {
			return backgroundDrawableId;
		}
		
		public static AgentStatus agentStatusForStatusText(String statusText) {
			if(statusText.equals(AVAILABLE.statusText)) {
				return AVAILABLE;
			} else if(statusText.equals(SIGN_IN.statusText)) {
				return SIGN_IN;
			} else if(statusText.equals(SIGN_OUT.statusText)) {
				return SIGN_OUT;
			} else if(statusText.equals(UNAVAILABLE.statusText)) {
				return UNAVAILABLE;
			} else {
				return null;
			}
		}
	}

	public String name;
	public String userId;
	public AgentStatus status;

	public AgentListItem(String name, String userId) {
		this.name = name;
		this.userId = userId;
		this.status = AgentStatus.AVAILABLE;
	}
}
