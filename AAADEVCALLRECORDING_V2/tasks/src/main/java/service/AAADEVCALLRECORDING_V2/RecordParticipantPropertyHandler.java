package service.AAADEVCALLRECORDING_V2;

import java.util.ArrayList;
import java.util.List;

public class RecordParticipantPropertyHandler {
	public static final String CALLING = "Calling";
	public static final String CALLED = "Called";
	public static final String ANSWERING = "Answering";
	public static final String RECORDEDPARTY_CALLID = "RecordedParty_CallId";
	public static final String CALLPARTYNUMBER_CALLID = "CallPartyNumber_CallId";
	public static final String CALLPARTYNUMBER_TIMESTAMP = "CallPartyNumber_Timestamp";

	public List<String> getRecordedParties() {
		List<String> parties = new ArrayList();
		parties.add("Calling");
		parties.add("Called");
		parties.add("Answering");
		parties.add("CallRecording");
		parties.add("CallRecordingAndMix");
		return parties;
	}

	public List<String> getFileNamePatterns() {
		List<String> patterns = new ArrayList();
		patterns.add("RecordedParty_CallId");
		patterns.add("CallPartyNumber_CallId");
		patterns.add("CallPartyNumber_Timestamp");
		return patterns;
	}
}
