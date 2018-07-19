/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amie.typing.classifier.simple;

import amie.data.SimpleTypingKB;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import javatools.datatypes.ByteString;
import javatools.datatypes.IntHashMap;
import javatools.datatypes.Pair;

/**
 *
 * @author jlajus
 */
public class StrictSeparationSimpleClassifier extends SeparationSimpleClassifier {

    public StrictSeparationSimpleClassifier(SimpleTypingKB db, double[] thresholds, Queue<SimpleClassifierOutput> output, Lock outputLock) {
        super(db, thresholds, output, outputLock);
        name = "strictCR";
    }

    public StrictSeparationSimpleClassifier(SimpleTypingKB db, double[] thresholds, Queue<SimpleClassifierOutput> output, Lock outputLock, boolean supportForTarget) {
        super(db, thresholds, output, outputLock, supportForTarget);
        name = "strictCR";
    }

    public StrictSeparationSimpleClassifier(SimpleTypingKB db, double[] thresholds, Queue<SimpleClassifierOutput> output, Lock outputLock, Map<ByteString, IntHashMap<ByteString>> classIntersectionSize) {
        super(db, thresholds, output, outputLock, classIntersectionSize);
        name = "strictCR";
    }

    public StrictSeparationSimpleClassifier(SimpleTypingKB db, double[] thresholds, Queue<SimpleClassifierOutput> output, Lock outputLock, Map<ByteString, IntHashMap<ByteString>> classIntersectionSize, boolean supportForTarget) {
        super(db, thresholds, output, outputLock, classIntersectionSize, supportForTarget);
        name = "strictCR";
    }
    
    @Override
    protected Pair<Double, Double> classesScore(int c1, int c1c2, int c1phi, int c1c2phi) {
        double s = ((double) c1c2phi) / c1phi;
        double edge = Math.log((double) c1c2 / (c1 - c1c2) * (1.0 - s) / s);
        return new Pair<>(-Math.abs(edge), -edge);
    }
}
