// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AllPairsDJ.java

package l3s.rdj.impl;

import java.util.Vector;

import l3s.rdj.document.Diversity;
import l3s.rdj.document.SimilarityComparator;
import l3s.rdj.document.Document;

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
