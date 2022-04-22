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

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private Compressor preCompress;
    private int numLeaves;
    private HuffTree hf;
    private Map<Integer, String> newCodes;
    private Map<Integer, Integer> freqs;
    private int totalNewBits;
    private int diff;
    private int headerFormat;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
    	Compressor preCompress = new Compressor(in);
    	this.preCompress = preCompress;
    	PriorityQueue<TreeNode> freqs = preCompress.readFile();
    	Map<Integer, Integer> mapFreqs = preCompress.freqs;
    	this.freqs = mapFreqs;
    	this.headerFormat = headerFormat;
    	HuffTree tree = new HuffTree(freqs.dequeue());
    	Map<Integer, String> newCodes = tree.getNewCodes();
    	this.newCodes = newCodes;
    	this.numLeaves = tree.numLeaves();
    	this.hf = tree;
    	int huffCodeLen = preCompress.getNewCodesSize(newCodes, mapFreqs);
    	totalNewBits = preCompress.getTotalNewSize(newCodes.size(), tree.size(), huffCodeLen, 
    			headerFormat);
        showString("Frequencies and codes for file:\n");
        for (int i : newCodes.keySet()) {
        	if (i == IHuffConstants.PSEUDO_EOF) {
        		myViewer.update("Value: 256, value as char: This is a Pseudo-EOF character,"
        				+ " frequency: 1, new code: " + newCodes.get(i));
        	} else {
	        	myViewer.update("Value: " + i + ", value as char: " + (char) i + ", frequency: " + 
	        			mapFreqs.get(i) + ", new code: " + newCodes.get(i));
        	}
        }
        this.diff = preCompress.getOldSize(mapFreqs) - totalNewBits;
    	return diff;
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	Compressor outCompress = new Compressor(out);
    	if (preCompress == null) {
    		throw new IllegalStateException("PreProcessCompress must "
    				+ "be called before Compression.");
    	}
    	if (diff < 0) {
    		myViewer.showError("Compressed file has " + diff/-1 + " more bits than"
    				+ " uncompressed file. Select \"force compression\" option to compress");
    		if (force) {
    			outCompress.compressFile(in, numLeaves, hf, newCodes, headerFormat, freqs);
    		}
    	} else {
    		outCompress.compressFile(in, numLeaves, hf, newCodes, headerFormat, freqs);
    	}
    	int extraPad = totalNewBits % 8;
    	while (extraPad != 0) {
    		totalNewBits += extraPad;
    		extraPad = totalNewBits%8;
    	}
    	totalNewBits += extraPad;
    	myViewer.update("\nTotal number of bits written: " + totalNewBits);
        return totalNewBits;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
	        Decompressor deCom = new Decompressor();
	        BitInputStream bitIn = new BitInputStream(in);
	        boolean magic = deCom.magicNumber(bitIn);
	        if (!magic) {
	        	myViewer.showError("Error when reading compressed file.\n"
	        			+ "File does not start with Magic Number.");
	        	return -1;
	        } else if (magic) {
	        	int headerFormat = deCom.getHeader(bitIn);
	        	if (headerFormat == -1) {
	        		myViewer.showError("Error when reading compressed file.\n"
	        				+ "File does not have proper header format indicate.");
	        		return -1;
	        	} if (headerFormat == STORE_COUNTS) {
	        		hf = deCom.readStandard(bitIn);
	        	} else if (headerFormat == STORE_TREE) {
	        		hf = deCom.readTreeHeader(bitIn);
	        	}
	        	boolean goodFile = deCom.processAndWrite(hf, bitIn, out);
	        	if (!goodFile) {
	        		myViewer.showError("Error when writing file.\n"
	        				+ "Compress data not written properly. No PEOF.");
	        		return -1;
	        	} else {
	        		int bitsWritten = deCom.getWrittenBits();
	        		myViewer.showMessage("Total number of bits written: " + bitsWritten);
	        	}
	        }
	        return -1;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }



    private void showString(String s){
        if(myViewer != null)
            myViewer.update(s);
    }
}
