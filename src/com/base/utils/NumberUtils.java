package com.base.utils;

import java.text.DecimalFormat;

public class NumberUtils {
	
	
	public static String digitUppercase(double n){

		DecimalFormat myformat = new DecimalFormat("#0.##");   
		String currencyDigits= myformat.format(n);  
		
		String CN_ZERO = "零";  
		String CN_SYMBOL = "";  
		String CN_DOLLAR = "元";  
		String CN_INTEGER = "整";  
		  
		String[] digits = new String[] { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };  
		String[] radices = new String[] { "", "拾", "佰", "仟" };  
		String[] bigRadices = new String[] { "", "万", "亿", "万" };  
		String[] decimals = new String[] { "角", "分" }; 
		
		String integral = null; // 整数部分  
		String decimal = null; // 小数部分  
		String outputCharacters = null; // 最终转换输出结果  
		
		String d = null;  
		int zeroCount = 0, p = 0, quotient = 0, modulus = 0;  
		
		 // 删除数字中的逗号,  
		 currencyDigits = currencyDigits.replace("/,/g", "");  
		 // 删除数字左边的0  
		 currencyDigits = currencyDigits.replace("/^0+/", "");  
		
		 // 拆分数字中的整数与小数部分  
		 String[] parts = currencyDigits.split("\\.");  
		 if (parts.length > 1) {  
		     integral = parts[0];  
		     decimal = parts[1];  
		     // 如果小数部分长度大于2，四舍五入，保留两位小数  
			 if (decimal.length() > 2) {  
				 long dd = Math.round(Double.parseDouble("0."+decimal) * 100) ;  
				 decimal = Long.toString(dd);  
			 }  
		 }else {  
		     integral = parts[0];  
		     decimal = "0";  
		 }  
		
		  outputCharacters = "";  
		 // Process integral part if it is larger than 0:  
		 if (Double.parseDouble(integral) > 0) {  
			 zeroCount = 0;  
			 for (int i = 0; i < integral.length(); i++) {  
				 p = integral.length() - i - 1;  
		         d = integral.substring(i, i + 1);  
		
		         quotient = p / 4;  
		         modulus = p % 4;  
		         if(d.equals("0")){  
		             zeroCount++;  
		         }else{  
		             if(zeroCount > 0){  
		                 outputCharacters += digits[0];  
		             }  
		             zeroCount = 0;  
		             outputCharacters += digits[Integer.parseInt(d)] + radices[modulus];  
		         }  
		         if(modulus == 0 && zeroCount < 4){  
		             outputCharacters += bigRadices[quotient];  
		         }  
		     }//end for 
		     outputCharacters += CN_DOLLAR;  
		 }//end if 
		
		 // Process decimal part if it is larger than 0:  
		 if (Double.parseDouble(decimal) > 0) {  
		     for (int i = 0; i < decimal.length(); i++) {  
		         d = decimal.substring(i, i + 1);  
		         if (!d.equals("0")) {  
		             outputCharacters += digits[Integer.parseInt(d)] + decimals[i];  
		         } else {  
		             if (i == 0) {  
		                 outputCharacters += CN_ZERO;  
		             }  
		         }  
		     }  
		 }  
		   
		 // Confirm and return the final output string:  
		 if (outputCharacters.equals("")) {  
		     outputCharacters = CN_ZERO + CN_DOLLAR;  
		 }  
		 if (decimal == null || decimal.equals("0")) {  
		     outputCharacters += CN_INTEGER;  
		 }  
		
		 outputCharacters = CN_SYMBOL + outputCharacters;  
		 return outputCharacters; 
	}
	
	
	// 将数字转化为大写   
	 public static String numToUpper(int num) {   
	        String u[] = {"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"};   
	        //String u[] = {"零","一","二","三","四","五","六","七","八","九"};   
	        char[] str = String.valueOf(num).toCharArray();   
	        String rstr = "";   
	        for (int i = 0; i < str.length; i++) {   
	                rstr = rstr + u[Integer.parseInt(str[i] + "")];   
	         }   
	        return rstr;   
	 }   
	    
	 // 月转化为大写   
	 public static String monthToUppder(int month) {   
	          if(month < 10) {   
	                  return numToUpper(month);           
	          } else if(month == 10){   
	                  return "拾";   
	          } else {   
	                  return "拾" + numToUpper(month - 10);   
	          }   
	 }
	 
	 // 月转化为大写   （支票）
	 public static String monthToUppderCheque(int month) {   
	          if(month < 10) {   
	                  return "零"+numToUpper(month);           
	          } else if(month == 10){   
	                  return "零壹拾";   
	          } else {   
	                  return "壹拾" + numToUpper(month - 10);   
	          }   
	 } 
	    
	 // 日转化为大写   
	 public static String dayToUppder(int day) {   
	          if(day < 20) {   
	                   return monthToUppder(day);   
	          } else {   
	                   char[] str = String.valueOf(day).toCharArray();   
	                   if(str[1] == '0') {   
	                            return numToUpper(Integer.parseInt(str[0] + "")) + "拾";   
	                   }else {   
	                            return numToUpper(Integer.parseInt(str[0] + "")) + "拾" + numToUpper(Integer.parseInt(str[1] + ""));   
	                   }   
	        }   
	}   
	 
	 
	 // 日转化为大写   （支票）
	 public static String dayToUppderCheque(int day) {
		 	  if(day<10){
		 		  return "零"+numToUpper(day);
		 	  }else if(day==10){
		 		 return "零壹拾";
		 	  }else if(day>10&&day<20){
		 		  return "壹拾" + numToUpper(day - 10);
		 	  }else if(day==20){
		 		 return "零贰拾";
		 	  }else if(day>20&&day<30){
		 		  return "贰拾" + numToUpper(day - 20);
		 	  }else if(day==30){
		 		 return "零叁拾";
		 	  }else if(day>30&&day<32){
		 		 return "叁拾" + numToUpper(day - 30);
		 	  }else{
		 		 return "错误";
		 	  }
	} 
}
