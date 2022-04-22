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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Compressor {
	private InputStream in;
	private OutputStream out;
	public int oldSize;
	public Map<Integer, Integer> freqs;

	/**
	 * single argument constructor to construct a Compressor with an inputStream
	 * @param in must not be null
	 */
	public Compressor(InputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("Violation of preconditions for Compressor()."
					+ " in must not be null.");
		}
		this.in = in;
	}
	
	///
	/**
	 * single argument constructor to construct a Compressor with an outputStream
	 * @param out must not be null
	 */
	public Compressor(OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException("Violation of preconditions for Compressor()."
					+ " out must not be null.");
		}
		this.out = out;
	}
	
	/**
	 * method to make new priorityQueue with singular treeNode (root)
	 * @return a new priorityQueue of treeNodes with frequencies and values from data
	 * @throws IOException
	 */
	public PriorityQueue<TreeNode> readFile() throws IOException {
		Map<Integer, Integer> freqs = makeMap();
		this.freqs = freqs;
		PriorityQueue<TreeNode> result = makePQ(freqs);
		TreeNode PEOF = new TreeNode(IHuffConstants.PSEUDO_EOF, 1);
		result.enqueue(PEOF);
		result = makeTree(result);
		return result;
	}
	
	/**
	 * method to make new map with (key) frequencies and (value) values
	 * @return a new map with frequencies mapped with values from data
	 * @throws IOException
	 */
	public Map<Integer, Integer> makeMap() throws IOException {
		Map<Integer, Integer> freq = new TreeMap<>();
		int bit = 0;
		boolean read = false;
		BitInputStream bitReader = new BitInputStream(in);
		while (!read) {
			bit = bitReader.readBits(IHuffConstants.BITS_PER_WORD);
			if (bit != -1) {
				if (freq.containsKey(bit)) {
					freq.put(bit, freq.get(bit) + 1);
				} else {
					freq.put(bit, 1);
				}
			} else {
				read = true;
			}	
		}
		bitReader.close();
		return freq;
	}
	
	/**
	 * method to make new pq with frequencies and values sorted in ascending order
	 * @param freqs must not be null
	 * @return a new priorityQueue of type treeNode with
	 */
	private PriorityQueue<TreeNode> makePQ(Map<Integer, Integer> freqs) {
		if (freqs == null) {
			throw new IllegalArgumentException("Violation of preconditions for makePQ(). "
					+ "freqs must not be null.");
		}
		PriorityQueue<TreeNode> result = new PriorityQueue<>();
		for (Integer val: freqs.keySet()) {
			TreeNode temp = new TreeNode(val, freqs.get(val));
			result.enqueue(temp);
		}
		return result;
	}
	
	///
	/**
	 * method to make new pq with singular tree node (root)
	 * @param pq must not be null
	 * @return new pq with singular tree node
	 */
	private PriorityQueue<TreeNode> makeTree(PriorityQueue<TreeNode> pq) {
		if (pq == null) {
			throw new IllegalArgumentException("Violation of preconditions for makeTree(). "
					+ "pq must not be null.");
		}
		while (pq.size() > 1) {
			TreeNode left = pq.dequeue();
			TreeNode right = pq.dequeue();
			TreeNode root = new TreeNode(left, left.getFrequency() + right.getFrequency(), 
					right);
			pq.enqueue(root);
		}
		return pq;
	}

	
	///
	/**
	 * overall method to compress the file
	 * @param in must not be null
	 * @param numNodes must be >0
	 * @param hf must not be null
	 * @param map must not be null
	 * @param headerFormat must not be null
	 * @param freqs must not be null
	 * @throws IOException
	 */
	public void compressFile(InputStream in, int numNodes, HuffTree hf, 
			Map<Integer, String> map, int headerFormat, Map<Integer, Integer> freqs) 
			throws IOException {
		if (in == null || numNodes < 0 || hf == null || map == null || freqs == null) {
			throw new IllegalArgumentException("Violation of preconditions for compressFile(). "
					+ "freqs, in, numNodes, hf, and map must not be null.");
		}
		BitOutputStream bitOut = new BitOutputStream(out);
		bitOut.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
		if (headerFormat == IHuffConstants.STORE_TREE) {
			treeHeader(numNodes, hf, bitOut);
		} else if (headerFormat == IHuffConstants.STORE_COUNTS) {
			countsHeader(bitOut, freqs);
		}
		writeData(map, in, bitOut);
		bitOut.close();
	}
	
	///
	/**
	 * writes header for Store count header
	 * @param bitOut must not be null
	 * @param freqs must not be null
	 */
	private void countsHeader(BitOutputStream bitOut, Map<Integer, Integer> freqs) {
		if (freqs == null || bitOut == null) {
			throw new IllegalArgumentException("Violation of preconditions for countsHeader(). "
					+ "freqs and bitOut must not be null.");
		}
		int[] counts = countsToArray(freqs);
		bitOut.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			bitOut.writeBits(IHuffConstants.BITS_PER_INT, counts[i]);
		}
	}
	
	/**
	 * method to convert map of
	 * @param freqs must not be null
	 * @return new int[] to be put in the count header
	 */
	private int[] countsToArray(Map<Integer, Integer> freqs) {
		if (freqs == null) {
			throw new IllegalArgumentException("Violation of preconditions for countsToArray(). "
					+ "freqs must not be null.");
		}
		int[] counts = new int[IHuffConstants.ALPH_SIZE];
		for (int val : freqs.keySet()) {
			counts[val] = freqs.get(val);
		}
		return counts;
	}
	
	/**
	 * method to write treeHeader in huff file
	 * @param numNodes must >0
	 * @param hf must not be null
	 * @param bitOut must not be null
	 * @throws IOException
	 */
	private void treeHeader(int numNodes, HuffTree hf, BitOutputStream bitOut) throws IOException {
		if (bitOut == null || hf == null || numNodes < 0) {
			throw new IllegalArgumentException("Violation of preconditions for makePQ(). "
					+ "freqs and hf must not be null. numNodes must be <0");
		}
		bitOut.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_TREE);
		bitOut.writeBits(IHuffConstants.BITS_PER_INT, (numNodes * 9) + hf.size());
		treeHeaderTree(bitOut, hf);
	} 
	
	/**
	 * method to write the tree part of tree header
	 * @param bitOut must not be null
	 * @param hf must not be null
	 * @throws IOException
	 */
	private void treeHeaderTree(BitOutputStream bitOut, HuffTree hf) throws IOException {
		if (bitOut == null || hf == null) {
			throw new IllegalArgumentException("Violation of preconditions for makePQ(). "
					+ "freqs must not be null.");
		}
		ArrayList<Integer> preOrder = hf.preOrderTrav();
		for (int i: preOrder) {
			if (i != 0 && i != 1) {
				bitOut.writeBits(IHuffConstants.BITS_PER_WORD + 1, i);
			} else {
				bitOut.writeBits(1, i);
			}
		}
	}
	 
	/**
	 * @param numNodes must be >0
	 * @param treeSize must be >0
	 * @param dataSize must be >0
	 * @param headerFormat must be >0
	 * @return
	 */
	public int getTotalNewSize(int numNodes, int treeSize, int dataSize, int headerFormat) {
		if (numNodes < 0 || treeSize < 0 || dataSize < 0) {
			throw new IllegalArgumentException("Violation of preconditions for getTotalNewSize()."
					+ " numNodes, treeSize, dataSize, and headerFormat must be > 0.");
		}
		int totalNew = 0;
		if (headerFormat == IHuffConstants.STORE_TREE) {
		totalNew += IHuffConstants.BITS_PER_INT * 3;
		totalNew += ((numNodes * 9) + treeSize);
		totalNew += dataSize;
		} else if (headerFormat == IHuffConstants.STORE_COUNTS) {
			totalNew += IHuffConstants.BITS_PER_INT * 258 + dataSize;
		}
		return totalNew;
	}
	
	/**
	 * method to find the size of the new huff code to be written
	 * @param newCodes must not be null
	 * @param freqs must not be null
	 * @return number of bits going to writen in huff code
	 */
	public int getNewCodesSize(Map<Integer, String> newCodes, Map<Integer, Integer> freqs) {
		if (freqs == null || newCodes == null) {
			throw new IllegalArgumentException("Violation of preconditions for getNewCodesSize()."
					+ " freqs and newCodes must not be null.");
		}
		int size = 0;
		for (int val : newCodes.keySet()) {
			if (val != IHuffConstants.PSEUDO_EOF) {
				for (int i = 0; i < freqs.get(val); i++) {
					size += newCodes.get(val).length();
				}
			}
		}
		size += newCodes.get(IHuffConstants.PSEUDO_EOF).length();
		return size;
	}
	
	/**
	 * method to write the huff codes to the file
	 * @param map must not be null
	 * @param in must not be null
	 * @param bitOut must not be null
	 * @throws IOException
	 */
	public void writeData(Map<Integer, String> map, InputStream in, 
			BitOutputStream bitOut) throws IOException {
		if (map == null || in == null || bitOut == null) {
			throw new IllegalArgumentException("Violation of preconditions for writeData(). "
					+ "map, in, and bitOut must not be null.");
		}
		boolean read = false;
		BitInputStream bit = new BitInputStream(in);
		int bits;
		while (!read) {
			bits = bit.readBits(IHuffConstants.BITS_PER_WORD);
			if (bits != -1) {
				String newCode = map.get(bits);
				for (int i = 0; i < newCode.length(); i++) {
					bitOut.writeBits(1, (int) newCode.charAt(i));
				}
			} else if (bits == -1) {
				read = true;
			}
		}
		String PEOF = map.get(IHuffConstants.PSEUDO_EOF);
		for (int i = 0; i < PEOF.length(); i++) {
			bitOut.writeBits(1, (int) PEOF.charAt(i));
		}
		bit.close();
	}

	/**
	 * method to find the size of the original file
	 * @param freqs must not be null
	 * @return number of bits in the original file
	 */
	public int getOldSize(Map<Integer, Integer> freqs) {
		if (freqs == null) {
			throw new IllegalArgumentException("Violation of preconditions for getOldSize()."
					+ "freqs must not be null.");
		}
		int oldSize = 0;
		for (int x : freqs.keySet()) {
			oldSize += IHuffConstants.BITS_PER_WORD * freqs.get(x);
		}
		return oldSize;
	}
}
