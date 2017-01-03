/**
 * @author Alan Garcia
 * Node of the tree.
 * There are some helper methods here specific to the application.
 */
class Node {
    public Point record;
    public String satellite;
    public Node left, right, parent;

    Node(Point rec, String sat) {
        record = rec;
        satellite = sat;
    }

    public String toString() {
        return "record = " + record + ", satellite = " + satellite;
    }

    boolean isRoot() {
        return parent == null;
    }

    boolean isLeftChild() {
        return this == parent.left;
    }

    boolean isRightChild() {
        return this == parent.right;
    }
}


/**
 * Splay tree (BST invented by Sleator and Tarjan).
 */
public class SplayTree {
    Node root, current;

    public SplayTree() {
        root = null;
    }

    /** 
     * Portray tree as a string.
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        toStringHelper(sb, root);
        return sb.toString();
    }

    /**
     * Pre-order traversal of tree.
     */
    private void toStringHelper( StringBuilder strb, Node current ) {
        if(current == null)
            return;

        strb.append("point " + current.record.toString() + 
                    " " + current.satellite);
        if(current.left != null)
            strb.append(" " + current.left.record.toString());
        if(current.right != null)
            strb.append(" " + current.right.record.toString());
        strb.append("\n");

        if(current.left != null)
            toStringHelper(strb, current.left);
        if(current.right != null)
            toStringHelper(strb, current.right);
    }

    /**
     * Search tree for key k.  Return its satellite data if found,
     * and splay the found node.
     * If not found, return null, and splay the would-be parent node.
     */
    public String lookup(Point key) {
        return lookupHelper(key, root);
    }

    private String lookupHelper(Point key, Node current) {
        if(current == null)
            return null;

        // recurse down the tree
        if(key.compareTo(current.record) > 0)
            return lookupHelper(key, current.left);

        else if(key.compareTo(current.record) < 0)
            return lookupHelper(key, current.right);

        else if(key.compareTo(current.record) == 0) {
            splay(current);
            return current.satellite;
        } else
            return null;
    }

    /**
     * It's lookup but without splaying and
     * returns a node instead of a point.
     * Used for deletion.
     */
    private Node find(Point key, Node current) {
        if(current == null)
            return null;

        if(key.compareTo(current.record) > 0)
            return find(key, current.left);

        else if(key.compareTo(current.record) < 0)
            return find(key, current.right);

        else if(key.compareTo(current.record) == 0)
            return current;
        else
            return null;
    }

    /**
     * Insert a new record.
     * First we do a search for the key.
     * If the search fails, we insert a new record.
     * Otherwise we update the satellite data with sat.
     * Splay the new, or altered, node.
     */
    public void insert_record(Point key, String sat) {
        root = insertHelper(key, sat, root);
        splay(root);
    }

    /**
     * Recurses down the tree to where the new node 
     * should go, if a node exists with that key already,
     * the data is updated instead.
     */
    private Node insertHelper(Point key, String sat, Node current) {
        if(current == null) // tree is empty or node doesnt exist: create it
            current = new Node(key, sat);

        if(key.compareTo(current.record) > 0) {
            Node temp = insertHelper(key, sat, current.left);
            current.left = temp;
            temp.parent = current;
        }
        else if(key.compareTo(current.record) < 0) {
            Node temp = insertHelper(key, sat, current.right);
            current.right = temp;
            temp.parent = current;
        }
        else if(key.compareTo(current.record) == 0)
            current.satellite = sat;

        return current;
    }

    /**
     * Remove a record.
     * Search for the key.  If not found, return null.
     * If found, save the satellite data, remove the record, 
     * and splay the bereaved parent.
     *
     * Return the satellite data from the deleted node.
     */
    public String delete(Point key) {
        Node n = find(key, root);
        // avoid null pointers for return string
        String result = n == null ? null : n.satellite;
        // do the work
        root = deleteHelper(key, root);
        // splay the bereaved parent
        if(root != null && !root.isRoot())
            splay(root.parent);
        return result;
    }

    /**
     * Recurses down the tree, finds the node to delete
     * and gets rid of it.
     */
    private Node deleteHelper(Point key, Node current) {
        if(current == null)
            return current;
        if(key.compareTo(current.record) > 0)
            current.left = deleteHelper(key, current.left);
        else if(key.compareTo(current.record) < 0)
            current.right = deleteHelper(key, current.right);
        else {
            if(current.left == null) {
                if(current.right != null)
                    current.right.parent = current.parent;
                return current.right;
            }
            else if(current.right == null) {
                if(current.left != null)
                    current.left.parent = current.parent;
                return current.left;
            }
            else {
                Node temp = findMin(current.right); // in-order successor
                current.record = temp.record; // update values
                current.satellite = temp.satellite;
                temp = deleteHelper(current.record, current.right);
                current.right = temp; // update right and parent pointers
                if(temp != null)
                    temp.parent = current;
            }
        }
        return current;
    }

    /**
     * Recurses down the tree and finds the
     * minimum value in that tree.
     * Useful for finding the in-order successor
     * of a given node by calling findMin(node.right).
     * Uses tail recursion.
     */
    private Node findMin(Node current) {
        if(current == null || current.left == null)
            return current;
        return findMin(current.left);
    }

    /**
     * Following two methods are simple
     * node rotations, there's nothing 
     * special to them.
     */
    private void rotateLeft(Node n) {
        Node temp = n.right;

        if(temp != null) {
            n.right = temp.left;
            if(temp.left != null)
                temp.left.parent = n;
            temp.parent = n.parent;
        }
        if(n.isRoot())
            root = temp;
        else if(n.isLeftChild())
            n.parent.left = temp;
        else
            n.parent.right = temp;

        if(temp != null)
            temp.left = n;
        n.parent = temp;
    }

    private void rotateRight(Node n) {
        Node temp = n.left;

        if(temp != null) {
            n.left = temp.right;
            if(temp.right != null)
                temp.right.parent = n;
            temp.parent = n.parent;
        }
        if(n.isRoot())
            root = temp;
        else if(n.isLeftChild())
            n.parent.left = temp;
        else
            n.parent.right = temp;

        if(temp != null)
            temp.right = n;
        n.parent = temp;
    }

    /**
     * Recursive bottom-up splaying of tree at node n.
     */
    private void splay(Node n) {
        if(n.isRoot()) // n is the root, we're done.
            return;
        if(n.parent.isRoot()) { // zig!
            if(n.isLeftChild())
                rotateRight(n.parent);
            else
                rotateLeft(n.parent);
        }
        else if(n.isLeftChild() && n.parent.isLeftChild()) { // zig-zig!
            rotateRight(n.parent.parent);
            rotateRight(n.parent);
        }
        else if(n.isRightChild() && n.parent.isRightChild()) { // zig-zig!
            rotateLeft(n.parent.parent);
            rotateLeft(n.parent);
        }
        else if(n.isLeftChild() && n.parent.isRightChild()) { // zig-zag!
            rotateRight(n.parent);
            rotateLeft(n.parent);
        }
        else { // zig-zag!
            rotateLeft(n.parent);
            rotateRight(n.parent);
        }
        splay(n); // tail recursion!
    }
}
