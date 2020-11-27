package com.gwt.shared;

public class FieldVerifier {

	/**
	 * @param count the number to validate
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidCount(String count) {
		if(count.isEmpty() || !count.matches("\\d+")) {
			return false;
		}
		int number = Integer.parseInt(count);

		return number > 0 && number < 1000;
	}
}
