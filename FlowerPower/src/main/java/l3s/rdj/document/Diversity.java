// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Diversity.java

package l3s.rdj.document;

import java.util.Vector;

// Referenced classes of package l3s.rdj:
//            SimilarityComparator

public abstract class Diversity
{

    public Diversity(Vector docs, double error, double confidence, SimilarityComparator comp)
    {
        collection = new Vector();
        setCollection(docs);
        this.error = error;
        this.confidence = confidence;
        similarityComparator = comp;
    }

    public SimilarityComparator getSimilarityComparator()
    {
        return similarityComparator;
    }

    public double getError()
    {
        return error;
    }

    public void setError(double error)
    {
        this.error = error;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public void setConfidence(double confidence)
    {
        this.confidence = confidence;
    }

    public abstract double getRDJ();

    public Vector getCollection()
    {
        return collection;
    }

    public void setCollection(Vector collection)
    {
        this.collection = collection;
    }

    private Vector collection;
    private double error;
    private double confidence;
    SimilarityComparator similarityComparator;
}
