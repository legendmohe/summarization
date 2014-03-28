package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;



public class SummarizationUtil {
	
	public static File fileFromPath(String filePath, boolean create) throws IOException {
		if (filePath == null || filePath.length() == 0) {
			throw new FileNotFoundException();
		}
		File file = new File(filePath);
		if (!file.exists() && create) {
			file.createNewFile();
		}
		if (!file.isFile()) {
			throw new FileNotFoundException(filePath + " is not a file.");
		}
		return file;
	}
	
	public static File[] filesFromPath(String filePath) throws IOException {
		if (filePath == null || filePath.length() == 0) {
			throw new FileNotFoundException();
		}
		File file = new File(filePath);
		if (!file.isDirectory()) {
			throw new FileNotFoundException(filePath + "is not a directory.");
		}
		
		return file.listFiles();
	}
	
	public static String contentOfFileContent(File file, boolean keepLineSeparator, String encoding) throws IOException {
		if (file == null || !file.isFile()) {
			throw new FileNotFoundException("file invaild");
		}
		
		@SuppressWarnings("resource")
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		StringBuffer stringBuffer = new StringBuffer();
		String readLineString = null;
		while ((readLineString = bufferedReader.readLine()) != null) {
			stringBuffer.append(readLineString);
			if (keepLineSeparator) {
				stringBuffer.append("\n");
			}
		}
		
		return stringBuffer.toString();
	}
	
	public static FileWriter fileWriteFormFile(File file, boolean append) throws IOException {
		if (file == null || !file.isFile()) {
			throw new FileNotFoundException();
		}
		
		FileWriter fileWriter = new FileWriter(file, append);
		return fileWriter;
	}
	
	public static void iterateFileContent(File file, FileContentIterator fileContentIterator, String encoding) throws IOException {
		if (file == null || !file.isFile()) {
			throw new FileNotFoundException("file invaild");
		}
		
		@SuppressWarnings("resource")
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String readLineString = null;
		while ((readLineString = bufferedReader.readLine()) != null) {
			fileContentIterator.iteratorLine(readLineString);
		}
	}
	
	abstract public class FileContentIterator {
		abstract public void iteratorLine(String lineString);
	}
}
