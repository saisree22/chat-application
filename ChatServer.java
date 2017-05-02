import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer implements Runnable
{
	SharedMsg nextMsg=new SharedMsg();
	Socket socket;
	ChatDaemon daemon;

	BufferedReader input;
	PrintStream output;

	String userName="Anonymous";
	boolean running=false;

	boolean alive()
	{
		return running;
	}

	public ChatServer(Socket s,ChatDaemon d)
	{
		socket=s;
		daemon=d;
		try
		{
			input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output=new PrintStream(socket.getOutputStream(),true);
			running=true;
			Thread writeThread=new Thread(this);
			writeThread.start();
			Thread readThread=new Thread(new ReadThread());
			readThread.start();
			// client logged off if it comes here
		}
		catch(IOException e)
		{
			System.out.println("Abnormal chat server socket condition 1:"+e);;
		}
	}

	public void run()
	{
		writeLoop();
	}
	
	class ReadThread implements Runnable
	{
		public void run()
		{
			readLoop();
		}
	}
			

	void readLoop()
	{
		try
		{
			outside:
			while(running)
			{
				String line=input.readLine();
				System.out.println("Server Received: "+line);
				switch(line.charAt(0))
				{
					case 'i': // login
						daemon.shrMsg.put(line);
						userName=line.substring(2);
						String userListMsg="u";
						for(int i=0;i<daemon.numUsers;i++)
							if (daemon.user[i].alive())
								userListMsg+= " "+daemon.user[i].userName;
						nextMsg.put(userListMsg);
						break;
					case 'o': // logout
						daemon.shrMsg.put(line);
						running = false;
						break outside;
					case 'm': // new message
						daemon.shrMsg.put(line);
						break;
					case 'v': // invite
						daemon.createNewRoom(line);
						break;
					default:
				}
			}
		}catch (IOException e) {}
		try
		{		
			input.close();
			output.close();
			socket.close();
		}
		catch (IOException e) {}
		System.out.println(userName+ " logged off...server thread exiting");
	}
		
	void writeLoop()
	{
		while(running)
		{
			String s=nextMsg.get();
			if (s.charAt(0)!='d')
			{
				System.out.println("Server sent :"+s);
				output.println(s);
			}
		}
	}

}
