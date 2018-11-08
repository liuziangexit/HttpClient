package com.game.http.Util;

public class KMP {

    static public int indexOf(byte[] data, int begin, int end, byte[] pattern) {
	if (data == null || pattern == null)
	    return -1;

	int[] failure = computeFailure(pattern);

	int j = 0;

	while (begin++ < end) {
	    while (j > 0 && (pattern[j] != '*' && pattern[j] != data[begin])) {
		j = failure[j - 1];
	    }
	    if (pattern[j] == '*' || pattern[j] == data[begin]) {
		j++;
	    }
	    if (j == pattern.length) {
		return begin - pattern.length + 1;
	    }
	}
	return -1;

    }

    private static int[] computeFailure(byte[] pattern) {
	int[] failure = new int[pattern.length];

	int j = 0;
	for (int i = 1; i < pattern.length; i++) {
	    while (j > 0 && pattern[j] != pattern[i]) {
		j = failure[j - 1];
	    }
	    if (pattern[j] == pattern[i]) {
		j++;
	    }
	    failure[i] = j;
	}

	return failure;
    }

}
