package service.AAADEVCALLRECORDING_V2.util;

import java.util.ArrayList;
import java.util.List;

public class FillerUtil {
	private static FillerUtil instance = null;

	public static FillerUtil getInstance() {
		if (instance == null) {
			synchronized (FillerUtil.class) {
				if (instance == null) {
					instance = new FillerUtil();
				}
			}
		}
		return instance;
	}

	public List<String> booleanType() {
		List<String> booleanTypes = new ArrayList();
		booleanTypes.add("True");
		booleanTypes.add("False");
		return booleanTypes;
	}

}