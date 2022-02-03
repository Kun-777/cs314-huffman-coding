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
import java.util.ArrayList;

/**
 * A binary tree used for Huffman encoding and decoding
 */
public class HuffmanCodeTree implements IHuffConstants {

    private final TreeNode root;
    // A 2D array of 2 rows. The first row stores the number of bits of the code for each value.
    // The second row stores the decimal representation of the code for each value.
    private int[][] codes;

    // Build the HuffmanCodeTree using an array of frequencies
    public HuffmanCodeTree(int[] frequencies) {
        PriorityQueue<TreeNode> pq = prioritize(frequencies);
        while (pq.size() > 1) {
            TreeNode leftChild = pq.dequeue();
            TreeNode rightChild = pq.dequeue();
            TreeNode newNode = new TreeNode(leftChild, -1, rightChild);
            pq.enqueue(newNode);
        }
        // There is only one node left in pq, which means this is our root
        root = pq.dequeue();
        codes = new int[2][ALPH_SIZE + 1]; // one extra spot for the pseudo-EOF value
        // Perform a traversal of the tree to obtain new codes
        encodeHelp(root, new ArrayList<>());
    }

    // Build the HuffmanCodeTree by reading the Store Tree Format header
    public HuffmanCodeTree(BitInputStream in) throws IOException {
        int treeSize = in.readBits(BITS_PER_INT);
        root = huffTreeBuilderHelp(in, new int[] {treeSize});
    }

    // Recursive helper method to form a huffman code tree by reading the STF header content
    // This recursion will end if the file is compressed correctly because the huffman code tree
    // is a full binary tree, the rightmost node of the tree must be a leaf node.
    // treeSize is an array that contains a single value which indicate the number of bits left
    // to read to complete the huffman code tree, used for debugging purposes
    private TreeNode huffTreeBuilderHelp(BitInputStream in, int[] numBitsLeft) throws IOException {
        if (numBitsLeft[0] < 0) {
            // If this happens, then something is incorrect about the format of the input file.
            // Throw an exception
            throw new IOException("Error reading header content. \nHuffman code tree cannot be " +
                    "completed with the header content.");
        }
        int bit = in.readBits(1);
        numBitsLeft[0]--;
        if (bit == 0) {
            // this is an internal node
            return new TreeNode(huffTreeBuilderHelp(in, numBitsLeft), -1,
                    huffTreeBuilderHelp(in, numBitsLeft));
        } else {
            // this is a leaf node, read the value of the node
            bit = in.readBits(BITS_PER_WORD + 1);
            numBitsLeft[0] -= (BITS_PER_WORD + 1);
            // assign an arbitrary frequency
            return new TreeNode(bit, -1);
        }
    }

    // return an array of the number of bits for each code
    public int[] codeBits() {
        int[] result = new int[ALPH_SIZE + 1];
        for (int i = 0; i < codes[0].length; i++) {
            result[i] = codes[0][i];
        }
        return result;
    }

    // return an array of the decimal representations of the codes
    public int[] decimalCodes() {
        int[] result = new int[ALPH_SIZE + 1];
        for (int i = 0; i < codes[1].length; i++) {
            result[i] = codes[1][i];
        }
        return result;
    }

    // perform a pre-order traversal to count the space needed for the Standard Tree Format header
    // This method does not write out the header content
    // 32 bits for the size of the tree in the front, then a single zero-bit for internal nodes, a
    // single one-bit for a leaf, and nine bits for the value stored in a leaf
    public int countSTFHeaderBits() {
        return BITS_PER_INT + countSTFHelp(root);
    }

    // Recursive helper method that performs a pre-order traversal and count the number of bits
    private int countSTFHelp(TreeNode n) {
        // Base case: reach the leaf node, add one bit to indicate leaf, and nine bits for the value
        if (n.isLeaf()) {
            return 1 + BITS_PER_WORD + 1;
        }
        // Recursive case: add one bit to indicate internal node and go to its children
        return 1 + countSTFHelp(n.getLeft()) + countSTFHelp(n.getRight());
    }

    // Write out the header content for Store Tree Format
    public void writeSTFHeaderContent(BitOutputStream out) {
        // first determine the size of the tree representation in bits
        out.writeBits(BITS_PER_INT, countSTFHelp(root));
        writeSTFHeaderHelp(out, root);
    }

    // Recursive helper method that performs a pre-order traversal and write out the bit
    // representation of this tree
    // 0 represents a non-leaf node, 1 represent a leaf node which stores a value
    // 9 bits are used to store the value of that leaf node
    private void writeSTFHeaderHelp(BitOutputStream out, TreeNode n) {
        // Base case: reach the leaf node, add a 1 to indicate leaf, and nine bits for the value
        if (n.isLeaf()) {
            out.writeBits(1, 1);
            out.writeBits(BITS_PER_WORD + 1, n.getValue());
        } else {
            // Recursive case: add a 0 to indicate internal node and go to its children
            out.writeBits(1, 0);
            writeSTFHeaderHelp(out, n.getLeft());
            writeSTFHeaderHelp(out, n.getRight());
        }
    }

    public int decode(BitInputStream in, BitOutputStream out) throws IOException {
        int numBitsWritten = 0;
        TreeNode currentNode = root;
        // stop reading when see the code for PSEUDO_EOF value
        while (currentNode.getValue() != PSEUDO_EOF) {
            int nextBit = in.readBits(1);
            if (currentNode.isLeaf()) {
                // found a value for current code sequence, write it out
                out.writeBits(BITS_PER_WORD, currentNode.getValue());
                numBitsWritten += BITS_PER_WORD;
                // go back to the root and start searching for the next value
                currentNode = root;
            }
            if (nextBit == -1) {
                throw new IOException("Error reading compressed file. \nUnexpected end of input. " +
                        "No PSEUDO_EOF value.");
            } else if (nextBit == 0) {
                currentNode = currentNode.getLeft();
            } else {
                // nextBit == 1
                currentNode = currentNode.getRight();
            }
        }
        return numBitsWritten;
    }

    // Recursive helper method that performs a pre-order traversal and generate codes
    // currentCode is an ArrayList of integers that stores each bit of the current code
    private void encodeHelp(TreeNode n, ArrayList<Integer> currentCode) {
        // Base case: reach the leaf node, a code is completed for the value in this leaf, add it
        // to codes
        if (n.isLeaf()) {
            // Obtain the decimal representation of the current code
            int decimalVal = 0;
            for (int i = 0; i < currentCode.size(); i++) {
                // Last (right-most) bit * 2 ^ 0 + second last bit * 2 ^ 1 + ... +
                // first bit * 2 ^ (length - 1)
                decimalVal += Math.pow(currentCode.get(i) * 2, currentCode.size() - 1 - i);
            }
            // Special case: 0^0 = 1, if last bit is 0 then decrement 1
            if (currentCode.get(currentCode.size() - 1) == 0) {
                decimalVal--;
            }
            codes[0][n.getValue()] = currentCode.size();
            codes[1][n.getValue()] = decimalVal;
        } else {
            // Recursive case
            // First go to the left child, add a 0 to the code
            currentCode.add(0);
            encodeHelp(n.getLeft(), currentCode);
            // Backtracking: remove the bit we added
            currentCode.remove(currentCode.size() - 1);
            // Then go to the right child, add a 1
            currentCode.add(1);
            encodeHelp(n.getRight(), currentCode);
            // Backtracking
            currentCode.remove(currentCode.size() - 1);
        }

    }

    // Prioritize values based on their frequencies.
    // Create tree nodes with a value and the frequency for each value, then place nodes in
    // a PriorityQueue
    private PriorityQueue<TreeNode> prioritize(int[] frequencies) {
        PriorityQueue<TreeNode> resultPQ = new PriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] != 0) {
                TreeNode newNode = new TreeNode(i, frequencies[i]);
                resultPQ.enqueue(newNode);
            }
        }
        // Adding a Pseudo-EOF value to indicate the end of the file for decoding purposes
        TreeNode peof = new TreeNode(PSEUDO_EOF, 1);
        resultPQ.enqueue(peof);
        return resultPQ;
    }

    // Return a String version of all codes
    public String showCodesTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Codes for values in file: \n");
        for (int i = 0; i < codes[0].length; i++) {
            if (codes[0][i] != 0) {
                sb.append(i);
                sb.append("    ");
                sb.append(decimalToBinary(codes[1][i], codes[0][i]));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // convert a decimal value to a String representation of its binary value for debugging purposes
    private String decimalToBinary(int decimalVal, int numBits) {
        StringBuilder sb = new StringBuilder();
        // start building the binary sequence from the left-most bit
        int currentBit = numBits;
        while (currentBit > 0) {
            int oneBitInDec = (int) Math.pow(2, currentBit - 1);
            if (oneBitInDec <= decimalVal) {
                decimalVal -= oneBitInDec;
                sb.append(1);
            } else {
                sb.append(0);
            }
            currentBit--;
        }
        return sb.toString();
    }
}
