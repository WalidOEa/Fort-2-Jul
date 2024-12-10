package uk.ac.soton.comp3200.fort2jul.parser;

import java.util.*;

/**
 * Allows us to create and manipulate a tree data structure in Java. Comprised of nodes each with at least 1 parent.
 * @param <String> Type of data in nodes
 */
public class Ast<String> {

    /**
     * Creates Node data structure with parent, children (if any) and data contained within Node.
     * @param <String> Type of data in nodes
     */
    public static class Node<String> {

        /**
         * Data of Nodes
         */
        private String data;

        /**
         * Parent of Node
         */
        private Node<String> parent;

        /**
         * Children of Node (if any)
         */
        private List<Node<String>> children;

        /**
         * Get data
         * @return Data
         */
        public String getData() {
            return data;
        }
    }

    /**
     * Root of AST
     */
    public Node<String> root;

    /**
     * Assign root of AST
     * @param rootNode Root of AST
     */
    public Ast(Node<String> rootNode) {
        root = rootNode;
    }

    /**
     * Create Node of AST
     * @param data Data of Node
     * @return Node
     */
    public Node<String> createNode(String data) {
        Node<String> node = new Node<>();

        node.data = data;
        node.children = new ArrayList<>();

        return node;
    }

    /**
     * Add child to Node
     * @param parent Node to add child to
     * @param child Child Node
     */
    public void addChild(Node<String> parent, Node<String> child) {
        parent.children.add(child);
    }

    /**
     * Get all children of any specified parent Node
     * @param node Parent Node
     * @return Child Nodes
     */
    public List<Node<String>> getChildren(Node<String> node) {
        return node.children;
    }

    /**
     * Find parent from currentNode
     * @param currentNode Node to start at
     * @param targetData Target Node
     * @return Boolean
     */
    public boolean findParent(Node<String> currentNode, String targetData) {
        if (currentNode == null) {
            return false;
        }

        if (currentNode.data.equals(targetData)) {
            return true; // Found the target node
        }

        return findParent(currentNode.parent, targetData);
    }

    /**
     * Get root Node of AST.
     * @return Root Node
     */
    public Node<String> getRootNode() {
        return root;
    }
}