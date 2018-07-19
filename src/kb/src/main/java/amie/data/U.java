package amie.data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;
import javatools.datatypes.Triple;

/**
 * Set of commonly used functions.
 * 
 * @author lgalarra
 *
 */
public class U {

	
	/**
	 * It performs a KB coalesce between 2 KBs consisting of all the facts in both ontologies
	 * for the intersection of all entities in the first KB with the subjects of the second KB.
	 * @param source1
	 * @param source2
	 * @param withObjs If true, the coalesce is done between all the entities in the first KB
	 * and all the entities in the second KB.
	 */
	public static void coalesce(KB source1, 
			KB source2, boolean withObjs) {
		Set<ByteString> sourceEntities = new LinkedHashSet<>();
		sourceEntities.addAll(source1.subjectSize);
		sourceEntities.addAll(source1.objectSize);
		for(ByteString entity: sourceEntities){
			//Print all facts of the source ontology
			Map<ByteString, IntHashMap<ByteString>> tail1 = source1.subject2relation2object.get(entity);
			Map<ByteString, IntHashMap<ByteString>> tail2 = source2.subject2relation2object.get(entity);
			if(tail2 == null)
				continue;
						
			for(ByteString predicate: tail1.keySet()){
				for(ByteString object: tail1.get(predicate)){
					System.out.println(entity + "\t" + predicate + "\t" + object);
				}
			}
			//Print all facts in the target ontology
			for(ByteString predicate: tail2.keySet()){
				for(ByteString object: tail2.get(predicate)){
					System.out.println(entity + "\t" + predicate + "\t" + object);
				}
			}
		}
		
		if(withObjs){
			for(ByteString entity: source2.objectSize){
				if(sourceEntities.contains(entity)) continue;
				
				Map<ByteString, IntHashMap<ByteString>> tail2 = source2.subject2relation2object.get(entity);
				if(tail2 == null) continue;
				
				//Print all facts in the target ontology
				for(ByteString predicate: tail2.keySet()){
					for(ByteString object: tail2.get(predicate)){
						System.out.println(entity + "\t" + predicate + "\t" + object);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param source
	 */
	public static void printOverlapTable(KB source) {
		//for each pair of relations, print the overlap table
		System.out.println("Relation1\tRelation2\tRelation1-subjects"
				+ "\tRelation1-objects\tRelation2-subjects\tRelation2-objects"
				+ "\tSubject-Subject\tSubject-Object\tObject-Subject\tObject-Object");
		for(ByteString r1: source.relationSize){
			Set<ByteString> subjects1 = source.relation2subject2object.get(r1).keySet();
			Set<ByteString> objects1 = source.relation2object2subject.get(r1).keySet();
			int nSubjectsr1 = subjects1.size();
			int nObjectsr1 = objects1.size();
			for(ByteString r2: source.relationSize){
				if(r1.equals(r2))
					continue;				
				System.out.print(r1 + "\t");
				System.out.print(r2 + "\t");
				Set<ByteString> subjects2 = source.relation2subject2object.get(r2).keySet();
				Set<ByteString> objects2 = source.relation2object2subject.get(r2).keySet();
				int nSubjectr2 = subjects2.size();
				int nObjectsr2 = objects2.size();
				System.out.print(nSubjectsr1 + "\t" + nObjectsr1 + "\t" + nSubjectr2 + "\t" + nObjectsr2 + "\t");
				System.out.print(computeOverlap(subjects1, subjects2) + "\t");
				System.out.print(computeOverlap(subjects1, objects2) + "\t");
				System.out.print(computeOverlap(subjects2, objects1) + "\t");
				System.out.println(computeOverlap(objects1, objects2));
			}
		}		
	}
		
	public static KB loadFiles(String args[]) throws IOException {
            return loadFiles(args, "\t");
        }
	/**
	 * Returns a KB with the content of all the files referenced in the string array.
	 * @param args
         * @param delimiter
	 * @return
	 * @throws IOException
	 */
	public static KB loadFiles(String args[], String delimiter) throws IOException {
		// Load the data
		KB kb = new KB();
                kb.setDelimiter(delimiter);
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < args.length; ++i) {
			files.add(new File(args[i]));
		}
		kb.load(files);
		return kb;
	}
	
	/**
	 * Returns a KB with the content of all the files referenced in the object array.
	 * Each element of the array is converted to a string object
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public static KB loadFiles(Object args[]) throws IOException {
		// Load the data
		KB kb = new KB();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < args.length; ++i) {
			files.add(new File((String)args[i]));
		}
		kb.load(files);
		return kb;
	}
	
	
	/**
	 * Returns a KB with the content of all the files referenced in the string array
	 * starting from a given position.
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public static KB loadFiles(String args[], int fromIndex) throws IOException {
		if (fromIndex >= args.length)
			throw new IllegalArgumentException("Index " + fromIndex + 
					" equal or bigger than size of the array.");
		// Load the data
		KB kb = new KB();
		List<File> files = new ArrayList<File>();
		for (int i = fromIndex; i < args.length; ++i) {
			files.add(new File(args[i]));
		}
		kb.load(files);
		return kb;
	}
	
	/**
	 * Returns a KB with the content of all the files referenced in the string array.
	 * @param args
	 * @return
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static KB loadFiles(String args[], Class kbSubclass) 
			throws IOException, InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Load the data
		KB kb = (KB) kbSubclass.getConstructor().newInstance();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < args.length; ++i) {
			files.add(new File(args[i]));
		}
		kb.load(files);
		return kb;
	}
	
	/**
	 * Returns a KB with the content of all the files referenced in the subarray starting
	 * at the given index of the input array 'args'.
	 * @param args
	 * @return
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static KB loadFiles(String args[], int fromIndex, Class kbSubclass) 
			throws IOException, InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Load the data
		KB kb = (KB) kbSubclass.getConstructor().newInstance();
		List<File> files = new ArrayList<File>();
		for (int i = fromIndex; i < args.length; ++i) {
			files.add(new File(args[i]));
		}
		kb.load(files);
		return kb;
	}

	/**
	 * 
	 * @param subjects1
	 * @param subjects2
	 * @return
	 */
	private static int computeOverlap(Set<ByteString> subjects1,
			Set<ByteString> subjects2) {
		int overlap = 0; 
		for(ByteString entity1 : subjects1){
			if(subjects2.contains(entity1))
				++overlap;
		}
		
		return overlap;
	}
	


	
	/**
	 * It returns the number of facts where the given entity participates as
	 * a subject or object.
	 * @param kb
	 * @param entity
	 * @return
	 */
	public static int numberOfFacts(KB kb, ByteString entity) {
		ByteString[] querySubject = KB.triple(entity, ByteString.of("?r"), ByteString.of("?o")); 
		ByteString[] queryObject = KB.triple(ByteString.of("?s"), ByteString.of("?r"), entity); 
		return (int)kb.count(querySubject) + (int)kb.count(queryObject);
	}
	
	/**
	 * It returns the number of facts where the given entity participates as
	 * a subject or object.
	 * @param kb
	 * @param entity
	 * @param omittedRelations These relations are not counted as facts.
	 * @return
	 */
	public static int numberOfFacts(KB kb, ByteString entity, Collection<ByteString> omittedRelations) {
		ByteString[] querySubject = KB.triple(entity, ByteString.of("?r"), ByteString.of("?o")); 
		ByteString[] queryObject = KB.triple(ByteString.of("?s"), ByteString.of("?r"), entity); 
		Map<ByteString, IntHashMap<ByteString>> relationsSubject = 
				kb.resultsTwoVariables(ByteString.of("?r"), ByteString.of("?o"), querySubject);
		Map<ByteString, IntHashMap<ByteString>> relationsObject = 
				kb.resultsTwoVariables(ByteString.of("?r"), ByteString.of("?s"), queryObject);
		int count1 = 0;
		int count2 = 0;
		for (ByteString relation : relationsSubject.keySet()) {
			if (!omittedRelations.contains(relation))
				count1 += relationsSubject.get(relation).size();
		}
		
		for (ByteString relation : relationsObject.keySet()) {
			if (!omittedRelations.contains(relation))
				count1 += relationsObject.get(relation).size();
		}

		return count1 + count2;
	}
	
	/**
	 * Returns true if the relation is defined as a function.
	 * @return
	 */
	public static boolean isFunction(KB kb, ByteString relation) {
		return kb.contains(relation, ByteString.of("<isFunction>"), ByteString.of("TRUE"));
	}
	
	/**
	 * Returns true if the relation is defined as compulsory for all members 
	 * of its domain (this function assumes relations are always analyzed from 
	 * their most functional side.
	 * @return
	 */
	public static boolean isMandatory(KB kb, ByteString relation) {
		return kb.contains(relation, ByteString.of("<isMandatory>"), ByteString.of("TRUE"));
	}

	/**
	 * It returns all the entities that have 'cardinality' different number of values
	 * for the given relation.
	 * @param kb
	 * @param relation
	 * @param cardinality
	 * @return
	 */
	public static Set<ByteString> getEntitiesWithCardinality(KB kb, ByteString relation, int cardinality) {
		Map<ByteString, IntHashMap<ByteString>> results = null;
		List<ByteString[]> query = KB.triples(KB.triple(ByteString.of("?s"), 
				relation, ByteString.of("?o")));
		if (kb.isFunctional(relation)) {
			results = kb.selectDistinct(ByteString.of("?s"), ByteString.of("?o"), query);
		} else {
			results = kb.selectDistinct(ByteString.of("?o"), ByteString.of("?s"), query);			
		}
		Set<ByteString> entities = new LinkedHashSet<>();
		for (ByteString e : results.keySet()) {
			if (results.get(e).size() == cardinality) {
				entities.add(e);
			}
		}
		return entities;
	}

	/**
	 * Outputs a list of objects separated by tabs in one line.
	 * @param list
	 */
	public static <T> void tsvOutput(List<T> line) {
		for (int i = 0; i < line.size() - 1; ++i) {
			System.out.print(line.get(i) + "\t");
		}	
		System.out.println(line.get(line.size() - 1));
	}
	
	/**
	 * Prints a IntHashMap representing a histogram.
	 * @param histogram
	 */
	public static void printHistogram(IntHashMap<Integer> histogram) {
		for (Integer key : histogram.keys()) {			
			System.out.println(key + "\t" + histogram.get(key));
		}
	}
	
	/**
	 * Prints a histogram as well as the probability that X > Xi
	 * for each Xi in the histogram.
	 * @param histogram
	 */
	public static void printHistogramAndCumulativeDistribution(IntHashMap<Integer> histogram) {
		double total = 1.0;
		double accum = 0.0;
		double sum = histogram.computeSum();
		for (Integer key : histogram.keys()) {
			double prob = histogram.get(key) / sum;
			accum += prob;
			System.out.println(key + "\t" + histogram.get(key) + "\t" + prob + "\t" + (total - accum));
		}
	}
	
	/**
	 * It constructs a histogram based on a multimap.
	 * @param map
	 * @return
	 */
	public static <E, T> IntHashMap<Integer> buildHistogram(Map<T, List<E>> map) {
		IntHashMap<Integer> histogram = new IntHashMap<>();
		for (T key : map.keySet()) {
			histogram.increase(map.get(key).size());
		}
		return histogram;
	}
	
	/**
	 * Converts an array into a triple
	 * @param array
	 * @return
	 */
	public static <T> Triple<T, T, T> toTriple(T[] array) {
		if (array.length < 3) {
			return null;
		} else {
			return new Triple<T, T, T>(array[0], array[1], array[2]);
		}
	}
	
	/**
	 * Converts an array into a triple
	 * @param array
	 * @return
	 */
	public static ByteString[] toArray(Triple<ByteString, ByteString, ByteString> triple) {
		return new ByteString[] { triple.first, triple.second, triple.third};
	}
	
	/**
	 * Performs a deep clone of the given list, i.e., it returns a new list where 
	 * each element has been cloned.
	 * @param collection
	 */
	public static <T> List<T[]> deepClone(List<T[]> collection) {
		List<T[]> newList = new ArrayList<>(collection.size());
		for (T[] t : collection) {
			newList.add(t.clone());
		}
		return newList;
	}
	
}
