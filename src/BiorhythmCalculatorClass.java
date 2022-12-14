import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class BiorhythmCalculatorClass
{
    private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private final int[] daysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy"); //Format day/month/year
    private final LocalDate currentDate = LocalDate.now();
    private final Font titleFont = new Font("Verdana", Font.BOLD, 22);
    private final Font normalFont = new Font("Verdana", Font.PLAIN, 16);
    private final Font smallFont = new Font("Verdana", Font.PLAIN, 14);

    // Declared here as these components need to be initialized, updated and reset in different parts of the program
    private DefaultTableModel model;
    private JTextField bDayTextField;
    private JComboBox<String> bMonth;
    private JTextField bYearTextField;
    private JTextField targetDayTextField;
    private JComboBox<String> targetMonth;
    private JTextField targetYearTextField;
    private JPanel centerPanel;
    private JFreeChart chart;

    // variable responsible for storing the difference between the two dates in days
    private int dayDifference;

    public static void main(String[] args)
    {
        try
        {
        	// Adjusts the program's components to be of the same design language as the operating system that the program runs on
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "UIManager failed to load the setLookAndFeel method!", "Error!", JOptionPane.ERROR_MESSAGE);
        }
        
        new BiorhythmCalculatorClass();
    }

    BiorhythmCalculatorClass()
    {
        tutorialScreen();
    }

    void tutorialScreen()
    {
        // Initializes the tutorial window
        JFrame frame = new JFrame("Welcome!");

        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initializes the title
        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 30));

        // Initializes the Panel which will contain the image and the text
        JPanel centerPanel = new JPanel();

        JLabel text = new JLabel(
        		"<html>Biorhythms, as pseudoscience, were developed by Wilhelm Fliess in the late 19th century,<br/>" +
                "and were popularized in the United States in the late 1970s.<br/><br/>" +
                "The biorhythm theory suggests that humans have 3 biological cycles:<br/><br/>"
                + "<font color='red'>Physical - coordination, strength, well-being</font><br/>"
                + "<font color='green'>Emotional - creativity, sensitivity, mood</font><br/>"
                + "<font color='blue'>Intellectual - alertness, memory, analytical functioning</font><br/><br/>"
                + "All the cycles begin at birth, and have a sinusoidal pattern<br/>"
                + "All the cycles have values ranging from 1 to -1<br/>"
                + "The higher the value, the better you will do in that particular area<br/>"
                + "The lower the cycle, the more difficult your life will be<br/><br/>"
                + "Want to know your biorhythm?<br/>"
                + "The knowledge is one 'continue' press away!<html>");

        text.setFont(normalFont);

        // Initializes the image
        JLabel img = new JLabel();

        try
        {
            ImageIcon temporary = new ImageIcon(new URL("https://miro.medium.com/max/512/1*XGk-4-N5QFRC15NVvFHkzQ.jpeg"));
            img.setIcon(new ImageIcon(temporary.getImage().getScaledInstance(500, 350, Image.SCALE_SMOOTH)));
        }
        catch (MalformedURLException e)
        {
            JOptionPane.showMessageDialog(null, "Error!", "Tutorial image URL could not be found!", JOptionPane.ERROR_MESSAGE);
        }

        centerPanel.add(img);
        centerPanel.add(text);

        JButton continueBtn = new JButton("Continue");
        continueBtn.setBackground(Color.GREEN);
        continueBtn.setFont(titleFont);
        
        // Launches the program after the 'continue' button is pressed
        continueBtn.addActionListener(e -> {
            frame.dispose();
            initializeJFrame();
            resetComponents();
        });

        frame.add(title, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(continueBtn, BorderLayout.SOUTH);

        frame.setCursor(initializeCursor());
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    void initializeJFrame()
    {
        JFrame f = new JFrame("Biorhythm Calculator");
        f.setBackground(Color.BLACK);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setCursor(initializeCursor());

        
        // splits the window in half vertically
        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initializeLeftSide(), initializeRightSide());
        // makes the middle divider impossible to move around
        mainPanel.setEnabled(false);
        
        // adds all the components of the window (buttons, labels, text fields)
        f.add(mainPanel);

        f.pack();
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.setVisible(true);
    }

    // Sets a custom cursor for the program
    Cursor initializeCursor()
    {
        try 
        {
        	Image image = new ImageIcon(new URL("https://img.icons8.com/fluency/96/cursor--v2.png")).getImage();
        	return Toolkit.getDefaultToolkit().createCustomCursor(image , new Point(0,0), "img");
        } 
        catch (MalformedURLException e1) 
        {
            System.out.println("The cursor image could not be set! (initializeCursor())");
            return null;
        }
    }

    // Calculates the user's biorhythm given the difference between the two dates
    Object[] calculate(String bFullDate, String tFullDate) {

        // DIFFERENCE BETWEEN DATES //

        // Converts birthdate and target date into readable format for java.time
        LocalDateTime date1 = LocalDate.parse(bFullDate, dtf).atStartOfDay();
        LocalDateTime date2 = LocalDate.parse(tFullDate, dtf).atStartOfDay();

        // Calculates the difference of days between birthdate and target date
        dayDifference = (int) Duration.between(date1, date2).toDays();

        // UPDATES THE GRAPH //
        createGraph();

        // CALCULATE THE BIORHYTHMS FOR TABLE //

        Object[] results = new Object[4];

        // sets the first cell of the table equal to the target date (adjust as dates change)
        results[0] = date2.getDayOfMonth() + " " + date2.getMonth().toString().substring(0, 3);

        // Calculates the three biorhythms and rounds their values to four decimal places
        for (int l = 1; l < 4; l++)
            results[l] = (double) Math.round(Math.sin((2 * Math.PI * dayDifference) / (23 + 5 * (l-1))) * 10000) / 10000;

        return results;
    }


    // Resets all the text fields and the JTable when the 'clear' button is pressed
    void resetComponents()
    {
        // Set the birthday text fields equal to the first of January, 2000
        bDayTextField.setText("1");
        bMonth.setSelectedIndex(0);
        bYearTextField.setText("1999");

        // Set the target text fields equal to the current date
        targetDayTextField.setText(Integer.toString(currentDate.getDayOfMonth()));
        targetMonth.setSelectedIndex(currentDate.getMonthValue()-1);
        targetYearTextField.setText(Integer.toString(currentDate.getYear()));

        // Clears the JTable
        for(int i = 0; i < 4; i++)
            model.setValueAt(null, 0, i);

        // RESETS THE GRAPH //
        dayDifference = 0;
        createGraph();
    }

    
    @SuppressWarnings("serial")
	// Initializes the right side of the JFrame
    JComponent initializeRightSide()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        centerPanel = new JPanel(new BorderLayout());

        // "Results" Label //
        {
            JLabel resultsLabel = new JLabel("Results", SwingConstants.CENTER);
            resultsLabel.setForeground(Color.WHITE);
            resultsLabel.setFont(titleFont);
            mainPanel.add(resultsLabel, BorderLayout.NORTH);
        }

        // TABLE //
        {
            JPanel tablePanel = new JPanel (new BorderLayout());

            // Initialize the model of the Table (header + one row)
            model = new DefaultTableModel(new String[] {"Date",
                    "<html><font color='red'>Physical</font><html>",
                    "<html><font color='blue'>Emotional</font><html>",
                    "<html><font color='green'>Intellectual</font><html>"}, 1)
            {
                // Overrides the 'isCellEditable' method of the JTable, which by default, allows the user to edit the contents of a cell in a JTable
                // Instead, this overridden method restricts the user from editing the JTable
                @Override
                public boolean isCellEditable(int row, int column) 
                {
                    //all cells false
                    return false;
                }
            };

            JTable table = new JTable(model)
            {
                // Overrides the 'prepareRenderer' method of the JTable, which by default, renders every cell of the JTable a white color
                // Instead, this overridden method changes the cell color depending on the value that it contains
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int columnIndex)
                {
                    // Accesses the 'prepareRenderer' method of the superclass (JTable) that has not been overridden and stores its results into a JComponent for further manipulation
                    // In other words, this method returns the standard, plain white version of a cell in a JTable
                    JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                    // Check the values of all the cells except the first one, which contains the date
                    if(columnIndex == 0)
                    {
                        component.setBackground(Color.WHITE);
                    }
                    else
                    {
                        try
                        {
                            double value = Double.parseDouble(getValueAt(rowIndex, columnIndex).toString());

                            if(value > 0.3)
                                component.setBackground(Color.GREEN);
                            else if (value < - 0.3)
                                component.setBackground(Color.RED);
                            else
                                component.setBackground(Color.YELLOW);
                        }
                        catch(NullPointerException e)
                        {
                            // No need to print out an error, as this exception will only occur when the JTable is empty
                        }
                    }
                    return component;
                }
            };

            table.setRowSelectionAllowed(false);
            table.setShowGrid(false);
            table.setFont(smallFont);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setFont(smallFont);

            tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
            tablePanel.add(table, BorderLayout.CENTER);

            centerPanel.add(tablePanel, BorderLayout.NORTH);
        }


        // GRAPH //
        createGraph();
        // the graph is added to the centerPanel is the createGraph() function

        mainPanel.add(centerPanel, BorderLayout.CENTER);


        // BUTTONS //

        {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.BLACK);

            JButton saveBtn = new JButton("Save");
            JButton clearBtn = new JButton("Clear");

            saveBtn.setFont(normalFont);
            clearBtn.setFont(normalFont);

            saveBtn.addActionListener(e -> {
                try
                {
                    final File file = new File("Chart.png");
                    ChartUtilities.saveChartAsPNG(file, chart, 400, 300);
                    JOptionPane.showMessageDialog(null, "Chart Successfully Saved to " + file.getCanonicalPath());

                }
                catch (IOException e1)
                {
                    JOptionPane.showMessageDialog(null, "Could not save file!", "Error!", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Calls the 'reset()' function when pressed
            clearBtn.addActionListener(e -> resetComponents());

            buttonPanel.add(saveBtn);
            buttonPanel.add(clearBtn);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        return mainPanel;
    }

    void createGraph()
    {
    	// creates three functions
        XYSeries phys = new XYSeries("Physical");
        XYSeries emo = new XYSeries("Emotional");
        XYSeries intel = new XYSeries("Intellectual");

        // adds the data points to the function
        for (double x = 0.0; x < 10; x += 0.1)
        {
            phys.add(x, Math.sin((2 * Math.PI * (x + dayDifference))/23));
            emo.add(x, Math.sin((2 * Math.PI * (x + dayDifference))/28));
            intel.add(x, Math.sin((2 * Math.PI * (x + dayDifference))/33));
        }

        // stores all functions in a shared group
        XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(phys);
        dataset.addSeries(emo);
        dataset.addSeries(intel);

        // Chart title, X axis, Y axis, dataset used, plot orientation, legend, tooltips, urls
        chart = ChartFactory.createXYLineChart(null, "Date (From the Target Date)", null, dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.getXYPlot().setDataset(chart.getXYPlot().getDataset());

        ChartPanel chartPanel = new ChartPanel(chart);

        centerPanel.removeAll();
        centerPanel.revalidate();
        centerPanel.add(chartPanel, BorderLayout.CENTER);
        centerPanel.repaint();
    }

    // Initializes the left-side-panel of the JFrame
    JComponent initializeLeftSide()
    {
        // COMPONENT INITIALIZATION //

        JPanel leftSidePanel = new JPanel(new GridBagLayout());
        leftSidePanel.setBackground(Color.BLACK);

        // Image displayed at the top of the screen
        JLabel imgFinal = new JLabel();

        try
        {
            ImageIcon img2 = new ImageIcon(new URL("https://sun9-55.userapi.com/impf/mw5y60DKKbsWUa_IVT6JM4Yd3L7lTR46K-xzkQ/TotfrMQj4X8.jpg?size=600x335&quality=96&sign=905b294e882c2307e9f6bdd8a2a02935&type=album"));
            imgFinal.setIcon(new ImageIcon(img2.getImage().getScaledInstance(300, 150, Image.SCALE_SMOOTH)));
        }
        catch (MalformedURLException e1)
        {
            JOptionPane.showMessageDialog(null, "Error!", "Image URL could not be found!", JOptionPane.ERROR_MESSAGE);
        }

        // Birthday Input Section
        JLabel bDayLabel = new JLabel("Birthdate");
        bDayLabel.setForeground(Color.WHITE);
        bDayLabel.setFont(titleFont);
        
        {
            bDayTextField = new JTextField(5);
            bDayTextField.setFont(smallFont);

            bMonth = new JComboBox<>(months);
            bMonth.setFont(smallFont);

            bYearTextField = new JTextField(5);
            bYearTextField.setFont(smallFont);
        }

        // Target Date Input Section
        JLabel targetDayLabel = new JLabel("Target Date");
        targetDayLabel.setForeground(Color.WHITE);
        targetDayLabel.setFont(titleFont);
        
        {
            targetDayTextField = new JTextField(5);
            targetDayTextField.setFont(smallFont);

            targetMonth = new JComboBox<>(months);
            targetMonth.setFont(smallFont);

            targetYearTextField = new JTextField(5);
            targetYearTextField.setFont(smallFont);
        }

        // The 'Calculate' button
        JButton calcButton = new JButton("Calculate");
        calcButton.setFont(titleFont);

        // BUTTON PRESS //
        calcButton.addActionListener(e -> {
            try
            {
                // ERROR CHECKING //

                // Converts all the user's inputs into integers to verify that they are valid
                int bDayInt = Integer.parseInt(bDayTextField.getText());
                int targetDayInt = Integer.parseInt(targetDayTextField.getText());

                // Converts the name of the month selected into the number of said month
                int bDayMonthInt = bMonth.getSelectedIndex() + 1;
                int targetMonthInt = targetMonth.getSelectedIndex() + 1;

                int bYearInt = Integer.parseInt(bYearTextField.getText());
                int targetYearInt = Integer.parseInt(targetYearTextField.getText());

                /*
                 * Four key conditions are checked:
                 * You cannot enter yourself to be older than the oldest person alive
                 * You cannot enter your birthday to be after your target date in terms of the same year and different months
                 * You cannot enter your birthday to be after your target date in terms of the same year, month and different days
                 */

                if(bYearInt < 1903 ||
                        bYearInt > targetYearInt ||
                        ((bYearInt == targetYearInt) && (targetMonthInt < bDayMonthInt)) ||
                        ((bYearInt == targetYearInt) && (bDayMonthInt == targetMonthInt) && (targetDayInt < bDayInt)))
                    throw new NumberFormatException();

                // if the user has not entered a reasonable date, throw an error message
                if(checkIfInputValid(bDayInt, bDayMonthInt, bYearInt) || checkIfInputValid(targetDayInt, targetMonthInt , targetYearInt))
                    throw new NumberFormatException();


                // FORMATTING //

                // To calculate the difference in days between the two dates given, the formatting has to be very precise
                // If the date or month has a number less than 10, add a zero in front of it: (ex. 01, 02)
                String bDayFinal = bDayInt < 10 ? ("0" + bDayInt) : (String.valueOf(bDayInt));
                String targetDayFinal = targetDayInt < 10 ? ("0" + targetDayInt) : (String.valueOf(targetDayInt));
                String bMonthFinal = bDayMonthInt < 10 ? ("0" + bDayMonthInt) : (String.valueOf(bDayMonthInt));
                String targetMonthFinal = targetMonthInt < 10 ? ("0" + targetMonthInt) : (String.valueOf(targetMonthInt));

                // Combines all the inputs into two strings that will be later used by the calculate() function
                String bDateFinal = bDayFinal + " " + bMonthFinal + " " + bYearInt;
                String targetDateFinal = targetDayFinal + " " + targetMonthFinal + " " + targetYearInt;


                // CALCULATIONS //

                // If the user has entered valid inputs, their biorhythm is calculated
                Object[] results = calculate(bDateFinal, targetDateFinal);

                // OUTPUT //

                // Updates the JTable
                for(int i = 0; i < 4; i++)
                    model.setValueAt(results[i], 0, i);

            }
            catch (NumberFormatException exception)
            {
                JOptionPane.showMessageDialog(null, "Invalid Input Entered!", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        });


        // COMPONENT PLACEMENT//

        GridBagConstraints gbc = new GridBagConstraints();

        // Image
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftSidePanel.add(imgFinal, gbc);

        // Birthday Label
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15,0,0,0);  //top padding
        leftSidePanel.add(bDayLabel, gbc);

        // Date Label
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5,0,0,0);  //top padding
        {
            JLabel day = new JLabel("Day");
            day.setFont(smallFont);
            day.setForeground(Color.WHITE);
            leftSidePanel.add(day, gbc);
        }


        // Month Label
        gbc.gridx = 1;
        {
            JLabel month = new JLabel("Month");
            month.setFont(smallFont);
            month.setForeground(Color.WHITE);
            leftSidePanel.add(month, gbc);
        }

        // Year Label
        gbc.gridx = 2;
        {
            JLabel year = new JLabel("Year");
            year.setFont(smallFont);
            year.setForeground(Color.WHITE);
            leftSidePanel.add(year, gbc);
        }

        // BDay Text Field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(0,0,0,0);  //top padding
        leftSidePanel.add(bDayTextField, gbc);

        // BDay Month Dropdown
        gbc.gridx = 1;
        leftSidePanel.add(bMonth, gbc);

        // BDay Year Text Field
        gbc.gridx = 2;
        leftSidePanel.add(bYearTextField, gbc);

        // Target Date Label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(15,0,0,0);  //top padding
        leftSidePanel.add(targetDayLabel, gbc);

        // Date Label
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5,0,0,0);  //top padding
        {
            JLabel day = new JLabel("Day");
            day.setFont(smallFont);
            day.setForeground(Color.WHITE);
            leftSidePanel.add(day, gbc);
        }

        // Month Label
        gbc.gridx = 1;
        {
            JLabel month = new JLabel("Month");
            month.setFont(smallFont);
            month.setForeground(Color.WHITE);
            leftSidePanel.add(month, gbc);
        }

        // Year Label
        gbc.gridx = 2;
        {
            JLabel year = new JLabel("Year");
            year.setFont(smallFont);
            year.setForeground(Color.WHITE);
            leftSidePanel.add(year, gbc);
        }

        // Target Day Text Field
        gbc.gridx = 0;
        gbc.gridy = 6;
        leftSidePanel.add(targetDayTextField, gbc);

        // Month Label
        gbc.gridx = 1;
        leftSidePanel.add(targetMonth, gbc);

        // Year Label
        gbc.gridx = 2;
        leftSidePanel.add(targetYearTextField, gbc);

        // Calculate Button
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.ipady = 15;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(50,0,0,0);  //top padding
        leftSidePanel.add(calcButton, gbc);

        return leftSidePanel;
    }
    
    // This function checks if the user has entered a valid input. If not, it returns an exception, which is handled in the 'initializeLeftSide()' function
    boolean checkIfInputValid(int days, int monthNum, int year)
    {
        int maxDays = daysPerMonth[--monthNum];

        // If the month is February, and it's a leap year, expand the maximum days allowed by one
        if(monthNum == 1 && year % 4 == 0)
            maxDays++;

        // if the day that the user has entered is within the reasonable bounds, they can proceed, else, return an error
        return !(0 < days && days <= maxDays);
    }
}