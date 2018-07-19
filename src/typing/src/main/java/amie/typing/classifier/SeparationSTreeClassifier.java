/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amie.typing.classifier;

import amie.data.KB;
import amie.data.SimpleTypingKB;
import amie.typing.heuristics.TypingHeuristic;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;

/**
 *
 * @author jlajus
 */
public class SeparationSTreeClassifier extends SeparationTreeClassifier {

    public SeparationSTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS) {
        super(source, cS, cIS);
    }

    public SeparationSTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile) {
        super(source, typeCountFile, typeIntersectionCountFile);
    }

    public SeparationSTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS, boolean supportForTarget) {
        super(source, cS, cIS, supportForTarget);
    }

    public SeparationSTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile, boolean supportForTarget) {
        super(source, typeCountFile, typeIntersectionCountFile, supportForTarget);
    }
    
    public void computeStatistics(List<ByteString[]> query, ByteString variable, int classSizeThreshold) {
        Set<ByteString> relevantClasses = index.keySet();
        ByteString relation = (query.get(0)[0].equals(variable)) ? query.get(0)[1] : ByteString.of(query.get(0)[1].toString() + "-1");

        for (ByteString class1 : relevantClasses) {
            int c1size = classSize.get(class1);
            Set<ByteString> c1phi = null;
            
            if (localdb != null) {
                c1phi = new HashSet<>(localdb.relations.get(relation));
                c1phi.retainAll(localdb.classes.get(class1));
                if (c1phi.size() == c1size) {
                    continue;
                }
            }

            List<ByteString[]> clause = TypingHeuristic.typeL(class1, variable);
            clause.addAll(query);
            Set<ByteString> targetClasses = (supportForTarget) ? relevantClasses : classIntersectionSize.get(class1);

            for (ByteString class2 : targetClasses) {
                assert (clause.size() == query.size() + 1);
                if (class1 == class2) {
                    continue;
                }
                if (classSize.get(class2) < classSizeThreshold) {
                    // Ensure the symmetry of the output.
                    continue;
                }
                if (!classIntersectionSize.containsKey(class1) || !classIntersectionSize.get(class1).contains(class2)) {
                    continue;
                }

                int c1c2size = classIntersectionSize.get(class1).get(class2);

                if (c1c2size < classSizeThreshold) {
                    continue;
                } else if (c1size - c1c2size < classSizeThreshold) {
                    continue;
                } else {
                    Double s = (localdb == null) ? getStandardConfidenceWithThreshold(TypingHeuristic.typeL(class2, variable), clause, variable, -1, true) : 1.0 * SimpleTypingKB.countIntersection(c1phi, localdb.classes.get(class2)) / c1phi.size();
                    Double c1c2edge;
                    c1c2edge = Math.log((double) c1c2size / (c1size - c1c2size) * (1.0 - s) / s);
                    if (c1c2edge < 0) {
                        index.get(class1).separationScore = Math.min(index.get(class1).separationScore, c1c2edge);
                    } else {
                        if (index.containsKey(class2)) {
                            index.get(class2).separationScore = Math.min(index.get(class2).separationScore, -c1c2edge);
                        }
                        index.get(class1).separationScore = Math.min(index.get(class1).separationScore, -c1c2edge);
                    }
                }
            }
        }
    }
}
