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

public class PriorityQueue<E extends Comparable<? super E>> {
	private ArrayList<E> con;
	private int size;
	
	/**
	 * zero argument constructor for PQ
	 */
	public PriorityQueue() {
		con = new ArrayList<>();
		size = 0;
	}
	
	/** 
	 * method to enqueue (add) element to PQ based on value
	 * @param item to be added to PQ
	 * @return if item was added to PQ (always true)
	 */
	public boolean enqueue(E item) {
		if (item == null) {
			throw new IllegalArgumentException("Violation of preconditions for enqueue.");
		}
		int oldSize = size;
		addElement(item);
		return oldSize != size;
	}
	
	/**
	 * method to dequeue element at front of PQ
	 * @return element dequeued
	 */
	public E dequeue() {
		if (con.size() <= 0) {
			throw new IllegalArgumentException("Violation of preconditions for dequeue(). "
					+ "Size must be >0");
		}
		return con.remove(0);
	}
	
	/**
	 * method to add element to PQ based on compareTo() method
	 * @param <T>
	 * @param item item to be added to PQ
	 */
	private <T> void addElement(E item) {
		boolean placed = false;
			for (int i = 0; i < con.size(); i++) {
				if (item.compareTo(con.get(i)) < 0) {
					con.add(i, item);
					placed = true;
					i = con.size();
				} else if (item.compareTo(con.get(i)) == 0) {
					placed = breakTie(i++, item);
					i = con.size();
				}
			}
		if (!placed) {
			con.add(item);
		}
	}
	
	/**
	 * method to break ties in the PQ
	 * @param <T> given data type that implements comparable
	 * @param index of item
	 * @param item to be compared
	 * @return boolean if tie was broken or not
	 */
	private <T> boolean breakTie(int index, E item) {
		while (index < con.size()) {
			if (item.compareTo(con.get(index)) < 0) {
				con.add(index, item);
				return true;
			}
			index++;
		}
		return false;
	}
	
	// returns size of PQ
	public int size() {
		return con.size();
	}
	
	/**
	 * method to convert PQ to string, for testing purposes
	 * @return string of PQ
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < con.size(); i++) {
			if (i != con.size() - 1) {
				sb.append(con.get(i));
				sb.append(", ");
			} else {
				sb.append(con.get(i));
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
