package nro.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame quản lý Anti-DDoS & Server
 */
public class AntiDdosFrame extends JFrame {

    // Thêm instance để bên ngoài truy cập
    public static AntiDdosFrame instance;

    private JLabel statusLabel;
    private JTextArea logArea;
    private JButton btnEnable;
    private JButton btnDisable;
    private ChartPanel chartPanel;

    // Tab quản lý cổng
    private DefaultListModel<String> portListModel;
    private JList<String> portList;
    private JSpinner thresholdSpinner;

    // Nút quản lý server
    public JButton btnBaoTri;
    public JButton btnDaAll;
    public JButton btnItemRecovery;  // ✅ THÊM NÚT MỚI
    private JTextArea serverLogArea;

    public AntiDdosFrame() {
        instance = this; // Gán instance khi khởi tạo

        setTitle("Anti-DDoS & Quản lý Server");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tab chính
        JTabbedPane tabbedPane = new JTabbedPane();

        // ================= Tab Anti-DDoS =================
        JPanel antiDdosPanel = new JPanel(new BorderLayout(10, 10));
        antiDdosPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Trạng thái: Chưa kích hoạt");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.RED);

        btnEnable = new JButton("Bật Anti-DDoS");
        btnEnable.setBackground(new Color(46, 204, 113));
        btnEnable.setForeground(Color.WHITE);
        btnEnable.setFont(new Font("Arial", Font.BOLD, 14));
        btnEnable.addActionListener(this::enableAntiDdos);

        btnDisable = new JButton("Tắt Anti-DDoS");
        btnDisable.setBackground(new Color(231, 76, 60));
        btnDisable.setForeground(Color.WHITE);
        btnDisable.setFont(new Font("Arial", Font.BOLD, 14));
        btnDisable.addActionListener(this::disableAntiDdos);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.add(statusLabel);
        topPanel.add(btnEnable);
        topPanel.add(btnDisable);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(800, 300));

        antiDdosPanel.add(topPanel, BorderLayout.NORTH);
        antiDdosPanel.add(scrollPane, BorderLayout.CENTER);
        antiDdosPanel.add(chartPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Cổng bảo vệ (Anti-DDoS)", antiDdosPanel);

        // ================= Tab Quản lý Server =================
        JPanel serverPanel = new JPanel(new BorderLayout(10, 10));
        serverPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ✅ THAY ĐỔI: Từ GridLayout(1, 2) -> GridLayout(2, 2) để có 3 nút
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));

        btnBaoTri = new JButton("🔧 Bảo Trì Máy Chủ");
        btnBaoTri.setFont(new Font("Arial", Font.BOLD, 14));
        btnBaoTri.setBackground(new Color(241, 196, 15));
        btnBaoTri.setFocusPainted(false);

        btnDaAll = new JButton("⚠️ Đá All Player");
        btnDaAll.setFont(new Font("Arial", Font.BOLD, 14));
        btnDaAll.setBackground(new Color(231, 76, 60));
        btnDaAll.setForeground(Color.WHITE);
        btnDaAll.setFocusPainted(false);

        // ✅ NÚT THU HỒI VẬT PHẨM
        btnItemRecovery = new JButton("🔄 Thu Hồi Vật Phẩm");
        btnItemRecovery.setFont(new Font("Arial", Font.BOLD, 14));
        btnItemRecovery.setBackground(new Color(52, 152, 219));
        btnItemRecovery.setForeground(Color.WHITE);
        btnItemRecovery.setFocusPainted(false);

        buttonPanel.add(btnBaoTri);
        buttonPanel.add(btnDaAll);
        buttonPanel.add(btnItemRecovery);

        // Log nhỏ trong tab quản lý server
        serverLogArea = new JTextArea();
        serverLogArea.setEditable(false);
        JScrollPane serverLogScroll = new JScrollPane(serverLogArea);
        serverLogScroll.setPreferredSize(new Dimension(800, 150));

        serverPanel.add(buttonPanel, BorderLayout.NORTH);
        serverPanel.add(serverLogScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Quản lý Server", serverPanel);

        // ================= Tab quản lý cổng bảo vệ =================
        JPanel portPanel = new JPanel(new BorderLayout(10, 10));
        portPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        portListModel = new DefaultListModel<>();
        portList = new JList<>(portListModel);
        JScrollPane portScroll = new JScrollPane(portList);

        JPanel portControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField txtPort = new JTextField(6);
        JButton btnAddPort = new JButton("Thêm cổng");
        JButton btnRemovePort = new JButton("Xoá cổng");

        btnAddPort.addActionListener(e -> {
            String port = txtPort.getText().trim();
            if (!port.isEmpty() && !portListModel.contains(port)) {
                portListModel.addElement(port);
                appendLog("Đã thêm cổng bảo vệ: " + port);
            }
        });

        btnRemovePort.addActionListener(e -> {
            String selected = portList.getSelectedValue();
            if (selected != null) {
                portListModel.removeElement(selected);
                appendLog("Đã xoá cổng bảo vệ: " + selected);
            }
        });

        portControl.add(new JLabel("Cổng:"));
        portControl.add(txtPort);
        portControl.add(btnAddPort);
        portControl.add(btnRemovePort);

        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 100, 10));
        thresholdPanel.add(new JLabel("Ngưỡng nhận diện DDoS (req/s):"));
        thresholdPanel.add(thresholdSpinner);

        portPanel.add(portScroll, BorderLayout.CENTER);
        portPanel.add(portControl, BorderLayout.NORTH);
        portPanel.add(thresholdPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Cổng đang được bảo vệ", portPanel);

        add(tabbedPane);

        // Khởi động Timer (giữ nguyên logic)
        initTimer();
    }

    private void initTimer() {
        Timer timer = new Timer(1000, e -> {
            // ở đây chỉ test giả lập random online
            int online = (int) (Math.random() * 200);
            chartPanel.addData(online); // update chart
        });
        timer.start();
    }

    private void enableAntiDdos(ActionEvent e) {
        statusLabel.setText("Trạng thái: Đang kích hoạt");
        statusLabel.setForeground(Color.GREEN);
        appendLog("Anti-DDoS đã bật.");
    }

    private void disableAntiDdos(ActionEvent e) {
        statusLabel.setText("Trạng thái: Chưa kích hoạt");
        statusLabel.setForeground(Color.RED);
        appendLog("Anti-DDoS đã tắt.");
    }

    // Log chung
    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // Log riêng trong tab server
    public void appendServerLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            serverLogArea.append(msg + "\n");
            serverLogArea.setCaretPosition(serverLogArea.getDocument().getLength());
        });
    }

    // Panel vẽ biểu đồ online
    private static class ChartPanel extends JPanel {
        private final List<Integer> data = new ArrayList<>();

        public void addData(int value) {
            if (data.size() > 60) { // lưu 60 giây gần nhất
                data.remove(0);
            }
            data.add(value);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLACK);
            g2.drawLine(40, getHeight() - 30, getWidth() - 10, getHeight() - 30); // trục X
            g2.drawLine(40, 20, 40, getHeight() - 30); // trục Y

            int max = data.stream().max(Integer::compare).orElse(1);
            int xStep = (getWidth() - 60) / 60;
            int yMax = getHeight() - 60;

            g2.setColor(new Color(52, 152, 219));
            for (int i = 1; i < data.size(); i++) {
                int x1 = 40 + (i - 1) * xStep;
                int y1 = getHeight() - 30 - (data.get(i - 1) * yMax / max);
                int x2 = 40 + i * xStep;
                int y2 = getHeight() - 30 - (data.get(i) * yMax / max);
                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AntiDdosFrame().setVisible(true));
    }
}