package amie.typing.testing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import amie.data.KB;
import amie.data.U;

import javatools.datatypes.ByteString;
import javatools.datatypes.FrequencyVector;
import javatools.filehandlers.FileLines;

public class FrequencyTester {

	private Map<ByteString, Set<ByteString>> attribute2classes = null;
	private Map<String, Map<ByteString, Map<ByteString, Double>>> heuristic2attribute2class2score;

	public void loadResults(File f) throws IOException {
		heuristic2attribute2class2score = new HashMap<>();
		for (String line : new FileLines(f, "UTF-8", null)) {
			String[] split = line.trim().split("\t");
			if (split.length == 4) {
				String heuristic = split[0].trim();
				Map<ByteString, Map<ByteString, Double>> attribute2class2score = heuristic2attribute2class2score.get(heuristic);
				if (attribute2class2score == null) {
					heuristic2attribute2class2score.put(heuristic, attribute2class2score = new HashMap<>());
				}
				ByteString attribute = ByteString.of(split[2].trim());
				if (attribute2classes != null && !attribute2classes.containsKey(attribute)) continue;
				Map<ByteString, Double> class2score = attribute2class2score.get(attribute);
				if (class2score == null) {
					attribute2class2score.put(attribute, class2score = new HashMap<>());
				}
				class2score.put(ByteString.of(split[1].trim()), Double.valueOf(split[3].trim()));
			}
		}
	}
	
	public void loadGoldStandard(KB taxo, File f) throws IOException {
		attribute2classes = new HashMap<>();
		for (String line : new FileLines(f, "UTF-8", null)) {
			String[] split = line.trim().split("\t");
			if (split.length == 2) {
				ByteString attribute = ByteString.of(split[0].trim());
				Set<ByteString> classes = attribute2classes.get(attribute);
				if (classes == null) {
					classes = new HashSet<ByteString>();
					attribute2classes.put(attribute, classes);
				}
				ByteString dclass = ByteString.of("<http://dbpedia.org/ontology/" + split[1].trim() + ">");
				if(dclass != ByteString.of("<http://dbpedia.org/ontology/None>")) {
					classes.add(dclass);
					classes.addAll(amie.data.Schema.getAllSubTypes(taxo, dclass));
				}
			}
		}
	}
	
	public void printGoldStandard() {
		for (ByteString attribute : attribute2classes.keySet()) {
			for (ByteString classes : attribute2classes.get(attribute)) {
				System.out.println(attribute.toString() + "\t" + classes.toString());
			}
		}
	}
	
	public void test(String heuristic, ByteString attribute) {
		FrequencyVector<ByteString, Double> resultFV = new FrequencyVector<>(heuristic2attribute2class2score.get(heuristic).get(attribute));
		double precision = resultFV.weightedPrecisionWithRespectTo(attribute2classes.get(attribute));
		double recall = resultFV.recallWithRespectTo(attribute2classes.get(attribute));
		System.out.println(heuristic + "\t" + attribute.toString() + "\t" 
							+ String.valueOf(precision) + "\t" + String.valueOf(recall) 
							+ ((attribute2classes.get(attribute).isEmpty())?"\tE":""));
	}
	
	public void testAll() {
		for (String heuristic : heuristic2attribute2class2score.keySet()) {
			for (ByteString attribute : attribute2classes.keySet()) {
				if (!attribute2classes.get(attribute).isEmpty()) test(heuristic, attribute);
			}
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length < 3) {
			System.err.println("USAGE: FrequencyTester taxonomy.ttl goldstandard.tsv results.tsv");
			System.exit(1);
		}
		
		FrequencyTester ft = new FrequencyTester();
		KB taxo = new KB();
		taxo.setDelimiter(" ");
		taxo.load(new File(args[0]));
		ft.loadGoldStandard(taxo, new File(args[1]));
		ft.loadResults(new File(args[2]));
		//ft.test("StdConf", ByteString.of("<http://dbpedia.org/ontology/architect>-1"));
		ft.testAll();
		//ft.printGoldStandard();
	}

}
