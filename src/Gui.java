import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class Gui {

    int frameWidth = 600;
    int frameHeight = 600;
    JFrame frame = new JFrame("goated tuner");

    JPanel titlePanel = new JPanel();
    JPanel noteAndCentsPanel = new JPanel();
    JPanel frequencyPanel = new JPanel();

    JLabel title = new JLabel();
    JLabel frequencyText = new JLabel();
    JLabel noteNameText = new JLabel();
    JLabel centsLabelText = new JLabel();
    JLabel centsValueText = new JLabel();
    

    GridBagConstraints constraints = new GridBagConstraints();

    Gui () {
        frame.setVisible(true);
        frame.setSize(frameWidth, frameHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setBackground(Color.darkGray);
        frame.getContentPane().setBackground(Color.darkGray);

        title.setBackground(Color.DARK_GRAY);
        title.setForeground(Color.green);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("Comic Sans MS", Font.BOLD, 50));
        title.setText("epic tuner");
        title.setOpaque(true);

        noteNameText.setBackground(Color.gray);
        noteNameText.setForeground(Color.white);
        noteNameText.setHorizontalAlignment(JLabel.CENTER);
        noteNameText.setFont(new Font("Comic Sans MS", Font.BOLD, 200));
        noteNameText.setText("<html>A<sub>4<sub></html>");
        noteNameText.setOpaque(true);

        frequencyText.setBackground(Color.gray);
        frequencyText.setForeground(Color.white);
        frequencyText.setHorizontalAlignment(JLabel.CENTER);
        frequencyText.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
        frequencyText.setText("440");
        frequencyText.setOpaque(true);

        // centsLabelText.setBackground(Color.gray);
        // centsLabelText.setForeground(Color.white);
        // centsLabelText.setHorizontalAlignment(JLabel.CENTER);
        // centsLabelText.setVerticalAlignment(JLabel.BOTTOM);
        // centsValueText.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
        // centsLabelText.setText("CENTS: ");
        
        centsValueText.setBackground(Color.gray);
        centsValueText.setForeground(Color.white);
        centsValueText.setHorizontalAlignment(JLabel.CENTER);
        centsValueText.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
        centsValueText.setText("<html>Cents: <br>0.0</html>");
        centsValueText.setOpaque(true);

        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(title);

        noteAndCentsPanel.setLayout(new BorderLayout());
        noteAndCentsPanel.add(noteNameText);
        noteAndCentsPanel.add(centsValueText, BorderLayout.EAST);
        
        frequencyPanel.setLayout(new BorderLayout());
        frequencyPanel.add(frequencyText);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        frame.add(titlePanel, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        // constraints.weightx = 1.0;
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.weighty = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        frame.add(noteNameText, constraints);
        

        constraints.gridy = 1;
        constraints.gridx = 1;
        constraints.weighty = 0;
        constraints.weighty = 0;
        constraints.gridheight = 1;
        // frame.add(centsLabelText, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 2;
        constraints.gridx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        frame.add(centsValueText, constraints);


        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        frame.add(frequencyPanel, constraints);
    }

    void updateFrequency(double frequency, String noteAndOctave, double cents){
        frequencyText.setText("Hertz: " + Double.toString(frequency));
        noteNameText.setText(noteAndOctave);
        centsValueText.setText("<html> Cents: <br>" + Double.toString(cents) + "</html");


    }

    public void loudEnoughColor(boolean loudEnough) {
        if (loudEnough){
            frequencyText.setBackground(Color.blue);
        } else {
            frequencyText.setBackground(Color.gray);
        }    }



}