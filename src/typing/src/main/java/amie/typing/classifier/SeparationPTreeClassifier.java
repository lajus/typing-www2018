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
import jsc.contingencytables.*;
import org.apache.commons.math3.distribution.HypergeometricDistribution;

/**
 *
 * @author jlajus
 */
public class SeparationPTreeClassifier extends SeparationTreeClassifier {
    
    public SimpleTypingKB localdb = null;

    public SeparationPTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS) {
        super(source, cS, cIS);
        if (!(db instanceof SimpleTypingKB)) {
            throw new UnsupportedOperationException("Need simple typing KB");
        }
        localdb = (SimpleTypingKB) db;
    }

    public SeparationPTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile) {
        super(source, typeCountFile, typeIntersectionCountFile);
        if (!(db instanceof SimpleTypingKB)) {
            throw new UnsupportedOperationException("Need simple typing KB");
        }
        localdb = (SimpleTypingKB) db;
    }

    public SeparationPTreeClassifier(KB source, IntHashMap<ByteString> cS, Map<ByteString, IntHashMap<ByteString>> cIS, boolean supportForTarget) {
        super(source, cS, cIS, supportForTarget);
        if (!(db instanceof SimpleTypingKB)) {
            throw new UnsupportedOperationException("Need simple typing KB");
        }
        localdb = (SimpleTypingKB) db;
    }

    public SeparationPTreeClassifier(KB source, File typeCountFile, File typeIntersectionCountFile, boolean supportForTarget) {
        super(source, typeCountFile, typeIntersectionCountFile, supportForTarget);
        if (!(db instanceof SimpleTypingKB)) {
            throw new UnsupportedOperationException("Need simple typing KB");
        }
        localdb = (SimpleTypingKB) db;
    }
    
    private static final double DBL_EPSILON = Math.ulp(1.0);
    
    private static double pdhyper(double x, double NR, double NB, double n) {
        double sum = 0.0;
        double term = 1.0;
        while (x > 0 && term >= DBL_EPSILON * sum) {
            term *= x * (NB - n + x) / (n + 1 - x) / (NR + 1 - x);
            sum += term;
            x--;
        }
        System.err.println(1 + sum);
        return 1 + sum;
    }
    
    private static double phyper(int x, int NR, int NB, int n, boolean tail) {
        if (!tail) {
            int oldNB = NB;
            NB = NR;
            NR = oldNB;
            x = n - x - 1;
        }
        if (x < 0) {
            return 0.0;
        }
        if (x >= n) {
            return 1.0;
        }
        HypergeometricDistribution hg = new HypergeometricDistribution(NR+NB, NR, n);
        //System.err.println(hg.cumulativeProbability(x));
        return hg.cumulativeProbability(x); //hg.probability(x) * pdhyper(Math.floor(x + 1e-7), (double) NR, (double) NB, (double) n);
     }
    
    public static double fisherTest(int x, int m, int n, int k, boolean tail) {
        if (!tail) return phyper(x - 1 , m, n, k, tail);
        return phyper(x, m, n, k, tail);
    }
    
    public static double fisherTest(int x, int m, int n, int k) {
        return fisherTest(x, m, n, k, true);
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
            
            assert(c1size > 0);
            assert(c1phi.size() < c1size);
            assert(c1phi.size() > 0);

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
                    int c1c2phi = (int) SimpleTypingKB.countIntersection(c1phi, localdb.classes.get(class2));
                    assert(c1c2size > 0);
                    assert(c1size > c1c2size);
//                    if (Double.isNaN(Math.log(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, true))) 
//                            || Double.isNaN(Math.log(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, false)))) {
//                        throw new IllegalArgumentException("NaN: " +
//                                Double.toString(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, true)) + ", " +
//                                Double.toString(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, false)) + ", " +
//                                Integer.toString(c1c2phi) + ", " + Integer.toString(c1phi.size()) + ", " +
//                                Integer.toString(c1size - c1phi.size()) + ", " + Integer.toString(c1c2size));
//                    }
                    index.get(class1).separationScore = Math.min(index.get(class1).separationScore, 
                            Math.log(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, false)));
                    if (index.containsKey(class2)) {
                        index.get(class2).separationScore = Math.min(index.get(class2).separationScore, 
                            Math.log(fisherTest(c1c2phi, c1phi.size(), c1size - c1phi.size(), c1c2size, true)));
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println(fisherTest(0, 25000, 5000050, 50));
        System.out.println(fisherTest(0, 25000, 5000050, 50, false));
        System.out.println(fisherTest(1, 25001, 5000049, 50));
        System.out.println(fisherTest(1, 25001, 5000049, 50, false));
        System.out.println(fisherTest(548, 81396, 5048714, 2248));
    }
    
    
}
