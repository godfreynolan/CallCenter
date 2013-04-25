package com.riis.callcenter.broadsoftrequest;

public class BroadsoftRequests {
	public static final String URL_TAG = "%URL%";
	public static final String USERNAME_TAG = "%USERNAME%";
	public static final String PARAMETER_TAG = "%PARAMETER%";
	
	public enum BroadsoftRequest {
		QUEUES_REQUEST(URL_TAG + "com.broadsoft.xsi-actions/v2.0/user/" + USERNAME_TAG + "/directories/CallCenters?user=Supervisor"),
		QUEUE_DETAILS_REQUEST(URL_TAG + "com.broadsoft.xsi-actions/v2.0/callcenter/" + PARAMETER_TAG + "/profile"),
		AGENTS_REQUEST(URL_TAG + "com.broadsoft.xsi-actions/v2.0/user/" + USERNAME_TAG + "/directories/Agents?callCenter=" + PARAMETER_TAG),
		AGENT_STATUS(URL_TAG + "com.broadsoft.xsi-actions/v2.0/user/" + PARAMETER_TAG + "/services/CallCenter"),
		CALLS_REQUEST(URL_TAG + "/com.broadsoft.xsi-actions/v2.0/callcenter/" + PARAMETER_TAG + "/calls"),
		AVAILABLE_USERS_REQUEST(URL_TAG + "com.broadsoft.xsi-actions/v2.0/user/" + USERNAME_TAG + "/directories/Group");
		
		private String requestUrl;

		private BroadsoftRequest(String requestUrl) {
			this.requestUrl = requestUrl;
		}

		public String getRequestUrl() {
			return requestUrl;
		}
	}
}
