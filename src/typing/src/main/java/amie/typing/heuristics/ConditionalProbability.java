package amie.typing.heuristics;

import java.util.List;

import amie.data.KB;
import amie.data.U;

import javatools.datatypes.ByteString;


/**
 * For a class C and a superclass C' of C,
 * Let r = conf(C => clause) / conf(C' => clause)
 * Returns: log(r) / log(referenceRatio) capped between 0 and 1.
 * 
 * @author jlajus
 */

public class ConditionalProbability extends TypingHeuristic {

	public static double referenceRatio = 1000;
	
	public ConditionalProbability(KB kb) {
		super(kb);
		name = "Conditional";
		// TODO Auto-generated constructor stub
	}

	@Override 
	public double evaluate(ByteString type, List<ByteString[]> clause,
			ByteString variable) {
		double t, stdConf, superClassMaxConf = 0;
		for (ByteString c : amie.data.Schema.getSuperTypes(db, type)) {
			if ((t = getStandardConfidence(c, clause, variable)) > superClassMaxConf) {
				superClassMaxConf = t;
			}
		}
		stdConf = getStandardConfidence(type, clause, variable);
		if (superClassMaxConf > stdConf || superClassMaxConf == 0)
			return 0;
		if (stdConf / superClassMaxConf > referenceRatio)
			return 1;
		// Should be continuous
		return Math.log(stdConf / superClassMaxConf) / Math.log(referenceRatio);
	}
}
