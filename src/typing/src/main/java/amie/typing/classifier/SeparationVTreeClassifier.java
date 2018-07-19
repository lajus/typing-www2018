/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amie.typing.classifier;

import amie.data.KB;
import amie.data.SimpleTypingKB;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;
import org.apache.commons.math3.distribution.HypergeometricDistribution;

/**
 *
 * @author jlajus
 */
public class SeparationVTreeClassifier extends SeparationPTreeClassifier {

    public SeparationVTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS) {
        super(source, cS, cIS);
    }

    public SeparationVTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile) {
        super(source, typeCountFile, typeIntersectionCountFile);
    }

    public SeparationVTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS, boolean supportForTarget) {
        super(source, cS, cIS, supportForTarget);
    }

    public SeparationVTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile, boolean supportForTarget) {
        super(source, typeCountFile, typeIntersectionCountFile, supportForTarget);
    }
    
    @Override
    public void computeStatistics(List<ByteString[]> query, ByteString variable, int classSizeThreshold) {
        Set<ByteString> relevantClasses = index.keySet();
        ByteString relation = (query.get(0)[0].equals(variable)) ? query.get(0)[1] : ByteString.of(query.get(0)[1].toString() + "-1");

        for (ByteString class1 : relevantClasses) {
            int c1size = classSize.get(class1);
            Set<ByteString> c1phi = new HashSet<>(localdb.relations.get(relation));
            c1phi.retainAll(localdb.classes.get(class1));
            if (c1phi.size() == c1size) {
                continue;
            }

            Set<ByteString> targetClasses = (supportForTarget) ? relevantClasses : classIntersectionSize.get(class1);

            for (ByteString class2 : targetClasses) {
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
                    HypergeometricDistribution hgd = new HypergeometricDistribution(c1size, c1phi.size(), c1c2size);
                    int c1c2phi = (int) SimpleTypingKB.countIntersection(c1phi, localdb.classes.get(class2));
                    double c1c2phiExpected = hgd.getNumericalMean();
                    
                    double sp = hgd.getNumericalVariance() / Math.pow(c1c2phi - c1c2phiExpected, 2.);
                    
                    if (c1c2phi >= c1c2phiExpected) {
                        index.get(class1).separationScore = Math.min(index.get(class1).separationScore, Math.log(sp));
                    } else if (index.containsKey(class2)) {
                        index.get(class2).separationScore = Math.min(index.get(class2).separationScore, Math.log(sp));
                    }
                }
            }
        }
    }
}
