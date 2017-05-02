import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ChatClient extends JFrame implements Runnable 
{
	Socket socket;
	
	Room currentRoom=null;
	DefaultListModel myRooms = new DefaultListModel();
	DefaultListModel onlineUsers = new DefaultListModel();
	String myName;

	BufferedReader input;
	PrintStream output;
	
	JTextArea txtMessages;
	JList lstRooms;
	JList lstUsers;
	JTextField txtSend;
	boolean active;
	
	public static void main(String[] argv) throws IOException
	{
		InetAddress clientAddr= InetAddress.getLocalHost();
		int portNumber=1666;
		System.out.println("Chat Client by Vish starting...");
		String host = JOptionPane.showInputDialog(null,"Enter the hostname/IP Address of the server \n and port number seperated by a space","Vish Chat",JOptionPane.QUESTION_MESSAGE);
		if (host==null)
			System.exit(1);
		StringTokenizer t=new StringTokenizer(host);
		do
		{
			try 
			{
				if (t.hasMoreTokens())
					clientAddr=InetAddress.getByName(t.nextToken());
				if (t.hasMoreTokens())
					portNumber=Integer.parseInt(t.nextToken());
				break;
			}
			catch(Exception e) 
			{
				JOptionPane.showMessageDialog(null,"Invalid input. Please try again","Vish Chat!",JOptionPane.ERROR_MESSAGE);
			}
		}
		while (true);

		System.out.println("Connecting to Chat Server at "+clientAddr+"...");
		ChatClient cc = new ChatClient(clientAddr,portNumber);
		cc.pack();
		cc.setLocation(15,1);
		cc.setVisible(true);
	}

	public ChatClient(InetAddress adx,int portNumber)
	{
		super( "Vish Chat!" );
		try
		{
			socket=new Socket(adx,portNumber);
			input =new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output=new PrintStream(socket.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("Could not connect to the server...exiting");;
			System.exit(-1);
		}
		System.out.println("Connected....starting GUI...");
		JLabel lbl1 = new JLabel("Room Messages");
		JLabel lbl2 = new JLabel("Your Rooms");
		JLabel lbl3 = new JLabel("Online Users");

		txtMessages = new JTextArea();
		txtMessages.setEditable(false);
		txtMessages.setLineWrap(true);
		txtMessages.setWrapStyleWord(true);

		JScrollPane sclMessages = new JScrollPane(txtMessages);
		sclMessages.setPreferredSize(new Dimension(250, 250));
		
		lstRooms=new JList(myRooms);
		lstRooms.addListSelectionListener(new ListListener());
		JScrollPane sclRooms = new JScrollPane(lstRooms);
		sclRooms.setPreferredSize(new Dimension(100, 250));

		lstUsers=new JList(onlineUsers);
		//lstUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane sclUsers = new JScrollPane(lstUsers);
		sclUsers.setPreferredSize(new Dimension(100, 250));

		txtSend = new JTextField(20);
		JButton btnSend = new JButton("Send");
		JButton btnCreate = new JButton("Invite users to a new Room");
		btnSend.setActionCommand("Send");
		btnCreate.setActionCommand("Create");
		MyActionListener listener=new MyActionListener();
		btnSend.addActionListener(listener);
		btnCreate.addActionListener(listener);

		GriddedPanel mainPanel = new GriddedPanel();
		mainPanel.setBorder( new EmptyBorder( new Insets( 2, 2, 2, 2 ) ) );
    	mainPanel.addComponent(lbl1,		0,0,5,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
    	mainPanel.addComponent(lbl2,		0,5,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
    	mainPanel.addComponent(lbl3,		0,6,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
    	mainPanel.addComponent(sclMessages,	1,0,5,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
    	mainPanel.addComponent(sclRooms,	1,5,1,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
    	mainPanel.addComponent(sclUsers,	1,6,1,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
    	mainPanel.addComponent(txtSend,		4,0,4,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
    	mainPanel.addComponent(btnSend,		4,4,1,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
    	mainPanel.addComponent(btnCreate,	4,5,2,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
		getContentPane().add( BorderLayout.CENTER, mainPanel );
		WindowListener wndCloser = new WindowAdapter()
		{
			public void	windowClosing(WindowEvent e)
			{
				JFrame f=(JFrame)e.getSource();
				f.dispose();
				active=false;
				output.println("o "+myName);
				System.exit(1);
			}
		};
		addWindowListener( wndCloser );
		pack();
		setLocation(500,250);
		setVisible(true);
		myName = JOptionPane.showInputDialog(null,"Enter your Name:","Vish Chat!",JOptionPane.QUESTION_MESSAGE);
		if (myName==null || myName.equals(""))
			myName="Anonymous";
		output.println("i "+myName);
		setTitle("Vish Chat! - "+myName+" currently not in any room");
		active=true;
		Thread readThread=new Thread(this);
		readThread.start();
	}

	public void run()
	{
		try
		{
			readLoop();
			input.close();
			output.close();
			socket.close();
			System.exit(0);
		}
		catch(IOException e)
		{
			System.out.println("Abnormal chat client socket condition:"+e);;
		}

	}

	public void readLoop() throws IOException
	{
		while(active)
		{
			String line=input.readLine();
			if (line==null)
				continue;
			System.out.println("Client Received:"+line);
			StringTokenizer t;
			switch(line.charAt(0))
			{
				case 'i': // a user logged in
					if (!myName.equals(line.substring(2)))
					{
						JOptionPane.showMessageDialog(null,line.substring(2)+" logged in","Vish Chat! - "+myName,JOptionPane.INFORMATION_MESSAGE);
						onlineUsers.addElement(line.substring(2));
					}
					break;
				case 'o': // a user logged out
					for(int i=0;i<onlineUsers.size();i++)
						if (((String)onlineUsers.elementAt(i)).equals(line.substring(2)))
						{
							onlineUsers.removeElementAt(i);
							break;
						}
					JOptionPane.showMessageDialog(null,line.substring(2)+" logged out","Vish Chat! - "+myName,JOptionPane.INFORMATION_MESSAGE);
					break;
				case 'm': // new message
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					int roomid=Integer.parseInt(t.nextToken());
					for(int i=0;i<myRooms.size();i++)
						if (((Room)myRooms.elementAt(i)).roomId == roomid)
						{
							((Room)myRooms.elementAt(i)).message+= "\n"+t.nextToken("");
							break;
						}
					if (currentRoom!=null)
						txtMessages.setText(currentRoom.message);
					break;
				case 'v': // invitation
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					String host=t.nextToken();
					Room newRoom=new Room();
					newRoom.roomId=Integer.parseInt(t.nextToken());
					newRoom.roomName=t.nextToken();
					newRoom.message=host+" created the room\n";
					if (!host.equals(myName))
					{
						int n = JOptionPane.showConfirmDialog(null,host+" has invited you to the room with name \""+newRoom.roomName+"\". Do you want to enter?","Vish Chat! - "+myName,JOptionPane.YES_NO_OPTION);
						if (n!=0)
							continue;
					}
					myRooms.addElement(newRoom);
					break;
				case 'u': // user list
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					while (t.hasMoreTokens())
						onlineUsers.addElement(t.nextToken());
					break;
				default:
			}
		}
	}

	class ListListener implements ListSelectionListener
	{
		public void	valueChanged(ListSelectionEvent	e)	
		{
			Room roomToGo=(Room)lstRooms.getSelectedValue();
			currentRoom=roomToGo;
			txtMessages.setText(roomToGo.message);
			setTitle("Vish Chat! - "+myName+" in "+currentRoom.roomName);
		}
	}


	class Room
	{
		int roomId;
		String roomName;
		String message="";
		public String toString()
		{
			return roomName;
		}
	}
	
	
	class MyActionListener implements ActionListener
	{
		public void	actionPerformed(ActionEvent	e)	
		{
			if(e.getActionCommand().equals("Send"))
			{
				if (currentRoom==null)
				{
					JOptionPane.showMessageDialog(null,"You are currently not in any room. Create one or wait for another user to invite you.","Vish Chat! - "+myName,JOptionPane.INFORMATION_MESSAGE);					
					return;
				}
				output.println("m "+currentRoom.roomId+" "+myName+": "+txtSend.getText());
				System.out.println("Client sent: m "+currentRoom.roomId+" "+txtSend.getText());
				txtSend.setText("");
			}
			else if(e.getActionCommand().equals("Create"))
			{
				int[] selections=lstUsers.getSelectedIndices();
				if (selections.length==0)
				{
					JOptionPane.showMessageDialog(null,"Please select users in the room using Cntrl key.","Vish Chat! - "+myName,JOptionPane.ERROR_MESSAGE);					
					return;
				}
				String inviteMessage="";
				for(int i=0;i<selections.length;i++)
					inviteMessage += " "+(String)onlineUsers.elementAt(selections[i]);
				String roomName = JOptionPane.showInputDialog(null,"Enter name of the room:","Vish Chat! - "+myName,JOptionPane.QUESTION_MESSAGE);
				if (roomName==null)
					return;
				if (roomName=="")
					roomName="Unnamed";
				inviteMessage = "v "+roomName+" "+myName+inviteMessage;				
				output.println(inviteMessage);
				System.out.println("Client sent: "+inviteMessage);
			}
		}
	}
}




