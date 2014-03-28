package constants;

import java.io.File;

public class SummarizationConstants {
	public static final String TARGET_FOLDER_PATH =  "." + File.separator + "data" + File.separator + "target" ;
	public static final String CORPUS_FOLDER_PATH =  "." + File.separator + "data" + File.separator + "corpus" ;
	public static final String CHINESE_STOPWORD_PATH =  "." + File.separator + "data" + File.separator + "stopwords.txt" ;
	public static final String SUM_MODELS_PATH =  "." + File.separator + "data" + File.separator + "rouge" + File.separator + "models" ;
	public static final String SUM_CANDIDATES_PATH =  "." + File.separator + "data" + File.separator + "rouge" + File.separator + "candidates" ;
	public static final String SUM_SETTINGS_PATH =  "." + File.separator + "data" + File.separator + "rouge" + File.separator + "settings.lst" ;
	public static final String ENGLISH_STOPWORD_PATH =  "." + File.separator + "data" + File.separator + "englishStopwords.txt" ;
	
	public static final int MinimalFeatureCount = 5;
	public static final double compressRate = 0.05;
	
	public static final double pageRankPrecision = 0.000001;
	public static final double pageRankMaxIterationCount = 40;
	public static final double pageRankResistance = 0.5;
	
	public static final int passageFeatureLimit = 50;
}
