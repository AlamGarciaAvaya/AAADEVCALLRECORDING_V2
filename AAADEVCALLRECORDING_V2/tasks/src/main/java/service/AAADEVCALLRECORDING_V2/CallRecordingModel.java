package service.AAADEVCALLRECORDING_V2;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class CallRecordingModel extends BpmNode {
	private static final long serialVersionUID = 1L;
	private String fileUri;
	private String recordedParty;
	private String maxDuration;
	private String terminationKey;
	private String fileNamePattern;
	private String retrievalUrl;
	private Boolean isAsync = null;

	public CallRecordingModel(String name, String id) {
		super(name, id);
	}

	public String getFileUri() {
		return this.fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

	public String getRecordedParty() {
		return this.recordedParty;
	}

	public void setRecordedParty(String recordedParty) {
		this.recordedParty = recordedParty;
	}

	public String getMaxDuration() {
		return this.maxDuration;
	}

	public void setMaxDuration(String maxDuration) {
		this.maxDuration = maxDuration;
	}

	public String getTerminationKey() {
		return this.terminationKey;
	}

	public void setTerminationKey(String terminationKey) {
		this.terminationKey = terminationKey;
	}

	public String getFileNamePattern() {
		return this.fileNamePattern;
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}

	public String getRetrievalUrl() {
		return this.retrievalUrl;
	}

	public void setRetrievalUrl(String retrievalUrl) {
		this.retrievalUrl = retrievalUrl;
	}

	public Boolean isAsync() {
		return this.isAsync;
	}

	public void setAsync(String async) {
		if ("true".equalsIgnoreCase(async)) {
			this.isAsync = Boolean.valueOf(true);
		} else {
			this.isAsync = Boolean.valueOf(false);
		}
	}

	public boolean validateProperties(List<String> w, List<String> e) {
		boolean isValid = true;
		if (!WFUtil.validateUCIDMapping(w, e, getDataInputAssociations(),
				getName())) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"fileUri"))
				&& ((this.fileUri == null) || (this.fileUri.trim().isEmpty()))) {
			e.add("No value assigned to 'File URI' either through mapping or property.");
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"recordedParty"))
				&& ((this.recordedParty == null) || (this.recordedParty.trim()
						.isEmpty()))) {
			e.add("No value assigned to 'Recorded Party' either through mapping or property.");
			isValid = false;
		}
		if ((!StringUtils.isBlank(this.maxDuration))
				&& (!WFUtil.validateNumberProperty(this.maxDuration,
						"maxDuration", e))) {
			isValid = false;
		}
		if (StringUtils.isBlank(this.fileNamePattern)) {
			w.add("The Redording File Name Pattern is not seleted. The default RECORDEDPARTY_CALLID will be used.");
		}
		if (!StringUtils.isBlank(this.terminationKey)) {
			Pattern paramPattern = Pattern.compile("[0-9*#]");
			Matcher matcher = paramPattern.matcher(this.terminationKey);
			if (!matcher.matches()) {
				e.add("Invalid Termination Key. Valid keys are 0 to 9, * and #.");
				isValid = false;
			}
		}
		return (super.validateProperties(w, e)) && (isValid);
	}
}
