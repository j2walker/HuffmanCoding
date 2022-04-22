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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public class Decompressor implements IHuffConstants{
	// private instance variable to make sure the magic number was checked
	private boolean magicChecked;
	// private instance variable to store what headerFormat is being used
	private int headerFormat;
	//private instance variable to store the new tree size
	private int treeSize;
	// private instance variable to help the recursive method to build the tree
	private int count;
	// private instance varaible to track number of bits written to new file
	private int writtenBits;
	
	
	/**
	 * zero argument constructor for Decompressor
	 * returns and does nothing
	 */
	public Decompressor() {

	}
	
	/**
	 * method to check if magic number is present in huffed file
	 * @param bitIn - the bitInputStream that the method will use, must not be null
	 * @return - true if magic number present, false otherwise
	 * @throws IOException
	 */
	public boolean magicNumber(BitInputStream bitIn) throws IOException {
		if (bitIn == null) {
			throw new IllegalArgumentException("Violation of preconditions for magicNumber(). "
					+ "bitIn must noe be null.");
		}
		int num = bitIn.readBits(IHuffConstants.BITS_PER_INT);
		bitIn.close();
		if (num != IHuffConstants.MAGIC_NUMBER) {
			return false;
		} else {
			magicChecked = true;
			return true;
		}
		
	}
	/**
	 * method to check what type of header is used in huffed file
	 * @param bitIn must not be null
	 * @return the number of the header type
	 * @throws IOException
	 */
	public int getHeader(BitInputStream bitIn) throws IOException {
		if (!magicChecked) {
			throw new IllegalStateException("magicNumber() must be checked before getHeader().");
		} else if (bitIn == null) {
			throw new IllegalArgumentException("Violation of preconditions for getHeader()."
					+ " bitIn must not be null.");
		}
		headerFormat = bitIn.readBits(IHuffConstants.BITS_PER_INT);
		int tree = STORE_TREE;
		int count = STORE_COUNTS;
		if (headerFormat != tree && headerFormat != count) {
			bitIn.close();
			return -1;
		}
		bitIn.close();
		return headerFormat;
	}
	
	/**
	 * method to build huffTree from given header info if tree Header
	 * @param bitIn must not be null
	 * @return a new huffTree with the given values from huffed file
	 * @throws IOException
	 */
	public HuffTree readTreeHeader(BitInputStream bitIn) throws IOException {
		if (bitIn == null) {
			throw new IllegalArgumentException("Violation of preconditions for readTreeHeader()."
					+ " bitIn must not be null.");
		}
		this.treeSize = bitIn.readBits(BITS_PER_INT);
		TreeNode root = readTreeHelper(bitIn);
		return new HuffTree(root);
		}
	
	/**
	 * method to build huffTree from given data in huffed file
	 * @param bitIn must not be null
	 * @return a new tree node with all other tree nodes connected from huff data
	 * @throws IOException
	 */
	private TreeNode readTreeHelper(BitInputStream bitIn) throws IOException {
		if (bitIn == null) {
			throw new IllegalArgumentException("Violation of preconditions for readTreeHelper()."
					+ " bitIn must not be null.");
		}
		if (count++ < treeSize) {
			int nextBit = bitIn.readBits(1);
			if (nextBit == 0) {
				TreeNode internal = new TreeNode(0,0);
				internal.setLeft(readTreeHelper(bitIn));
				internal.setRight(readTreeHelper(bitIn));
				return internal;
			} else if (nextBit == 1 ) {
				int val = bitIn.readBits(9);
				count += 9;
				TreeNode child = new TreeNode(val, 0);
				return child;
			} else {
				throw new IllegalStateException("Something is wrong with the comrpessed file.");
			}
		}
		else {
			return null;
		}
	}
	
	/**
	 * method to build tree from standard count header
	 * @param in must not be null
	 * @return a new HuffTree with given data from huffed file
	 * @throws IOException
	 */
	public HuffTree readStandard(InputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("Violation of preconditions for readStandard()."
					+ " in must not be null.");
		}
		int[] freqs = new int[ALPH_SIZE + 1];
		BitInputStream bitIn = new BitInputStream(in);
		for (int i = 0; i < freqs.length - 1; i++) {
			int oldFreq = bitIn.readBits(BITS_PER_INT);
			freqs[i] = oldFreq;
		}
		freqs[freqs.length - 1] = 1;
		Map<Integer, Integer> mapFreqs = makeMap(freqs);
		bitIn.close();
		return makeHTStandard(mapFreqs);
	}
	
	/**
	 * method to make new HuffTree from given data in huffed file, standard count
	 * @param freqs must not be null
	 * @return new huff tree with given frequencies
	 */
	private HuffTree makeHTStandard(Map<Integer, Integer> freqs) {
		if (freqs == null) {
			throw new IllegalArgumentException("Violation of preconditions for makeHTStandard()."
					+ " freqs must not be null.");
		}
		PriorityQueue<TreeNode> result = new PriorityQueue<>();
		for (Integer val: freqs.keySet()) {
			TreeNode temp = new TreeNode(val, freqs.get(val));
			result.enqueue(temp);
		}
		return makeTree(result);
	}
	
	/**
	 * method to build tree from priority queue for huffed file
	 * @param pq must not be null
	 * @return new HuffTree with given data in huffed file, for count header
	 */
	private HuffTree makeTree(PriorityQueue<TreeNode> pq) {
		if (pq == null) {
			throw new IllegalArgumentException("Violation of preconditions for makeTree()."
					+ " pq must not be null.");
		}
		while (pq.size() > 1) {
			TreeNode left = pq.dequeue();
			TreeNode right = pq.dequeue();
			TreeNode root = new TreeNode(left, left.getFrequency() + right.getFrequency(), 
					right);
			pq.enqueue(root);
		}
		return new HuffTree(pq.dequeue());
	}
	
	/**
	 * method to make map from frequencies from standard header
	 * @param freqs.length must be >0
	 * @return a new map with mapped frequencies to byte
	 */
	private Map<Integer, Integer> makeMap(int[] freqs){
		if (freqs.length < 0) {
			throw new IllegalArgumentException("Violation of preconditions for makeMap()."
					+ " freqs must not be null.");
		}
		Map<Integer, Integer> mapFreqs = new TreeMap<>();
		for (int i = 0; i < freqs.length; i++) {
			if (freqs[i] != 0) {
				mapFreqs.put(i, freqs[i]);
			}
		}
		return mapFreqs;
	}
	
	/**
	 * method to take in huffTree and write the original data back to file
	 * @param hf must not be null
	 * @param bitIn must not be null
	 * @param out must not be null
	 * @return true if writing was successful
	 * @throws IOException
	 */
	public boolean processAndWrite(HuffTree hf, BitInputStream bitIn, OutputStream out) throws IOException {
		if (hf == null || bitIn == null || out == null) {
			throw new IllegalArgumentException("Violation of preconditions for processAndWrite()."
					+ " bitIn, hf, and out must not be null.");
		}
		BitOutputStream bitOut = new BitOutputStream(out);
		TreeNode currentNode = hf.getRoot();
		boolean PEOFFound = false;
		while (!PEOFFound) {
			if (!currentNode.isLeaf()) {
				int bit = bitIn.readBits(1);
				if (bit == 0) {
					currentNode = currentNode.getLeft();
				} else if (bit == 1) {
					currentNode = currentNode.getRight();
				} else if (bit == -1) {
					bitOut.close();
					return false;
				}
			} else if (currentNode.isLeaf()) {
				if (currentNode.getValue() != PSEUDO_EOF) {
					bitOut.writeBits(BITS_PER_WORD, currentNode.getValue());
					writtenBits += BITS_PER_WORD;
					currentNode = hf.getRoot();
				} else {
					PEOFFound = true;
					bitOut.close();
					return true;
				}
			}
		}
		bitOut.close();
		return true;
	}
	
	/**
	 * method to return writtenBits
	 * @return number of bits written to file
	 */
	public int getWrittenBits() {
		return writtenBits;
	}
}