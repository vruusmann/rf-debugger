package org.jpmml.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.dmg.pmml.FeatureType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.Node;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.ScoreDistribution;
import org.dmg.pmml.Segment;
import org.dmg.pmml.Segmentation;
import org.dmg.pmml.VisitorAction;
import org.jpmml.evaluator.BatchUtil;
import org.jpmml.evaluator.CsvUtil;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.MiningModelEvaluator;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.xml.sax.InputSource;

public class RfDebugger {

	static
	public void main(String[] args) throws Exception {

		if(args.length != 2){
			System.err.println("Usage: " + RfDebugger.class.getName() + " <RF model PMML file> <Input CSV file>");

			System.exit(-1);
		}

		PMML pmml = loadPMML(new File(args[0]));

		// A random forest model is represented using the MiningModel element
		MiningModel miningModel = (MiningModel)Iterables.getOnlyElement(pmml.getModels());

		// Get (or create) its Output element
		Output output = miningModel.getOutput();
		if(output == null){
			output = new Output();
			miningModel.setOutput(output);
		}

		// Add a new special-purpose OutputField element for every member decision tree that will retrieve the identifier of the winning Node element.
		Segmentation segmentation = miningModel.getSegmentation();
		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			OutputField outputField = new OutputField(new FieldName("segment_" + segment.getId() + "_nodeId"))
				.setSegmentId(segment.getId())
				.setFeature(FeatureType.ENTITY_ID);

			output.addOutputFields(outputField);
		}

		MiningModelEvaluator evaluator = new MiningModelEvaluator(pmml);

		List<Map<FieldName, String>> records = loadCSV(new File(args[1]));
		for(int i = 0; i < records.size(); i++){
			Map<FieldName, String> record = records.get(i);

			Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

			List<FieldName> activeFields = evaluator.getActiveFields();
			for(FieldName activeField : activeFields){
				FieldValue activeValue = evaluator.prepare(activeField, record.get(activeField));

				arguments.put(activeField, activeValue);
			}

			//System.out.println(arguments);

			Map<FieldName, ?> result = evaluator.evaluate(arguments);

			// Get the predicted value
			// For classification models this is the winning class as java.lang.String value
			String resultLabel = (String)EvaluatorUtil.decode(result.get(evaluator.getTargetField()));

			// Print the summary for every member decision tree
			for(Segment segment : segments){
				String nodeId = (String)result.get(new FieldName("segment_" + segment.getId() + "_nodeId")); // The "entityId" output feature is a java.lang.String value

				// Resolve the identifier to actual Node element
				NodeResolver nodeResolver = new NodeResolver(nodeId);
				nodeResolver.applyTo(segment);

				Node node = nodeResolver.getResult();
				if(node == null){
					throw new IllegalStateException();
				}

				Object nodeDebugInfo;

				// If the Node represents a true probability distribution (eg. Scikit-Learn RF models), then it is encoded as a list of ScoreDistribution elements
				if(node.hasScoreDistributions()){
					nodeDebugInfo = getProbability(node, resultLabel);
				} else

				// Otherwise, the list of ScoreDistribution elements is empty, and the predicted value is given as the value of the score attribute
				{
					nodeDebugInfo = node.getScore();
				}

				// Print the result: record Id, class, node Id, debug information for the given node Id
				System.out.println(String.valueOf(i + 1) + "," + resultLabel + "," + nodeId + "," + nodeDebugInfo);
			}
		}
	}

	static
	private Double getProbability(Node node, String value){

		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();
		for(ScoreDistribution scoreDistribution : scoreDistributions){

			if((scoreDistribution.getValue()).equals(value)){
				Double probability = scoreDistribution.getProbability();

				// Use the pre-calculated probability if one is available
				if(probability != null){
					return probability;
				}

				// Otherwise, calculate one based on record counts
				return (scoreDistribution.getRecordCount() / node.getRecordCount());
			}
		}

		throw new IllegalStateException();
	}

	static
	private PMML loadPMML(File file) throws Exception {
		InputStream is = new FileInputStream(file);

		try {
			Source source = ImportFilter.apply(new InputSource(is));

			return JAXBUtil.unmarshalPMML(source);
		} finally {
			is.close();
		}
	}

	static
	private List<Map<FieldName, String>> loadCSV(File file) throws Exception {
		InputStream is = new FileInputStream(file);

		try {
			CsvUtil.Table table = CsvUtil.readTable(is, ",");

			Function<String, String> function = new Function<String, String>(){

				@Override
				public String apply(String string){

					if("N/A".equals(string)){
						return null;
					}

					return string;
				}
			};

			return BatchUtil.parseRecords(table, function);
		} finally {
			is.close();
		}
	}

	static
	private class NodeResolver extends AbstractVisitor {

		private String id = null;

		private Node node = null;


		private NodeResolver(String id){
			setId(id);
		}

		@Override
		public VisitorAction visit(Node node){

			// Find a Node element whose id attribute matches the search key
			if((node.getId()).equals(getId())){
				this.node = node;
			}

			return super.visit(node);
		}

		public String getId(){
			return this.id;
		}

		private void setId(String id){
			this.id = id;
		}

		public Node getResult(){
			return this.node;
		}
	}
}