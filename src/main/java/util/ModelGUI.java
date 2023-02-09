package util;

import model.Bucket;
import model.GeoLocation;
import model.Model;
import model.Neighbourhood;
import org.apache.commons.lang3.StringUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ModelGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    public JLabel labelDataAboutRun = new JLabel("");

    JTextField txtGetTimeSeries = new JTextField(10);
    JButton btnGetTimeSeries = new JButton("Get time series");
    JCheckBox cbTimeSeriesPrint = new JCheckBox("Print to console");

    JLabel labelFromPeriod = new JLabel("From");
    JTextField txtFromPeriod = new JTextField(3);
    JLabel labelToPeriod = new JLabel("To");
    public JTextField txtToPeriod = new JTextField(3);

    JButton btnWriteOutputDataCsv = new JButton("write outputData csv");

    JButton btnSeriesToView1 = new JButton("l0.2/l0.1 l1.2/l1.1");
    JButton btnSeriesToView2 = new JButton("l1");
    JButton btnSeriesToView3 = new JButton("l2/l1");
    JButton btnSeriesToView4 = new JButton("l0.2/l0.1/m10 l1.2/l1.1/m10");
    JButton btnSeriesToView5 = new JButton("l0.3 l1.3 m10");
    JButton btnSeriesToView6 = new JButton("l2490 l2590 l2690 l2790 l2890 l2990");
    JButton btnSeriesToView7 = new JButton("");
    JButton btnSeriesToView8 = new JButton("");
    JButton btnSeriesToView9 = new JButton("");
    JButton btnSeriesToView10 = new JButton("");
    JButton btnSeriesToView11 = new JButton("");
    JButton btnSeriesToView12 = new JButton("");
    JButton btnSeriesToView13 = new JButton("");
    JButton btnSeriesToView14 = new JButton("");
    JButton btnSeriesToView15 = new JButton("");
    JButton btnSeriesToView16 = new JButton("");
    JButton btnSeriesToView17 = new JButton("");
    JButton btnSeriesToView18 = new JButton("");
    JButton btnSeriesToView19 = new JButton("");
    JButton btnSeriesToView20 = new JButton("");

    JButton btnSeriesToView21 = new JButton("");
    JButton btnSeriesToView22 = new JButton("");
    JButton btnSeriesToView23 = new JButton("");
    JButton btnSeriesToView24 = new JButton("");
    JButton btnSeriesToView25 = new JButton("");
    JButton btnSeriesToView26 = new JButton("");
    JButton btnSeriesToView27 = new JButton("");
    JButton btnSeriesToView28 = new JButton("");
    JButton btnSeriesToView29 = new JButton("");
    JButton btnSeriesToView30 = new JButton("");
    JButton btnSeriesToView31 = new JButton("");
    JButton btnSeriesToView32 = new JButton("");
    JButton btnSeriesToView33 = new JButton("");
    JButton btnSeriesToView34 = new JButton("");
    JButton btnSeriesToView35 = new JButton("");
    JButton btnSeriesToView36 = new JButton("");

    ArrayList<ArrayList<Double>> answers = new ArrayList<ArrayList<Double>>();

    public ModelGUI() {
        this.setBounds(300, 200, 1060, 450);
        setLayout(null);

        add(labelDataAboutRun);
        labelDataAboutRun.setBounds(400, 20, 500, 20);
        labelDataAboutRun.setText(Model.runSpecification);

        txtFromPeriod.setText("0");
        txtToPeriod.setText(Integer.toString(Model.nPeriods-1));

        int firstXCoordForMainLine = 50;
        int yCoordForMainLine = 60;
        add(txtGetTimeSeries);
        txtGetTimeSeries.setBounds(firstXCoordForMainLine + 0, yCoordForMainLine, 210, 20);
        add(btnGetTimeSeries);
        btnGetTimeSeries.setBounds(firstXCoordForMainLine + 230, yCoordForMainLine, 120, 20);
        add(labelFromPeriod);
        labelFromPeriod.setBounds(firstXCoordForMainLine + 370, yCoordForMainLine, 35, 20);
        add(txtFromPeriod);
        txtFromPeriod.setBounds(firstXCoordForMainLine + 420, yCoordForMainLine, 50, 20);
        add(labelToPeriod);
        labelToPeriod.setBounds(firstXCoordForMainLine + 490, yCoordForMainLine, 30, 20);
        add(txtToPeriod);
        txtToPeriod.setBounds(firstXCoordForMainLine + 520, yCoordForMainLine, 50, 20);
        add(cbTimeSeriesPrint);
        cbTimeSeriesPrint.setBounds(firstXCoordForMainLine + 580, yCoordForMainLine, 130, 25);

        add(btnWriteOutputDataCsv);
        btnWriteOutputDataCsv.setBounds(firstXCoordForMainLine + 730, yCoordForMainLine, 180, 20);

        int seriesToViewWidth = 150;
        int seriesToViewHeight= 20;
        int seriesToViewFirstY = 110;
        int seriesToViewFirstX = 170;
        int seriesToViewXDifference = 170;

        add(btnSeriesToView1);
        btnSeriesToView1.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 0 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView2);
        btnSeriesToView2.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 0 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView3);
        btnSeriesToView3.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 0 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView4);
        btnSeriesToView4.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 0 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView5);
        btnSeriesToView5.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 1 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView6);
        btnSeriesToView6.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 1 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView7);
        btnSeriesToView7.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 1 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView8);
        btnSeriesToView8.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 1 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView9);
        btnSeriesToView9.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 2 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView10);
        btnSeriesToView10.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 2 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView11);
        btnSeriesToView11.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 2 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView12);
        btnSeriesToView12.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 2 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView13);
        btnSeriesToView13.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 3 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView14);
        btnSeriesToView14.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 3 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView15);
        btnSeriesToView15.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 3 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView16);
        btnSeriesToView16.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 3 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView17);
        btnSeriesToView17.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 4 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView18);
        btnSeriesToView18.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 4 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView19);
        btnSeriesToView19.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 4 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView20);
        btnSeriesToView20.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 4 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView21);
        btnSeriesToView21.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 5 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView22);
        btnSeriesToView22.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 5 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView23);
        btnSeriesToView23.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 5 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView24);
        btnSeriesToView24.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 5 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView25);
        btnSeriesToView25.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 6 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView26);
        btnSeriesToView26.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 6 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView27);
        btnSeriesToView27.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 6 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView28);
        btnSeriesToView28.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 6 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView29);
        btnSeriesToView29.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 7 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView30);
        btnSeriesToView30.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 7 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView31);
        btnSeriesToView31.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 7 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView32);
        btnSeriesToView32.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 7 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView33);
        btnSeriesToView33.setBounds(seriesToViewFirstX + 0 * seriesToViewXDifference, seriesToViewFirstY + 8 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView34);
        btnSeriesToView34.setBounds(seriesToViewFirstX + 1 * seriesToViewXDifference, seriesToViewFirstY + 8 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView35);
        btnSeriesToView35.setBounds(seriesToViewFirstX + 2 * seriesToViewXDifference, seriesToViewFirstY + 8 * 30, seriesToViewWidth, seriesToViewHeight);
        add(btnSeriesToView36);
        btnSeriesToView36.setBounds(seriesToViewFirstX + 3 * seriesToViewXDifference, seriesToViewFirstY + 8 * 30, seriesToViewWidth, seriesToViewHeight);


        btnGetTimeSeries.addActionListener(this);
        btnWriteOutputDataCsv.addActionListener(this);
        btnSeriesToView1.addActionListener(this);
        btnSeriesToView2.addActionListener(this);
        btnSeriesToView3.addActionListener(this);
        btnSeriesToView4.addActionListener(this);
        btnSeriesToView5.addActionListener(this);
        btnSeriesToView6.addActionListener(this);
        btnSeriesToView7.addActionListener(this);
        btnSeriesToView8.addActionListener(this);
        btnSeriesToView9.addActionListener(this);
        btnSeriesToView10.addActionListener(this);
        btnSeriesToView11.addActionListener(this);
        btnSeriesToView12.addActionListener(this);
        btnSeriesToView13.addActionListener(this);
        btnSeriesToView14.addActionListener(this);
        btnSeriesToView15.addActionListener(this);
        btnSeriesToView16.addActionListener(this);
        btnSeriesToView17.addActionListener(this);
        btnSeriesToView18.addActionListener(this);
        btnSeriesToView19.addActionListener(this);
        btnSeriesToView20.addActionListener(this);
        btnSeriesToView21.addActionListener(this);
        btnSeriesToView22.addActionListener(this);
        btnSeriesToView23.addActionListener(this);
        btnSeriesToView24.addActionListener(this);
        btnSeriesToView25.addActionListener(this);
        btnSeriesToView26.addActionListener(this);
        btnSeriesToView27.addActionListener(this);
        btnSeriesToView28.addActionListener(this);
        btnSeriesToView29.addActionListener(this);
        btnSeriesToView30.addActionListener(this);
        btnSeriesToView31.addActionListener(this);
        btnSeriesToView32.addActionListener(this);
        btnSeriesToView33.addActionListener(this);
        btnSeriesToView34.addActionListener(this);
        btnSeriesToView35.addActionListener(this);
        btnSeriesToView36.addActionListener(this);

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {



        if (e.getSource() == btnGetTimeSeries) {

            getTimeSeries();


        } else if (e.getSource() == btnWriteOutputDataCsv) {
            Model.writeOutputDataCsv();
        }  else if (e.getSource() == btnSeriesToView1) {
            txtGetTimeSeries.setText(btnSeriesToView1.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView2) {
            txtGetTimeSeries.setText(btnSeriesToView2.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView3) {
            txtGetTimeSeries.setText(btnSeriesToView3.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView4) {
            txtGetTimeSeries.setText(btnSeriesToView4.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView5) {
            txtGetTimeSeries.setText(btnSeriesToView5.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView6) {
            txtGetTimeSeries.setText(btnSeriesToView6.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView7) {
            txtGetTimeSeries.setText(btnSeriesToView7.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView8) {
            txtGetTimeSeries.setText(btnSeriesToView8.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView9) {
            txtGetTimeSeries.setText(btnSeriesToView9.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView10) {
            txtGetTimeSeries.setText(btnSeriesToView10.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView11) {
            txtGetTimeSeries.setText(btnSeriesToView11.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView12) {
            txtGetTimeSeries.setText(btnSeriesToView12.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView13) {
            txtGetTimeSeries.setText(btnSeriesToView13.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView14) {
            txtGetTimeSeries.setText(btnSeriesToView14.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView15) {
            txtGetTimeSeries.setText(btnSeriesToView15.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView16) {
            txtGetTimeSeries.setText(btnSeriesToView16.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView17) {
            txtGetTimeSeries.setText(btnSeriesToView17.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView18) {
            txtGetTimeSeries.setText(btnSeriesToView18.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView19) {
            txtGetTimeSeries.setText(btnSeriesToView19.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView20) {
            txtGetTimeSeries.setText(btnSeriesToView20.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView21) {
            txtGetTimeSeries.setText(btnSeriesToView21.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView22) {
            txtGetTimeSeries.setText(btnSeriesToView22.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView23) {
            txtGetTimeSeries.setText(btnSeriesToView23.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView24) {
            txtGetTimeSeries.setText(btnSeriesToView24.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView25) {
            txtGetTimeSeries.setText(btnSeriesToView25.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView26) {
            txtGetTimeSeries.setText(btnSeriesToView26.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView27) {
            txtGetTimeSeries.setText(btnSeriesToView27.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView28) {
            txtGetTimeSeries.setText(btnSeriesToView28.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView29) {
            txtGetTimeSeries.setText(btnSeriesToView29.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView30) {
            txtGetTimeSeries.setText(btnSeriesToView30.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView31) {
            txtGetTimeSeries.setText(btnSeriesToView31.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView32) {
            txtGetTimeSeries.setText(btnSeriesToView32.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView33) {
            txtGetTimeSeries.setText(btnSeriesToView33.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView34) {
            txtGetTimeSeries.setText(btnSeriesToView34.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView35) {
            txtGetTimeSeries.setText(btnSeriesToView35.getText());
            getTimeSeries();
        } else if (e.getSource() == btnSeriesToView36) {
            txtGetTimeSeries.setText(btnSeriesToView36.getText());
            getTimeSeries();
        }


    }


    void getTimeSeries() {
        int beginPeriod = Integer.parseInt(txtFromPeriod.getText());
        int endPeriod = Integer.parseInt(txtToPeriod.getText());

        String[] evaluations = txtGetTimeSeries.getText().split("\\s");



        XYSeriesCollection series = new XYSeriesCollection();

        for (int i = 0; i < evaluations.length; i++) {
            StringBuilder evaluation = new StringBuilder(evaluations[i]);
            evaluation.insert(0, "+");

            StringBuilder legend=new StringBuilder(evaluations[i]);

            String[] characters=new String[] {"m", "l", "n"};
            for (String character: characters) {
                //System.out.println("Újabb: " + character);
                int fromIndex=0;
                while (legend.indexOf(character,fromIndex)>-1) {

                    int newNumber=-1;
                    int newFromIndex=legend.indexOf(character,fromIndex);
                    if (newFromIndex==legend.length()-1 || StringUtils.isNumeric(legend.substring(newFromIndex+1,newFromIndex+2))==false) break;

                    fromIndex=newFromIndex+1;
                    int toIndex=newFromIndex+1;
                    while (StringUtils.isNumeric(legend.substring(newFromIndex+1,++toIndex))) {
                        newNumber=Integer.parseInt(legend.substring(fromIndex,toIndex));
                        if (toIndex==legend.length()) break;
                        if (legend.charAt(toIndex)=='.') {
                            newFromIndex=toIndex;
                            fromIndex=newFromIndex+1;
                            toIndex=newFromIndex+1;
                            while (StringUtils.isNumeric(legend.substring(newFromIndex+1,++toIndex))) {
                                newNumber=Integer.parseInt(legend.substring(fromIndex,toIndex));
                                if (toIndex==legend.length()) break;
                            }

                            break;
                        }

                    }

                    if (character=="m" && newNumber<OutputDataNames.m.length) {
                        String newLegend=OutputDataNames.m[newNumber];
                        int newNumberLength=Integer.toString(newNumber).length();
                        int legendLength=newLegend.length();
                        legend.replace(fromIndex,fromIndex+newNumberLength,newLegend);
                        fromIndex=newFromIndex+legendLength+1;
                    } else if (character=="l" && newNumber<OutputDataNames.l.length) {
                        String newLegend=OutputDataNames.l[newNumber];
                        int newNumberLength=Integer.toString(newNumber).length();
                        int legendLength=newLegend.length();
                        legend.replace(fromIndex,fromIndex+newNumberLength,newLegend);
                        fromIndex=newFromIndex+legendLength+1;
                    } else if (character=="n" && newNumber<OutputDataNames.n.length) {
                        String newLegend=OutputDataNames.n[newNumber];
                        int newNumberLength=Integer.toString(newNumber).length();
                        int legendLength=newLegend.length();
                        legend.replace(fromIndex,fromIndex+newNumberLength,newLegend);
                        fromIndex=newFromIndex+legendLength+1;
                    }


                }
            }

            double[] newDoubleSeries = getEvaluatedTimeSeries(evaluation,beginPeriod,endPeriod);

            XYSeries newSeries = new XYSeries(legend.toString());
            for (int j = 0; j < newDoubleSeries.length; j++) {
                newSeries.add(beginPeriod + j, newDoubleSeries[j]);
            }

            series.addSeries(newSeries);
        }

        String title = labelDataAboutRun.getText();

        ModelTimeSeries modelTimeSeries = new ModelTimeSeries(series, 23, 46, title);

        double[][] allSeries = new double[series.getSeriesCount()][];
        for (int i = 0; i < series.getSeriesCount(); i++) {
            XYSeries newSeries = series.getSeries(i);
            allSeries[i] = new double[newSeries.getItemCount()];
            for (int j = 0; j < newSeries.getItemCount() ; j++) {
                allSeries[i][j] = newSeries.getY(j).doubleValue();
            }

        }


        for (int i = 0; i < allSeries[0].length; i++) {
            StringBuffer string = new StringBuffer();
            for (int j = 0; j < allSeries.length; j++) {
                string.append(allSeries[j][i]);
                string.append(" ");
            }

        }

    }

    double[] subStringEvaluate(String subStringToEvaluate) {
        StringBuilder subString = new StringBuilder(subStringToEvaluate);
        subString.insert(0, '*');
        int beginPeriod = Integer.parseInt(txtFromPeriod.getText());
        int endPeriod = Integer.parseInt(txtToPeriod.getText());

        double[] newDoubleSeries = new double[endPeriod - beginPeriod + 1];
        for (int i = 0; i < newDoubleSeries.length; i++) {
            newDoubleSeries[i] = 1;
        }

        while (subString.length() > 0) {

            int endCharacter = subString.length();

            if (subString.indexOf("*", 1) > -1)
                endCharacter = subString.indexOf("*", 1);
            if (subString.indexOf("/", 1) > -1 && subString.indexOf("/", 1) < endCharacter)
                endCharacter = subString.indexOf("/", 1);

            double[] outputDataSeries = getOutputDataSeries(subString.substring(1, endCharacter));

            if (subString.charAt(0) == '*') {
                for (int i = 0; i < newDoubleSeries.length; i++) {
                    newDoubleSeries[i] *= outputDataSeries[i];
                }
            } else if (subString.charAt(0) == '/') {
                for (int i = 0; i < newDoubleSeries.length; i++) {
                    newDoubleSeries[i] /= outputDataSeries[i];
                }
            }

            subString.delete(0, endCharacter);

        }

        return newDoubleSeries;
    }

    double[] getOutputDataSeries(String outputDataName) {

        int numberOfVariable = 0;
        int numberOfObject = 0;

        if (outputDataName.charAt(0) == 'l' || outputDataName.charAt(0) == 'b' || outputDataName.charAt(0) == 'n' || outputDataName.charAt(0) == 'm') {
            if (outputDataName.contains(".")) {
                numberOfVariable = Integer
                        .parseInt(outputDataName.substring(outputDataName.indexOf(".") + 1, outputDataName.length()));
                numberOfObject = Integer.parseInt(outputDataName.substring(1, outputDataName.indexOf(".")));
            } else
                numberOfVariable = Integer.parseInt(outputDataName.substring(1, outputDataName.length()));
        }

        int beginPeriod = Integer.parseInt(txtFromPeriod.getText());
        int endPeriod = Integer.parseInt(txtToPeriod.getText());

        double[] newSeries = new double[endPeriod - beginPeriod + 1];

        if (outputDataName.charAt(0) == 'l') {

            for (int i = 0; i < newSeries.length; i++) {
                int periodNum = beginPeriod + i;

                if (outputDataName.contains(".")) {

                    newSeries[i] = Model.geoLocations.get(numberOfObject).outputData[numberOfVariable][periodNum];

                    if (cbTimeSeriesPrint.isSelected()) {

                        System.out.println("Period" + periodNum + " " + outputDataName + ": "
                                + Model.geoLocations.get(numberOfObject).outputData[numberOfVariable][periodNum]);
                    }
                } else {
                    double newSum = 0;
                    for (GeoLocation geoLocation: Model.geoLocations.values()) {
                        newSum = newSum + geoLocation.outputData[numberOfVariable][periodNum];
                    }

                    newSeries[i] = newSum;
                    if (cbTimeSeriesPrint.isSelected())
                        System.out.println("Period" + periodNum + " " + outputDataName + ": " + newSum);
                }

            }


        } else if (outputDataName.charAt(0) == 'b') {

            for (int i = 0; i < newSeries.length; i++) {
                int periodNum = beginPeriod + i;

                if (outputDataName.contains(".")) {

                    newSeries[i] = Model.buckets.get(numberOfObject).outputData[numberOfVariable][periodNum];

                    if (cbTimeSeriesPrint.isSelected()) {

                        System.out.println("Period" + periodNum + " " + outputDataName + ": "
                                + Model.buckets.get(numberOfObject).outputData[numberOfVariable][periodNum]);
                    }
                } else {
                    double newSum = 0;
                    for (Bucket bucket : Model.buckets.values()) {
                        newSum = newSum + bucket.outputData[numberOfVariable][periodNum];
                    }

                    newSeries[i] = newSum;
                    if (cbTimeSeriesPrint.isSelected())
                        System.out.println("Period" + periodNum + " " + outputDataName + ": " + newSum);
                }

            }

        } else if (outputDataName.charAt(0) == 'n') {

            for (int i = 0; i < newSeries.length; i++) {
                int periodNum = beginPeriod + i;

                if (outputDataName.contains(".")) {

                    newSeries[i] = Model.neighbourhoods.get(numberOfObject).outputData[numberOfVariable][periodNum];

                    if (cbTimeSeriesPrint.isSelected()) {

                        System.out.println("Period" + periodNum + " " + outputDataName + ": "
                                + Model.neighbourhoods.get(numberOfObject).outputData[numberOfVariable][periodNum]);
                    }
                } else {
                    double newSum = 0;
                    for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
                        newSum = newSum + neighbourhood.outputData[numberOfVariable][periodNum];
                    }

                    newSeries[i] = newSum;
                    if (cbTimeSeriesPrint.isSelected())
                        System.out.println("Period" + periodNum + " " + outputDataName + ": " + newSum);
                }

            }

        } else if (outputDataName.charAt(0) == 'm') {

            for (int i = 0; i < newSeries.length; i++) {
                int periodNum = beginPeriod + i;

                newSeries[i] = Model.outputData[numberOfVariable][periodNum];

                if (cbTimeSeriesPrint.isSelected()) {

                    System.out.println("Period" + periodNum + " " + outputDataName + ": "
                            + Model.outputData[numberOfVariable][periodNum]);
                }

            }

        } else { // plain number
            double number = Double.parseDouble(outputDataName);
            for (int i = 0; i < newSeries.length; i++) {
                newSeries[i] = number;
            }
        }

        return newSeries;

    }

    public double[] getEvaluatedTimeSeries(StringBuilder evaluation, int beginPeriod, int endPeriod) {
        double[] newDoubleSeries = new double[endPeriod - beginPeriod + 1];

        while (evaluation.length() > 0) {

            int endCharacter = evaluation.length();

            if (evaluation.indexOf("+", 1) > -1)
                endCharacter = evaluation.indexOf("+", 1);
            if (evaluation.indexOf("-", 1) > -1 && evaluation.indexOf("-", 1) < endCharacter)
                endCharacter = evaluation.indexOf("-", 1);

            double[] addorsubtractSeries = subStringEvaluate(evaluation.substring(1, endCharacter).toString());

            if (evaluation.charAt(0) == '+') {
                for (int j = 0; j < newDoubleSeries.length; j++) {
                    newDoubleSeries[j] += addorsubtractSeries[j];
                }
            } else if (evaluation.charAt(0) == '-') {
                for (int j = 0; j < newDoubleSeries.length; j++) {
                    newDoubleSeries[j] -= addorsubtractSeries[j];
                }
            } else if (evaluation.charAt(0) == '#') {
                for (int j = 0; j < newDoubleSeries.length; j++) {
                    newDoubleSeries[j] *= addorsubtractSeries[j];
                }
            }else if (evaluation.charAt(0) == '%') {
                for (int j = 0; j < newDoubleSeries.length; j++) {
                    newDoubleSeries[j] /= addorsubtractSeries[j];
                }
            }

            evaluation.delete(0, endCharacter);

        }

        return newDoubleSeries;
    }


}

