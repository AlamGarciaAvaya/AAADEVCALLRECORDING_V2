package service.AAADEVCALLRECORDING_V2.util;

import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.call.CallPolicies;
import com.avaya.collaboration.call.media.MediaServerInclusion;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;

public class CommTaskUtil
{
  private static final Logger log = LoggerFactory.getLogger(CommTaskUtil.class);
  
  public static void setMediaOnCall(Call call)
  {
    if (call == null)
    {
      log.warn("call object null. cannot perform operation setMediaOnCall() ");
      return;
    }
    try
    {
      CallPolicies cp = call.getCallPolicies();
      log.finest("Default PreAnswerMediaState for call-ucid:" + call.getUCID() + " is " + cp.getPreAnswerMediaState());
      MediaServerInclusion oldMediaServerInclusion = cp.getMediaServerInclusion();
      cp.setMediaServerInclusion(MediaServerInclusion.INCLUDED);
      log.finest("CallPolicies - MediaServerInclusion changed from " + oldMediaServerInclusion + " to " + cp.getMediaServerInclusion() + " for call-ucid:" + call.getUCID());
    }
    catch (Exception e)
    {
      log.error("Error is setting the media path into a call. ", e);
    }
  }
  
  public static Call getCall(String ucid)
  {
    try
    {
      return CallFactory.getCall(ucid);
    }
    catch (Exception e)
    {
      log.error("Error in getting the call object. Invalid callID:" + ucid, e);
    }
    return null;
  }
  
  public static boolean isCallValid(String ucid)
  {
    if (ucid == null)
    {
      log.fine("ucid null...");
      return false;
    }
    try
    {
      Call call = CallFactory.getCall(ucid);
      log.fine("call is valid. ucid: " + ucid);
      return true;
    }
    catch (Exception e)
    {
      log.fine("Call not valid, Invalid callID: " + ucid);
    }
    return false;
  }
}
