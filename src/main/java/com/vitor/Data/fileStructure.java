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
    public T data;
    public fileStructure<T> parent;
    public List<fileStructure<T>> children;

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    private List<fileStructure<T>> elementsIndex;

    public fileStructure(T data) {
        this.data = data;
        this.children = new LinkedList<>();
        this.elementsIndex = new LinkedList<>();
        this.elementsIndex.add(this);
    }

    public fileStructure<T> addChild(T child) {
        fileStructure<T> childNode = new fileStructure<>(child);
        childNode.parent = this;
        this.children.add(childNode);
        this.registerChildForSearch(childNode);
        return childNode;
    }

    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    private void registerChildForSearch(fileStructure<T> node) {
        elementsIndex.add(node);
        if (parent != null)
            parent.registerChildForSearch(node);
    }

    public fileStructure<T> findTreeNode(Comparable<T> cmp) {
        for (fileStructure<T> element : this.elementsIndex) {
            T elData = element.data;
            if (cmp.compareTo(elData) == 0)
                return element;
        }

        return null;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "[data null]";
    }

    @Override
    public Iterator<fileStructure<T>> iterator() {
        fileStructureIter<T> iter = new fileStructureIter<T>(this);
        return iter;
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
