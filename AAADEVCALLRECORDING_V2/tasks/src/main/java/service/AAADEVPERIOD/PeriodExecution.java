package service.AAADEVPERIOD;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import static org.joda.time.DateTimeConstants.SECONDS_PER_HOUR;
import static org.joda.time.DateTimeConstants.SECONDS_PER_MINUTE;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;
import com.avaya.workflow.logger.*;

public class PeriodExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory
			.getLogger(PeriodExecution.class);

	public PeriodExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		StringBuilder durationSB = new StringBuilder();
		try{
		String enterFirstDate = (String) get("firstTimeOfEvent");
		if (StringUtils.isBlank(enterFirstDate)) {
			throw new IllegalArgumentException(
					"Invalid Argument! firstTimeOfEvent cannot be empty...");
		}

		String enterSecondDate = (String) get("secondTimeOfEvent");
		if (StringUtils.isBlank(enterSecondDate)) {
			throw new IllegalArgumentException(
					"Invalid Argument! secondTimeOfEvent cannot be empty...");
		}

		// First Date, Creando objeto LocalDateTime
		String replacEnterFirstDate = enterFirstDate.substring(0,19);
		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.parse(replacEnterFirstDate,
				formatter);

		// Second Date,Creando objeto LocalDateTime
		String replacEnterSecondDate = enterSecondDate.substring(0,19);
		DateTimeFormatter formatterSecond = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateSecond = LocalDateTime.parse(replacEnterSecondDate,
				formatterSecond);
		
		
		durationSB.append(enterFirstDate + " , " + enterSecondDate + " ");
		
		
		Period period = getPeriod(date, dateSecond);
		long time[] = getTime(date, dateSecond);

		if (period.getYears() != 0) {
			durationSB.append(period.getYears());
			durationSB.append(" Years ");
		}
		if (period.getMonths() != 0) {
			durationSB.append(period.getMonths());
			durationSB.append(" Months ");
		}
		if (period.getDays() != 0) {
			durationSB.append(period.getDays());
			durationSB.append(" Days ");
		}
		if (time[0] != 0) {
			durationSB.append(time[0]);
			durationSB.append(" Hours ");
		}
		if (time[1] != 0) {
			durationSB.append(time[1]);
			durationSB.append(" Minutes ");
		}
		if (time[2] != 0) {
			durationSB.append(time[2]);
			durationSB.append(" Seconds ");
		}
		
		}catch(Exception e){
			JSONObject json = new JSONObject();
			json.put("Error", e.toString());
			return json;
		}
		
		
		
		JSONObject json = new JSONObject();
		json.put("duration", durationSB.toString());
		return json;
	}

	private static Period getPeriod(LocalDateTime dob, LocalDateTime now) {
		return Period.between(dob.toLocalDate(), now.toLocalDate());
	}

	private static long[] getTime(LocalDateTime dob, LocalDateTime now) {
		LocalDateTime today = LocalDateTime.of(now.getYear(),
				now.getMonthValue(), now.getDayOfMonth(), dob.getHour(),
				dob.getMinute(), dob.getSecond());
		Duration duration = Duration.between(today, now);

		long seconds = duration.getSeconds();

		long hours = seconds / SECONDS_PER_HOUR;
		long minutes = ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
		long secs = (seconds % SECONDS_PER_MINUTE);

		return new long[] { hours, minutes, secs };
	}

}
