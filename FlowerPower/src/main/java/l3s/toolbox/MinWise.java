// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MinWise.java

package l3s.toolbox;

import java.util.Iterator;
import java.util.Random;

import l3s.rdj.document.Document;

public class MinWise
{

    public MinWise()
    {
    }

    static long hash31(long a, long b, long x)
    {
        long result = a * x + b;
        result = (result >> HL) + result & (long)primeMOD;
        long lresult = result;
        return lresult;
    }

    static int minHash31(long r[], Document document)
    {
        int result = 0;
        int min = 0;
        min = 0x7fffffff;
        for(Iterator iterator = document.iterator(); iterator.hasNext();)
        {
            Object o = iterator.next();
            int t = (int)hash31(r[0], r[1], o.hashCode());
            if(t < min)
            {
                min = t;
                result = o.hashCode();
            }
        }

        return result;
    }

    static long[][] initMinHash(int l, int d)
    {
        long r[][] = new long[l][];
        for(int i = 0; i < l; i++)
        {
            r[i] = new long[d + 1];
            for(int j = 0; j <= d; j++)
                r[i][j] = Math.abs(myrand.nextLong());

        }

        return r;
    }

    static Random myrand = new Random();
    static int primeMOD = 0x7fffffff;
    static int HL = 31;

}
