/*  Student information for assignment:
 *
 *  On MY honor, Fulin Jiang, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1: Fulin Jiang
 *  UTEID: fj3279
 *  email address: fj3279@utexas.edu
 *  Grader name: Nina
 *
 */

import java.io.IOException;

/**
 * A class that manage all the details of decompression
 */
public class Decompressor implements IHuffConstants {
    private final BitInputStream inStream;
    private final BitOutputStream outStream;

    public Decompressor(BitInputStream in, BitOutputStream out) {
        inStream = in;
        outStream = out;
    }

    // decompress the file compressed using Store Counts Format
    public int decompressSCF() throws IOException {
        // regenerate frequencies
        int[] freq = new int[ALPH_SIZE];
        for (int i = 0; i < freq.length; i++) {
            int curFreq = inStream.readBits(BITS_PER_INT);
            freq[i] = curFreq;
        }
        // rebuild the huffman tree by frequencies
        HuffmanCodeTree huffTree = new HuffmanCodeTree(freq);
        return huffTree.decode(inStream, outStream);
    }

    // decompress the file compressed using Store Tree Format
    public int decompressSTF() throws IOException {
        // Let HuffmanCodeTree class read the header and rebuild the tree
        HuffmanCodeTree huffTree = new HuffmanCodeTree(inStream);
        return huffTree.decode(inStream, outStream);
    }
}
