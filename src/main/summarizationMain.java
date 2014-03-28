package main;
import java.util.ArrayList;
import java.util.Date;

import model.Sentence;
import model.SummarizationResult;
import result.ResultParser;
import result.ResultParserImpl;
import analyzer.PageRankAnalyzer;
import analyzer.SummarizationAnalyzer;
import constants.SummarizationConstants;
import controller.SummarizationPrepressor;
import featureselector.FeatureSelector;
import featureselector.ParagraphFeatureSelector;
import featureselector.SentenceFeatureSelector;
import featureweightor.CHIFeatureWeighter;


public class summarizationMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		SummarizationPrepressor.setStemming(true);
		
//		FeatureWeighter weightor = new CCFeatureWeighter();
//		SummarizationAnalyzer analyzer = new CCAnalyzer(weightor);
		FeatureSelector featureSelector = new ParagraphFeatureSelector(new CHIFeatureWeighter());
		SummarizationAnalyzer analyzer = new PageRankAnalyzer(SummarizationConstants.pageRankPrecision, null);
//		SummarizationAnalyzer analyzer = new RandomAnalyzer();
//		SummarizationAnalyzer analyzer = new FilterAnalyzer(featureSelector);
		Date startDate = new Date();
		ArrayList<SummarizationResult> result = analyzer.analyze(SummarizationConstants.CORPUS_FOLDER_PATH);
		Date endDate = new Date();
		System.out.println("分类时间：" + (endDate.getTime() - startDate.getTime()));
		
		ResultParser resultParser = new ResultParserImpl();
		resultParser.parseResult(result, true);
		
	}

}
