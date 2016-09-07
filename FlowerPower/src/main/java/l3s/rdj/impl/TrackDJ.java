// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TrackDJ.java

package l3s.rdj.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import l3s.rdj.document.Diversity;
import l3s.rdj.document.Document;
import l3s.toolbox.JaccardSimilarityComparator;
import l3s.toolbox.Median;

// Referenced classes of package l3s.rdj.impl:
//            indexItemType

public class TrackDJ extends Diversity
{

    public TrackDJ(Vector docs, double error, double confidence)
    {
        super(docs, error, confidence, new JaccardSimilarityComparator());
        HL = 31;
        primeMOD = 0x7fffffff;
    }

    public double getRDJ()
    {
        int line[][] = new int[getCollection().size()][];
        for(int i = 0; i < line.length; i++)
        {
            Document doc = (Document)getCollection().get(i);
            line[i] = new int[doc.size()];
            int y = 0;
            for(Iterator iterator = doc.iterator(); iterator.hasNext();)
            {
                Object o = iterator.next();
                line[i][y++] = o.hashCode();
            }

        }

        return computeRDJOldStyle(line, getError(), getConfidence());
    }

    public static void main(String args1[])
    {
    }

    public double computeRDJOldStyle(int line[][], double eps, double del)
    {
        for(int i = 0; i < line.length; i++)
            Arrays.sort(line[i]);

        int shortLine[][] = line;
        int longLine[][] = new int[0][];
        int result[] = extractLineStatistics(shortLine);
        extractLineStatistics(longLine);
        double bufRdj = trackDjBuf(shortLine, result, eps, del);
        double simSum = (bufRdj * (double)shortLine.length * (double)(shortLine.length - 1)) / 2D;
        for(int i = 0; i < shortLine.length; i++)
        {
            for(int j = 0; j < longLine.length; j++)
                simSum += jacSim(shortLine[i], longLine[j]);

        }

        for(int i = 0; i < longLine.length; i++)
        {
            for(int j = 0; j < longLine.length; j++)
                simSum += jacSim(longLine[i], longLine[j]);

        }

        int n = line.length;
        bufRdj = (simSum * 2D) / (double)n / (double)(n - 1);
        return bufRdj;
    }

    int[] extractLineStatistics(int line[][])
    {
        int maxLineSize = 0;
        int ret[] = new int[3];
        if(line.length == 0)
        {
            ret[0] = 0;
            ret[1] = 0;
            ret[2] = 0;
            return ret;
        }
        int t1 = line[0][0];
        int t2 = line[0][0];
        for(int i = 0; i < line.length; i++)
        {
            if(line[i].length > maxLineSize)
                maxLineSize = line[i].length;
            for(int j = 0; j < line[i].length; j++)
                if(line[i][j] < t1)
                    t1 = line[i][j];
                else
                if(line[i][j] > t2)
                    t2 = line[i][j];

        }

        ret[0] = t1;
        ret[1] = t2;
        ret[2] = maxLineSize;
        return ret;
    }

    int[][] initMinHash(int l, int d)
    {
        Random rand = new Random(12345L);
        int r[][] = new int[l][];
        for(int i = 0; i < l; i++)
        {
            r[i] = new int[d + 1];
            for(int j = 0; j <= d; j++)
                r[i][j] = rand.nextInt();

        }

        return r;
    }

    int[] initFreqBuf(int maxItem)
    {
        int result[] = new int[maxItem + 1];
        for(int i = 0; i <= maxItem; i++)
            result[i] = 0;

        return result;
    }

    double trackDjBuf(int line[][], int result[], double epsilon, double delta)
    {
        int L1 = (int)(((double)(result[2] - 1) / epsilon / epsilon) * 8D);
        int L2 = (int)Math.ceil(Math.log(1.0D / delta) / Math.log(2D));
        int idFreqBuf[] = initFreqBuf(result[1]);
        double f2list[] = new double[L2];
        int d = 1;
        int r[][] = initMinHash(L1 * L2, d);
        indexItemType itemIndex = null;
        indexItemType anItem = new indexItemType();
        int n = line.length;
        double f2 = 0.0D;
        for(int j = 0; j < L2; j++)
        {
            for(int l = 0; l < L1; l++)
            {
                itemIndex = null;
                for(int i = 0; i < n; i++)
                {
                    int id = minHash31(r[j * L1 + l], line[i]);
                    if(idFreqBuf[id] == 0)
                    {
                        anItem = new indexItemType();
                        anItem.item = id;
                        anItem.next = itemIndex;
                        itemIndex = anItem;
                    }
                    idFreqBuf[id]++;
                }

                double aDouble = f2Buf(idFreqBuf, itemIndex);
                f2list[j] += aDouble;
            }

        }

        f2 = Median.median(f2list) / (double)L1;
        double bufRdj = (f2 - (double)n) / (double)n / (double)(n - 1);
        return bufRdj;
    }

    int minHash31(int a[], int s[])
    {
        int min = (int)hash31(a[0], a[1], s[0]);
        int result = s[0];
        for(int i = 0; i < s.length; i++)
        {
            int t = (int)hash31(a[0], a[1], s[i]);
            if(t < min)
            {
                min = t;
                result = s[i];
            }
        }

        return result;
    }

    long hash31(long a, long b, long x)
    {
        long result = a * x + b;
        result = (result >> HL) + result & (long)primeMOD;
        long lresult = result;
        return lresult;
    }

    double f2Buf(int freqBuf[], indexItemType itemIndex)
    {
        double result = 0.0D;
        for(indexItemType p = itemIndex; p != null; p = p.next)
        {
            int i = p.item;
            double tmp = (double)freqBuf[i] * (double)freqBuf[i];
            result += tmp;
            freqBuf[i] = 0;
        }

        return result;
    }

    double jacSim(int a[], int b[])
    {
        int t = countOverlap(a, b);
        double result = (double)t / (double)((a.length + b.length) - t);
        return result;
    }

    int countOverlap(int a[], int b[])
    {
        int i = 0;
        int j = 0;
        int count = 0;
        while(i < a.length || j < b.length) 
            if(a[i] == b[j])
            {
                count++;
                i++;
                j++;
                if(i >= a.length || j >= b.length)
                    return count;
            } else
            if(a[i] < b[j])
            {
                if(++i >= a.length)
                    return count;
            } else
            if(a[i] > b[j] && ++j >= b.length)
                return count;
        return -1;
    }

    int HL;
    int primeMOD;
}
