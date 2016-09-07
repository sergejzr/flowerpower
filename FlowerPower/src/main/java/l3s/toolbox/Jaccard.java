// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Jaccard.java

package l3s.toolbox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Jaccard
{

    public Jaccard()
    {
    }

    public static double jaccardSimilarity(Set d1, Set d2)
    {
        int overlap = 0;
        Set<String> shortd;
        Set<String> longd;
        if(d1.size() > d2.size())
        {
            shortd = d2;
            longd = d1;
        } else
        {
            shortd = d1;
            longd = d2;
        }
        HashSet<String> union=new HashSet<String>(d1);
        union.addAll(d2);
        for(Iterator<String> iterator = shortd.iterator(); iterator.hasNext();)
        {
            Object d = iterator.next();
            if(longd.contains(d))
                overlap++;
            
        }

        return ((double)overlap * 1.0D) / union.size();
    }
}
