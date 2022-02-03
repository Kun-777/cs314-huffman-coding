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
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;

    private Compressor comp;

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
        myViewer.update("Preprocessing...");
        BitInputStream inStream = new BitInputStream(in);
        comp = new Compressor(inStream, headerFormat);
        // debugging: show the code table
        myViewer.update(comp.codeTable());
        inStream.close();
        return comp.bitsSaved();
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
        myViewer.update("Compressing...");
        int compBits = 0;
        if (!force && comp.bitsSaved() < 0) {
            myViewer.showError("Compressed file has " + comp.bitsSaved() + " more bits " +
                    "than uncompressed file. Select \"force compression\" option to compress.");
            myViewer.update("Compression terminated.");
        } else {
            BitInputStream inStream = new BitInputStream(in);
            BitOutputStream outStream = new BitOutputStream(out);
            compBits = comp.compress(inStream, outStream);
            inStream.close();
            outStream.close();
            myViewer.update("Compression completed.\nThe number of bits written: " + compBits);
            myViewer.update("The number of bits saved: " + comp.bitsSaved() + "\n");
            myViewer.showMessage("Compression completed.");
        }
        return compBits;
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
        myViewer.update("Decompressing...");
        int compBits = 0;
        BitInputStream inStream = new BitInputStream(in);
        BitOutputStream outStream = new BitOutputStream(out);
        // first read the magic number, throw an error if the first 32 bits is not MAGIC_NUMBER
        int chunk = inStream.readBits(BITS_PER_INT);
        if (chunk != MAGIC_NUMBER) {
            myViewer.showError("No Huffman Magic Number found in header. This is not a Huffman " +
                    "compressed file.");
            myViewer.update("Decompression terminated.");
        } else {
            // read the header format constant, throw an error if it's neither SCF nor STF
            chunk = inStream.readBits(BITS_PER_INT);
            Decompressor decomp = new Decompressor(inStream, outStream);
            if (chunk == STORE_COUNTS) {
                compBits += decomp.decompressSCF();
            } else if (chunk == STORE_TREE) {
                compBits += decomp.decompressSTF();
            } else {
                myViewer.showError("No header format constant found. This is not a Huffman " +
                        "compressed file.");
                myViewer.update("Decompression terminated.");
            }
        }
        inStream.close();
        outStream.close();
        myViewer.update("The number of bits written: " + compBits + "\n");
        myViewer.showMessage("Decompression completed.");
        return compBits;
    }

    /**
     * Make sure this model communicates with some view.
     * @param viewer is the view for communicating.
     */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    /**
     * Display strings in the View if there is a Viewer. Displays string on a single line. Call
     * multiple times with no interleaved clear to show several strings.
     * @param s is string to be displayed
     */
    private void showString(String s){
        if(myViewer != null)
            myViewer.update(s);
    }
}
