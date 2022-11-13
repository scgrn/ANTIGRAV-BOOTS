/*
 * The MIT License
 *
 * Copyright 2020 andrew.krause.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package combinecsv;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.border.EmptyBorder;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import mslinks.ShellLink;

/**
 *
 * @author andrew.krause
 */
public class CombineCSV {
  private static String executablePath;

  private boolean register() {
    Runtime rt = Runtime.getRuntime();
    
    // After this character-escaping morass, we should end up with something that looks like: 
    // [reg, add, "HKEY_CURRENT_USER\Software\Classes\Excel.CSV\shell\Combine CVS files\command", /ve, /d, "\"G:\TechCenter Utils\Antigrav Boots.exe\" \"%1\"", /f]
    String[] commands = {"reg",
      "add",
      "\"HKEY_CURRENT_USER\\Software\\Classes\\Excel.CSV\\shell\\Combine CVS files\\command\"",
      "/ve",
      "/d",
      "\"\\\"" + executablePath + "\\\" \\\"%1\\\"\"",
      "/f"
    };
    
    try {
      Process proc = rt.exec(commands);
      proc.waitFor();

      int exitCode = proc.exitValue();
      if (exitCode == 0) {
        // success!
        return true;
      } else {
        JOptionPane.showMessageDialog(null, "An error occured while attempting to update the registry.\nReg command exit code: " + exitCode, "DRAT", JOptionPane.ERROR_MESSAGE);
      }
    } catch (IOException | InterruptedException ex) {
      JOptionPane.showMessageDialog(null, "An error occured while attempting to update the registry. " + ex.toString(), "OH NO", JOptionPane.ERROR_MESSAGE);
    }

    return false;
  }
  
  private void done(String outputFile) {
    final JDialog dialog = new JDialog();
    
	javax.swing.JPanel messagePane = new javax.swing.JPanel();
    messagePane.setBorder(new EmptyBorder(20, 10, 20, 10));

    messagePane.add(new javax.swing.JLabel("CVS files combined."));
    dialog.getContentPane().add(messagePane);
    javax.swing.JPanel buttonPane = new javax.swing.JPanel();

    javax.swing.JButton button1 = new javax.swing.JButton("View combined file");
    buttonPane.add(button1);
    button1.addActionListener((ActionEvent e) -> {
      try {
        dialog.setVisible(false);
        dialog.dispose();
        java.awt.Desktop.getDesktop().open(new File(outputFile));
      } catch (IOException ex) {
        Logger.getLogger(CombineCSV.class.getName()).log(Level.SEVERE, null, ex);
      }
    });

    javax.swing.JButton button2 = new javax.swing.JButton("Close");
    buttonPane.add(button2);
    button2.addActionListener((ActionEvent e) -> {
      dialog.setVisible(false);
      dialog.dispose();
    });

    dialog.getContentPane().add(buttonPane, java.awt.BorderLayout.SOUTH);
    dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    dialog.pack();
    dialog.setTitle("OH YEAH!");
    dialog.setModal(true);

    dialog.setSize(260, 120);
    dialog.setResizable(false);
    dialog.setLocationRelativeTo(null);

    dialog.setVisible(true);
  }

  private void combine(ArrayList<String> fileList) {
    Boolean first = true;
    String[] header = {};
    List<String[]> combinedList = new ArrayList<>();

    for (String filename : fileList) {
      //  check extension is .csv
      if (!filename.endsWith(".csv")) {
        JOptionPane.showMessageDialog(null, filename + " is not a CSV file!", "VERY FUNNY.", JOptionPane.ERROR_MESSAGE);
      } else {
        try {
          FileReader reader = new FileReader(filename);

          RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
          CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(reader).withCSVParser(rfc4180Parser);
          CSVReader csvReader = csvReaderBuilder.build();

          List<String[]> list = csvReader.readAll();
          reader.close();
          csvReader.close();

          if (first) {
            //  read header
            header = new String[list.get(0).length];
            System.arraycopy(list.get(0), 0, header, 0, header.length);
          }

          //  check header matches
          if (header.length != list.get(0).length) {
            JOptionPane.showMessageDialog(null, "Header mismatch! Cannot combine files.", "HEY.", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
          }
          for (int i = 0; i < header.length; i++) {
            if (!header[i].equals(list.get(0)[i])) {
              JOptionPane.showMessageDialog(null, "Header mismatch! Cannot combine files.", "HEY.", JOptionPane.ERROR_MESSAGE);
              System.exit(1);
            }
          }

          //  remove header
          if (!first) {
            list.remove(0);
          }

          //  append csv data
          combinedList.addAll(list);

          first = false;
        } catch (FileNotFoundException ex) {
          Logger.getLogger(CombineCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.io.IOException ex) {
          Logger.getLogger(CombineCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    //  prompt for output file
    String outputFile = "combined.csv";
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);

    File firstFile = new File(fileList.get(0));
    chooser.setCurrentDirectory(new File(firstFile.getAbsolutePath()));

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setSelectedFile(new File(outputFile));

    int result = chooser.showSaveDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file != null) {
        outputFile = file.getPath();
      }

      if (file.exists()) {
        int dialogResult = JOptionPane.showConfirmDialog(null, "Output file exists! Overwrite?", "Hey.", JOptionPane.YES_NO_OPTION);
        if (dialogResult != JOptionPane.YES_OPTION) {
          return;
        }
      }

      try {
        //  write csv data
        FileWriter writer = new FileWriter(outputFile);
        CSVWriter csvWriter = new CSVWriter(writer);

        csvWriter.writeAll(combinedList);

        csvWriter.close();
        writer.close();

        done(outputFile);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "Error writing CSV: " + ex.toString(), "OH NO", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  @SuppressWarnings("DeadBranch")
  public static void main(String[] args) {
    //  look before you leap.
    if (true == false) {
      System.out.println("A catastrophic error has occurred. Terminating program.");
      System.exit(1);
    }

    //  set system l&f
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      JOptionPane.showMessageDialog(null, ex.getStackTrace(), ex.toString(), JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    //  make sure we're running on windows
    String os = System.getProperty("os.name").toLowerCase();
    if (!os.contains("win")) {
      JOptionPane.showMessageDialog(null, "I regret to inform you that this program is for Windows only.", "Beat it!", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    CombineCSV combiner = new CombineCSV();

    if (args.length == 0) {
      //  get location of running jar or exe
      try {
        String path = CombineCSV.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        executablePath = URLDecoder.decode(path, "UTF-8");
        executablePath = executablePath.substring(1).replace("/", "\\");
      } catch (UnsupportedEncodingException ex) {
        System.out.println(ex);
        Logger.getLogger(CombineCSV.class.getName()).log(Level.SEVERE, null, ex);
      }

      if (!combiner.register()) {
        System.exit(0);
      }
      
      //  copy shortcut to user folder
      String username = System.getProperty("user.name");
      String dest = "C:\\Users\\" + username + "\\AppData\\Roaming\\Microsoft\\Windows\\SendTo\\Combine CSV files....lnk";

      try {
        ShellLink.createLink(executablePath, dest);
      } catch (IOException ex) {
        Logger.getLogger(CombineCSV.class.getName()).log(Level.SEVERE, null, ex);
        JOptionPane.showMessageDialog(null, "Could not copy shortcut.\n\nSource: " + executablePath + "\nDest: " + dest, "UH OH", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }

      JOptionPane.showMessageDialog(null, "Installation succesful. Right-click a selection of CSV files from\nWindows Explorer and select Send To -> Combine CSV files.",
          "ANTIGRAV BOOTS", JOptionPane.INFORMATION_MESSAGE);
    } else {
      //  freak out if there's only one file selected.
      if (args.length == 1) {
        JOptionPane.showMessageDialog(null, "It doesn't make ANY sense to combine one file!\nPerhaps select multiple files?", "HEY.", JOptionPane.ERROR_MESSAGE);
      } else {
        //  do the thing
        ArrayList<String> fileList = new ArrayList();
        fileList.addAll(Arrays.asList(args));
        combiner.combine(fileList);
      }
    }
  }
}
