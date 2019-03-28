package service.AAADEVCALLRECORDING_V2;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.RecordItem;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.model.BpmNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import service.AAADEVCALLRECORDING_V2.util.CommTaskUtil;
import service.AAADEVCALLRECORDING_V2.util.WFMediaListener;

public class CallRecordingExecution extends NodeInstance {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory
			.getLogger(CallRecordingExecution.class);
	private String ucid;
	private String retrievalUrl;

	public String fileUri;
	public static UUID uuid;
	public static UUID uuidCalled;
	public static String fileName;
	public static String fileNameCalled;

	public CallRecordingExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	private boolean isAsync = false;
	private WFMediaListener mediaListener;
	private boolean recordedParticipantCallRecording = false;
	private String firstAudioURL;
	private String secondAudioURL;
	private RecordMediaListener mediaListenerCalling;
	private RecordMediaListenerCalled mediaListenerCalled;

	public String getfirstAudioURL() {
		return this.firstAudioURL;
	}

	public String getsecondAudioURL() {
		return this.secondAudioURL;
	}

	public boolean getRecordedParticipantCallRecording() {
		return this.recordedParticipantCallRecording;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getUcid() {
		return this.ucid;
	}

	public String getRetrievalUrl() {
		return this.retrievalUrl;
	}

	public boolean Async() {
		return this.isAsync;
	}

	public WFMediaListener getMediaListener() {
		if (this.mediaListener == null) {
			this.mediaListener = new WFMediaListener();
		}
		return this.mediaListener;
	}

	public Object execute() throws Exception {
		/*
		 * Recuperando Recorded Party
		 */
		CallRecordingModel model = (CallRecordingModel) getNode();
		String recordedParty = (String) get("recordedParty");
		if (StringUtils.isBlank(recordedParty)) {
			recordedParty = model.getRecordedParty();
			if (StringUtils.isBlank(recordedParty)) {
				throw new IllegalArgumentException(
						"Invalid Argument! recordedParty cannot be empty...");
			}
		}
		/*******************************************************************************/

		if (recordedParty.equals("Calling") || recordedParty.equals("Called")
				|| recordedParty.equals("Answering")) {
			JSONObject jsonBasicRecord = basicRecord(recordedParty);
			return jsonBasicRecord;
		}
		if (recordedParty.equals("CallRecording")) {
			this.recordedParticipantCallRecording = true;
			recordedParty = "Called";
			JSONObject jsonCalled = basicRecord(recordedParty);
			this.firstAudioURL = jsonCalled.getString("retrievalUrl");

			recordedParty = "Calling";
			JSONObject jsonCalling = basicRecord(recordedParty);
			this.secondAudioURL = jsonCalling.getString("retrievalUrl");
			jsonCalling.put("urlFirstAudio", this.firstAudioURL);
			jsonCalling.put("urlSecondAudio", this.secondAudioURL);
			return jsonCalling;
		}
		if (recordedParty.equals("CallRecordingAndMix")) {
			JSONObject jsonCallRecording = callRecording();
			return jsonCallRecording;
		}
		JSONObject error = new JSONObject();
		error.put("Error", "Favor de seleccionar Recorded Party");
		return error;

	}

	public JSONObject callRecording() throws Exception {
		CallRecordingModel model = (CallRecordingModel)getNode();
		RecordItem recordItem = MediaFactory.createRecordItem();
		RecordItem recordItemCalled = MediaFactory.createRecordItem();
		
	    MediaService mediaService = MediaFactory.createMediaService();
	    MediaService mediaServiceCalled = MediaFactory.createMediaService();
	    
	    String callID = (String)get("ucid");
	    if (StringUtils.isBlank(callID)) {
	      throw new IllegalArgumentException("Invalid Argument! CallID cannot be empty...");
	    }
	    setUcid(callID);	
	    fileUri = (String)get("fileUri");
	    if (StringUtils.isBlank(fileUri))
	    {
	      fileUri = model.getFileUri();
	      if (StringUtils.isBlank(fileUri)) {
	        throw new IllegalArgumentException("Invalid Argument! fileUri cannot be empty...");
	      }
	    }
	    String maxDuration = (String)get("maxDuration");
	    if (StringUtils.isBlank(maxDuration)) {
	      maxDuration = model.getMaxDuration();
	    }
	    if ((maxDuration != null) && (!maxDuration.isEmpty())) {
	      try
	      {
	        int duration = Integer.parseInt(maxDuration);
	        recordItem.setMaxDuration(duration);
	        recordItemCalled.setMaxDuration(duration);
	      }
	      catch (NumberFormatException nfe)
	      {
	        throw new IllegalArgumentException("Invalid Argument! maxDuration is not in the number format...");
	      }
	    }
	    String terminationKey = (String)get("terminationKey");
	    if (StringUtils.isBlank(terminationKey)) {
	      terminationKey = model.getTerminationKey();
	    }
	    if (!StringUtils.isBlank(terminationKey)) {
	        recordItem.setTerminationKey(terminationKey);
	        recordItemCalled.setTerminationKey(terminationKey);
	      }
	    String fileNamePattern = model.getFileNamePattern();
	      fileNamePattern = "CallPartyNumber_CallId";

	    Call call = CommTaskUtil.getCall(callID);
	    
	    if (call == null)
	    {
	      throw new IllegalArgumentException("Call object not found...");
	    }
	    Participant participant = call.getCallingParty();
	    Participant participantCalled = call.getCalledParty();
	    
	    fileName = generateFileName(fileNamePattern, call, callID, participant);
	    fileNameCalled = generateFileNameCalled(fileNamePattern, call, callID, participantCalled);
	    
	    String fileUriCalling = modifyFileUri(fileUri, fileName);
	    String fileUriCalled = modifyFileUriCalled(fileUri, fileNameCalled);
	    
	    recordItem.setFileUri(fileUriCalling);
	    recordItemCalled.setFileUri(fileUriCalled);
	    
	    uuid = null;
	    uuidCalled = null;
	    if (participant != null)
	    {
	    	this.mediaListenerCalling = new RecordMediaListener(fileUri, call, uuidCalled, mediaServiceCalled);
	        this.mediaListenerCalling.setNodeInstance(this);
	        
	        this.mediaListenerCalled = new RecordMediaListenerCalled(fileUri, call, uuid, mediaService);
	        this.mediaListenerCalled.setNodeInstance(this);
	        
	      uuid = mediaService.record(participant, recordItem, this.mediaListenerCalling);
	      uuidCalled = mediaServiceCalled.record(participantCalled, recordItemCalled, this.mediaListenerCalled);
	      
	      if (model.isAsync().booleanValue())
	      {
	        this.isAsync = true;
	        String subscribeID = subscribeByCorrelationKey("Media", "MEDIA_PROCESSED", "recordingUUID", uuid.toString());
	        String subscribeIDCalled = subscribeByCorrelationKey("Media", "MEDIA_PROCESSED", "recordingUUID", uuidCalled.toString());
	      }
	    }
	    else
	    {
	    
	      throw new IllegalArgumentException("The participant cannot be identified at the current call state.");
	    }

	    String [] separateDominio = fileUri.split("/");
	    JSONObject output = new JSONObject();
	    output.put("cause", "Proceeded without waiting until the recording is completed.");
	    output.put("status", NodeInstance.Status.SUCCESS.toString());
	    output.put("url", "https://"+separateDominio[2]+"/services/AAADEVLOGGER/AAACallRecording.html");
	    
	    return output;

	}

	public JSONObject basicRecord(String recordedParty) throws Exception {
		CallRecordingModel model = (CallRecordingModel) getNode();
		RecordItem recordItem = MediaFactory.createRecordItem();
		MediaService mediaService = MediaFactory.createMediaService();

		String callID = (String) get("ucid");
		if (StringUtils.isBlank(callID)) {
			throw new IllegalArgumentException(
					"Invalid Argument! CallID cannot be empty...");
		}
		setUcid(callID);

		String fileUri = (String) get("fileUri");
		if (StringUtils.isBlank(fileUri)) {
			fileUri = model.getFileUri();
			if (StringUtils.isBlank(fileUri)) {
				throw new IllegalArgumentException(
						"Invalid Argument! fileUri cannot be empty...");
			}
		}
		String maxDuration = (String) get("maxDuration");
		if (StringUtils.isBlank(maxDuration)) {
			maxDuration = model.getMaxDuration();
		}
		if ((maxDuration != null) && (!maxDuration.isEmpty())) {
			try {
				int duration = Integer.parseInt(maxDuration);
				recordItem.setMaxDuration(duration);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(
						"Invalid Argument! maxDuration is not in the number format...");
			}
		}
		String terminationKey = (String) get("terminationKey");
		if (StringUtils.isBlank(terminationKey)) {
			terminationKey = model.getTerminationKey();
		}
		if (!StringUtils.isBlank(terminationKey)) {
			recordItem.setTerminationKey(terminationKey);
		}
		String fileNamePattern = model.getFileNamePattern();
		if (StringUtils.isBlank(fileNamePattern)) {
			fileNamePattern = "RecordedParty_CallId";
		}
		this.retrievalUrl = ((String) get("retrievalUrl"));
		if (StringUtils.isBlank(this.retrievalUrl)) {
			this.retrievalUrl = model.getRetrievalUrl();
			if (StringUtils.isBlank(this.retrievalUrl)) {
				this.retrievalUrl = fileUri;
			}
		}
		Call call = CommTaskUtil.getCall(callID);
		if (call == null) {
			log.error("RecordParticipantExecution: Error getting call object...");
			throw new IllegalArgumentException("Call object not found...");
		}
		validateRecordedParty(recordedParty);
		Participant participant = getParticipant(recordedParty, call);
		String fileName = generateFileName(fileNamePattern, recordedParty,
				call, callID, participant);
		fileUri = modifyFileUri(fileUri, fileName);
		this.retrievalUrl = (this.retrievalUrl + fileName + ".wav");
		recordItem.setFileUri(fileUri);
		if (log.isFineEnabled()) {
			log.fine("RecordParticipantExecution: ucid = " + callID
					+ "; fileUri = " + fileUri + "; recordedParty = "
					+ recordedParty + "; maxDuration = " + maxDuration
					+ "; termincationKey = " + terminationKey
					+ "; retrievalUrl = " + this.retrievalUrl);
		}
		UUID uuid = null;
		if (participant != null) {
			if (log.isFinestEnabled()) {
				log.finest("RecordParticipantExecution: Call " + call
						+ ", Participant " + participant + "; Record Item: "
						+ recordItem);
			}
			this.mediaListener = getMediaListener();
			this.mediaListener.setNodeInstance(this);

			uuid = mediaService.record(participant, recordItem,
					this.mediaListener);
			if (model.isAsync().booleanValue()) {
				this.isAsync = true;
				String subscribeID = subscribeByCorrelationKey("Media",
						"MEDIA_PROCESSED", "recordingUUID", uuid.toString());
				if (log.isFinestEnabled()) {
					log.finest("RecordParticipantExecution: work as the async task, subscribeID = "
							+ subscribeID);
				}
			}
		} else {
			log.error("RecordParticipantExecution: participant is null. (UN-SUPPORTED for CALL) Call "
					+ call + "; Record Item: " + recordItem);

			throw new IllegalArgumentException(
					"The participant cannot be identified at the current call state.");
		}
		if (log.isFinestEnabled()) {
			log.finest("RecordParticipantExecution: completed. UUID: " + uuid);
		}
		JSONObject output = new JSONObject();
		output.put("cause",
				"Proceeded without waiting until the recording is completed.");
		output.put("retrievalUrl", this.retrievalUrl);
		output.put("status", NodeInstance.Status.SUCCESS.toString());

		return output;
	}

	private Participant getParticipant(String recordedParty, Call call) {
		Participant participant = null;
		if (recordedParty.equals("Calling")) {
			participant = call.getCallingParty();
		} else if (recordedParty.equals("Called")) {
			participant = call.getCalledParty();
		} else if (recordedParty.equals("Answering")) {
			participant = call.getAnsweringParty();
			if (participant == null) {
				log.error("RecordParticipantExecution: The answering party cannot be identified at the current call state.");

				throw new IllegalArgumentException(
						"The answering party cannot be identified at the current call state.");
			}
		}
		return participant;
	}

	private void validateRecordedParty(String recordedParty) {
		if ((!recordedParty.equals("Calling"))
				&& (!recordedParty.equals("Called"))
				&& (!recordedParty.equals("Answering"))
				&& (!recordedParty.equals("CallRecording"))
				&& (!recordedParty.equals("CallRecordingAndMix"))) {
			log.error("RecordParticipantExecution: invalid recordedParty: "
					+ recordedParty);
			throw new IllegalArgumentException("Invalid recordedParty: "
					+ recordedParty);
		}
	}

	private String generateFileName(String fileNamePattern,
			String recordedParty, Call call, String callID,
			Participant participant) {
		String filename = null;
		if (fileNamePattern.equals("CallPartyNumber_CallId")) {
			filename = participant.getHandle() + "_" + callID;
		} else if (fileNamePattern.equals("CallPartyNumber_Timestamp")) {
			String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
					.format(new Date());
			filename = participant.getHandle() + "_" + timeStamp;
		} else {
			filename = recordedParty + "_" + callID;
		}
		return filename;
	}

	private String modifyFileUri(String fileUri, String fileName) {
		String modifiedUri = null;
		if (fileUri.startsWith("cstore://?")) {
			modifiedUri = fileUri.replace("cstore://?", "cstore://" + fileName
					+ "?");
		} else {
			modifiedUri = fileUri + fileName + ".wav";
		}
		return modifiedUri;
	}

	private String generateFileName(String fileNamePattern, Call call,
			String callID, Participant participant) {
		String filename = null;
		if (fileNamePattern.equals("CallPartyNumber_CallId")) {
			filename = participant.getHandle() + "_" + callID;
		} else if (fileNamePattern.equals("CallPartyNumber_Timestamp")) {
			String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
					.format(new Date());
			filename = participant.getHandle() + "_" + timeStamp;
		} else {
			filename = "_" + callID;
		}
		return filename;
	}

	private String generateFileNameCalled(String fileNamePattern, Call call,
			String callID, Participant participant) {
		String filename = null;
		if (fileNamePattern.equals("CallPartyNumber_CallId")) {
			filename = participant.getHandle() + "_" + callID;
		} else if (fileNamePattern.equals("CallPartyNumber_Timestamp")) {
			String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
					.format(new Date());
			filename = participant.getHandle() + "_" + timeStamp;
		} else {
			filename = "_" + callID;
		}
		return filename;
	}

	private String modifyFileUriCalled(String fileUri, String fileName) {
		String modifiedUri = null;
		if (fileUri.startsWith("cstore://?")) {
			modifiedUri = fileUri.replace("cstore://?", "cstore://" + fileName
					+ "?");
		} else {
			modifiedUri = fileUri + fileName + ".wav";
		}
		return modifiedUri;
	}
}
