package result;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import model.Sentence;
import model.SummarizationResult;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import constants.SummarizationConstants;
import controller.MySentenceTokenizer;
import controller.SummarizationUtil;

public class RougeResultParser implements ResultParser {

	@Override
	public void parseResult(ArrayList<SummarizationResult> results,
			boolean showStatus) {
		
		this.removeSettings();
		
		int index = 0;
		for (SummarizationResult summarizationResult : results) {
			if (showStatus) {
				System.out.println("- " + summarizationResult.passageName + " | compress:" + SummarizationConstants.compressRate*100 + "%");
			}
			
			StringBuffer resultAbstractBuffer = new StringBuffer();
			for (Sentence sentence : summarizationResult.sentences) {
				if (showStatus) {
					System.out.println(sentence.weight + ":" + sentence.content);
				}
				resultAbstractBuffer.append(sentence.content);
//				resultAbstractBuffer.append(sentence.punctuation);
				resultAbstractBuffer.append("\n");
				if (resultAbstractBuffer.length()*1.0/summarizationResult.originalLength > SummarizationConstants.compressRate) {
					break;//压缩率
				}
			}
			
			String resultString = resultAbstractBuffer.toString();
			String originalString = this.insertReturnBetweenEachSentence(summarizationResult.originalAbstract);
			String candidateFilePath = SummarizationConstants.SUM_CANDIDATES_PATH + File.separator + String.valueOf(index) + ".spl";
			this.saveSummaries(resultString, candidateFilePath);
			String modelFilePath = SummarizationConstants.SUM_MODELS_PATH + File.separator + String.valueOf(index) + ".spl";
			this.saveSummaries(originalString, modelFilePath);
			this.appendSettings(index);
			
			index ++;
		}	
	}
	
	private String insertReturnBetweenEachSentence(String originalString) {
		
		StringBuffer resultString = new StringBuffer();
		@SuppressWarnings("resource")
		MySentenceTokenizer sentenceTokenizer = new MySentenceTokenizer(new StringReader(originalString));//不去逗号
		try {
			while (sentenceTokenizer.incrementToken()) {
				CharTermAttribute attribute = sentenceTokenizer.getAttribute(CharTermAttribute.class);
				resultString.append(attribute.toString().trim());
				resultString.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultString.toString();
	}
	
	private void saveSummaries(String resultString, String fileName) {
		FileWriter fileWriter = null;
		try {
			File settingsFile = SummarizationUtil.fileFromPath(fileName, true);
			fileWriter = SummarizationUtil.fileWriteFormFile(settingsFile, false);
			fileWriter.write(resultString);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void appendSettings(int index) {
		try {
			File settingsFile = SummarizationUtil.fileFromPath(SummarizationConstants.SUM_SETTINGS_PATH, true);
			FileWriter fileWriter = SummarizationUtil.fileWriteFormFile(settingsFile, true);
			
			String content = "candidates\\" + index + ".spl models\\" + index + ".spl\n";
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void removeSettings() {
		try {
			File settingsFile = SummarizationUtil.fileFromPath(SummarizationConstants.SUM_SETTINGS_PATH, true);
			FileWriter fileWriter = SummarizationUtil.fileWriteFormFile(settingsFile, false);
			fileWriter.write("");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
