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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A First-In-First-Out linear structure that stores elements with priority associated. Elements
 * with higher priorities is dequeued before elements with lower priorities. Elements with same
 * priorities will be ordered on a first come, first served basis.
 * @param <E> The data type of the elements of this PriorityQueue.
 * Must implement Comparable or inherit from a class that implements
 * Comparable.
 */
public class PriorityQueue<E extends Comparable<? super E>> {
    private LinkedList<E> con;

    public PriorityQueue() {
        con = new LinkedList<>();
    }

    // Insert item in this queue based on its priority. The smaller the item the higher the
    // priority. The item with lower priority must go behind the items with higher priorities.
    // When adding an item to the queue with a priority equal to other items in the queue, the
    // new item must go behind the items already present.
    // pre: item != null
    public void enqueue(E item) {
        if (item == null) {
            throw new IllegalArgumentException("Parameter item must not be null");
        }
        int posToInsert = 0;
        Iterator<E> it = con.iterator();
        while (it.hasNext() && it.next().compareTo(item) <= 0) {
            posToInsert++;
        }
        con.add(posToInsert, item);
    }

    // Remove item at front of this queue, return the removed item
    // pre: !isEmpty()
    public E dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("This PriorityQueue is empty");
        }
        return con.removeFirst();
    }

    // Return true if con.size() == 0, false otherwise
    public boolean isEmpty() {
        return con.size() == 0;
    }

    // Return the number of elements in this PriorityQueue
    public int size() {
        return con.size();
    }

    // Return a String version of this PriorityQueue
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        int count = 0;
        for (E item: con) {
            result.append(item.toString());
            count++;
            if (count != con.size()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }
}
