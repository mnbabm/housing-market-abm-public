package util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class ModelTimeSeries extends JFrame {

    private static final long serialVersionUID = 1L;
    JTextField testField = new JTextField(10);

    public ModelTimeSeries() {


    }

    public ModelTimeSeries(XYSeriesCollection series, int beginPeriod, int endPeriod, String title){

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Period", "Value",
                series, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel cp = new ChartPanel(chart) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(720, 240);
            }
        };
        cp.setMouseWheelEnabled(true);
        add(cp);
        pack();

        setSize(1000, 520);
        setVisible(true);
    }

    public ModelTimeSeries(XYSeriesCollection series, int Xcoordinate, int Ycoordinate, int width, int height, String title){

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Period", "Value",
                series, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel cp = new ChartPanel(chart) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(720, 240);
            }
        };
        cp.setMouseWheelEnabled(true);
        add(cp);
        pack();

        setBounds(Xcoordinate,Ycoordinate,width, height);
        setVisible(true);
    }


}
