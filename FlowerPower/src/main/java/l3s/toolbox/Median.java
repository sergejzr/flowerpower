// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Median.java

package l3s.toolbox;

import java.util.Arrays;

public class Median
{

    public Median()
    {
    }

    public static double median(double a[])
    {
        Arrays.sort(a);
        double sum = 0.0D;
        double ad[];
        int j = (ad = a).length;
        for(int i = 0; i < j; i++)
        {
            double cur = ad[i];
            sum += cur;
        }

        double mean = sum / (double)a.length;
        double before = a[0];
        double ad1[];
        int l = (ad1 = a).length;
        for(int k = 0; k < l; k++)
        {
            double cur = ad1[k];
            if(cur > mean)
                return Math.abs(cur - mean) >= Math.abs(cur - before) ? before : cur;
            before = cur;
        }

        return a[a.length - 1];
    }
}
