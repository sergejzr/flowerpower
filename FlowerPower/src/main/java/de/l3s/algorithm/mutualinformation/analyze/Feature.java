package de.l3s.algorithm.mutualinformation.analyze;



/**
* Objekte dieser Klasse sind Paare aus Termen und deren Gewichtung
*/
public class Feature
{

  private String term; 
  public double weight; 

  public Feature( String aTerm, double aWeight ){
    term = aTerm ; 
    weight = aWeight ; 
  }

  public Feature( String aTerm )
  {
    term = aTerm ; 
    weight = 1 ; 
  }




  public String getTerm()
  {
    return term ; 
  }

  public double getWeight()
  {
    return weight ; 
  }

  public String toString()
  {
    return "(" + term + "," + weight + ")"; 
  }


}
