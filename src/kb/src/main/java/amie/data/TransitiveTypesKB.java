/**
 * 
 */
package amie.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;

/**
 * Class Transitive Types KB
 * 
 * Extends KB to simulate transitive types from KB schema.
 * 
 * @author jlajus
 * 
 */
public class TransitiveTypesKB extends KB {
	
	public static final String TRANSITIVETYPEstr = "transitiveType";
	
	public static final ByteString TRANSITIVETYPEbs = ByteString.of(TRANSITIVETYPEstr);
	
	@Override
	protected boolean contains(ByteString... fact) {
		if (fact[1].equals(TRANSITIVETYPEbs)) {
			for (ByteString type : get(this.subject2relation2object, fact[0], Schema.typeRelationBS)) {
				if (Schema.isTransitiveSuperType(this, fact[2], type)) {
					return true;
				}
			}
			return false;
		} else {
			return super.contains(fact);
		}
	}

	@Override
	protected long countTwoVariables(ByteString... triple) {
		if (triple[1].equals(TRANSITIVETYPEbs)) {
			Map<ByteString, IntHashMap<ByteString>> resultTwoVars = 
					resultsTwoVariables(0, 2, triple);
			long count = 0;
			for (ByteString subject : resultTwoVars.keySet()) {
				count += resultTwoVars.get(subject).size();
			}
			return count;
		} else {
			return super.countTwoVariables(triple);
		}
	}
	
	@Override
	public IntHashMap<ByteString> resultsOneVariable(ByteString... triple) {
		if (triple[1].equals(TRANSITIVETYPEbs)) {
			if (isVariable(triple[0])) {
				/*
				 * Return all the entities in subclasses of triple[2]
				 */
				IntHashMap<ByteString> result = new IntHashMap<>();
				for (ByteString subtype : Schema.getAllSubTypes(this, triple[2])) {
					result.addAll(get(relation2object2subject, Schema.typeRelationBS, subtype));
				}
				return result;
			} else { // assert(isVariable(triple[2]));
				/*
				 * Return all the super-classes of an entity
				 */
				return Schema.getAllTypesForEntity(this, triple[0]);
			}
		}
		else {
			return super.resultsOneVariable(triple);
		}
	}
	
	@Override
	public Map<ByteString, IntHashMap<ByteString>> resultsTwoVariables(
			int pos1, int pos2, ByteString[] triple) {
		if (triple[1].equals(TRANSITIVETYPEbs)) {
			Map<ByteString, IntHashMap<ByteString>> result = new LinkedHashMap<>();
			switch(pos1) {
			case 0:
				/*
				 * Return a map from all entities to all super-classes
				 */
				for (ByteString entity : get(relation2subject2object, Schema.typeRelationBS).keySet()) {
					result.put(entity, Schema.getAllTypesForEntity(this, entity));
				}
				return result;
			case 2:
				/*
				 * Return a map from all types to all entities of sub-classes
				 */
				for (ByteString type : get(relation2object2subject, Schema.typeRelationBS).keySet()) {
					result.put(type, resultsOneVariable(triple(ByteString.of("?s"), TRANSITIVETYPEbs, type)));
				}
				return result;
			case 1:
			default:
				throw new IllegalArgumentException("The argument at position " + pos1 
						+ " should be a variable");
			}
		} else {
			return super.resultsTwoVariables(pos1, pos2, triple);
		}
	}
	
	public static void main(String[] args) {
		TransitiveTypesKB kb = new TransitiveTypesKB();
		List<File> files = new ArrayList<>();
		files.add(new File("/run/media/jo/442C35F22C35DF9A/yago/yagoTaxonomy.tsv"));
		files.add(new File("/run/media/jo/442C35F22C35DF9A/yago/yagoTypesWordnet.tsv"));
		try {
			kb.load(files);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (kb.contains(ByteString.of("<John_Ford_(musician)>"), Schema.typeRelationBS, ByteString.of("<wordnet_person_100007846>"))) {
			System.err.println("Check failed: contains rdf:type not valid.");
		} else {
			System.out.println("Check passed: contains rdf:type.");
		}
		if (!kb.contains(ByteString.of("<John_Ford_(musician)>"), TRANSITIVETYPEbs, ByteString.of("<wordnet_person_100007846>"))) {
			System.err.println("Check failed: contains transitiveType not valid.");
		} else {
			System.out.println("Check passed: contains transitiveType.");
		}
		System.out.println(String.valueOf(kb.countOneVariable(ByteString.of("?s"), Schema.typeRelationBS, ByteString.of("<wordnet_person_100007846>"))) + " persons");
		System.out.println(String.valueOf(kb.countOneVariable(ByteString.of("?s"), TRANSITIVETYPEbs, ByteString.of("<wordnet_person_100007846>"))) + " transitive persons");
		 
		System.out.println(kb.countTwoVariables(ByteString.of("?x"), TRANSITIVETYPEbs, ByteString.of("?y")));
	}	
}
