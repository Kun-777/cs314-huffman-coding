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
 * A class that manage all the details of compression
 */
public class Compressor implements IHuffConstants {

    private BitInputStream inStream;
    private BitOutputStream outStream;
    // An array that stores the frequencies for each "index" which represents the value of each
    // chunk read in
    private final int[] frequencies;
    // The huffman code tree built by frequencies when constructing the class
    private final HuffmanCodeTree huffTree;
    // The header format constant that determines which type of header content is coming next
    private final int hFormat;
    // The number of bits in the original file
    private int originalBits;
    // The number of bits in the compressed file
    private int compBits;

    public Compressor(BitInputStream in, int headerFormat) throws IOException {
        inStream = in;
        originalBits = 0;
        compBits = 0;
        frequencies = new int[ALPH_SIZE];
        int chunk = inStream.readBits(BITS_PER_WORD);
        while (chunk != -1) {
            originalBits += BITS_PER_WORD;
            frequencies[chunk]++;
            chunk = inStream.readBits(BITS_PER_WORD);
        }
        huffTree = new HuffmanCodeTree(frequencies);
        // start calculating the size of the compressed file
        // first the Magic number and the header format constant
        compBits += BITS_PER_INT * 2;
        // header content which depends on headerFormat
        hFormat = headerFormat;
        if (hFormat == STORE_COUNTS) {
            // array of ints of length ALPH_SIZE
            compBits += ALPH_SIZE * BITS_PER_INT;
        } else if (hFormat == STORE_TREE) {
            compBits += huffTree.countSTFHeaderBits();
        }
        // calculate the size of compressed data by summing up the frequency * number of bits for
        // the code for each value
        int[] codeBits = huffTree.codeBits();
        for (int i = 0; i < frequencies.length; i++) {
            compBits += frequencies[i] * codeBits[i];
        }
        // add the pseudo-eof at the end
        compBits += codeBits[PSEUDO_EOF];
    }

    // return the number of bits saved without actually compressing and write out the file
    public int bitsSaved() {
        return originalBits - compBits;
    }

    // return a String version of the code table for myViewer to show
    public String codeTable() {
        return huffTree.showCodesTable();
    }

    // the main method to write the compressed file
    // return the number of bits written in the compressed file
    public int compress(BitInputStream in, BitOutputStream out) throws IOException {
        inStream = in;
        outStream = out;
        writeHeader();
        writeCompData();
        return compBits;
    }

    // write out the header information
    private void writeHeader() {
        // first 32 bits for the magic number
        outStream.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        // then the header format constant and the content depends on headerFormat
        if (hFormat == STORE_COUNTS) {
            outStream.writeBits(BITS_PER_INT, STORE_COUNTS);
            for (int i = 0; i < frequencies.length; i++) {
                outStream.writeBits(BITS_PER_INT, frequencies[i]);
            }
        } else if (hFormat == STORE_TREE) {
            outStream.writeBits(BITS_PER_INT, STORE_TREE);
            huffTree.writeSTFHeaderContent(outStream);
        }
    }

    // write out the compressed data
    private void writeCompData() throws IOException {
        // obtain the codings from huffTree
        int[] codeBits = huffTree.codeBits();
        int[] decimalCodes = huffTree.decimalCodes();
        int chunk = inStream.readBits(BITS_PER_WORD);
        while (chunk != -1) {
            outStream.writeBits(codeBits[chunk], decimalCodes[chunk]);
            chunk = inStream.readBits(BITS_PER_WORD);
        }
        // finally, write the code for pseudo-eof value
        outStream.writeBits(codeBits[PSEUDO_EOF], decimalCodes[PSEUDO_EOF]);
    }
}
