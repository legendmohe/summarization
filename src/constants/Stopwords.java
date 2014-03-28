package constants;

import java.io.File;
import java.io.IOException;

import controller.SummarizationUtil;

public class Stopwords {
	
	public static String[] getStopwords(String filePath) throws IOException {
		File file = SummarizationUtil.fileFromPath(filePath, false);
		return SummarizationUtil.contentOfFileContent(file, true, "UTF-8").split("\n");
	}
	
}
