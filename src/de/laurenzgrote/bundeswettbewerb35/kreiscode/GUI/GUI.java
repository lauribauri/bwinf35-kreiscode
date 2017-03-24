package de.laurenzgrote.bundeswettbewerb35.kreiscode.GUI;

import de.laurenzgrote.bundeswettbewerb35.kreiscode.Coordinate;
import de.laurenzgrote.bundeswettbewerb35.kreiscode.ImageDecoder;
import de.laurenzgrote.bundeswettbewerb35.kreiscode.ImageMagickWrapper;
import de.laurenzgrote.bundeswettbewerb35.kreiscode.Kreismittelpunkte;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class GUI extends JFrame {
    // FileFilter für JFilceChooser
    private final FileFilter imageFilter = new FileNameExtensionFilter("Bilddateien", "png", "jpg", "jpeg", "gif");
    private final FileFilter dictFilter = new FileNameExtensionFilter("Wörterbuchdateien", "txt");

    // Buttons in der Swing-Oberfläche
    private final JMenuItem showSW;
    private final JMenuItem showCC;
    private final JMenuItem showTrap;
    private final JButton decodeButton;
    private final JButton selectDictButton;
    private final JLabel selectedDictLabel;
    private final JLabel selectedFileLabel;
    private final JButton selectFileButton;
    private final JCheckBox useImageMagick;
    private final ImagePanel imagePanel;

    // Farbiges Bild von der Platte
    private BufferedImage originalImage;
    private File selectedDict;

    private Kreismittelpunkte kreismittelpunkte;
    private ImageDecoder imageDecoder;

    // Konstruktor der die GUI erzeugt
    public GUI() {
        super("Kreiscode - Laurenz Grote");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // JMenu
        JMenuBar jMenuBar = new JMenuBar();
        showSW = new JMenuItem("Zeige S/W-Bild");
        showCC = new JMenuItem("Zeige Kreismittelpunkte");
        showTrap = new JMenuItem("Zeige Trapeze");
        jMenuBar.add(showCC);
        jMenuBar.add(showSW);
        jMenuBar.add(showTrap);

        // Center Panel
        imagePanel = new ImagePanel();

        // Einstellungen Panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Einstellungen"));
        settingsPanel.setPreferredSize(new Dimension(500, 800));
        settingsPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(4,4,4,4);

        JLabel dictLabel = new JLabel("Wörterbuch:");
        selectedDictLabel = new JLabel("BwInf-Default");
        selectDictButton = new JButton("Öffnen");

        JLabel fileLabel = new JLabel("Bild:");
        selectedFileLabel = new JLabel(".");
        selectFileButton = new JButton("Öffnen");
        useImageMagick = new JCheckBox("ImageMagick");

        decodeButton = new JButton("Dekodieren");

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        settingsPanel.add(dictLabel, c);

        c.gridx = 2;
        c.gridwidth = 3;
        c.weightx = 0.5;
        settingsPanel.add(selectedDictLabel, c);

        c.gridx = 5;
        c.gridwidth = 1;
        c.weightx = 0;
        settingsPanel.add(selectDictButton, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        settingsPanel.add(fileLabel, c);

        c.gridx = 2;
        c.gridwidth = 3;
        settingsPanel.add(selectedFileLabel, c);

        c.gridx = 5;
        c.gridwidth = 1;
        settingsPanel.add(selectFileButton, c);

        c.gridy = 3;
        c.gridx = 5;
        settingsPanel.add(useImageMagick, c);

        c.gridx = 0;
        c.gridy = 100;
        c.gridwidth = 100;
        settingsPanel.add(decodeButton, c);

        // Alles ins JFRAME
        this.add(jMenuBar, BorderLayout.NORTH);
        this.add(settingsPanel, BorderLayout.EAST);
        this.add(imagePanel, BorderLayout.CENTER);

        // Listeners drauf
        jMenuBarListeners();
        selectFileListener();
        selectDictListener();
        decodeListener();

        setVisible(true); // TADA!
    }

    private void jMenuBarListeners() {
        showSW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (kreismittelpunkte != null) {
                    CustomDialogs.showSWDialog(kreismittelpunkte.getSwImage());
                }
            }
        });
        showCC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (kreismittelpunkte != null) {
                    CustomDialogs.showCCDialog(kreismittelpunkte.getSwImage(), kreismittelpunkte.getCircleCenters());
                }
            }
        });
        showTrap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CustomDialogs.showTrapezialsDialog(kreismittelpunkte.getSwImage(), imageDecoder.getTrapezials());
            }
        });
    }

    // Dekodieren des ausgewählten Bildes
    private void decodeListener() {
        decodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Kreismittelpunkte ermitteln
                kreismittelpunkte = new Kreismittelpunkte(originalImage);
                    // Mit den Kreismittelpunkten die Beudeutung der Kreiscodes ermitteln
                    java.util.List<Coordinate> circleCenters = kreismittelpunkte.getCircleCenters();
                    boolean[][] swImage = kreismittelpunkte.getSwImage();
                    // Dekodieren des Bildes
                    imageDecoder = new ImageDecoder(swImage, circleCenters, selectedDict);
                    circleCenters = imageDecoder.getUpdatedCircleCenters();
                    // Einzeichnen der Ergebnisse
                    for (int i = 0; i < circleCenters.size(); i++) {
                        Coordinate c = circleCenters.get(i);
                        imagePanel.addText(c.getX(), c.getY(), c.getCircleMeaning());
                    }
            }
        });
    }

    // Auswahl der Bilddatei
    private void selectFileListener() {
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(imageFilter);
                int returnVal = jFileChooser.showOpenDialog(GUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = jFileChooser.getSelectedFile();
                    try {
                        // Muss der Imagemagick-Wrapper benutzt werden?
                        if (useImageMagick.isSelected()) {
                            originalImage = ImageMagickWrapper.read(file);
                        } else {
                            originalImage = ImageIO.read(file);
                        }
                        // Schreiben des Dateinamens in die GUI
                        selectedFileLabel.setText(file.getName());
                        // Laden des Bildes
                        imagePanel.setImage(originalImage);
                    } catch (IOException|InterruptedException e) {
                        System.err.println("Kritischer Fehler beim Einlesen der Bilddatei");
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Bildeinlesevorgang durch Nutzer abgebrochen");
                }
            }
        });
    }

    // Auswahl der Wörterbuchdatei
    private void selectDictListener() {
        selectDictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(dictFilter);
                int returnVal = jFileChooser.showOpenDialog(GUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = jFileChooser.getSelectedFile();
                    // Abspeichern der URL des Wörterbuches in einem Feld
                    selectedDict = file;
                    String filename = file.toString();
                    // Schreiben des Dateinamens in die GUI
                    selectedDictLabel.setText(filename);
                } else {
                    System.err.println("Abbruch");
                }
            }
        });
    }
}
