package io.github.townyadvanced.iconomy.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StringMgmt {
	public static String[] remFirstArg(String[] arr) {

		return remArgs(arr, 1);
	}

	public static String[] remLastArg(String[] arr) {

		return subArray(arr, 0, arr.length - 1);
	}

	public static String[] remArgs(String[] arr, int startFromIndex) {

		if (arr.length == 0)
			return arr;
		else if (arr.length < startFromIndex)
			return new String[0];
		else {
			String[] newSplit = new String[arr.length - startFromIndex];
			System.arraycopy(arr, startFromIndex, newSplit, 0, arr.length - startFromIndex);
			return newSplit;
		}
	}

	public static String[] subArray(String[] arr, int start, int end) {

		if (arr.length == 0)
			return arr;
		else if (end < start)
			return new String[0];
		else {
			int length = end - start;
			String[] newSplit = new String[length];
			System.arraycopy(arr, start, newSplit, 0, length);
			return newSplit;
		}
	}

	/**
	 * Returns strings that start with a string
	 *
	 * @param list strings to check
	 * @param startingWith string to check with list
	 * @return strings from list that start with startingWith
	 */
	public static List<String> filterByStart(List<String> list, String startingWith) {
		if (list == null || startingWith == null) {
			return Collections.emptyList();
		}
		return list.stream().filter(name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(startingWith.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
	}

	
	/**
     * Checks text against two variables, if it equals at least one returns true.
     *
     * @param text The text that we were provided with.
     * @param against The first variable that needs to be checked against
     * @param or The second variable that it could possibly be.
     *
     * @return <code>Boolean</code> - True or false based on text.
     */
    public static boolean is(String text, String[] is) {
        for (String s : is) {
            if (text.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}
