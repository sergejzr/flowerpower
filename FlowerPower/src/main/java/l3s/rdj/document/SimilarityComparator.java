// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SimilarityComparator.java

package l3s.rdj.document;


// Referenced classes of package l3s.rdj:
//            Document

public interface SimilarityComparator
{

    public abstract double similarity(Document document, Document document1);
}
