// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SampleDJ.java

package l3s.rdj.impl;

import java.util.Random;
import java.util.Vector;

import l3s.rdj.document.Diversity;
import l3s.rdj.document.Document;
import l3s.rdj.document.SimilarityComparator;
import l3s.toolbox.Median;

public class SampleDJ extends Diversity
{

    public SampleDJ(Vector docs, double error, double confidence, SimilarityComparator comp)
    {
        super(docs, error, confidence, comp);
    }

    double randomSampling(Vector docs, double epsilon, double delta, int W)
    {
        double rdj = 0.0D;
        int r1 = 0;
        int r2 = (int)Math.ceil(Math.log(1.0D / delta) / Math.log(2D));
        double jsSum[] = new double[r2];
        Random r = new Random(124L);
        double abs_error;
        do
        {
            for(int i = 0; i < r2; i++)
            {
                for(int j = 1; j <= W; j++)
                {
                    int a = r.nextInt(docs.size());
                    int b;
                    for(b = r.nextInt(docs.size()); a == b; b = r.nextInt(docs.size()));
                    
                    jsSum[i] += getSimilarityComparator().similarity((Document)docs.elementAt(a), (Document)docs.elementAt(b));
                }

            }

            r1 += W;
            for(int i = 0; i < r2; i++)
                rdj += jsSum[i];

      //      rdj = rdj / (double)r2 / (double)r1;
            rdj = Median.median(jsSum) / (double)r1;
            abs_error = 1.0D / Math.sqrt(r1);
        } while(abs_error / Math.abs(rdj - abs_error) > epsilon);
        return rdj;
    }

    public double getRDJ()
    {
        return randomSampling(getCollection(), getError(), getConfidence(), 100);
    }
}
