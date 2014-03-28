package analyzer;

import java.util.ArrayList;

import model.Passage;
import model.SummarizationResult;
import controller.SummarizationPrepressor;

public abstract class SummarizationAnalyzer {
	
	public ArrayList<SummarizationResult> analyze(String targetPath){
		
		System.out.println("开始进行摘要...");
		
		ArrayList<Passage> passages = SummarizationPrepressor.extractEvaluatePassageFromFolder(targetPath);
		ArrayList<SummarizationResult> resultArrayList = new ArrayList<SummarizationResult>();
		this.analyze(passages, resultArrayList);
		
		for (int i = 0; i < passages.size(); i++) {
			resultArrayList.get(i).originalAbstract = passages.get(i).getOriginalAbstractString();
			resultArrayList.get(i).originalLength = passages.get(i).getLength();
		}
		
		System.out.println("摘要结束");
		
		return resultArrayList;
	}
	
	abstract public void analyze(ArrayList<Passage> passages, ArrayList<SummarizationResult> results);
}
