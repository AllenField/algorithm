package org.fubai.algorithm.tree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 二叉树的可视化器
 */
public class BinaryTreeVisualizer {

    private TreePanel treePanel;
    private boolean initialized = false;

    /**
     * 展示可视化器
     * 
     * @return
     */
    public BinaryTreeVisualizer show() {
        return this.show(-1, -1, null);
    }

    /**
     * 展示可视化器
     * 
     * @param handler 事件处理器
     * @return
     */
    public BinaryTreeVisualizer show(TreeVisualizerEventHandler handler) {
        return this.show(-1, -1, handler);
    }

    /**
     * 展示可视化器
     * 
     * @param viewWidth 视图宽度
     * @param viewHeight 视图高度
     * @param handler 事件处理器
     * @return
     */
    public BinaryTreeVisualizer show(int viewWidth, int viewHeight, TreeVisualizerEventHandler handler) {
        if (this.initialized) {
            return this;
        }
        this.treePanel = buildFrame(viewWidth, viewHeight, handler);
        this.initialized = true;
        return this;
    }

    /**
     * 绘制树
     * 
     * @param tree 待绘制的树
     */
    public void draw(DisplayTree tree) {
        treePanel.draw(tree.prepareDisplaying());
    }

    private TreePanel buildFrame(int viewWidth, int viewHeight, TreeVisualizerEventHandler handler) {
        JFrame frame = new JFrame();
        frame.setTitle("二叉树");
        frame.setLayout(null);
        if (viewWidth == -1) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            frame.setSize(viewWidth, viewHeight);
        }
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (handler != null) {
            frame.add(this.buildInput(handler));
        }

        int treePanelPaddingX = 40;
        int treePanelPaddingY = 60;
        TreePanel treePanel = new TreePanel(viewWidth, viewHeight, treePanelPaddingX, treePanelPaddingY);
        frame.add(treePanel);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                treePanel.doResize(frame.getWidth(), frame.getHeight(), treePanelPaddingX, treePanelPaddingY);
            }
        });

        return treePanel;
    }

    private JTextField buildInput(TreeVisualizerEventHandler handler) {
        final BinaryTreeVisualizer treeVisualizer = this;

        JTextField input = new JTextField(20);
        input.setBounds(10, 10, 160, 30);
        input.setToolTipText("回车添加数据，Shift + 回车删除数据，ESC清空数据");
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = e.getKeyChar();
                if (key == KeyEvent.VK_ESCAPE) {
                    handler.handleClear(treeVisualizer);
                    return;
                }

                if (key != KeyEvent.VK_ENTER && key != 13) {
                    return;
                }

                String text = input.getText();
                if (text == null || (text = text.trim()).length() == 0) {
                    return;
                }
                if (key == KeyEvent.VK_ENTER) {
                    handler.handleAdd(text, treeVisualizer);
                } else {
                    handler.handleRemove(text, treeVisualizer);
                }
                input.setText("");
            }
        });
        return input;
    }

    private static class TreePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private int width;
        private int height;
        private int paddingX;
        private int paddingY;
        private int xScale;
        private int yScale;
        private DisplayTree tree;

        public TreePanel(int width, int height, int paddingX, int paddingY) {
            this.width = width;
            this.height = height;
            this.paddingX = paddingX;
            this.paddingY = paddingY;
            super.setBounds(0, 0, width, height);
            super.setBackground(Color.WHITE);
        }

        public void doResize(int width, int height, int paddingX, int paddingY) {
            this.width = width;
            this.height = height;
            this.paddingX = paddingX;
            this.paddingY = paddingY;
            super.setBounds(0, 0, width, height);

            if (this.tree != null) {
                draw(this.tree);
            }
        }

        /**
         * 绘制树
         *
         * @param tree 待绘制的树
         */
        public void draw(DisplayTree tree) {
            this.tree = tree;

            // 根据树的宽度，将屏幕水平切分的数量
            // 比如树中共有3个节点，这3个节点将屏幕水平切分成两份，那么xCount就等于2
            int xCount = tree.calcWidth();
            if (xCount == 0) {
                this.xScale = 0;
                this.yScale = 0;
            } else {
                // 根据树的高度，将屏幕垂直切分的数量
                int yCount = tree.calcHeight();
                this.xScale = (width - paddingX * 2) / xCount;
                this.yScale = (height - paddingY * 2) / yCount;
            }

            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (this.tree == null || this.tree.root == null) {
                return;
            }

            graphics.setFont(new Font(null, Font.PLAIN, 16));

            if (this.xScale == 0) {
                drawNode0(graphics, this.tree.root);
            } else {
                // 先绘制线，然后再绘制节点，这样节点会覆盖在线的上面
                this.drawLine(graphics, this.tree.root);
                this.drawNode(graphics, this.tree.root);
            }
        }

        private void drawLine(Graphics graphics, DisplayNode node) {
            if (node == null) {
                return;
            }

            drawLine(graphics, node.left);

            int dx = node.xPos * xScale + paddingX;
            int dy = node.yPos * yScale + paddingY;
            if (node.left != null) {
                int dx2 = node.left.xPos * xScale + paddingX;
                int dy2 = node.left.yPos * yScale + paddingY;
                graphics.setColor(Color.GREEN);
                graphics.drawLine(dx, dy, dx2, dy2);
            }
            if (node.right != null) {
                int dx2 = node.right.xPos * xScale + paddingX;
                int dy2 = node.right.yPos * yScale + paddingY;
                graphics.setColor(Color.GREEN);
                graphics.drawLine(dx, dy, dx2, dy2);
            }

            drawLine(graphics, node.right);
        }

        private void drawNode(Graphics graphics, DisplayNode node) {
            if (node == null) {
                return;
            }

            drawNode(graphics, node.left);
            drawNode0(graphics, node);
            drawNode(graphics, node.right);
        }

        private void drawNode0(Graphics graphics, DisplayNode node) {
            int dx, dy;
            if (xScale == 0) {
                dx = width / 2;
                dy = height / 2;
            } else {
                dx = node.xPos * xScale + paddingX;
                dy = node.yPos * yScale + paddingY;
            }

            graphics.setColor(node.red ? Color.RED : Color.BLACK);
            graphics.fillOval(dx - 18, dy - 18, 36, 36);

            graphics.setColor(node.red ? Color.BLACK : Color.CYAN);
            if (node.data.length() == 1) {
                graphics.drawString(node.data, dx - 4, dy + 4);
            } else if (node.data.length() == 2) {
                graphics.drawString(node.data, dx - 8, dy + 4);
            } else {
                graphics.drawString(node.data, dx - 14, dy + 4);
            }
        }
    }

    /**
     * 树的可视化器中的事件处理器
     */
    public interface TreeVisualizerEventHandler {

        /**
         * 添加数据的处理方法
         * @param data 新添加的数据
         * @param treeVisualizer 树的可视化器
         */
        default void handleAdd(String data, BinaryTreeVisualizer treeVisualizer) {
        }

        /**
         * 删除数据的处理方法
         * @param data 要删除的数据
         * @param treeVisualizer 树的可视化器
         */
        default void handleRemove(String data, BinaryTreeVisualizer treeVisualizer) {
        }

        /**
         * 清空数据的处理方法
         * @param treeVisualizer 树的可视化器
         */
        default void handleClear(BinaryTreeVisualizer treeVisualizer) {
        }

    }

    /**
     * 用于绘制的树
     */
    public static class DisplayTree {

        public DisplayNode root;
        private int size = -1;

        /**
         * 插入数据
         *
         * @param data 待插入的数据
         */
        public void insert(String data) {
            if (root == null) {
                root = new DisplayNode(data);
                return;
            }

            DisplayNode parent = null;
            DisplayNode node = root;
            while (node != null) {
                if (node.data.equals(data)) {
                    return;
                }

                parent = node;
                if (data.compareTo(node.data) < 0) {
                    node = node.left;
                } else {
                    node = node.right;
                }
            }

            if (data.compareTo(parent.data) < 0) {
                parent.left = new DisplayNode(data);
            } else {
                parent.right = new DisplayNode(data);
            }
        }

        /**
         * 在绘制树之前需要先调用该方法，为绘制准备数据
         * @return this
         */
        public DisplayTree prepareDisplaying() {
            this.size = this.calcCoordinate(this.root, 0, 0);
            return this;
        }

        /**
         * 使用中序遍历为节点建立坐标
         *
         * 为什么使用中序遍历？
         * 因为节点的横坐标在中序遍历中是线性增长的，具有和节点顺序相同的性质
         *
         * @param node 待建立坐标的节点
         * @param depth 节点的深度
         * @param counted 已计算的节点的数量
         * @return 水平的偏移量，等于节点总数
         */
        private int calcCoordinate(DisplayNode node, int depth, int counted) {
            if (node == null) {
                return counted;
            }

            counted = calcCoordinate(node.left, depth + 1, counted);
            node.xPos = counted++;
            node.yPos = depth;
            counted = calcCoordinate(node.right, depth + 1, counted);

            return counted;
        }

        /**
         * 计算树的宽度
         *
         * @return 树的宽度
         */
        public int calcWidth() {
            return this.size - 1;
        }

        /**
         * 计算树的高度，从0开始算高度
         *
         * @return 树的高度
         */
        public int calcHeight() {
            return calcHeight(root);
        }

        /**
         * 计算节点的高度，从0开始算高度
         *
         * @param node 待计算的节点
         * @return 节点的高度
         */
        private int calcHeight(DisplayNode node) {
            if (node == null) {
                return -1;
            }
            return 1 + Math.max(calcHeight(node.left), calcHeight(node.right));
        }

    }

    public static class DisplayNode {

        public String data;
        public boolean red;
        public DisplayNode left;
        public DisplayNode right;

        public int xPos;
        public int yPos;

        public DisplayNode(String data) {
            this.data = data;
        }

        public DisplayNode(String data, boolean red) {
            this.data = data;
            this.red = red;
        }

    }

}
