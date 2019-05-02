package br.ufg.emc.termografia.diagnosis;

import androidx.core.util.Pair;

import java.util.List;

import br.ufg.emc.termografia.Meter;

public class BushingDiagnoser {
    public static final int L1 = 15;
    public static final int L2 = 25;
    public static final int L3 = 35;
    public static final int L4 = 60;

    public static final double V1 = 0.9;
    public static final double V2 = 0.8;
    public static final double V3 = 0.7;
    public static final double V4 = 0.4;

    private double c;
    private double k;
    private double[] limits;

    public BushingDiagnoser(double loading, String material) {
        c = loading;
        k = Material.getConstant(material);

        limits = new double[6];
        limits[0] = 0.0;
        limits[1] = getLimit(L1);
        limits[2] = getLimit(L2);
        limits[3] = getLimit(L3);
        limits[4] = getLimit(L4);
        limits[5] = 150.0;
    }

    private double getLimit(int l) {
        double aux = l * Math.pow(c, 2);
        return (aux * (k + aux)) / (k + l);
    }

    public Concept getIndividualConcept(Meter meter) {
        double gradient = meter.getDifference();

        if (gradient <= limits[1]) return Concept.A;
        else if (gradient <= limits[2]) return Concept.B;
        else if (gradient <= limits[3]) return Concept.C;
        else if (gradient <= limits[4]) return Concept.D;
        else return Concept.E;
    }

    public double getIndividualScore(Meter meter) {
        double gradient = meter.getDifference();
        Concept concept = getIndividualConcept(meter);

        Pair<Double, Double> conceptLimits;
        Pair<Double, Double> scoreLimits;

        switch (concept) {
            case A:
                conceptLimits = Pair.create(limits[0], limits[1]);
                scoreLimits = Pair.create(1.0, V1);
                break;

            case B:
                conceptLimits = Pair.create(limits[1], limits[2]);
                scoreLimits = Pair.create(V1, V2);
                break;

            case C:
                conceptLimits = Pair.create(limits[2], limits[3]);
                scoreLimits = Pair.create(V2, V3);
                break;

            case D:
                conceptLimits = Pair.create(limits[3], limits[4]);
                scoreLimits = Pair.create(V3, V4);
                break;

            default:
                conceptLimits = Pair.create(limits[4], limits[5]);
                scoreLimits = Pair.create(V4, 0.0);
                break;
        }

        return BushingDiagnoser.calculateScore(gradient, conceptLimits, scoreLimits);
    }

    public double getScore(List<Meter> meterList) {
        double n, p, num = 0, den = 0;
        Meter meter;

        for (int i = 0; i < meterList.size(); i++) {
            meter = meterList.get(i);
            if (meter.isAmbient()) continue;

            n = getIndividualScore(meter);
            p = calculateWeightedScore(n);

            num += p * c * n;
            den += p * c;
        }

        return num / den;
    }

    public Concept getConcept(List<Meter> meters) {
        double score = getScore(meters);

        if (score >= V1) return Concept.A;
        else if (score >= V2) return Concept.B;
        else if (score >= V3) return Concept.C;
        else if (score >= V4) return Concept.D;
        else return Concept.E;
    }

    public static double calculateScore(double gradient, Pair<Double, Double> conceptLimits, Pair<Double, Double> scoreLimits) {
        if (gradient < conceptLimits.first) gradient = conceptLimits.first;
        else if (gradient > conceptLimits.second) gradient = conceptLimits.second;

        double p = (gradient - conceptLimits.first) / (conceptLimits.second - conceptLimits.first);
        return ((scoreLimits.second - scoreLimits.first) * p) + scoreLimits.first;
    }


    public static double calculateWeightedScore(double score) {
        return 3 * Math.exp(2.53775 * score) + 0.59912;
    }
}
