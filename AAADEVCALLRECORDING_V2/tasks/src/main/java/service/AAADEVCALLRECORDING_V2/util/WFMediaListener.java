package service.AAADEVCALLRECORDING_V2.util;

import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.MediaListener;
import com.avaya.collaboration.call.media.PlayOperationCause;
import com.avaya.collaboration.call.media.RecordOperationCause;
import com.avaya.collaboration.call.media.SendDigitsOperationCause;
import com.avaya.collaboration.eventing.EventMetaData;
import com.avaya.collaboration.eventing.EventProducer;
import com.avaya.collaboration.eventing.EventingFactory;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import service.AAADEVCALLRECORDING_V2.CallRecordingExecution;

public class WFMediaListener
  implements MediaListener
{
  private final Logger log = LoggerFactory.getLogger(WFMediaListener.class);
  private Map<UUID, Participant> uuidMap;
  private Map<UUID, String> uuidCallIdMap;
  private Call call;
  private NodeInstance nodeInstance;
  
  public WFMediaListener()
  {
    this.uuidMap = new ConcurrentHashMap();
    this.uuidCallIdMap = new ConcurrentHashMap();
  }
  
  public void addUUIDToMap(UUID uuid, Participant participant)
  {
    this.uuidMap.put(uuid, participant);
  }
  
  public void addUUIDToCallIdMap(UUID uuid, String callId)
  {
    this.uuidCallIdMap.put(uuid, callId);
  }
  
  public void removeUUIDFromMap(UUID uuid)
  {
    this.uuidMap.remove(uuid);
  }
  
  public void removeUUIDFromCallIdMap(UUID uuid)
  {
    this.uuidCallIdMap.remove(uuid);
  }
  
  public void setCall(Call call)
  {
    this.call = call;
  }
  
  public Call getCall()
  {
    return this.call;
  }
  
  public void setNodeInstance(NodeInstance nodeInstance)
  {
    this.nodeInstance = nodeInstance;
  }
  
  public NodeInstance getNodeInstance()
  {
    return this.nodeInstance;
  }
  
  public Map<UUID, Participant> getUuidMap()
  {
    return this.uuidMap;
  }
  
  public void digitsCollected(UUID requestId, String digits, DigitCollectorOperationCause cause){   }
  
  public void playCompleted(UUID requestId, PlayOperationCause cause) {  }
  
  public void sendDigitsCompleted(UUID arg0, SendDigitsOperationCause arg1) {}
  
  public void recordCompleted(UUID requestId, RecordOperationCause cause)
  {
    if (this.log.isFinestEnabled()) {
      this.log.finest("Record operation completed for UUID = " + requestId + "; Cause: " + cause);
    }
    CallRecordingExecution thisNode = (CallRecordingExecution)getNodeInstance();
   
    if (thisNode.Async())
    {
      if (this.log.isFinestEnabled()) {
        this.log.finest("recordCompleted: worked as Async task, publish an event.");
      }
      EventMetaData meta = EventingFactory.createEventMetaData();
      meta.addValue("recordingUUID", requestId.toString());
      
      JSONObject output = new JSONObject();
      try
      {
        output.put("cause", cause);
        output.put("retrievalUrl", thisNode.getRetrievalUrl());
        output.put("urlFirstAudio", thisNode.getfirstAudioURL());
        output.put("urlSecondAudio", thisNode.getsecondAudioURL());
        if (RecordOperationCause.FAILED.equals(cause)) {
          output.put("status", "FAILED");
        } else {
          output.put("status", "SUCCESS");
        }
      }
      catch (Exception e)
      {
        this.log.error("recordCompleted: Json error: ", e);
      }
      EventProducer producer = EventingFactory.createEventProducer("Media", "MEDIA_PROCESSED", meta, output
        .toString(), "");
      producer.publish();
    }
  }
  
  public Map<UUID, String> getUuidCallMap()
  {
    return this.uuidCallIdMap;
  }
  
  public void setUuidCallMap(Map<UUID, String> uuidCallMap)
  {
    this.uuidCallIdMap = uuidCallMap;
  }
}
