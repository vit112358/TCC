package com.vitor.Data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class fileStructure<T> implements Iterable<fileStructure<T>>  {

    /** Usage
     * TreeNode<String> root = new TreeNode<String>("root");
     * {
     *     TreeNode<String> node0 = root.addChild("node0");
     *     TreeNode<String> node1 = root.addChild("node1");
     *     TreeNode<String> node2 = root.addChild("node2");
     *     {
     *         TreeNode<String> node20 = node2.addChild(null);
     *         TreeNode<String> node21 = node2.addChild("node21");
     *         {
     *             TreeNode<String> node210 = node20.addChild("node210");
     *         }
     *     }
     * }
      */
    private T data;
    private fileStructure<T> parent;
    private List<fileStructure<T>> children;

    public fileStructure(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public fileStructure<T> addChild(T child) {
        fileStructure<T> childNode = new fileStructure<>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    /**
     * Not Implemented yet
     * @return null
     */
    @Override
    public Iterator<fileStructure<T>> iterator() {
        //Not implemented yet
        return null;
    }

    /**
     * Not Implemented yet
     */
    @Override
    public void forEach(Consumer<? super fileStructure<T>> action) {
        //Not implemented yet
    }

    /**
     * Not Implemented yet
     * @return null
     */
    @Override
    public Spliterator<fileStructure<T>> spliterator() {
        return null;
    }
}
