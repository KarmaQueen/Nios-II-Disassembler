package paraverity;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
	
	public static final String filename = "Cyclone3_EP3C25F256C8N_25P28V6P.BIN";
	public static final String outputName = "output.txt";
	public static byte[] bytes;
	
	public static int lines;
	
	public static boolean printToCnsl = false;
	public static boolean printToFile = true;
	
	public static boolean printBinaryCode = true;
	public static boolean printAddress = true;
	public static boolean printParsedCode = true;
	

	public static void main(String[] args) {
		
		test();
		
		//initialize
		NiosDecoder.init();
		
		//read the binary file
		bytes = readBinaryFile(filename);
		if(bytes == null) 
			System.out.println("Error: File does not exist!");
		
		//Each instruction is 4 bytes, so the number of instructions is num of bytes / 4.
		lines = bytes.length>>2;
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputName);
		} catch(Exception e){
			e.printStackTrace();
		}
		
		String header = "";
		if(printBinaryCode) header += "Bit code                          ";
		if(printAddress) header += "Address     ";
		if(printParsedCode) header += "Assembly Code";
		
		if(printToCnsl) System.out.println(header);
		if(printToFile) pw.write(header);
		
		String s = "";
		for(int i = 0; i < lines; i++){
			s = printLine(i);
			if(printToCnsl) System.out.print(s);
			if(printToFile) pw.write(s);
		}
		
		System.out.println("Done!");
		pw.close();
	}
	
	public static void test(){
		
		//System.exit(0);
	}
	
	public static String getLine(int line){
		int startingByte = line<<2;
		
		//converts bytes to binary code in string
		String instruction = "";
		for(int i = startingByte; i < startingByte + 4; i++)
			instruction += Convert.toBinaryString(bytes[i]);
		
		return instruction;
	}
	
	public static String printLine(int line){

		String instruction = getLine(line);
		
		String printContent = "";
		
		//prints the binary code
		if(printBinaryCode){
			printContent += instruction;
			if(printAddress)
				printContent += "  ";
		}
		
		if(printAddress){
			String str = "0x" + String.format("%8s", Integer.toHexString(line*16)).replace(' ', '0');
			printContent += str;
			if(printParsedCode)
				printContent += "  ";
		}
		
		if(printParsedCode) {
			String code = NiosDecoder.decode(instruction);
			printContent += code;
		}
		
		printContent += "\n";
		
		return printContent;
	}
	
	public static byte[] readBinaryFile(String filename){
		try{
			Path path = Paths.get(filename);
			return Files.readAllBytes(path);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
