// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AllPairsDJ.java

package de.l3s.aglorithm.diversity;

import java.util.Vector;

import de.l3s.algorithm.diversity.document.Diversity;
import de.l3s.algorithm.diversity.document.Document;
import de.l3s.algorithm.diversity.document.SimilarityComparator;

public class AllPairsDJ extends Diversity
{

    public AllPairsDJ(Vector docs, double error, double confidence, SimilarityComparator comp)
    {
        super(docs, error, confidence, comp);
    }

    public double jsSum(Vector docs)
    {
        double result = 0.0D;
        for(int i = 0; i < docs.size(); i++)
        {
            for(int j = i + 1; j < docs.size(); j++)
            {
                Double curres = Double.valueOf(getSimilarityComparator().similarity((Document)docs.elementAt(i), (Document)docs.elementAt(j)));
                result += curres.doubleValue();
            }

        }

        return result;
    }

    public double getRDJ()
    {
        setCollection(getCollection());
        return (2D * jsSum(getCollection())) / (double)getCollection().size() / (double)(getCollection().size() - 1);
    }
}
