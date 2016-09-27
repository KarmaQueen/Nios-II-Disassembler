package paraverity;

public class Convert {
	
	public static String toBinaryString(byte b){
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	
	public static String binToHex(String s){
		return Integer.toString(Integer.parseInt(s, 2), 16);
	}
	
	public static String hexToBin(String s){
		return Integer.toString(Integer.parseInt(s, 16), 2);
	}
	
	public static String decToBin(int dec){
		return Integer.toBinaryString(dec);
	}
	
	public static int binToDec(String s){
		while(s.length() < 32) s = s.substring(0, 1) + s;
		return (int)Long.parseLong(s, 2);
	}
	
	public static int binToUDec(String s){
		return Integer.parseInt(s, 2);
	}
	
	public static String decToHex(int dec){
		return binToHex(Integer.toBinaryString(dec));
	}
	
	public static int hexToDec(String s){
		return Integer.parseInt(s, 16);
	}
	
}
