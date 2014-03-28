package result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import model.Sentence;
import model.SummarizationResult;
import constants.SummarizationConstants;
import controller.SummarizationPrepressor;

public class ResultParserImpl implements ResultParser {
	public void parseResult(ArrayList<SummarizationResult> results, boolean showStatus) {
		
		double avgP = 0.0;
		double avgC = 0.0;
		double avgR = 0.0;
		for (SummarizationResult summarizationResult : results) {
			System.out.println("- " + summarizationResult.passageName + " | compress:" + SummarizationConstants.compressRate);
			
			int totalLength = 0;
			ArrayList<Sentence> sentenceArrayList = new ArrayList<Sentence>();
			for (Sentence sentence : summarizationResult.sentences) {
				sentenceArrayList.add(sentence);
				totalLength += sentence.content.length();
				if (totalLength*1.0/summarizationResult.originalLength > SummarizationConstants.compressRate) {
					break;//压缩率
				}
			}
			
			Collections.sort(sentenceArrayList, new Comparator<Sentence>() {

				@Override
				public int compare(Sentence o1, Sentence o2) {
					if (o1.id > o2.id) {
						return 1;
					}else if(o1.id < o2.id){
						return -1;
					}else {
						return 0;
					}
				}
			});
			
			StringBuffer resultAbstractBuffer = new StringBuffer();
			for (Sentence sentence : sentenceArrayList) {
				System.out.println(sentence.weight + ":" + sentence.content);
				resultAbstractBuffer.append(sentence.content);
			}
			if (showStatus) {
				//生成摘要关键词
				HashSet<String> wordHashSet = SummarizationPrepressor.wordSetOfStrings(resultAbstractBuffer.toString());
				//生成参考摘要关键词
				HashSet<String> originalWordHashSet = SummarizationPrepressor.wordSetOfStrings(summarizationResult.originalAbstract);
				
				HashSet<String> retianSet = new HashSet<String>(wordHashSet);
				retianSet.retainAll(originalWordHashSet);
				double P = 1.0*retianSet.size()/wordHashSet.size();
				double C = 1.0*retianSet.size()/originalWordHashSet.size();
				double R = 1 - P;
				double F = P*C*2/(P + C);
				System.out.print(" P:" + P);
				System.out.print(" C:" + C);
				System.out.print(" R:" + R);
				System.out.println(" F:" + F);
				
				avgP += P;
				avgC += C;
				avgR += R;
			}
		}
		if (showStatus) {
			avgP = avgP/results.size();
			avgC = avgC/results.size();
			avgR = avgR/results.size();
			double marcoF = avgP*avgC*2/(avgP + avgC);
			System.out.println("--------------------------------------");
			System.out.print(" marcoP:" + avgP);
			System.out.print(" marcoC:" + avgC);
			System.out.print(" marcoR:" + avgR);
			System.out.println(" marcoF:" + marcoF);
		}
	}
}
