import java.util.*;

public class SharedMsg
{
	Vector messages=new Vector();
	synchronized String get()
	{
		if (messages.size()==0)
			return "d";
		else
			return (String)messages.remove(0);
	}

	synchronized void put(String s)
	{
		messages.addElement(s);
	}
}
