package com.base.utils;

import java.math.BigDecimal;

public class MathUtils {

	public static double add(double d1, double d2) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.add(dd2);
		BigDecimal dd3 = dd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double add(double d1, double d2, int scala) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.add(dd2);
		BigDecimal dd3 = dd.setScale(scala, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double subtract(double d1, double d2) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.subtract(dd2);
		BigDecimal dd3 = dd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double subtract(double d1, double d2, int scala) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.subtract(dd2);
		BigDecimal dd3 = dd.setScale(scala, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double multiply(double d1, double d2) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.multiply(dd2);
		BigDecimal dd3 = dd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double multiply(double d1, double d2, int scala) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.multiply(dd2);
		BigDecimal dd3 = dd.setScale(scala, BigDecimal.ROUND_HALF_UP);
		return dd3.doubleValue();
	}

	public static double divide(double d1, double d2) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.divide(dd2, 2, BigDecimal.ROUND_HALF_UP);
		return dd.doubleValue();
	}

	public static double divide(double d1, double d2, int scala) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		BigDecimal dd = dd1.divide(dd2, scala, BigDecimal.ROUND_HALF_UP);
		return dd.doubleValue();
	}

	public static int compareTo(double d1, double d2) {
		BigDecimal dd1 = new BigDecimal(d1);
		BigDecimal dd2 = new BigDecimal(d2);
		return dd1.compareTo(dd2);
	}

	public static void main(String[] args) {
		double d1 = divide(30000000*0.8, 100,2);
//		double dd1 = 1.000001;
//		double dd2 = 1.00000100001;
		System.out.println(d1);
	}

}
