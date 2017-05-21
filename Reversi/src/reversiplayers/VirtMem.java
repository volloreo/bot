/**
 * @author Aryo Vollenweider
 * @version v0.2
 */
package reversiplayers;

import java.util.ArrayList;
import reversiplayers.IMemMan.NoFreeNodesException;

public class VirtMem<T> implements IMemMan<T>{
	private int cur_head = 0;
	private int curId = 0;	//id pointing at next free node
	private ArrayList<VirtMemNode<T>> mem;
	private final int max_length;
	private long time;
	private static final boolean DEBUG = false;
	
	//CONSTRUCTORS
	public VirtMem(){
		this(512);
	}
	public VirtMem(int init_size){
		max_length = init_size;
		mem = new ArrayList<VirtMemNode<T>>(max_length);
		for(int i = 0; i < max_length; i++)
			mem.add(new VirtMemNode<T>());
	}
	
	private void measureTime(String msg) {
		if (DEBUG) {
			System.out.println(msg + ( System.nanoTime() - time) + " NS");
		}
	}
	
	//PUBLIC FUNCTIONS
	
	/**
	 * Creates a new node in the virtual memory, returning the id to it
	 * The object saved in it will be "random" data, use set() to replace it with desired object
	 * @return The id of the node to be used to access it later.
	 * @throws NoFreeNodesException Is thrown when all nodes in the memory are in use
	 */
	public int newNode() throws NoFreeNodesException{
		//See if node at cur_id is free to overwrite
		int start_cur_id = curId;
		while(mem.get(curId).IN_USE){
			curId = iter(curId);
			//This is only true if cur_id has looped through the entire memory and hasn't found a free node -> memory full
			if(curId == start_cur_id)
				throw new NoFreeNodesException();
		}
		mem.get(curId).IN_USE = true;
		mem.get(curId).children.clear();
		int newi = curId;
		curId = iter(curId);
		return newi;
	}
	/**
	 * Creates a new node in the virtual memory, saving object in it
	 * @param object the object which will be saved in the new node
	 * @return The id of the node to be used to access it later.
	 * @throws NoFreeNodesException Is thrown when all nodes in the memory are in use
	 */
	public int newNode(T object) throws NoFreeNodesException{
		time = System.nanoTime();
		int newi = newNode();
		if(newi >= 0)
			mem.get(newi).object = object;
		measureTime("new Node took: " );
		return newi;
	}
	/**
	 * Sets a node as a child of another node, which will become a parent
	 * @param idOfParent id of node, which will become parent
	 * @param idOfChild if of node, which will become child
	 * @throws IllegalArgumentException idOfParent or idOfChild was invalid
	 */
	public void linkNodes(int idOfParent, int idOfChild) throws IllegalArgumentException{

		time = System.nanoTime();
		if(idOfParent < 0 || idOfParent >= max_length || idOfChild < 0 || idOfChild >= max_length)
			throw new IllegalArgumentException();
		mem.get(idOfParent).children.add(idOfChild);
		//Child obviously in use, this could be used to raise nodes "from the dead" -> not intended
		if(mem.get(idOfParent).IN_USE)
			mem.get(idOfChild).IN_USE = true;
		measureTime("Link nodes took: " );
	}
	/**
	 * Attempts to set one node as head of a tree, all descendants of the head is safe from refreshMemory
	 * Also calls refreshMemory() if successful (Temporary)
	 * @param idOfHead id of Head
	 * @throws IllegalArgumentException idOfHead was invalid
	 */
	public void setHeadNode(int idOfHead) throws IllegalArgumentException{

		time = System.nanoTime();
		if(idOfHead < 0 || idOfHead >= max_length)
			throw new IllegalArgumentException();
		cur_head = idOfHead;
		refreshMemory();
		measureTime("setHeadNode took: ");
	}
	/**
	 * Makes all nodes not part of the tree of head node inaccessible and overwriteable
	 * Accessing deleted nodes will lead to unexpected behaviour
	 */
	public void refreshMemory(){
		for (VirtMemNode<T> node : mem) {
			node.IN_USE = false;
		}
		/*
		for(int i = 0; i < max_length; i++){
			mem.get(i).IN_USE = false;
		}*/
		//Now set head and all its children to true
		set_parent_children_true(cur_head);
		
	}
	/**
	 * Gets object from node
	 * @param idOfNode id of Node, from which object will be retrieved
	 * @return requested object, null if node doesn't exist or idOfNode invalid
	 */
	public T get(int idOfNode){
		time = System.nanoTime();
		if(idOfNode < 0 || idOfNode >= max_length)
			return null;
		T obj =  mem.get(idOfNode).object;
		measureTime("Get Took:" );
		return obj;
	}
	/**
	 * Replaces the object in a node
	 * @param idOfNode id of node, in which the object will be replaced
	 * @param object the object to replace the old object in the node
	 * @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public void set(int idOfNode, T object) throws IllegalArgumentException{
		time = System.nanoTime();
		if(idOfNode < 0 || idOfNode >= max_length)
			throw new IllegalArgumentException();
		//if(mem.get(idOfNode) == null || mem.get(idOfNode).IN_USE)
		//	throw new IllegalArgumentException();
		else {
			mem.get(idOfNode).object = object;
		}

		measureTime("Set Took" );
	}
	/**
	 * Returns the List of Children of a node
	 * @param idOfNode	id of the node, from which the children is returned
	 * @return The requested list, is empty ArrayList if node has no children
	 * @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public ArrayList<Integer> getChildren(int idOfNode) throws IllegalArgumentException{
		if(idOfNode < 0 || idOfNode >= max_length)
			throw new IllegalArgumentException();
		/*if(mem.get(idOfNode)==null)
			return null;*/
		else
			return mem.get(idOfNode).children;
	}
	/**
	 * Returns the List of all grandchildren (also all children of the children) of a node
	 * @param idOfNode	id of the node, from which the grandchildren is returned
	 * @return The requested list, is empty ArrayList if node has no grandchildren
	 * @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public ArrayList<Integer> getGrandchildren(int idOfNode) throws IllegalArgumentException{
		time = System.nanoTime();
		if(idOfNode < 0 || idOfNode >= max_length)
			throw new IllegalArgumentException();
		ArrayList<Integer> grand_children = new ArrayList<Integer>();
		for(int i = 0; i < mem.get(idOfNode).children.size(); i++)
			grand_children.addAll(getChildren(mem.get(idOfNode).children.get(i)));
		measureTime("GetGrandchildren took:");
		return grand_children;
	}
	
	//public static class NoFreeNodesException extends Exception{}
	
	//PRIVATE FUNCTIONS
	private void set_parent_children_true(int id_parent){
		VirtMemNode<T> node = mem.get(id_parent);
		node.IN_USE = true;
		
		for(int i = 0; i < node.children.size(); i++){
			set_parent_children_true(node.children.get(i));
		}
	}
	private int iter(int i){
		i++;
		if(i >= max_length)
			return 0;
		else
			return i;
	}
	public static class VirtMemNode<T>{
		public boolean IN_USE;
		public T object;
		public ArrayList<Integer> children;
		
		public VirtMemNode(T thing){
			IN_USE = true;
			object = thing;
			children = new ArrayList<Integer>();
		}
		//Use only for initializing of mem -> all nodes "empty"
		public VirtMemNode(){
			IN_USE = false;
			object = null;
			children = new ArrayList<Integer>();
		}
	}
}

