package com.blakebr0.cucumber.util;

import java.util.Random;

public final class Utils {
	public static final Random RANDOM = new Random();

	public static int randInt(int min, int max) {
		return RANDOM.nextInt(max - min + 1) + min;
	}
}
