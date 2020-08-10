package org.fubai.algorithm.tree;

import java.util.Comparator;

public class RedBlackTreeTest {

    public static void main(String[] args) {
        RedBlackTree<String, String> redBlackTree = new RedBlackTree<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null && o2 != null) {
                    return -1;
                }
                if (o1 != null && o2 == null) {
                    return 1;
                }
                if (o1.equals(o2)) {
                    return 0;
                }

                if (isNumber(o1) && isNumber(o2)) {
                    return Integer.valueOf(o1).intValue() > Integer.valueOf(o2).intValue() ? 1 : -1;
                }
                return o1.compareTo(o2);
            }

            private boolean isNumber(String key) {
                if (key == null) {
                    return false;
                }
                if (key.length() > 1 && key.charAt(0) == '0') {
                    return false;
                }
                for (int i = 0; i < key.length(); i++) {
                    if (!Character.isDigit(key.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        });

        new BinaryTreeVisualizer().show(new BinaryTreeVisualizer.TreeVisualizerEventHandler() {
            @Override
            public void handleAdd(String data, BinaryTreeVisualizer treeVisualizer) {
                redBlackTree.put(data, data);
                treeVisualizer.draw(transform(redBlackTree));
            }

            @Override
            public void handleRemove(String data, BinaryTreeVisualizer treeVisualizer) {
                redBlackTree.remove(data);
                treeVisualizer.draw(transform(redBlackTree));
            }

            @Override
            public void handleClear(BinaryTreeVisualizer treeVisualizer) {
                redBlackTree.clear();
                treeVisualizer.draw(transform(redBlackTree));
            }

            private BinaryTreeVisualizer.DisplayTree transform(RedBlackTree<String, String> redBlackTree) {
                BinaryTreeVisualizer.DisplayTree displayTree = new BinaryTreeVisualizer.DisplayTree();
                displayTree.root = transform(redBlackTree.getRoot());
                return displayTree;
            }

            private BinaryTreeVisualizer.DisplayNode transform(RedBlackTree.Node<String, String> node) {
                if (node == null) {
                    return null;
                }

                BinaryTreeVisualizer.DisplayNode displayNode = new BinaryTreeVisualizer.DisplayNode(node.getKey(), node.getColor() == RedBlackTree.Color.RED);
                displayNode.left = transform(node.getLeft());
                displayNode.right = transform(node.getRight());
                return displayNode;
            }

        });
    }

}
