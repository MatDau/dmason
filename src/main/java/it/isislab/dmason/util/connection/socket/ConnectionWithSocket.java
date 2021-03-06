/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.util.connection.socket;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.MyHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;


/**	 
 * 	This is a Connection implementation using Socket.The communication philosophy is the same of the other implementations.
 * 	ConnectionWithSocket provides one socket connection for the publisher,and a map of socket for the subscribers.
 * 	Each subscriber has a thread context,a socket because of ,in this type of implementation , server recognize a subscriber
 * 	by his socket.For abstract the messaging paradigm, we have create a wrapper class PubSubMessage, divided in three part: 
 * 	command (like publish,subscribe, unsubscribe),targopic ,message.This class is used by the server to recognize if the message is a 
 * 	publish instruction,a subscribe instruction,an unsubscribe instruction. Subscribe and Unsubscribe operation obviously has
 * 	third message part empty.The class also trace a map of publishers , that contain for each topic a preconfigured PubSubMessage
 * 	object,in which the first part of message is complied with "publish" and the second part is compiled with "topicName" ,  
 * 	when createTopic method is invoked. So this should be execution flow :
 *  -1-setupConnection: for more abstract network address metadata we use a wrapper , Address , in which we can set ip address
 *  and port number. 
 *  -2-Publish Service: if we want to publish messages
 *  	-2.1-createTopic method must be invoked for each topic we create
 *  	-2.2-publishToTopic method must be invoked to really publish the message
 *  -3-Subscribe Service
 *  	-3.1-subscribeToTopic method must be invoked to register a socket connection to a topic on the server
 *  	-3.2-asynchronousReceive method must called and a thread starts,whose duty it is to listen on the designated socket.
 *
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class ConnectionWithSocket implements Connection{
	
	private Socket publisher=null;
	private ObjectOutputStream out=null;
	private ConcurrentHashMap<String,Socket> subscribers;
	private HashMap<String,PubSubMessage> publishers;
	private HashMap<String, MyHashMap> contObj;
	private String triggerTopicName = "trigger";
	private String destinationTriggerTopicName = "destinationTrigger";
	
	/**
	 * Constructor:initializes subscribers map and publishers map.
	 */
	public ConnectionWithSocket() {
		subscribers = new ConcurrentHashMap<String, Socket>();
		publishers = new HashMap<String, PubSubMessage>();
	}
	
	@Override
	/**
	 * It instantiates a Socket connection with the server at the given address and port number.
	 * It also open an OutputStream from the socket.
	 * 
	 */
	public boolean setupConnection(Address arg) throws Exception {
		try{
			publisher = new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(publisher.getOutputStream());
			return true;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	/** The method creates a prepared PubSubMessage with "publish" command and "arg" as topic name
	 * and it put the message on the publishers' topic indexed list. 
	 */
	public boolean createTopic(String arg, int numFields)  {
		publishers.put(arg, new PubSubMessage("publish", arg, ""));
		contObj.put(arg, new MyHashMap(numFields));
		return true;
	}
	
	@Override
	/**Publishes on a given topic the Serializable object, and encapsulates it in the PubSubMessage message. */
	public boolean publishToTopic(Serializable arg, String mytopic, String name) throws JMSException 
	{	
		if (!mytopic.equals("") || !(arg == null))
		{
			MyHashMap mh = contObj.get(mytopic);
			mh.put(name, arg);
			contObj.put(mytopic, mh);
			if(mh.isFull()){
				
				PubSubMessage msg = publishers.get(mytopic);
				msg.setPart3(contObj.get(mytopic));
				
				MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
				contObj.put(mytopic, mm);
				try{
					out.writeObject(msg);
					out.flush();
					return true;
				}catch (IOException e) {
					e.printStackTrace();
					return false;
				}	
			}
		}
		return false;
	}
	
	

	@Override
	/**
	 * For subscribing to a topic, application must create a Socket,connects the socket to the server
	 * and send a PubSub message containing a "subscribe" command.When we subscribe to a topic,
	 * the server maintains the socket in a map, for publish updates on "that topic".
	 */
	public boolean subscribeToTopic(String arg)throws Exception{
		try{
			Socket sock = new Socket("127.0.0.1",5555);
			ObjectOutputStream o = new ObjectOutputStream(sock.getOutputStream());
			PubSubMessage msg = new PubSubMessage("subscribe",arg, "");
			o.writeObject(msg);
			o.flush();
			subscribers.put(msg.getPart2(),sock);
			return true;
		}catch (IOException e) {
			throw new Exception();
		}
	}
	
	
	@Override
	/**
	 * The method creates a thread, that listens for updates on a given topic(so on a given socket!!!) 
	 */
	public boolean asynchronousReceive(String arg){
		receiver r = new receiver(subscribers.get(arg));
		r.start();
		return true;
	}
	
	/** Return the list of activate topics. */
	@Override
	public ArrayList<String> getTopicList() throws Exception {
		return null;
	}
	
	
	/**
	 * This thread perform the receiving action. 
	 * @author iAdy
	 *
	 */
	class receiver extends Thread{
		
		Socket socket ;
		ObjectInputStream in;
		
		public receiver(Socket socket) {
			super();
			try{
				this.socket = socket;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			super.run();
			try{
				in = new ObjectInputStream(socket.getInputStream());
				while(true){
					// Customize your code!!!
					in.readObject();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public boolean unsubscribe(String topicName) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
