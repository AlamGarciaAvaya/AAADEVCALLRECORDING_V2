package service.AAADEVCALLRECORDING_V2;

import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaListener;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.PlayOperationCause;
import com.avaya.collaboration.call.media.RecordOperationCause;
import com.avaya.collaboration.call.media.SendDigitsOperationCause;
import com.avaya.collaboration.eventing.EventMetaData;
import com.avaya.collaboration.eventing.EventProducer;
import com.avaya.collaboration.eventing.EventingFactory;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import service.AAADEVCALLRECORDING_V2.util.MakingPost;

public class RecordMediaListenerCalled implements MediaListener {
	private final Logger log = LoggerFactory
			.getLogger(RecordMediaListenerCalled.class);
	private Map<UUID, Participant> uuidMap;
	private Map<UUID, String> uuidCallIdMap;
	private Call call;
	private NodeInstance nodeInstance;
	private String dominio;
	public UUID uuid;
	public MediaService mediaService;

	public RecordMediaListenerCalled(String dominio, Call call, UUID uuid,
			MediaService mediaService) {
		this.mediaService = mediaService;
		this.uuid = uuid;
		this.call = call;
		this.dominio = dominio;
		this.uuidMap = new ConcurrentHashMap();
		this.uuidCallIdMap = new ConcurrentHashMap();
	}

	public RecordMediaListenerCalled(String dominio) {
		this.dominio = dominio;
		this.uuidMap = new ConcurrentHashMap();
		this.uuidCallIdMap = new ConcurrentHashMap();
	}

	public RecordMediaListenerCalled() {
		this.uuidMap = new ConcurrentHashMap();
		this.uuidCallIdMap = new ConcurrentHashMap();
	}

	public void addUUIDToMap(UUID uuid, Participant participant) {
		this.uuidMap.put(uuid, participant);
	}

	public void addUUIDToCallIdMap(UUID uuid, String callId) {
		this.uuidCallIdMap.put(uuid, callId);
	}

	public void removeUUIDFromMap(UUID uuid) {
		this.uuidMap.remove(uuid);
	}

	public void removeUUIDFromCallIdMap(UUID uuid) {
		this.uuidCallIdMap.remove(uuid);
	}

	public void setNodeInstance(NodeInstance nodeInstance) {
		this.nodeInstance = nodeInstance;
	}

	public NodeInstance getNodeInstance() {
		return this.nodeInstance;
	}

	public Map<UUID, Participant> getUuidMap() {
		return this.uuidMap;
	}

	public void digitsCollected(UUID requestId, String digits,
			DigitCollectorOperationCause cause) {
	}

	public void playCompleted(UUID requestId, PlayOperationCause cause) {
	}

	public void sendDigitsCompleted(UUID arg0, SendDigitsOperationCause arg1) {
	}

	public void recordCompleted(UUID requestId, RecordOperationCause cause) {
		MakingPost post = new MakingPost();
		CallRecordingExecution audioName = null;
		mediaService.stop(call.getCallingParty(), audioName.uuid);
		CallRecordingExecution thisNode = (CallRecordingExecution) getNodeInstance();

		if (thisNode.Async()) {
			// mediaService.stop(call.getCallingParty(), audioName.uuid);
			EventMetaData meta = EventingFactory.createEventMetaData();
			JSONObject output = new JSONObject();
			try {
				meta.addValue("recordingUUID", requestId.toString());
				output.put("cause", cause);
				if (RecordOperationCause.FAILED.equals(cause)) {
					output.put("status", "FAILED");
				} else {

					String[] separateDominio = dominio.split("/");
					String[] respuesta = post.makingPOST(separateDominio[2],
							audioName.fileName, audioName.fileNameCalled);
					log.info("respuesta makingPOST " + respuesta[0]);
					output.put("status", "SUCCESS ");
					output.put(
							"url",
							"https://"
									+ separateDominio[2].toString()
									+ "/services/AAADEVLOGGER/AAACallRecording.html");
				}
			} catch (Exception e) {
				this.log.error("recordCompleted: Json error: ", e);
			}

			EventProducer producer = EventingFactory.createEventProducer(
					"Media", "MEDIA_PROCESSED", meta, output.toString(), "");
			String[] separateDominio = dominio.split("/");
			String[] respuesta = { null };
			try {
				respuesta = post.makingPOST(separateDominio[2],
						audioName.fileName, audioName.fileNameCalled);
			} catch (IOException e) {

				e.printStackTrace();
			} catch (SSLUtilityException e) {

				e.printStackTrace();
			}
			log.info("respuesta makingPOST isAsync" + respuesta[0]);
			producer.publish();

		} else {
			String[] separateDominio = dominio.split("/");
			String[] respuesta = { null };
			try {
				respuesta = post.makingPOST(separateDominio[2],
						audioName.fileName, audioName.fileNameCalled);
			} catch (IOException e) {

				e.printStackTrace();
			} catch (SSLUtilityException e) {

				e.printStackTrace();
			}
			log.info("respuesta makingPOST !isAsync" + respuesta[0]);
		}
	}

	public Map<UUID, String> getUuidCallMap() {
		return this.uuidCallIdMap;
	}

	public void setUuidCallMap(Map<UUID, String> uuidCallMap) {
		this.uuidCallIdMap = uuidCallMap;
	}
}