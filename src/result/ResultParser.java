package result;

import java.util.ArrayList;

import model.SummarizationResult;

public interface ResultParser {
	public void parseResult(ArrayList<SummarizationResult> results, boolean showStatus);
}
