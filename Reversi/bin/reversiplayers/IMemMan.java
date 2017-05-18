/**
 * @author Aryo Vollenweider
 * @version v0.1
 */
package reversiplayers;

import java.util.ArrayList;

public interface IMemMan<T>{
	/**
	 * Creates a new node in the virtual memory, returning the id to it
	 * The object saved in it will be "random" data, use set() to replace it with desired object
	 * @return The id of the node to be used to access it later. -1 IF NODE COULD NOT BE CREATED DUE TO OUTOFMEMORY
	 */
	public int newNode();
	
	/**
	 * Creates a new node in the virtual memory, saving object in it
	 * @param object the object which will be saved in the new node
	 * @return The id of the node to be used to access it later. -1 IF NODE COULD NOT BE CREATED DUE TO OUTOFMEMORY
	 */
	public int newNode(T object);
	
	/**
	 * Sets a node as a child of another node, which will become a parent
	 * @param idOfParent id of node, which will become parent
	 * @param idOfChild if of node, which will become child
	 * @return true if link success, false if not (idOfParent or idOfChild was invalid)
	 */
	public boolean linkNodes(int idOfParent, int idOfChild);
	
	/**
	 * Attempts to set one node as head of a tree, all descendants of the head is safe from refreshMemory
	 * Also calls refreshMemory() if successful (Temporary)
	 * @param idOfHead id of Head
	 * @return true if set success, false if not (idOfHead was invalid)
	 */
	public boolean setHeadNode(int idOfHead);
	
	/**
	 * Makes all nodes not part of the tree of head node "inaccessible" and overwriteable
	 * Accessing deleted nodes will lead to unexpected behaviour and will return garbage data
	 */
	public void refreshMemory();
	
	/**
	 * Gets object from node
	 * @param idOfNode id of Node, from which object will be retrieved
	 * @return requested object, null if node doesn't exist or idOfNode invalid
	 */
	public T get(int idOfNode);
	
	/**
	 * Replaces the object in a node
	 * @param idOfNode id of node, in which the object will be replaced
	 * @param object the object to replace the old object in the node
	 * @return false if node doesn't exist OR node is still in use, true if replacement successfull
	 */
	public Boolean set(int idOfNode, T object);
	
	/**
	 * Returns the List of Children of a node
	 * @param idOfNode	id of the node, from which the children is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no children
	 */
	public ArrayList<Integer> getChildren(int idOfNode);
	
	/**
	 * Returns the List of all grandchildren (also all children of the children) of a node
	 * @param idOfNode	id of the node, from which the grandchildren is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no grandchildren
	 */
	public ArrayList<Integer> getGrandchildren(int idOfNode);
}