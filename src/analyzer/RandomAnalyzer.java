package analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import model.Passage;
import model.Sentence;
import model.SummarizationResult;

public class RandomAnalyzer extends SummarizationAnalyzer {
	
	private static Random randomer = new Random();

	@Override
	public void analyze(ArrayList<Passage> passages,
			ArrayList<SummarizationResult> results) {
		for (Passage passage : passages) {
			
			//随机生成结果
			SummarizationResult passageResult = new SummarizationResult();
			passageResult.passageName = passage.getName();
			for (int i = 0; i < passage.getSentences().size(); i++) {
				passage.getSentences().get(i).weight = Math.abs(randomer.nextDouble()%10);
			}
			passageResult.sentences.addAll(passage.getSentences());
			Collections.sort(passageResult.sentences, new Comparator<Sentence>() {

				@Override
				public int compare(Sentence o1, Sentence o2) {
					if (o2.weight < o1.weight) {
						return -1;
					}else if(o2.weight > o1.weight){
						return 1;
					}
					return 0;
				}
			});
			
			results.add(passageResult);
		}
	}

}
