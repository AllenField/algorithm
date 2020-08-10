package org.fubai.algorithm.tree;

import java.util.Comparator;

/**
 * 红黑树
 *
 * 红黑树的性质如下：
 *
 * <ul>
 *   <li>1. 节点是红色或黑色。</li>
 *   <li>2. 根节点是黑色。</li>
 *   <li>3. 每个叶节点是黑色的。（这里的叶节点是指NULL节点，在《算法导论》中NULL节点叫哨兵节点，除了颜色属性外，其他属性值都为任意。为了和以前的叶子节点做区分，原来的叶子节点还叫叶子节点，这个节点就叫他NULL节点吧）</li>
 *   <li>4. 每个红色节点的两个子节点都是黑色。(从每个叶子到根的所有路径上不能有两个连续的红色节点，或者理解为红节点不能有红孩子)</li>
 *   <li>5. 从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点（黑节点的数目称为黑高black-height）。</li>
 * </ul>
 *
 * 注：本类不是线程安全的
 *
 * @param <K>
 * @param <V>
 *
 * @author wanglidong
 */
public class RedBlackTree<K extends Comparable<K>, V> {

    private transient Node<K, V> root;
    private transient int size = 0;

    private final Comparator<? super K> comparator;

    public RedBlackTree() {
        this.comparator = null;
    }

    public RedBlackTree(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    public Node<K, V> getRoot() {
        return this.root;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * 判断树中是否包含指定的key，时间复杂度为O(log n)
     *
     * @param key key用于关联指定的value
     * @return 树中包含指定的key则返回<tt>true</tt>，否则返回<tt>false</tt>。
     */
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }

        Node<K, V> node = this.root;
        while (node != null) {
            if (key.equals(node.key)) {
                return true;
            }

            node = compare(key, node.key) < 0 ? node.left : node.right;
        }
        return false;
    }

    /**
     * 获取指定key关联的value，时间复杂度为O(log n)
     *
     * @param key 用于关联value的key
     * @return value 返回值为{@code null}并不代表树中没有指定的key，
     * 也可能是指定的key关联的value本身就是{@code null}。
     * {@link #containsKey containsKey}方法可以用于区分
     * 树中没有指定的key和指定的key关联的值为{@code null}的情况。
     * 注意，当key为{@code null}时也会返回{@code null}。
     */
    public V get(K key) {
        Node<K, V> node = find(key);
        return node == null ? null : node.value;
    }

    /**
     * 获取树上最大的key，时间复杂度为O(log n)
     *
     * @return the max key
     */
    public K getMaxKey() {
        Node<K, V> maxNode = getMax();
        return maxNode == null ? null : maxNode.key;
    }

    /**
     * 获取树上最大的节点，时间复杂度为O(log n)
     *
     * @return the node with the max key
     */
    public Node<K, V> getMax() {
        if (root == null) {
            return null;
        }

        Node<K, V> node = this.root;
        while (node.right != null) {
            node = node.right;
        }
        return node;

    }

    /**
     * 获取树上最小的key，时间复杂度为O(log n)
     *
     * @return the min key
     */
    public K getMinKey() {
        Node<K, V> minNode = getMin();
        return minNode == null ? null : minNode.key;
    }

    /**
     * 获取树上最小的节点，时间复杂度为O(log n)
     *
     * @return the node with the min key
     */
    public Node<K, V> getMin() {
        if (root == null) {
            return null;
        }

        Node<K, V> node = this.root;
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     * 获取节点的前驱节点
     *
     * @param node 用于查找前驱节点的目标节点
     * @return 前驱节点
     */
    public Node<K, V> predecessor(Node<K, V> node) {
        return node == null ? null : node.predecessor();
    }

    /**
     * 获取节点的后继节点
     *
     * @param node 用于查找后继节点的目标节点
     * @return 后继节点
     */
    public Node<K, V> successor(Node<K, V> node) {
        return node == null ? null : node.successor();
    }

    /**
     * 添加
     *
     * @param key 用于关联value的key
     * @param value 被key关联的value
     * @return 用于树中存在指定的key，则返回其关联的value，否则返回null
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }

        if (root == null) {
            this.size = 1;
            this.root = new Node<>(key, value, Color.BLACK);
            return null;
        }

        Node<K, V> parent = null;
        Node<K, V> node = root;
        while (node != null) {
            if (key.equals(node.key)) {
                return node.setValue(value);
            }

            parent = node;
            node = compare(key, node.key) < 0 ? node.left : node.right;
        }

        node = new Node<>(key, value, parent);
        if (compare(key, parent.key) < 0) {
            parent.left = node;
        } else {
            parent.right = node;
        }
        this.size += 1;

        fixAfterInsertion(parent, node);

        return null;
    }

    private void fixAfterInsertion(Node<K, V> parent, Node<K, V> node) {
        // 父节点为黑色节点
        if (parent.color == Color.BLACK) {
            return;
        }

        // 如果parent.parent为null，说明parent是根节点
        // 根节点是黑色节点，在刚进入该方法时就返回了
        // 所以parent.parent不可能为null
        Node<K, V> grandpa = parent.parent;
        Node<K, V> uncle = grandpa.left == parent ? grandpa.right : grandpa.left;

        // 1 叔叔节点存在，且为红色 -> 将父节点和叔叔节点染为黑色，将爷爷节点染为红色，再以爷爷节点作为当前节点，递归进行下一轮处理
        if (uncle != null && uncle.color == Color.RED) {
            uncle.color = parent.color = Color.BLACK;
            if (grandpa.parent == null) {
                // grandpa是根节点，所以不需要将其变色了，直接结束
            } else {
                grandpa.color = Color.RED;
                fixAfterInsertion(grandpa.parent, grandpa);
            }
            return;
        }

        if (parent == grandpa.left) {
            // 2 叔叔节点不存在或为黑色，且父节点为爷爷节点的左子
            if (node == parent.left) {
                //   2.1 插入节点为父节点的左子节点：左左双红（LL） -> 将父节点染为黑色，将爷爷节点染为红色，然后右旋爷爷节点
                parent.color = Color.BLACK;
                grandpa.color = Color.RED;
                rightRotate(grandpa);
            } else {
                //   2.2 插入节点为父节点的右子节点：左右双红（LR） -> 左旋父节点，使得LR变为LL，然后执行LL的平衡操作
                leftRotate(parent);

                // 左旋父节点后，父节点与当前节点的父子关系调换，此时可以直接递归：balance(node, parent);
                // 递归将走到2.1分支中，这里不递归了，直接执行2.1中的逻辑
                node.color = Color.BLACK;
                grandpa.color = Color.RED;
                rightRotate(grandpa);
            }
        } else {
            // 3 叔叔节点不存在或为黑色，且父节点为爷爷节点的右子
            if (node == parent.right) {
                //   3.1 插入节点为父节点的右子节点：右右双红（RR） -> 将父节点染为黑色，将爷爷节点染为红色，然后左旋爷爷节点
                parent.color = Color.BLACK;
                grandpa.color = Color.RED;
                leftRotate(grandpa);
            } else {
                //   3.2 插入节点为父节点的左子节点：右左双红（RL） -> 右旋父节点，使得RL变为RR，然后执行RR的平衡操作
                rightRotate(parent);

                // 右旋父节点后，父节点与当前节点的父子关系调换，此时可以直接递归：balance(node, parent);
                // 递归将走到3.1分支中，这里不递归了，直接执行3.1中的逻辑
                node.color = Color.BLACK;
                grandpa.color = Color.RED;
                leftRotate(grandpa);
            }
        }
    }

    /**
     * 左旋节点
     *
     * @param node 待旋转的节点
     */
    private void leftRotate(Node<K, V> node) {
        Node<K, V> rightSon = node.right;
        if (rightSon == null) {
            throw new NullPointerException("没有右子节点，无法左旋");
        }

        // 将右子的左子改为当前节点的右子
        Node<K, V> leftGrandsonOfRightSon = rightSon.left;
        if (leftGrandsonOfRightSon != null) {
            leftGrandsonOfRightSon.parent = node;
        }
        node.right = leftGrandsonOfRightSon;

        // 将右子提升为当前节点
        Node<K, V> parent = node.parent;
        if (parent == null) {
            root = rightSon;
        } else {
            if (parent.left == node) {
                parent.left = rightSon;
            } else {
                parent.right = rightSon;
            }
        }
        rightSon.parent = parent;

        // 将当前节点改为右子的左子
        rightSon.left = node;
        node.parent = rightSon;
    }

    /**
     * 右旋节点
     *
     * @param node 待旋转的节点
     */
    private void rightRotate(Node<K, V> node) {
        Node<K, V> leftSon = node.left;
        if (leftSon == null) {
            throw new NullPointerException("没有左子节点，无法右旋");
        }

        // 将左子的右子改为当前节点的左子
        Node<K, V> rightGrandsonOfLeftSon = leftSon.right;
        if (rightGrandsonOfLeftSon != null) {
            rightGrandsonOfLeftSon.parent = node;
        }
        node.left = rightGrandsonOfLeftSon;

        // 将左子提升为当前节点
        Node<K, V> parent = node.parent;
        if (parent == null) {
            root = leftSon;
        } else {
            if (parent.left == node) {
                parent.left = leftSon;
            } else {
                parent.right = leftSon;
            }
        }
        leftSon.parent = parent;

        // 将当前节点改为左子的右子
        leftSon.right = node;
        node.parent = leftSon;
    }

    /**
     * 删除树中的节点
     *
     * @param key 用于关联value的key
     * @return 被key关联的value
     */
    public V remove(K key) {
        Node<K, V> node = find(key);
        if (node == null) {
            return null;
        }

        V value = node.value;
        remove(node);
        return value;
    }

    private void remove(Node<K, V> node) {
        this.size -= 1;

        // node拥有两个子节点，找到其后继节点（后继节点最多只有一个子节点），将node的key、value替换为后继节点的key、value，然后继续处理后继节点
        if (node.left != null && node.right != null) {
            Node<K, V> successor = node.subSuccessor();
            node.key = successor.key;
            node.value = successor.value;
            node = successor;
        }

        Node<K, V> replacement = node.left != null ? node.left : node.right;
        if (replacement != null) {// 只有一个子节点
            replacement.parent = node.parent;
            if (node.parent == null) {
                this.root = replacement;
            } else if (node.parent.left == node) {
                node.parent.left = replacement;
            } else {
                node.parent.right = replacement;
            }

            node.left = node.right = node.parent = null;

            // 删除一个黑色节点将破坏"黑高"，也就是性质5
            if (node.color == Color.BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (node.parent == null) {//无子，且node是根节点
            this.root = null;
        } else {// 无子，使用自己作为虚的替换节点，操作完成之后将自己从树中移除
            // 删除一个黑色节点将破坏"黑高"，也就是性质5
            if (node.color == Color.BLACK) {
                fixAfterDeletion(node);
            }

            if (node.parent != null) {
                if (node.parent.left == node) {
                    node.parent.left = null;
                } else if (node.parent.right == node) {
                    node.parent.right = null;
                }
                node.parent = null;
            }
        }
    }

    /**
     * 下面代码是参考JDK的TreeMap实现的
     * 
     * TODO 理解下面的操作
     * 
     * @param node
     */
    private void fixAfterDeletion(Node<K, V> node) {
        while (node != root && colorOf(node) == Color.BLACK) {
            if (node == leftOf(parentOf(node))) {
                Node<K, V> sibling = rightOf(parentOf(node));

                // node是左子，兄弟是右子
                // 左子是黑色，右子是红色
                // 将右子设为黑色，将父设为红色
                // 左旋父
                if (colorOf(sibling) == Color.RED) {
                    setColor(sibling, Color.BLACK);
                    setColor(parentOf(node), Color.RED);
                    // 将现在的兄弟节点提升到父节点的位置
                    leftRotate(parentOf(node));
                    sibling = rightOf(parentOf(node));// 此时的sibling是原sibling的左子
                }

                if (colorOf(leftOf(sibling)) == Color.BLACK &&
                    colorOf(rightOf(sibling)) == Color.BLACK) {
                    setColor(sibling, Color.RED);
                    node = parentOf(node);
                } else {
                    if (colorOf(rightOf(sibling)) == Color.BLACK) {
                        setColor(leftOf(sibling), Color.BLACK);
                        setColor(sibling, Color.RED);
                        rightRotate(sibling);
                        sibling = rightOf(parentOf(node));
                    }
                    setColor(sibling, colorOf(parentOf(node)));
                    setColor(parentOf(node), Color.BLACK);
                    setColor(rightOf(sibling), Color.BLACK);
                    leftRotate(parentOf(node));
                    node = root;
                }
            } else {// 和上面是对等的
                Node<K,V> sibling = leftOf(parentOf(node));

                if (colorOf(sibling) == Color.RED) {
                    setColor(sibling, Color.BLACK);
                    setColor(parentOf(node), Color.RED);
                    rightRotate(parentOf(node));
                    sibling = leftOf(parentOf(node));
                }

                if (colorOf(rightOf(sibling)) == Color.BLACK &&
                    colorOf(leftOf(sibling)) == Color.BLACK) {
                    setColor(sibling, Color.RED);
                    node = parentOf(node);
                } else {
                    if (colorOf(leftOf(sibling)) == Color.BLACK) {
                        setColor(rightOf(sibling), Color.BLACK);
                        setColor(sibling, Color.RED);
                        leftRotate(sibling);
                        sibling = leftOf(parentOf(node));
                    }
                    setColor(sibling, colorOf(parentOf(node)));
                    setColor(parentOf(node), Color.BLACK);
                    setColor(leftOf(sibling), Color.BLACK);
                    rightRotate(parentOf(node));
                    node = root;
                }
            }
        }
        setColor(node, Color.BLACK);
    }

    private Color colorOf(Node<K, V> node) {
        return node == null ? Color.BLACK : node.color;
    }

    private void setColor(Node<K, V> node, Color color) {
        if (node != null) {
            node.color = color;
        }
    }

    private Node<K, V> parentOf(Node<K, V> node) {
        return node == null ? null : node.parent;
    }

    private Node<K, V> leftOf(Node<K, V> node) {
        return node == null ? null : node.left;
    }

    private Node<K, V> rightOf(Node<K, V> node) {
        return node == null ? null : node.right;
    }

    /**
     * 清空树
     */
    public void clear() {
        this.root = null;
        this.size = 0;
    }

    /**
     * 通过key查找节点
     * @param key 用于关联value的key
     * @return 包含指定key的节点
     */
    private Node<K, V> find(K key) {
        if (key == null) {
            return null;
        }

        Node<K, V> node = this.root;
        while (node != null) {
            if (key.equals(node.key)) {
                return node;
            }

            node = compare(key, node.key) < 0 ? node.left : node.right;
        }
        return null;
    }

    private int compare(K k1, K k2) {
        if (this.comparator == null) {
            return k1.compareTo(k2);
        }
        return this.comparator.compare(k1, k2);
    }

    public static class Node<K, V> {

        private K key;
        private V value;
        private Color color;
        private Node<K, V> left;
        private Node<K, V> right;
        private Node<K, V> parent;

        public Node(K key, V value, Color color) {
            this.key = key;
            this.value = value;
            this.color = color == Color.BLACK ? Color.BLACK : Color.RED;
        }

        public Node(K key, V value, Node<K, V> parent) {
            this.key = key;
            this.value = value;
            this.color = Color.RED;
            this.parent = parent;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public Color getColor() {
            return color;
        }

        public Node<K, V> getLeft() {
            return left;
        }

        public Node<K, V> getRight() {
            return right;
        }

        public Node<K, V> getParent() {
            return parent;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        /**
         * 获取节点的前驱节点
         *
         * @return 前驱节点
         */
        public Node<K, V> predecessor() {
            Node<K, V> predecessor = subPredecessor();
            if (predecessor != null) {
                return predecessor;
            }

            Node<K, V> temp = this;
            predecessor = temp.parent;
            while (predecessor != null && predecessor.left == temp) {
                temp = temp.parent;
                predecessor = temp.parent;
            }
            return predecessor;
        }

        /**
         * 获取节点的“sub-前驱节点”
         *
         * “sub-前驱节点”不是在整棵树上寻找一个节点的前驱节点，而是只在节点的左子树上寻找其前驱节点。
         * 注意，“sub-前驱节点”只是本类的术语，仅在本类范围内生效。
         *
         * @return 前驱子节点
         */
        private Node<K, V> subPredecessor() {
            if (this.left == null) {
                return null;
            }

            Node<K, V> predecessor = this.left;
            while (predecessor.right != null) {
                predecessor = predecessor.right;
            }
            return predecessor;
        }

        /**
         * 获取节点的后继节点
         *
         * @return 后继节点
         */
        public Node<K, V> successor() {
            Node<K, V> successor = subSuccessor();
            if (successor != null) {
                return successor;
            }

            Node<K, V> temp = this;
            successor = temp.parent;
            while (successor != null && successor.right == temp) {
                temp = temp.parent;
                successor = temp.parent;
            }
            return successor;
        }

        /**
         * 获取节点的“sub-后继节点”
         *
         * “sub-后继节点”不是在整棵树上寻找一个节点的后继节点，而是只在节点的右子树上寻找其后继节点。
         * 注意，“sub-后继节点”只是本类的术语，仅在本类范围内生效。
         *
         * @return 后继子节点
         */
        private Node<K, V> subSuccessor() {
            if (this.right == null) {
                return null;
            }

            Node<K, V> successor = this.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            return successor;
        }

    }

    public enum Color {
        RED, BLACK
    }

}
