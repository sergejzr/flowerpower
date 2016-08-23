package analyze;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
//import oracle.jdbc.dbaccess.*;
/*
import oracle.jdbc.driver.*;
import oracle.sql.*;
*/

//import meta.dbaccess.*; 


//import weka.core.*; 
//import weka.classifiers.*; 
//import weka.classifiers.j48.*;
//import weka.filters.*; 

//import org.biojava.stats.svm.*;
//import org.biojava.stats.svm.tools.*;

//import meta.sparse.*; 


public class Terms
{
  
  private String url ;
  private ArrayList termPosList ; 
  private int size ; 
  private ArrayList featureList ; 
  //private FeatureSelection featureSelection ; 
  
  /*
  public Terms( String aUrl , ArrayList aTermPosList , FeatureSelection aFeatureSelection )
  {
    url = aUrl ; 
    termPosList = aTermPosList ;  
    size = termPosList.size(); 
    featureSelection = aFeatureSelection ; 
  }
*/

  
  public Terms(ArrayList aTermPosList)
  {
	  termPosList = aTermPosList ; 
	  size = termPosList.size();
  }


  /**
  * Konstruiert ein Objekt vom Typ Terms mit Praeselektion von Termmengen 
  */
  public Terms( ArrayList aTermPosList , HashSet selectedTerms)
  {
    termPosList = new ArrayList(); 
    size = aTermPosList.size();
    // Elimination von Termen 
    for (ListIterator iter = aTermPosList.listIterator(); iter.hasNext() ; ) 
    {
      TermPos tp = (TermPos)iter.next();
      String term = tp.getTerm(); 
      if (selectedTerms.contains(term))
      {
        termPosList.add(tp); 
      }
    }
  }
      

  
  public String getUrl()
  {
    return url ; 
  }
  
  public ArrayList getFeatureList()
  {
    return featureList ; 
  }
  
  public ArrayList getTermPosList()
  {
    return termPosList ; 
  }
  
  public int getSize()
  {
    return size; 
  }
  
  




  
  
  /**
  * Berechnet einen einfachen Feature-Vektor aus Termen 
  * unter Beraecksichtigung aller Terme des Dokuments 
  * @return der Featurevektor des Dokuments 
  */
  public ArrayList computeSimpleFeatureVector()
  {
    Map featureMap = new HashMap();
    ArrayList result = new ArrayList(); 
    
    for (ListIterator iter = termPosList.listIterator(); iter.hasNext(); )
    {
      TermPos tp = (TermPos)iter.next(); 
      String term = tp.getTerm(); 
      if (!featureMap.containsKey(term))
      {
        featureMap.put(term, new Feature(term)); 
      }
      else
      {
        Feature sel = (Feature)featureMap.get(term);
        sel.weight = sel.weight + 1 ; 
      }
    }
    result.addAll( featureMap.values() );
    for (ListIterator iter = result.listIterator(); iter.hasNext();)
    {
      Feature f = (Feature)iter.next(); 
      f.weight = f.weight / size ; 
    }
    featureList = result ;
    return featureList ; 
  }  
  
  /**
  * Berechnet einen Termvektor fuer ein Dokument , 
  * verwendet dabei eine Praeselektion von charakteristischen Termen
  * @param className die Klasse aus der die charakteristischen Terme stammen
  * @return der Featurevektor
  */
  /*
  public ArrayList computePreSelectedFeatureVector( String className , int n )
  throws Exception
  {
    Map featureMap = new HashMap();
    ArrayList result = new ArrayList(); 
    HashSet selectedTermSet = featureSelection.getTerms( className , n);
    
    for (ListIterator iter = termPosList.listIterator(); iter.hasNext(); )
    {
      TermPos tp = (TermPos)iter.next(); 
      String term = tp.getTerm(); 
      if (selectedTermSet.contains(term))
      {
        if (!featureMap.containsKey(term))
        {
          featureMap.put(term, new Feature(term)); 
        }
        else
        {
          Feature sel = (Feature)featureMap.get(term);
          sel.weight = sel.weight + 1 ; 
        }
      }
    }
    result.addAll( featureMap.values() );
    for (ListIterator iter = result.listIterator(); iter.hasNext();)
    {
      Feature f = (Feature)iter.next(); 
      f.weight = f.weight / size ; 
    }
    featureList = result ;
    return featureList; 
  }     
  */
  

  public ArrayList computeReducedFeatureVector( HashSet selectedTermSet)
  {
    Map featureMap = new HashMap();
    ArrayList result = new ArrayList(); 
    
    for (ListIterator iter = termPosList.listIterator(); iter.hasNext(); )
    {
      TermPos tp = (TermPos)iter.next(); 
      String term = tp.getTerm(); 
      if (selectedTermSet.contains(term))
      {
        if (!featureMap.containsKey(term))
        {
          featureMap.put(term, new Feature(term)); 
        }
        else
        {
          Feature sel = (Feature)featureMap.get(term);
          sel.weight = sel.weight + 1 ; 
        }
      }
    }
    result.addAll( featureMap.values() );
    for (ListIterator iter = result.listIterator(); iter.hasNext();)
    {
      Feature f = (Feature)iter.next(); 
      f.weight = f.weight / size ; 
    }
    featureList = result ;
    return featureList; 
  }   




  /**
  * berchnet einen Kontrastvektor aus Einzeltermen 
  * @param className Klassenname
  * @param cNumb Anzahl Terme der betrachteten Klasse 
  * @param neighNumb Anzahl Terme aus den Kontrastklassen
  */
  /*
  public ArrayList computeContrastFeatureVector( String className , int cNumb , int neighNumb)
  throws Exception
  {
    Map featureMap = new HashMap();
    ArrayList result = new ArrayList(); 
    HashSet selectedTermSet = featureSelection.getContrastTerms( className , cNumb , neighNumb);
    
    for (ListIterator iter = termPosList.listIterator(); iter.hasNext(); )
    {
      TermPos tp = (TermPos)iter.next(); 
      String term = tp.getTerm(); 
      if (selectedTermSet.contains(term))
      {
        if (!featureMap.containsKey(term))
        {
          featureMap.put(term, new Feature(term)); 
        }
        else
        {
          Feature sel = (Feature)featureMap.get(term);
          sel.weight = sel.weight + 1 ; 
        }
      }
    }
    result.addAll( featureMap.values() );
    for (ListIterator iter = result.listIterator(); iter.hasNext();)
    {
      Feature f = (Feature)iter.next(); 
      f.weight = f.weight / size ; 
    }
    featureList = result ;
    return featureList; 
  }     
  
  */
  
  
  
  
  /**
  * liefert den zum Featurevektor gehaerigen SparseVector
  * @return der SparseVector
  */
  /*
  public SparseVector getSparseVector()
  {
    SparseVector s = new SparseVector();
    for (ListIterator iter = featureList.listIterator(); iter.hasNext(); )
    {
      Feature f = (Feature)iter.next(); 
      int id = f.getTerm().hashCode(); 
      double weight = f.getWeight(); 
      s.put( id , weight); 
    }
    return s;  
  }
  */
  /**
  * liefert den zum Featurevektor gehaerigen MySparseVector
  * @return der SparseVector
  */
  /*
  public MySparseVector getMySparseVector()
  {
    MySparseVector s = new MySparseVector();
    for (ListIterator iter = featureList.listIterator(); iter.hasNext(); )
    {
      Feature f = (Feature)iter.next(); 
      int id = f.getTerm().hashCode(); 
      double weight = f.getWeight(); 
      s.put( id , weight); 
    }
    return s;  
  }

*/

  
  /**
  * liefert die ("WEKA")-Instanz zum Featurevektor der 
  * zu dem Atributraum der vorgegebenen Instanzenmenge passt
  * @param dataSet die Instanzenmenge
  * @return die WEKA-Instanz zum Featurevektor
  */
  /*
  public Instance getInstance(Instances dataSet)
  {
    int dataSize = dataSet.numAttributes();
    Instance result = new Instance(1, getNullArray(dataSize-1));
    result.setDataset(dataSet); 
    // Elemente des Featurevektors einfaegen
    for (ListIterator iter = featureList.listIterator(); iter.hasNext(); )
    {
      Feature f = (Feature)iter.next(); 
      String term = f.getTerm(); 
      double weight = f.getWeight(); 
      try
      {
        result.setValue(dataSet.attribute(term),weight); 
      }
      catch(Exception e)
      {
        //e.printStackTrace(); 
      }
    }
    return result ; 
  }
  */
  /**
  * liefert die ("WEKA")-Instanz die zum Clustern verwender wird
  */
  /*
  public Instance getClusterInstance(Instances dataSet)
  {
    int dataSize = dataSet.numAttributes();
    Instance result = new Instance(1, getNullArray(dataSize));
    result.setDataset(dataSet); 
    // Elemente des Featurevektors einfaegen
    for (ListIterator iter = featureList.listIterator(); iter.hasNext(); )
    {
      Feature f = (Feature)iter.next(); 
      String term = f.getTerm(); 
      double weight = f.getWeight(); 
      try
      {
        result.setValue(dataSet.attribute(term),weight); 
      }
      catch(Exception e)
      {
        //e.printStackTrace(); 
      }
    }
    return result ; 
  }

*/


  private static double[] getNullArray(int size)
  {
    double[] result = new double[size];
    return result ; 
  } 
  
  
  
  /**
  * liefert die ("WEKA")-Instanz zum Trainings-Featurevektor der 
  * zu dem Atributraum der vorgegebenen Instanzenmenge passt
  * @param dataSet die Instanzenmenge
  * @param eval die Intellektuelle binaere Klassifikation des Trainingsvektors (hier "true"/"false")
  * @return die WEKA-Instanz zum Featurevektor
  */
  /*
  public Instance getTrainingInstane(Instances dataSet , String eval)
  {
    int dataSize = dataSet.numAttributes();
    Instance result = new Instance(dataSize);
    result.setDataset(dataSet); 
    // Zunaechst alle Elemente auf "0" setzen
    for (int i = 0 ; i < dataSize - 1 ; i++ )
    {
      result.setValue(i,0); 
    }
    // Elemente des Featurevektors einfaegen
    for (ListIterator iter = featureList.listIterator(); iter.hasNext(); )
    {
      Feature f = (Feature)iter.next(); 
      String term = f.getTerm(); 
      double weight = f.getWeight(); 
      try
      {
        result.setValue(dataSet.attribute(term),weight); 
      }
      catch(Exception e)
      {
        e.printStackTrace(); 
      }
    }
    // Evaluation einfaegen
    try
    {
      result.setClassValue(eval);
    }
    catch (Exception e)
    {
      e.printStackTrace(); 
    }
    return result ; 
  }
  */
  
  
  
  /**
  * "main"-Methode zum Testen dieser Klasse
  */
  public static void main( String[] args )
  throws Exception
  {
    /* 
    ArrayList termPosList = DataBase.getTermPosList( "http://db.cs.berkeley.edu/oldlunch.html" ); 
    Terms terms = new Terms( "http://db.cs.berkeley.edu/oldlunch.html" , termPosList , FeatureSelection.getInstance() ); 
    ArrayList flist = terms.computeSimpleFeatureVector();
    System.out.println( flist ); 
    Terms terms2 = new Terms( "http://db.cs.berkeley.edu/oldlunch.html" , termPosList , FeatureSelection.getInstance());
    System.out.println( "ok" ); 
    flist = terms2.computePreSelectedFeatureVector("ROOT/sonstiges/"); 
    System.out.println( flist );
    //System.out.println( terms2.getFeatureList() );
    DBConnect.getInstance().close();  */
    



  }
  
  
  
}


