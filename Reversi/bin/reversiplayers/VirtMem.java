/**
 * @author Aryo Vollenweider
 * @version v0.1
 */
package reversiplayers;

import java.util.ArrayList;

public class VirtMem<T> implements IMemMan<T>{
	private int cur_head = 0;
	private int curId = 0;	//id pointing at next free node
	private ArrayList<VirtMemNode<T>> mem;
	private final int max_length;
	
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
	
	//PUBLIC FUNCTIONS
	
	/**
	 * Creates a new node in the virtual memory, returning the id to it
	 * The object saved in it will be "random" data, use set() to replace it with desired object
	 * @return The id of the node to be used to access it later. -1 IF NODE COULD NOT BE CREATED DUE TO OUTOFMEMORY
	 */
	public int newNode(){
		//See if node at cur_id is free to overwrite
		int start_cur_id = curId;
		while(mem.get(curId).IN_USE){
			iter(curId);
			//This is only true if cur_id has looped through the entire memory and hasn't found a free node -> memory full
			if(curId == start_cur_id)
				return -1;
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
	 * @return The id of the node to be used to access it later. -1 IF NODE COULD NOT BE CREATED DUE TO OUTOFMEMORY
	 */
	public int newNode(T object){
		int newi = newNode();
		if(newi >= 0)
			mem.get(newi).object = object;
		return newi;
	}
	/**
	 * Sets a node as a child of another node, which will become a parent
	 * @param idOfParent id of node, which will become parent
	 * @param idOfChild if of node, which will become child
	 * @return true if link success, false if not (idOfParent or idOfChild was invalid)
	 */
	public boolean linkNodes(int idOfParent, int idOfChild){
		if(idOfParent < 0 || idOfParent >= max_length || idOfChild < 0 || idOfChild >= max_length)
			return false;
		mem.get(idOfParent).children.add(idOfChild);
		//Child obviously in use, this could be used to raise nodes "from the dead" -> not intended
		if(mem.get(idOfParent).IN_USE)
			mem.get(idOfChild).IN_USE = true;
		return true;
	}
	/**
	 * Attempts to set one node as head of a tree, all descendants of the head is safe from refreshMemory
	 * Also calls refreshMemory() if successful (Temporary)
	 * @param idOfHead id of Head
	 * @return true if set success, false if not (idOfHead was invalid)
	 */
	public boolean setHeadNode(int idOfHead){
		if(idOfHead < 0 || idOfHead >= max_length)
			return false;
		cur_head = idOfHead;
		refreshMemory();
		return true;
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
		if(idOfNode < 0 || idOfNode >= max_length)
			return null;
		return mem.get(idOfNode).object;
	}
	/**
	 * Replaces the object in a node
	 * @param idOfNode id of node, in which the object will be replaced
	 * @param object the object to replace the old object in the node
	 * @return false if node doesn't exist OR node is still in use, true if replacement successfull
	 */
	public Boolean set(int idOfNode, T object){
		if(idOfNode < 0 || idOfNode >= max_length)
			return false;
		if(mem.get(idOfNode) == null || mem.get(idOfNode).IN_USE)
			return false;
		else{
			mem.set(idOfNode, new VirtMemNode<T>(object));
			return true;
		}
	}
	/**
	 * Returns the List of Children of a node
	 * @param idOfNode	id of the node, from which the children is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no children
	 */
	public ArrayList<Integer> getChildren(int idOfNode){
		if(idOfNode < 0 || idOfNode >= max_length)
			return null;
		/*if(mem.get(idOfNode)==null)
			return null;*/
		else
			return mem.get(idOfNode).children;
	}
	/**
	 * Returns the List of all grandchildren (also all children of the children) of a node
	 * @param idOfNode	id of the node, from which the grandchildren is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no grandchildren
	 */
	public ArrayList<Integer> getGrandchildren(int idOfNode){
		if(idOfNode < 0 || idOfNode >= max_length)
			return null;
		ArrayList<Integer> grand_children = new ArrayList<Integer>();
		for(int i = 0; i < mem.get(idOfNode).children.size(); i++)
			grand_children.addAll(getChildren(mem.get(idOfNode).children.get(i)));
		return grand_children;
	}
	//PRIVATE FUNCTIONS
	private void set_parent_children_true(int id_parent){
		mem.get(id_parent).IN_USE = true;
		for(int i = 0; i < mem.get(id_parent).children.size(); i++){
			set_parent_children_true(mem.get(id_parent).children.get(i));
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

