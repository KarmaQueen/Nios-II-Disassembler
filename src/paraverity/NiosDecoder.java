package paraverity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NiosDecoder {
	
	public static Map<String, String> opMap;
	public static Map<String, String> opxMap;
	
	public static void init(){
		initOP();
		initOPX();
	}
	
	public static void initOP(){
		opMap = new HashMap<String, String>();
		Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream("opcodeDecode.txt"));
			
			String[] lineCodes;
			while(scanner.hasNextLine()){
				lineCodes = scanner.nextLine().split(" ");
				opMap.put(lineCodes[0], lineCodes[1]);
			}
			
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public static void initOPX(){
		opxMap = new HashMap<String, String>();
		Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream("opxDecode.txt"));
			
			String[] lineCodes;
			while(scanner.hasNextLine()){
				lineCodes = scanner.nextLine().split(" ");
				opxMap.put(lineCodes[0], lineCodes[1]);
			}
			
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public static enum type{Itype, Rtype, Jtype};
	
	public static String getOpcodeBinary(String s){
		return s.substring(26);
	}
	
	public static int getOpcodeDec(String s){
		return Convert.binToDec(getOpcodeBinary(s));
	}
	
	public static String getOpcodeHex(String s){
		String str = Convert.binToHex(getOpcodeBinary(s));
		if(str.length() < 2) str = "0" + str; 
		return str;
	}
	
	/**
	 * Gets the Type of the 32-bit operation.
	 * @param instruction the 32-bit operation
	 * @return
	 */
	public static type getType(String instruction){
		String str = getOpcodeHex(instruction);
		if("3a".equals(str)) return type.Rtype;
		if("32".equals(str)) return type.Rtype;
		if("00".equals(str)) return type.Jtype;
		String opcodeInstr = opMap.get(str);
		if(opcodeInstr == null) return null;
		else return type.Itype;
	}
	
	public static String decodeOpcode(String s){
		String str = Convert.binToHex(s);
		if(str.length() < 2) str = "0" + str;
		return opMap.get(str);
	}
	
	public static String extractAndDecodeOpcode(String s){
		return opMap.get(getOpcodeHex(s));
	}
	public static String decodeOpx(String s){
		String str = Convert.binToHex(s);
		if(str.length() < 2) str = "0" + str;
		return opxMap.get(str);
	}
	
	public static String decode(String instruction){
		type t = getType(instruction);
		
		if(t == null){
			String opHex = Convert.binToHex(instruction.substring(26));
			if(opHex.length() < 2) opHex = "0" + opHex;
			return opHex;
		}
		switch(t){
		case Rtype: return decodeRtype(instruction);
		case Itype: return decodeItype(instruction);
		case Jtype: return decodeJtype(instruction);
		}
		return null;
	}
	
	//Input: 32-bit
	//Assume input is correct R-type
	public static String decodeRtype(String s){
		
		String rA = s.substring(0, 5);
		String rB = s.substring(5, 10);
		String rC = s.substring(10, 15);
		String opx = s.substring(15, 21);
		String extra = s.substring(21, 26);
		boolean extraIs0 = "00000".equals(extra);
		
		rA = decodeRegister(rA);
		rB = decodeRegister(rB);
		rC = decodeRegister(rC);
		opx = decodeOpx(opx);
		
		switch(opx){
		
		case "custom":
			return custom(s);
		
		case "add":
			if(extraIs0) return opx + " " + rC + ", " + rA + ", " + rB;
			else return invalid(opx);
		
			// opx extra(udec)
		case "break":
			if(rA.equals("zero") && rB.equals("zero") && rC.equals("sstatus"))
				return opx + " " + Convert.binToUDec(extra);
			else return invalid(opx);
			
		case "bret":
			if(rA.equals("sstatus") && rB.equals("zero") && rC.equals("sstatus") && extraIs0)
				return opx;
			else return invalid(opx);

		case "callr":
			if(rB.equals("zero") && rC.equals("ra") && extraIs0) return opx + " " + rA;
			else return invalid(opx);
			
		case "cmpeq":
		case "cmpge":
		case "cmpgeu":
		case "cmplt":
		case "cmpltu":
		case "cmpne":
		case "div":
		case "divu":
			if(extraIs0) return opx + " " + rC + ", " + rA + ", " + rB;
			else invalid(opx);
			
		case "eret":
			if(rA.equals("ea") && rB.equals("sstatus") && extraIs0) return opx;
			return invalid(opx);
			
		case "flushi":
		case "flushp":
		case "initi":
			if(rB.equals("zero") && rC.equals("zero") && extraIs0) return opx + " " + rA;
			else return invalid(opx);
			
			// op rA
		case "jmp":
			if(rB.equals("zero") && rC.equals("zero") && extraIs0)
				return opx + " " + rA;
			else return invalid(opx);
		}
		
		
		
		return opx + " " + rC + ", " + rA + ", " + rB + "RTYPE";
	}
	
	//Input: 32-bit
	//Assume input is correct I-type
	public static String decodeItype(String s){

		//Extract fields
		String rA, rB, IMM16, op;
		rA = s.substring(0, 5);
		rB = s.substring(5, 10);
		IMM16 = s.substring(10, 26);
		op = s.substring(26);
		
		rA = decodeRegister(rA);
		rB = decodeRegister(rB);
		op = decodeOpcode(op);
		
		switch(op){
		// op IMM16(rA)
		case "initd":
		case "initda":
		case "flushd":
		case "flushda":
			if(rB.equals("zero")) 
				return op + Convert.binToDec(IMM16) + "(" + rA + ")";
			else return invalid(op);
		
			// op rB, IMM16(rA)
		case "ldw":
		case "ldwio":
			if(IMM16.substring(15).equals("0")){
				return op + " " + rB + ", " + Convert.binToDec(IMM16) + "(" + rA + ")";
			} else return invalid(op);
			
		case "ldb":
		case "ldbio":
		case "ldbu":
		case "ldbuio":
		case "ldh":
			return op + " " + rB + ", " + Convert.binToDec(IMM16) + "(" + rA + ")";
			
			// op IMM16
		case "br":
			if(rA.equals("zero") && rB.equals("zero")) return op + " " + IMM16;
			return invalid(op);
			
			// op rA, rB, IMM16(hex)
		case "bne":
		case "bltu":
		case "blt":
		case "bgt":
		case "bgeu":
		case "bge":
		case "beq":
			return op + " " + rA + ", " + rB + ", " + Convert.binToHex(IMM16);
			
			// op rB, rA, IMM16(dec)
		case "andi":
		case "addi":
		case "cmpeqi":
		case "cmpgei":
		case "cmpgeui":
		case "cmplti":
		case "cmpltui":
		case "cmpnei":
			return op + " " + rB + ", " + rA + ", " + Convert.binToDec(IMM16);
		
		}
		
		IMM16 = "0x" + String.format("%4s", Convert.binToHex(IMM16)).replace(' ', '0').toUpperCase();
		
		return op + " " + rA + ", " + rB + ", " + IMM16;
	}
	
	//Input: 32-bit
	//Assume input is correct J-type
	public static String decodeJtype(String s){

		String op = s.substring(26);
		String IMM26 = s.substring(0, 26);
		
		op = decodeOpcode(op);
		IMM26 = "0x" + Convert.binToHex(IMM26 + "00");
		
		switch(op){
		case "call":
		case "jmpi":
			return op + " " + IMM26;
		
		}
		
		return op + " " + IMM26;
	}
	
	public static String decodeRegister(String s){
		if(s.length() != 5) return "LENGTH NOT EQUAL";
		int regVal = Convert.binToUDec(s);
		switch(regVal){
		case 0: return "zero";
		case 1: return "at";
		case 24: return "et";
		case 25: return "bt";
		case 26: return "gp";
		case 27: return "sp";
		case 28: return "fp";
		case 29: return "ea";
		case 30: return "sstatus";
		case 31: return "ra";
		default: return "r" + regVal;
		}
	}
	
	public static String invalid(String op){
		return String.format("%-16s", op + ":") + "invalid syntax";
	}
	
	public static String custom(String s){
		String rA, rB, rC, readra, readrb, writerc, N;
		rA = s.substring(0, 5);
		rB = s.substring(5, 10);
		rC = s.substring(10, 15);
		readra = s.substring(15, 16);
		readrb = s.substring(16, 17);
		writerc = s.substring(17, 18);
		N = s.substring(18, 26);
		
		if(readra.equals("1")) rA = decodeOpcode(rA);
		else rA = "c" + Convert.binToUDec(rA);
		
		if(readrb.equals("1")) rB = decodeOpcode(rB);
		else rB = "c" + Convert.binToUDec(rB);
		
		if(writerc.equals("1")) rC = decodeOpcode(rC);
		else rC = "c" + Convert.binToUDec(rC);
		
		return "custom " + Convert.binToUDec(N) + ", " + rC + ", " + rA + ", " + rB; 
	}
}
