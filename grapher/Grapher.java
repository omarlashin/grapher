package grapher;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;
import org.mariuszgromada.math.mxparser.*;

public class Grapher {
    
    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch(Exception ex){}
        GraphingFrame f = new GraphingFrame();
        f.setVisible(true);
    }
    
}

class GraphingFrame extends JFrame implements ActionListener {
    private GraphingPanel pnlGraph = new GraphingPanel();
    private JPanel pnlControl = new JPanel();
    private JLabel lblFn = new JLabel("Function:");
    private JLabel lblDomain = new JLabel("Domain:");
    private JTextField txtFn = new JTextField(15);
    private JTextField txtDomain = new JTextField(5);
    private JButton btnGraph = new JButton("Graph");
    private ButtonGroup btgFn = new ButtonGroup();
    private JRadioButton rdbC = new JRadioButton("Cartesian", true);
    private JRadioButton rdbP = new JRadioButton("Polar");
    
    public GraphingFrame(){
        this.init();
    }
    
    private void init(){
        this.setTitle("Graphing Calculator");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container c = this.getContentPane();
        c.add(pnlGraph);
        pnlGraph.setPreferredSize(new Dimension(800, 800));
        pnlGraph.setBackground(Color.DARK_GRAY);
        c.add(pnlControl, BorderLayout.NORTH);
        pnlControl.add(lblFn);
        pnlControl.add(txtFn);
        pnlControl.add(lblDomain);
        pnlControl.add(txtDomain);
        pnlControl.add(btnGraph);
        btgFn.add(rdbC);
        btgFn.add(rdbP);
        pnlControl.add(rdbC);
        pnlControl.add(rdbP);
        lblFn.setFont(new Font("Calibri", Font.BOLD, 20));
        txtFn.setFont(new Font("Arial", Font.PLAIN, 20));
        lblDomain.setFont(new Font("Calibri", Font.BOLD, 20));
        txtDomain.setFont(new Font("Arial", Font.PLAIN, 20));
        btnGraph.setFont(new Font("Calibri", Font.BOLD, 20));
        rdbC.setFont(new Font("Calibri", Font.BOLD, 20));
        rdbP.setFont(new Font("Calibri", Font.BOLD, 20));
        btnGraph.addActionListener(this);
        this.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((d.width-this.getWidth())/2, (d.height-this.getHeight())/2);
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        try{
            String domain = txtDomain.getText();
            Expression eLower = new Expression(domain.split(",")[0]);
            Expression eUpper = new Expression(domain.split(",")[1]);
            if(!eLower.checkSyntax() || !eUpper.checkSyntax())
                throw new NumberFormatException();
            double lower = eLower.calculate();
            double upper = eUpper.calculate();
            double x[] = new double[(int)Math.ceil((upper-lower)/0.001+1)];
            double y[] = new double[x.length];
            x[0] = lower;
            
            String fn = txtFn.getText();
            fn = fn.toLowerCase();
            fn = fn.replace((rdbP.isSelected())? "r":"y", "");
            fn = fn.replace("=", "");
            for(int i = 0; i < x.length; i++){
                Expression e1 = new Expression(fn.replace((rdbP.isSelected())? "t":"x", x[i]+""));
                if(!e1.checkSyntax())
                    throw new ArithmeticException();
                y[i] = e1.calculate();
                if(i != x.length-1)
                    x[i+1] = x[i] + 0.001;
                
                if(rdbP.isSelected()){
                    double temp = x[i];
                    x[i] = y[i] * Math.cos(temp);
                    y[i] = y[i] * Math.sin(temp);
                }
            }
            
            pnlGraph.setValues(x, y, rdbP.isSelected());
            pnlGraph.repaint();
        }
        catch(NumberFormatException | ArrayIndexOutOfBoundsException ex){
            JOptionPane.showMessageDialog(this, "Please enter the domain in the form a,b", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
        catch(ArithmeticException ex){
            JOptionPane.showMessageDialog(this, "Function is incorrect\ny in terms of x\nr in terms of t", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class GraphingPanel extends JPanel {
    private double x[];
    private double y[];
    private double sortedY[];
    private final int indices[] = new int[5];
    private double maximum, ratioX, ratioY;
    private double maximumX;
    
    public void setValues(double x[], double y[], boolean isPolar){
        this.x = x;
        this.y = y;
        this.maximum = Math.abs(this.y[0]);
        for(int i = 1; i < this.y.length; i++)
            if(Math.abs(this.y[i]) > this.maximum)
                this.maximum = Math.abs(this.y[i]);
        
        if(x[0]<-0.1 && x[x.length-1]>0.1)
            this.ratioX = x[x.length-1]/(Math.abs(x[0])+x[x.length-1]);
        else{
            if(x[0] < -0.1)
                this.ratioX = 0.05;
            else
                this.ratioX = 0.95;
        }
        this.maximumX = Double.max(Math.abs(x[0]), Math.abs(x[x.length-1]));
        
        if(isPolar){
            double p = x[0];
            for(int i = 1; i < x.length; i++)
                if(x[i] > p)
                    p = x[i];
            double n = y[0];
            for(int i = 1; i < x.length; i++)
                if(x[i] < n)
                    n = x[i];
            if(n < -0.1 && p > 0.1)
                this.ratioX = p/(Math.abs(n)+p);
            else{
                if(n < -0.1)
                    this.ratioX = 0.05;
                else
                    this.ratioX = 0.95;
            }
            this.maximumX = Math.abs(x[0]);
            for(int i = 1; i < x.length; i++)
                if(Math.abs(x[i]) > this.maximumX)
                    this.maximumX = Math.abs(x[i]);
        }
        
        double p = y[0];
        for(int i = 1; i < y.length; i++)
            if(y[i] > p)
                p = y[i];
        double n = y[0];
        for(int i = 1; i < y.length; i++)
            if(y[i] < n)
                n = y[i];
        if(n < -0.1 && p > 0.1)
            this.ratioY = p/(Math.abs(n)+p);
        else{
            if(n < -0.1)
                this.ratioY = 0.05;
            else
                this.ratioY = 0.95;
        }
        
        sortedY = Arrays.copyOf(this.y, this.y.length);
        Arrays.parallelSort(sortedY);
        for(int i = 1; i < 6; i++)
            indices[i-1] = i * this.x.length/6;
    }
    
    private Point getOrigin(){
        if(this.x == null)
            return new Point(this.getWidth()/2, this.getHeight()/2);
        return new Point((int)((1-ratioX)*this.getWidth()), (int)(ratioY*this.getHeight()));
    }
    
    private boolean isIndex(int a){
        for(int i = 0; i < this.indices.length; i++)
            if(indices[i] == a)
                return true;
        return false;
    }
    
    @Override
    public void paint(Graphics g){
        super.paint(g);
        Point o = this.getOrigin();
        int w = this.getWidth();
        int h = this.getHeight();
        g.setColor(Color.WHITE);
        g.drawLine(o.x, 0, o.x, h);
        g.drawLine(0, o.y, w, o.y);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("0", o.x-15, o.y+18);
        g.drawString("y", o.x+15, 15);
        g.drawString("x", w-15, o.y-15);
        g.fillPolygon(new int[]{o.x,o.x-8,o.x+8}, new int[]{0,16,16}, 3);
        g.fillPolygon(new int[]{o.x,o.x-8,o.x+8}, new int[]{h,h-16,h-16}, 3);
        g.fillPolygon(new int[]{0,16,16}, new int[]{o.y,o.y-8,o.y+8}, 3);
        g.fillPolygon(new int[]{w,w-16,w-16}, new int[]{o.y,o.y-8,o.y+8}, 3);
        
        if(this.x != null){
            g.setColor(Color.BLUE);
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            double scaleX = Math.max(o.x, w-o.x) / this.maximumX;
            double scaleY = Math.max(o.y, h-o.y) / this.maximum;
            scaleX *= 0.95;
            scaleY *= 0.95;
            for(int i = 0; i < x.length-1; i++){
                g.setColor(Color.BLUE);
                g.drawLine((int)(this.x[i]*scaleX+o.x), (int)(o.y-this.y[i]*scaleY), (int)(this.x[i+1]*scaleX+o.x), (int)(o.y-this.y[i+1]*scaleY));
                if(this.isIndex(i)){
                    int markX = (int)Math.round(this.x[i]);
                    int markY = (int)Math.round(this.sortedY[i]);
                    if(markX != 0){
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.PLAIN, 10));
                        g.drawString("|", (int)(markX*scaleX+o.x)-2, o.y+3);
                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        g.drawString(markX+"", (int)(markX*scaleX+o.x)-5, o.y+23);
                    }
                    if(markY != 0){
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        g.drawString("-", o.x-2, (int)(o.y-markY*scaleY)+5);
                        g.drawString(markY+"", o.x-20, (int)(o.y-markY*scaleY)+7);
                    }
                }
            }
        }
    }
}