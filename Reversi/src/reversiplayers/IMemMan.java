/**
 * @author Aryo Vollenweider
 * @version v0.2
 */
package reversiplayers;

import java.util.ArrayList;

public interface IMemMan<T>{
	/**
	 * Creates a new node in the virtual memory, returning the id to it
	 * The object saved in it will be "random" data, use set() to replace it with desired object
	 * @return The id of the node to be used to access it later.
	 * @throws NoFreeNodesException Is thrown when all nodes in the memory are in use
	 */
	public int newNode() throws NoFreeNodesException;
	
	/**
	 * Creates a new node in the virtual memory, saving object in it
	 * @param object the object which will be saved in the new node
	 * @return The id of the node to be used to access it later.
	 * @throws NoFreeNodesException Is thrown when all nodes in the memory are in use
	 */
	public int newNode(T object) throws NoFreeNodesException;
	
	/**
	 * Sets a node as a child of another node, which will become a parent
	 * @param idOfParent id of node, which will become parent
	 * @param idOfChild if of node, which will become child
	 * @throws IllegalArgumentException idOfParent or idOfChild was invalid
	 */
	public void linkNodes(int idOfParent, int idOfChild) throws IllegalArgumentException;
	
	/**
	 * Attempts to set one node as head of a tree, all descendants of the head is safe from refreshMemory
	 * Also calls refreshMemory() if successful (Temporary)
	 * @param idOfHead id of Head
	 * @throws IllegalArgumentException idOfHead was invalid
	 */
	public void setHeadNode(int idOfHead) throws IllegalArgumentException;
	
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
	 * @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public void set(int idOfNode, T object) throws IllegalArgumentException;
	
	/**
	 * Returns the List of Children of a node
	 * @param idOfNode	id of the node, from which the children is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no children
	 * @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public ArrayList<Integer> getChildren(int idOfNode) throws IllegalArgumentException;
	
	/**
	 * Returns the List of all grandchildren (also all children of the children) of a node
	 * @param idOfNode	id of the node, from which the grandchildren is returned
	 * @return The requested list, is null if node doesn't exist, is empty ArrayList if node has no grandchildren
	 @throws IllegalArgumentException idOfNode was invalid, or Node doesnt exist
	 */
	public ArrayList<Integer> getGrandchildren(int idOfNode) throws IllegalArgumentException;
	
	/**
	 * Is thrown when all nodes in the memory are in use, and a new node is attempted to be created
	 * Define a new head and/or refresh memory to clear some space
	 */
	public static class NoFreeNodesException extends Exception{}
}