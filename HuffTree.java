/*  Student information for assignment:
 *
 *  On MY honor, Jack Walker, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: JAW6756
 *  email address: jackwalker@utexas.edu
 *  Grader name: Elizabeth Luu
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class HuffTree {
	private final TreeNode root;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final int LAST = 1;
	private int size;
	private int numLeaves;
	
	/**
	 * single argument constructor for HuffTree
	 * @param root the root of the tree
	 */
	public HuffTree(TreeNode root) {
		this.root = root;
		sizeHelp(root);
		numLeavesHelper(root);
	}
	
	/**
	 * main method to print out tree
	 * adapted from Mike's printTree in a9 - TreeNode
	 */
	public void printTree() {
        printTree(root, "");
    }
	
	/**
	 * method to return the root of the HuffTree
	 * @return the root of the tree
	 */
	public TreeNode getRoot() {
		return root;
	}

	// print tree method from Mike's TreeNode class from a9
	private void printTree(TreeNode n, String spaces) {
		if (n != null) {
			printTree(n.getRight(), spaces + "  ");
			System.out.println(spaces + n.getValue());
			printTree(n.getLeft(), spaces + "  ");
		}
	}
	
	/**
	 * method to return the number of leaf nodes in HuffTree
	 * @return num leaves in HuffTree
	 */
	public int numLeaves() {
		return numLeaves;
	}
	
	/**
	 * recurive method to find number of leaf nodes in tree
	 * @param n current Node
	 */
	private void numLeavesHelper(TreeNode n) {
		if (n != null) {
			numLeavesHelper(n.getLeft());
			if (n.isLeaf()) {
				numLeaves++;
			}
			numLeavesHelper(n.getRight());
		}
	}
	
	/**
	 * method to return the size of the tree
	 * @return size of tree
	 */
	public int size() {
		return size;
	}
	
	/**
	 * recursive helper method to find size of tree
	 * @param n current Node
	 */
	private void sizeHelp(TreeNode n) {
		if (n != null) {
			sizeHelp(n.getLeft());
			size++;
			sizeHelp(n.getRight());
		}
	}
	
	/**
	 * method to build new map of new codes for huffman coding
	 * @return a new map of new huff codes
	 */
	public Map<Integer, String> getNewCodes() {
		Map<Integer, String> result = new TreeMap<>();
		StringBuilder sb = new StringBuilder();
		getNewHelper(result, root, sb);
		return result;
	}
	
	/**
	 * recursive helper method to find new codes from huff tree
	 * @param map
	 * @param n
	 * @param code
	 */
	private void getNewHelper(Map<Integer, String> map, TreeNode n, StringBuilder code) {
		if (n != null) {
			getNewHelper(map, n.getLeft(), code.append(LEFT));
			code.setLength(code.length() - LAST);
			if (n.isLeaf()) {
				map.put(n.getValue(), code.toString());
			}
			getNewHelper(map, n.getRight(), code.append(RIGHT));
			code.setLength(code.length() - LAST);
		}
	}
	
	/**
	 * method to get pre order traversal of tree in AL
	 * @return new arrayList of integers that is a pre order traversal of tree
	 */
	public ArrayList<Integer> preOrderTrav() {
		ArrayList<Integer> result  = new ArrayList<>();
		preOrderHelp(root, result);
		return result;
	}
	
	/**
	 * recursive helper method to get pre order traversal of tree
	 * @param n
	 * @param result
	 */
	private void preOrderHelp(TreeNode n, ArrayList<Integer> result) {
		if (n != null) {
			if (n.isLeaf()) {
				result.add(1);
				result.add(n.getValue());
			} else if (!n.isLeaf()) {
				result.add(0);
			}
			preOrderHelp(n.getLeft(), result);
			preOrderHelp(n.getRight(), result);
		}
	}
}