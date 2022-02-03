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

CS314 Assignment 10 Huffman Coding Analysis

Compressing rates for the Calgary Corpus

(Compression rate, as per www.data-compression.info, is defined as "the quotient of the size of the output in bits to the size of the input in bytes. A value of 8 bps means no compression, smaller values represent better (stronger) compression.")

File Name    |    Compression rate
bib               5.306
book1             4.573
book2             4.837
geo               5.749
news              5.249
obj1              6.356
obj2              6.325
paper1            5.172
paper2            4.735
pic               1.677
progc             5.443
progl             4.915
progp             5.063
trans             5.657
Average           4.520

Compressing rates for the waterloo directory which is a collection of .tiff images used in some compression benchmarking

File Name    |    Compression rate
sail.tif          7.361
monarch.tif       7.527
clegg.tif         7.574
lena.tif          7.792
serrano.tif       6.020
peppers.tif       7.699
tulips.tif        7.702
frymire.tif       4.724
Average           6.549

Compressing rates for the BooksAndHTML directory which contains a number of text files and html documents

File Name                  |      Compression rate
melville.txt                      4.613
A7_Recursion.html                 5.090
jnglb10.txt                       4.619
ThroughTheLookingGlass.txt        4.688
syllabus.htm                      5.131
revDictionary.txt                 4.328
CiaFactBook2000.txt               5.171
kjv10.txt                         4.584
rawMovieGross.txt                 3.672
quotes.htm                        4.993
Average                           4.763

What kinds of file lead to lots of compressions?
Text files with only "commonly-used" characters like English characters. Because "commonly-used" characters often repeat a lot in a file, there will be only few characters to encode. The huffman code tree will be small and the code foreach value will be short.

What kind of files had little or no compression?
Image files with colors. The reason might be that the bit representations of colors easily fill up to 256 bits, so all of the 256 (maximum) values will need to be encoded then the tree will be super big and the code for each value will be long.

What happens when you try and compress a huffman code file?
The compression rates for compressing already huff-compressed files are way bigger than the compression rates for compressing the original files, which means little compression is performed. Some files lead to no compression at all.
