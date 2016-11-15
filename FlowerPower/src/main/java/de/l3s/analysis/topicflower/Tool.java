package de.l3s.analysis.topicflower;

public class Tool {
public static String jarName(Object obj)
{

return	obj.getClass().getProtectionDomain()
	  .getCodeSource()
	  .getLocation()
	  .getPath();	
}
}
