// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JaccardSimilarityComparator.java

package l3s.toolbox;

import l3s.rdj.document.Document;
import l3s.rdj.document.SimilarityComparator;

// Referenced classes of package l3s.toolbox:
//            Jaccard

public class JaccardSimilarityComparator
    implements SimilarityComparator
{

    public JaccardSimilarityComparator()
    {
    }

    public double similarity(Document doc1, Document doc2)
    {
        return Jaccard.jaccardSimilarity(doc1, doc2);
    }
}
