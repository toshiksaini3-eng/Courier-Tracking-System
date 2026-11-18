package com.couriertracking.ui.panel;

import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Academic College Demonstration & B.Tech Java OOP Presentation Panel.
 * Comprehensively documents OOP concepts, Design Patterns, Multithreading, and Architecture.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Hero Card
        CardPanel heroCard = new CardPanel();
        heroCard.setLayout(new BorderLayout(16, 0));
        heroCard.setBackground(new Color(239, 246, 255));
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JPanel heroText = new JPanel();
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.setOpaque(false);

        JLabel lblTitle = new JLabel("🚚 Courier Tracking & Logistics Management System");
        lblTitle.setFont(UIConstants.FONT_HEADER_LARGE);
        lblTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("B.Tech Computer Science & Engineering — Java Object-Oriented Programming (OOP) Capstone Project");
        lblSub.setFont(UIConstants.FONT_BODY_BOLD);
        lblSub.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        JLabel lblVivaNote = new JLabel("Designed and developed for academic viva demonstration with full layered architecture, MySQL database, and live multithreading.");
        lblVivaNote.setFont(UIConstants.FONT_BODY);
        lblVivaNote.setForeground(UIConstants.COLOR_TEXT_MUTED);

        heroText.add(lblTitle);
        heroText.add(Box.createVerticalStrut(4));
        heroText.add(lblSub);
        heroText.add(Box.createVerticalStrut(6));
        heroText.add(lblVivaNote);

        heroCard.add(heroText, BorderLayout.CENTER);

        // 2. OOP Concepts Grid (4 Pillar Cards)
        JPanel oopGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        oopGrid.setOpaque(false);
        oopGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        oopGrid.add(createOopCard("1. ENCAPSULATION",
                "• Strict private data hiding across all domain entities (Parcel, Branch, TrackingHistory).\n" +
                "• Getter and setter accessor methods with input validation.\n" +
                "• Internal state transitions controlled via domain rules preventing external corruption."));

        oopGrid.add(createOopCard("2. INHERITANCE",
                "• Reusable UI component hierarchy extending Swing classes (CardPanel, ModernTable, StatusBadge).\n" +
                "• Custom Exception hierarchy: CourierException -> DatabaseException, ValidationException, InvalidStatusTransitionException.\n" +
                "• Code reuse and polymorphic dispatch through base class extension."));

        oopGrid.add(createOopCard("3. POLYMORPHISM",
                "• DAO Interface implementations (BranchDao -> BranchDaoImpl, ParcelDao -> ParcelDaoImpl).\n" +
                "• Method overriding across toString(), equals(), paintComponent(), and Swing table renderers.\n" +
                "• Dynamic method dispatch and flexible decoupled service components."));

        oopGrid.add(createOopCard("4. ABSTRACTION",
                "• Clean layered separation: UI Layer -> Service Layer -> DAO Layer -> Database Engine.\n" +
                "• UI has zero knowledge of raw SQL or database connection details.\n" +
                "• Database connectivity abstraction supporting both MySQL 8 and zero-setup fallback."));

        // 3. Advanced Engineering & Technical Highlights Card
        CardPanel techCard = new CardPanel();
        techCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTechTitle = new JLabel("⚙ Advanced Java & Software Engineering Highlights");
        lblTechTitle.setFont(UIConstants.FONT_TITLE);
        lblTechTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        String techDetails = 
                "✓ JAVA MULTITHREADING & CONCURRENCY:\n" +
                "  - Implemented background worker threads (ExecutorService) for simulated real-time parcel transit.\n" +
                "  - Strict Event Dispatch Thread (EDT) synchronization using SwingUtilities.invokeLater() to prevent GUI freezing.\n" +
                "  - Thread-safe unique sequence generation using AtomicInteger.\n\n" +
                "✓ JDBC & MYSQL 8 DATABASE ARCHITECTURE:\n" +
                "  - 100% Parameterized queries using PreparedStatement to eliminate SQL Injection vulnerabilities.\n" +
                "  - ResultSet row mapping, foreign key relational constraints, and multi-statement transaction management.\n" +
                "  - Singleton Database Connection Pattern ensuring optimal resource utilization.\n\n" +
                "✓ ROBUST EXCEPTION HANDLING & VALIDATION:\n" +
                "  - Try-with-resources ensuring leak-free automatic closing of SQL connections, statements, and streams.\n" +
                "  - Regex-based phone and input validation, business rule checking, and state machine transition validation.\n\n" +
                "✓ SOFTWARE DESIGN PATTERNS APPLIED:\n" +
                "  - Data Access Object (DAO) Pattern, Singleton Pattern, Factory Pattern, Strategy Pattern, MVC Layered Pattern.";

        JTextArea txtTech = new JTextArea(techDetails);
        txtTech.setFont(UIConstants.FONT_CODE);
        txtTech.setEditable(false);
        txtTech.setOpaque(false);
        txtTech.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        techCard.add(lblTechTitle, BorderLayout.NORTH);
        techCard.add(txtTech, BorderLayout.CENTER);

        // 4. Project Credentials & Viva Info Card
        CardPanel credCard = new CardPanel();
        credCard.setLayout(new GridLayout(1, 3, 20, 0));

        credCard.add(createMetaColumn("🎓 ACADEMIC METADATA", "Course: B.Tech Computer Science\nSubject: Java & OOP Mini-Project\nAcademic Year: 2026\nSemester: 5th / 6th Semester"));
        credCard.add(createMetaColumn("💻 TECHNOLOGY STACK", "Language: Java 17+ (SE Runtime)\nUI Framework: Java Swing + FlatLaf\nDatabase: MySQL 8 (Connector/J 8.4)\nBuild Tool: Maven & VS Code"));
        credCard.add(createMetaColumn("🏢 LOGISTICS DOMAIN", "Consignment ID: Auto-Generated\nLifecycle States: 8 Validated Stages\nModules: Booking, Tracking, Reports\nStatus Engine: State Machine"));

        // Assemble Content
        scrollContent.add(heroCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(oopGrid);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(techCard);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(credCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private CardPanel createOopCard(String title, String bulletPoints) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JTextArea area = new JTextArea(bulletPoints);
        area.setFont(UIConstants.FONT_BODY);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(UIConstants.COLOR_TEXT_SECONDARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        card.add(lbl, BorderLayout.NORTH);
        card.add(area, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetaColumn(String header, String content) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel h = new JLabel(header);
        h.setFont(UIConstants.FONT_BODY_BOLD);
        h.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JTextArea txt = new JTextArea(content);
        txt.setFont(UIConstants.FONT_BODY);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        p.add(h);
        p.add(Box.createVerticalStrut(6));
        p.add(txt);
        return p;
    }
}
