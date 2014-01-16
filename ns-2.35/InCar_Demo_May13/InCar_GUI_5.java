import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.image.BufferedImage;

public class InCar_GUI_5 extends JApplet {
	//private static final long serialVersionUID = 1L;
	private JPanel left, rightTop, rightMiddle,rightBottom;
	private Box box = new Box(BoxLayout.X_AXIS), rightBox = new Box(BoxLayout.Y_AXIS);
	private JSeparator vs, hs1, hs2, hs3;
	Button b_go, b_write;
    //	InCar_Object TM;
	int totalNum;
	double totalTime;
	BufferedWriter bw;

	JCheckBox l_node1_1, l_node2_1, l_node3_1, l_node4_1;
	JLabel l_node1, l_node1_x, l_node1_y, l_node1_tx, l_node1_traffic, l_node1_rate, l_node1_start, l_node1_end;
	JTextField b_node1_x, b_node1_y, b_node1_tx, b_node1_rate, b_node1_start, b_node1_end;
	JTextField b_node1_traffic;
	JLabel l_node2, l_node2_x, l_node2_y, l_node2_tx, l_node2_traffic, l_node2_rate, l_node2_start, l_node2_end;
	JTextField b_node2_x, b_node2_y, b_node2_tx, b_node2_rate, b_node2_start, b_node2_end;
	JTextField b_node2_traffic;
	JLabel l_node3, l_node3_x, l_node3_y, l_node3_tx, l_node3_traffic, l_node3_rate, l_node3_start, l_node3_end;
	JTextField b_node3_x, b_node3_y, b_node3_tx, b_node3_rate, b_node3_start, b_node3_end;
	JTextField b_node3_traffic;
	JLabel l_node4, l_node4_x, l_node4_y, l_node4_tx, l_node4_traffic, l_node4_rate, l_node4_start, l_node4_end;
	JTextField b_node4_x, b_node4_y, b_node4_tx, b_node4_rate, b_node4_start, b_node4_end;
	JTextField b_node4_traffic;
	JLabel l_BS_1, l_BS_x, l_BS_y, l_BS_tx;
	JTextField b_BS_x, b_BS_y, b_BS_tx;
	JLabel l_wifi, l_wifi_mode, l_wifi_pl, l_wifi_shadow, l_wifi_naka;
	JTextField b_wifi_pl, b_wifi_shadow, b_wifi_naka;
	JList b_wifi_mode;
	JLabel l_apbs, l_apbs_rate, l_apbs_delay;
	JTextField b_apbs_rate, b_apbs_delay;
	JLabel l_cellular, l_cellular_ul_rate, l_cellular_ul_delay, l_cellular_dl_rate,l_cellular_dl_delay, l_cellular_ul_loss, l_cellular_dl_loss;
	JTextField b_cellular, b_cellular_ul_rate, b_cellular_ul_delay, b_cellular_dl_rate,b_cellular_dl_delay;
	JTextField b_cellular_ul_loss, b_cellular_dl_loss;
	JLabel l_server, l_server_rate, l_server_loss, l_server_delay;
	JTextField b_server_rate, b_server_loss, b_server_delay;
	Choice choice1, choice2, choice3, choice4, choice_11, choice_dl_loss,choice_ul_loss;
	JLabel l_filename;
	JTextField b_filename;
    String filename = "", filenameTr = "";
	int count = 0;

	double xs = 10.0, ys = 10.0;
	String[] wifiMode = { "11a", "11g"};
	String[] LOSS = { "No LOSS", "COST231"};


	double node1_x, node1_y, node1_tx, node1_start, node1_end,
	node2_x, node2_y, node2_tx, node2_start, node2_end,
	node3_x, node3_y, node3_tx, node3_start, node3_end,
	node4_x, node4_y, node4_tx, node4_start, node4_end;
	double node1_rate, node2_rate, node3_rate, node4_rate;
	String node1_traffic, node2_traffic, node3_traffic, node4_traffic;
	double bs_x, bs_y, bs_tx, wifi_pl, wifi_shadow, wifi_naka;
	int wifi_mode;
	double cellular_ul_loss, cellular_dl_loss, server_loss;
	String cellular_ul_rate, cellular_ul_delay, cellular_dl_rate, cellular_dl_delay;
	double apbs_rate, apbs_delay, server_rate, server_delay;

	public void init(){
		createBoxes();
		setPanelBorders();
		setSeparatorPreferredSizes();

		left.setPreferredSize(new Dimension(510,0));

		getContentPane().add(box, BorderLayout.CENTER);
	}
	private void createBoxes(){
		//	Component vStrut = box.createVerticalStrut(10);
		//	Component hStrut = box.createHorizontalStrut(10);

		addRightTop();
		rightBox.add(rightTop);
		rightBox.add(box.createVerticalStrut(10));
		rightBox.add(hs1 = new JSeparator());
		rightBox.add(box.createVerticalStrut(10));

		addRightBottom();
		rightBox.add(rightBottom);
		rightBox.add(box.createVerticalStrut(10));
		rightBox.add(hs2 = new JSeparator());
		rightBox.add(box.createVerticalStrut(10));

		addRightMiddle();
		rightBox.add(rightMiddle);


		left = new JPanel();
		b_go = new Button("Draw!!");
		left.add(b_go);
		b_go.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				showStatus(event.getActionCommand() + " activated ");
				try{
					startCanvas();
					count = 1;
					repaint();
				}catch(IOException ie){}
			}
		});
		b_write = new Button("Run simulations!!");
		left.add(b_write);
		b_write.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				try{
				    startCanvas();
				    repaint();
				    writeToFile();
				    writeRunFile();
				    System.out.println("Write to file: DONE");
				    Process p = Runtime.getRuntime().exec("./run");
				    String s = "";
				    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				    System.out.println("execute passwd");
				    while((s = stdInput.readLine()) != null)
					System.out.println(s);
				    //System.exit(0);
				    stdInput.close();
				}catch(IOException ie){
					System.out.println("IOException");
				}
			}
		});


		box.add(left);
		box.add(box.createHorizontalStrut(10));
		box.add(vs = new JSeparator(JSeparator.VERTICAL));
		box.add(box.createHorizontalStrut(10));
		box.add(rightBox);

	}
	public void paint (Graphics g){
		super.paint(g);
		int yshift = 155, xshift = 35;
		int width=5, height = 5;
		g.setColor(Color.white);
		g.setColor(Color.black);
		g.drawString("Output trace: "+filenameTr,10,500);
		g.drawString("Script: "+filename,10,550);
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("car3.png"));
		}catch(IOException e){System.out.println("file not found");}

		g.drawImage(image, 10, 100, 210,700,0,0,500,1500,null);

		if(count != 0) {
			g.setColor(Color.red);
			if(l_node1_1.isSelected())
				g.fillOval((int)(node1_x*45)+xshift, (int)(node1_y*45)+yshift+10, width+2, height+2);
			if(l_node2_1.isSelected())
				g.fillOval((int)(node2_x*45)+xshift, (int)(node2_y*45)+yshift+10, width+2, height+2);
			if(l_node3_1.isSelected())
				g.fillOval((int)(node3_x*45)+xshift, (int)(node3_y*45)+yshift+10, width+2, height+2);
			if(l_node4_1.isSelected())
				g.fillOval((int)(node4_x*45)+xshift, (int)(node4_y*45)+yshift+10, width+2, height+2);
			g.setColor(Color.black);
			g.fillOval((int)bs_x*45+xshift, (int)bs_y*45+10+yshift, width*2, 2*height);
		}


	}

	private void setSeparatorPreferredSizes(){
		vs.setMaximumSize(new Dimension(vs.getPreferredSize().width, Integer.MAX_VALUE));
		hs1.setMaximumSize(new Dimension(Integer.MAX_VALUE, vs.getPreferredSize().height));
		hs2.setMaximumSize(new Dimension(Integer.MAX_VALUE, vs.getPreferredSize().height));

	}

	public void startCanvas() throws IOException{
		getParam();
		showStatus("Reading parameters");
		System.out.println("Retrieving parameter settings: DONE");
		if(checkValid()) {
			if(l_node4_1.isSelected()) totalNum=4;
			else if(l_node3_1.isSelected()) totalNum=3;
			else if(l_node2_1.isSelected()) totalNum=2;
			else if(l_node1_1.isSelected()) totalNum=1;
			else totalNum=0;
			showStatus("configuration okay");
		} else {
			showStatus("error selecting nodes");
		}
	}
	private boolean checkValid() {
		boolean x1 = l_node1_1.isSelected(), x2 = l_node2_1.isSelected();
		boolean x3 = l_node3_1.isSelected(), x4 = l_node4_1.isSelected();

		int xx1 = (x1) ? 1:0,xx2 = (x2) ? 1:0,xx3 = (x3) ? 1:0,xx4 = (x4) ? 1:0;
		double t1 = Math.max(node1_end*(int)xx1, node2_end*(int)xx2);
		double t2 = Math.max(node3_end*(int)xx3, node4_end*(int)xx4);
		totalTime = Math.max(t1,t2);

		if(!x1) {
			if(x2 || x3 || x4) return false;
		} else if(!x2) {
			if(x3 || x4) return false;
		} else if(!x3) {
			if(x4) return false;
		}
		return true;
	}
	private void setPanelBorders(){
		left.setBorder(BorderFactory.createTitledBorder("Simulated Topology"));
		rightTop.setBorder(BorderFactory.createTitledBorder("Mobile Device Setting"));
		rightMiddle.setBorder(BorderFactory.createTitledBorder("Output"));
		rightBottom.setBorder(BorderFactory.createTitledBorder("Links"));
	}
	private void addRightTop(){
		String[] trafficNames = { "TCP", "HTTP", "Streaming"};
		JLabel one_1, one_2, one_3, two_1, two_2, two_3, three_1,three_2,three_3,three_4,three_5,three_6;
		JLabel four_1, four_2, four_3, four_4,five_1,five_2,five_3;

		choice1 = new Choice();
		choice2 = new Choice();
		choice3 = new Choice();
		choice4 = new Choice();
		choice1.addItem("TCP");choice1.addItem("HTTP");choice1.addItem("Streaming");
		choice2.addItem("TCP");choice2.addItem("HTTP");choice2.addItem("Streaming");
		choice3.addItem("TCP");choice3.addItem("HTTP");choice3.addItem("Streaming");
		choice4.addItem("TCP");choice4.addItem("HTTP");choice4.addItem("Streaming");


		one_1 = new JLabel("");one_2 = new JLabel("");one_3 = new JLabel("");
		two_1 = new JLabel("");two_2 = new JLabel("");two_3 = new JLabel("");
		three_1 = new JLabel("");three_2 = new JLabel("");
		three_3 = new JLabel("");three_4 = new JLabel("");
		three_5 = new JLabel("");three_6 = new JLabel("");
		four_1 = new JLabel("");four_2 = new JLabel("");
		four_3 = new JLabel("");four_4 = new JLabel("");
		five_1 = new JLabel("");five_2 = new JLabel("");five_3= new JLabel("");
		rightTop = new JPanel();
		rightTop.setLayout(new GridLayout(10,5,0,0));

		l_node1 = new JLabel("");
		l_node2 = new JLabel("");
		l_node3 = new JLabel("");
		l_node4 = new JLabel("");

		l_node1_1 = new JCheckBox("Node 1");
		l_node1_x = new JLabel("X location (m)");
		l_node1_y = new JLabel("Y location (m)");
		l_node1_tx = new JLabel("TxPr (dBm)");
		l_node1_traffic = new JLabel("Traffic");
		l_node1_rate = new JLabel("Rate (kbps)");
		l_node1_start = new JLabel("Start time");
		l_node1_end = new JLabel("end time");

		b_node1_x = new JTextField("0.5",5);
		b_node1_y = new JTextField("2.0",5);
		b_node1_tx = new JTextField("0",10);
		b_node1_traffic = new JTextField("traffic_node1.txt",20);
		b_node1_rate = new JTextField("128",10);
		b_node1_start = new JTextField("2",10);
		b_node1_end = new JTextField("6",10);

		rightTop.add(l_node1_1);rightTop.add(l_node1_x);rightTop.add(b_node1_x);
		rightTop.add(l_node1_y);rightTop.add(b_node1_y);
		rightTop.add(l_node1);
		//rightTop.add(l_node4);rightTop.add(one_1);
		rightTop.add(l_node1_tx);rightTop.add(b_node1_tx);
		rightTop.add(l_node1_traffic);rightTop.add(b_node1_traffic);
		//rightTop.add(three_3);rightTop.add(three_4);

		/*rightTop.add(l_node1_rate);rightTop.add(b_node1_rate);
		rightTop.add(l_node1_start);rightTop.add(b_node1_start);
		rightTop.add(l_node1_end);rightTop.add(b_node1_end);*/

		l_node2_1 = new JCheckBox("Node 2");
		l_node2_x = new JLabel("X location (m)");
		l_node2_y = new JLabel("Y location (m)");
		l_node2_tx = new JLabel("TxPr (dBm)");
		l_node2_traffic = new JLabel("Traffic");
		l_node2_rate = new JLabel("Rate (kbps)");
		l_node2_start = new JLabel("Start time");
		l_node2_end = new JLabel("end time");

		b_node2_x = new JTextField("1.2",5);
		b_node2_y = new JTextField("0.5",5);
		b_node2_tx = new JTextField("10",10);
		b_node2_traffic = new JTextField("traffic_node2.txt",20);
		b_node2_rate = new JTextField("255",10);
		b_node2_start = new JTextField("2",10);
		b_node2_end = new JTextField("10",10);


		rightTop.add(l_node2_1);rightTop.add(l_node2_x);rightTop.add(b_node2_x);
		rightTop.add(l_node2_y);rightTop.add(b_node2_y);
		rightTop.add(one_2);
		//rightTop.add(one_3);rightTop.add(two_1);
		rightTop.add(l_node2_tx);rightTop.add(b_node2_tx);
		rightTop.add(l_node2_traffic);rightTop.add(b_node2_traffic);
		//rightTop.add(three_5);rightTop.add(three_6);
		//rightTop.add(b_node2_traffic);
		/*rightTop.add(l_node2_rate);rightTop.add(b_node2_rate);
		rightTop.add(l_node2_start);rightTop.add(b_node2_start);
		rightTop.add(l_node2_end);rightTop.add(b_node2_end);*/

		l_node3_1 = new JCheckBox("Node 3");
		l_node3_x = new JLabel("X location (m)");
		l_node3_y = new JLabel("Y location (m)");
		l_node3_tx = new JLabel("TxPr (dBm)");
		l_node3_traffic = new JLabel("Traffic");
		l_node3_rate = new JLabel("Rate (kbps)");
		l_node3_start = new JLabel("Start time");
		l_node3_end = new JLabel("end time");

		b_node3_x = new JTextField("1.5",5);
		b_node3_y = new JTextField("2.0",5);
		b_node3_tx = new JTextField("30",10);
		b_node3_traffic = new JTextField("traffic_node3.txt",20);
		b_node3_rate = new JTextField("128",10);
		b_node3_start = new JTextField("5",10);
		b_node3_end = new JTextField("8",10);


		rightTop.add(l_node3_1);rightTop.add(l_node3_x);rightTop.add(b_node3_x);
		rightTop.add(l_node3_y);rightTop.add(b_node3_y);
		rightTop.add(l_node2);
		//rightTop.add(l_node3);rightTop.add(two_2);
		rightTop.add(l_node3_tx);rightTop.add(b_node3_tx);
		rightTop.add(l_node3_traffic);rightTop.add(b_node3_traffic);
		//rightTop.add(four_1);rightTop.add(four_2);
		//rightTop.add(b_node3_traffic);
		/*rightTop.add(l_node3_rate);rightTop.add(b_node3_rate);
		rightTop.add(l_node3_start);rightTop.add(b_node3_start);
		rightTop.add(l_node3_end);rightTop.add(b_node3_end);*/

		l_node4_1 = new JCheckBox("Node 4");
		l_node4_x = new JLabel("X location (m)");
		l_node4_y = new JLabel("Y location (m)");
		l_node4_tx = new JLabel("TxPr (dBm)");
		l_node4_traffic = new JLabel("Traffic");
		l_node4_rate = new JLabel("Rate (kbps)");
		l_node4_start = new JLabel("Start time");
		l_node4_end = new JLabel("end time");

		b_node4_x = new JTextField("0.8",5);
		b_node4_y = new JTextField("0.8",5);
		b_node4_tx = new JTextField("0",10);
		b_node4_traffic = new JTextField("traffic_node4.txt",20);
		b_node4_rate = new JTextField("128",10);
		b_node4_start = new JTextField("10",10);
		b_node4_end = new JTextField("40",10);

		rightTop.add(l_node4_1);rightTop.add(l_node4_x);rightTop.add(b_node4_x);
		rightTop.add(l_node4_y);rightTop.add(b_node4_y);
		rightTop.add(two_3);
		//rightTop.add(three_1);rightTop.add(three_2);
		rightTop.add(l_node4_tx);rightTop.add(b_node4_tx);
		rightTop.add(l_node4_traffic);
		rightTop.add(b_node4_traffic);
		//rightTop.add(four_3);rightTop.add(four_4);
		//rightTop.add(choice4);
		/*rightTop.add(l_node4_rate);rightTop.add(b_node4_rate);
		rightTop.add(l_node4_start);rightTop.add(b_node4_start);
		rightTop.add(l_node4_end);rightTop.add(b_node4_end);*/
		//rightTop.add(one);rightTop.add(three);

		l_BS_1 = new JLabel("BS");
		l_BS_x = new JLabel("X location");
		l_BS_y = new JLabel("Y location");
		l_BS_tx = new JLabel("TxPr (dBm)");

		b_BS_x = new JTextField("1.0",5);
		b_BS_y = new JTextField("1.5",5);
		b_BS_tx = new JTextField("0",10);


		rightTop.add(l_BS_1);rightTop.add(l_BS_x);rightTop.add(b_BS_x);
		rightTop.add(l_BS_y);rightTop.add(b_BS_y);
		rightTop.add(five_1);
		//rightTop.add(five_2);rightTop.add(five_3);
		rightTop.add(l_BS_tx);rightTop.add(b_BS_tx);

	}

	private void addRightMiddle(){
		rightMiddle = new JPanel();
		rightMiddle.setLayout(new GridLayout(1,2,5,5));

		l_filename = new JLabel("Output file");
		b_filename = new JTextField("output.tr",20);
		rightMiddle.add(l_filename);rightMiddle.add(b_filename);

	}
	private void addRightBottom(){
		JLabel b1 = new JLabel(""),b2 = new JLabel(""),b3 = new JLabel(""),b4 = new JLabel("");
		JLabel c1 = new JLabel(""),c2 = new JLabel(""),c3 = new JLabel(""),c4 = new JLabel("");
		JLabel d1 = new JLabel(""),d2 = new JLabel(""),d3 = new JLabel(""),d4 = new JLabel("");
		choice_11 = new Choice();
		choice_ul_loss = new Choice();
		choice_dl_loss = new Choice();
		choice_11.add("11a");choice_11.add("11g");
		choice_dl_loss.add("No Loss");choice_dl_loss.add("COST231");
		choice_ul_loss.add("No Loss");choice_ul_loss.add("COST231");

		rightBottom = new JPanel();
		rightBottom.setLayout(new GridLayout(5,7,0,0));

		l_wifi = new JLabel("WiFi link");
		l_wifi_mode = new JLabel("Mode");
		l_wifi_pl = new JLabel("");
		l_wifi_shadow = new JLabel("std_db (log-normal)");
		l_wifi_naka = new JLabel("m (nakagami)");

		b_wifi_pl = new JTextField("2.0",5);
		b_wifi_shadow = new JTextField("4.0",5);
		b_wifi_naka = new JTextField("1.0",5);
		b_wifi_mode = new JList(wifiMode);
		//b_wifi_mode.ge
		//b_wifi_mode.setSelectedIndex(0);
		b_wifi_mode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rightBottom.add(l_wifi);rightBottom.add(l_wifi_mode);
		//rightBottom.add(b_wifi_mode);
		rightBottom.add(choice_11);
		//rightBottom.add(l_wifi_pl);
		//rightBottom.add(b_wifi_pl);
		rightBottom.add(l_wifi_shadow);
		rightBottom.add(b_wifi_shadow);rightBottom.add(l_wifi_naka);rightBottom.add(b_wifi_naka);
		//rightBottom.add(l_wifi_pl);rightBottom.add(d4);

		l_apbs = new JLabel("AP-BS link");
		l_apbs_rate = new JLabel("data rate (Mbps)");
		l_apbs_delay = new JLabel("delay (ms)");

		b_apbs_rate = new JTextField("100.0",5);
		b_apbs_delay = new JTextField("0.1",5);

		rightBottom.add(l_apbs);rightBottom.add(l_apbs_rate);rightBottom.add(b_apbs_rate);
		rightBottom.add(l_apbs_delay);rightBottom.add(b_apbs_delay);
		//rightBottom.add(b1);rightBottom.add(b2);
		rightBottom.add(b3);rightBottom.add(b4);

		l_cellular = new JLabel("Cellular link");
		l_cellular_ul_rate = new JLabel("UL data rate and delay (file)");
		l_cellular_ul_delay = new JLabel("UL delay (file)");
		l_cellular_dl_rate = new JLabel("DL data rate and delay (file)");
		l_cellular_dl_delay = new JLabel("DL delay (file)");
		l_cellular_dl_loss = new JLabel("DL loss [0-1]");
		l_cellular_ul_loss = new JLabel("UL loss [0-1]");

		b_cellular_dl_rate = new JTextField("dl_link.txt",5);
		b_cellular_dl_delay = new JTextField("140",5);
		b_cellular_ul_rate = new JTextField("ul_link.txt",5);
		b_cellular_ul_delay = new JTextField("140",5);
		/*b_cellular_dl_loss = new JList(LOSS);
		b_cellular_dl_loss.setSelectedIndex(0);
		b_cellular_dl_loss.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		b_cellular_ul_loss = new JList(LOSS);
		b_cellular_ul_loss.setSelectedIndex(0);
		b_cellular_ul_loss.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);*/
		b_cellular_dl_loss = new JTextField("0.0",5);
		b_cellular_ul_loss = new JTextField("0.0",5);

		rightBottom.add(l_cellular);rightBottom.add(l_cellular_dl_rate);rightBottom.add(b_cellular_dl_rate);
		//rightBottom.add(l_cellular_dl_delay);rightBottom.add(b_cellular_dl_delay);
		rightBottom.add(l_cellular_dl_loss);rightBottom.add(b_cellular_dl_loss);
		rightBottom.add(c1);rightBottom.add(c2);
		//rightBottom.add(choice_dl_loss);
		//rightBottom.add(c1);rightBottom.add(c2);rightBottom.add(c3);
		rightBottom.add(c4);
		rightBottom.add(l_cellular_ul_rate);rightBottom.add(b_cellular_ul_rate);
		//rightBottom.add(l_cellular_ul_delay);rightBottom.add(b_cellular_ul_delay);
		rightBottom.add(l_cellular_ul_loss);rightBottom.add(b_cellular_ul_loss);
		rightBottom.add(c3);rightBottom.add(d1);
		//rightBottom.add(choice_ul_loss);
		//rightBottom.add(d1);rightBottom.add(d2);
		//rightBottom.add(d3);

		l_server = new JLabel("Tower-Server");
		l_server_rate = new JLabel("Data rate (Mbps)");
		l_server_delay = new JLabel("Delay (ms)");
		l_server_loss = new JLabel("Loss [0-1] each way");
		b_server_rate = new JTextField("960",5);
		b_server_delay = new JTextField("40",5);
		b_server_loss = new JTextField("0.0",5);
		rightBottom.add(l_server);rightBottom.add(l_server_rate);rightBottom.add(b_server_rate);
		rightBottom.add(l_server_delay);rightBottom.add(b_server_delay);
		rightBottom.add(l_server_loss);rightBottom.add(b_server_loss);

	}


	public void stop() { 
		/*TM.stop();*/
		System.out.println("stop");
	}

	public void destroy() {
		System.out.println("destroy");
	}
	public void getParam(){

		node1_x = Double.parseDouble(b_node1_x.getText());
		node1_y = Double.parseDouble(b_node1_y.getText());
		node1_tx = Double.parseDouble(b_node1_tx.getText());
		node1_traffic = b_node1_traffic.getText();


		node2_x = Double.parseDouble(b_node2_x.getText());
		node2_y = Double.parseDouble(b_node2_y.getText());
		node2_tx = Double.parseDouble(b_node2_tx.getText());
		node2_traffic = b_node2_traffic.getText();

		node3_x = Double.parseDouble(b_node3_x.getText());
		node3_y = Double.parseDouble(b_node3_y.getText());
		node3_tx = Double.parseDouble(b_node3_tx.getText());
		node3_traffic = b_node3_traffic.getText();

		node4_x = Double.parseDouble(b_node4_x.getText());
		node4_y = Double.parseDouble(b_node4_y.getText());
		node4_tx = Double.parseDouble(b_node4_tx.getText());
		node4_traffic = b_node4_traffic.getText();

		bs_x = Double.parseDouble(b_BS_x.getText());
		bs_y = Double.parseDouble(b_BS_y.getText());
		bs_tx = Double.parseDouble(b_BS_tx.getText());

		wifi_mode = choice_11.getSelectedIndex();
		wifi_pl = Double.parseDouble(b_wifi_pl.getText());
		wifi_shadow = Double.parseDouble(b_wifi_shadow.getText());
		wifi_naka = Double.parseDouble(b_wifi_naka.getText());

		apbs_rate = Double.parseDouble(b_apbs_rate.getText());
		apbs_delay = Double.parseDouble(b_apbs_delay.getText());

		cellular_ul_rate = b_cellular_ul_rate.getText();
		//cellular_ul_delay = b_cellular_ul_delay.getText();
		cellular_ul_loss = Double.parseDouble(b_cellular_ul_loss.getText());
		cellular_dl_rate = b_cellular_dl_rate.getText();
		//cellular_dl_delay = b_cellular_dl_delay.getText();
		cellular_dl_loss = Double.parseDouble(b_cellular_dl_loss.getText());//b_cellular_dl_loss.getSelectedIndex();

		server_rate = Double.parseDouble(b_server_rate.getText());
		server_delay = Double.parseDouble(b_server_delay.getText());
		server_loss = Double.parseDouble(b_server_loss.getText());

		filenameTr = b_filename.getText();
		filename = "temp.tcl";
	}
    public void writeRunFile() throws IOException {
	BufferedWriter bb = new BufferedWriter(new FileWriter("run"));
	bb.write("rm "+filenameTr+" all.ps l\n");
	bb.write("ns temp.tcl\n");
	bb.write("awk -f process_trace_full.awk "+filenameTr+" > l\n");
	bb.write("gnuplot code_gnuplot_all.gnu\n");
	bb.write("gs all.ps\n");
	bb.write("rm l\n");
	bb.close();
    }
	public void writeToFile() throws IOException{
		bw = new BufferedWriter(new FileWriter(filename));
		System.out.println("writing to "+filename);
		writeToFile_11a();
		writeToFile_11_channel();
		writeToFile_base();

		bw.close();

	}
	public void writeToFile_11a() throws IOException {
		bw.write("Phy/WirelessPhyExt set CSThresh_                6.31e-12    ;#-82 dBm Wireless interface sensitivity (sensitivity defined in the standard)\n");
		bw.write("Phy/WirelessPhyExt set Pt_                      0.001  \n");     
		bw.write("Phy/WirelessPhyExt set freq_                    5.18e+9\n");
		bw.write("Phy/WirelessPhyExt set noise_floor_             2.512e-13   ;#-96 dBm for 10MHz bandwidth\n");
		bw.write("Phy/WirelessPhyExt set L_                       1.0         ;#default radio circuit gain/loss\n");
		bw.write("Phy/WirelessPhyExt set PowerMonitorThresh_      1.259e-13   ;#-99dBm power monitor  sensitivity\n");
		bw.write("Phy/WirelessPhyExt set HeaderDuration_          0.000020    ;#20 us\n");
		bw.write("Phy/WirelessPhyExt set BasicModulationScheme_   0  \n");
		bw.write("Phy/WirelessPhyExt set PreambleCaptureSwitch_   1\n");
		bw.write("Phy/WirelessPhyExt set DataCaptureSwitch_       0\n");
		bw.write("Phy/WirelessPhyExt set SINR_PreambleCapture_    2.5118;     ;# 4 dB\n");
		bw.write("Phy/WirelessPhyExt set SINR_DataCapture_        100.0;      ;# 10 dB\n");
		bw.write("Phy/WirelessPhyExt set trace_dist_              1e6         ;# PHY trace until distance of 1 Mio. km (\"infinty\")\n");
		bw.write("Phy/WirelessPhyExt set PHY_DBG_                 0\n");

		bw.write("Mac/802_11Ext set CWMin_                        15\n");
		bw.write("Mac/802_11Ext set CWMax_                        1023\n");
		bw.write("Mac/802_11Ext set SlotTime_                     0.000009\n");
		bw.write("Mac/802_11Ext set SIFS_                         0.000016\n");
		bw.write("Mac/802_11Ext set ShortRetryLimit_              7\n");
		bw.write("Mac/802_11Ext set LongRetryLimit_               4\n");
		bw.write("Mac/802_11Ext set HeaderDuration_               0.000020\n");
		bw.write("Mac/802_11Ext set SymbolDuration_               0.000004\n");
		bw.write("Mac/802_11Ext set BasicModulationScheme_        0\n");
		bw.write("Mac/802_11Ext set use_802_11a_flag_             true\n");
		bw.write("Mac/802_11Ext set RTSThreshold_                 2346\n");
		bw.write("Mac/802_11Ext set MAC_DBG                       0\n");
		bw.write("Mac/802_11Ext set Noise_floor_             2.512e-13   ;#-96 dBm for 10MHz bandwidth\n");
		bw.write("Mac/802_11Ext set Rate_Mode_ 3;\n");
		bw.write("Mac/802_11Ext set Fixed_rate_ 0;\n");
	}
	public void writeToFile_11_channel() throws IOException {
		bw.write("Propagation/InCar set std_db_ "+wifi_shadow+"\n");
		bw.write("Propagation/InCar set m_ "+wifi_naka+"\n");
		bw.write("Propagation/InCar seed_random 1\n");
		bw.write("set opt(nn) "+totalNum+"\n");
		bw.write("set opt(stop) "+(totalTime+1.0)+"\n");
	}
	public void writeToFile_mobile_device() throws IOException {
		bw.write("set temp {1.0.0 1.0.1 1.0.2 1.0.3 1.0.4}\n");
		bw.write("Phy/WirelessPhyExt set Pt_ "+Math.pow(10.0,bs_tx/10.0-3.0)+"\n");
		bw.write("$ns_ node-config -phyType $opt(netif)\n");
		bw.write("set BS(0) [$ns_ node [lindex $temp 0]]\n");
		bw.write("$BS(0) random-motion 0\n");
		bw.write("$BS(0) set X_ "+(xs+bs_x)+"\n");
		bw.write("$BS(0) set Y_ "+(ys+bs_y)+"\n");
		bw.write("$BS(0) set Z_ 0.0\n");

		bw.write("$ns_ node-config -wiredRouting OFF\n");
		if(l_node1_1.isSelected()) {
			bw.write("Phy/WirelessPhyExt set Pt_ "+Math.pow(10.0,node1_tx/10.0-3.0)+"\n");
			bw.write("$ns_ node-config -phyType $opt(netif)\n");
			bw.write("set node_(0) [$ns_ node [lindex $temp [expr 1]]]\n");
			bw.write("$node_(0) base-station [AddrParams addr2id [$BS(0) node-addr]]\n");
			bw.write("$node_(0) random-motion 0\n");
			bw.write("$node_(0) set X_ "+(xs+node1_x)+"\n");
			bw.write("$node_(0) set Y_ "+(ys+node1_y)+"\n");
			bw.write("$node_(0) set Z_ 0.0\n");
		}

		if(l_node2_1.isSelected()) {
			bw.write("Phy/WirelessPhyExt set Pt_ "+Math.pow(10.0,node2_tx/10.0-3.0)+"\n");
			bw.write("$ns_ node-config -phyType $opt(netif)\n");
			bw.write("set node_(1) [$ns_ node [lindex $temp [expr 2]]]\n");
			bw.write("$node_(1) base-station [AddrParams addr2id [$BS(0) node-addr]]\n");
			bw.write("$node_(1) random-motion 0\n");
			bw.write("$node_(1) set X_ "+(xs+node2_x)+"\n");
			bw.write("$node_(1) set Y_ "+(ys+node2_y)+"\n");
			bw.write("$node_(1) set Z_ 0.0\n");
		}

		if(l_node3_1.isSelected()) {
			bw.write("Phy/WirelessPhyExt set Pt_ "+Math.pow(10.0,node3_tx/10.0-3.0)+"\n");
			bw.write("$ns_ node-config -phyType $opt(netif)\n");
			bw.write("set node_(2) [$ns_ node [lindex $temp [expr 3]]]\n");
			bw.write("$node_(2) base-station [AddrParams addr2id [$BS(0) node-addr]]\n");
			bw.write("$node_(2) random-motion 0\n");
			bw.write("$node_(2) set X_ "+(xs+node3_x)+"\n");
			bw.write("$node_(2) set Y_ "+(ys+node3_y)+"\n");
			bw.write("$node_(2) set Z_ 0.0\n");
		}

		if(l_node4_1.isSelected()) {
			bw.write("Phy/WirelessPhyExt set Pt_ "+Math.pow(10.0,node4_tx/10.0-3.0)+"\n");
			bw.write("$ns_ node-config -phyType $opt(netif)\n");
			bw.write("set node_(3) [$ns_ node [lindex $temp [expr 4]]]\n");
			bw.write("$node_(3) base-station [AddrParams addr2id [$BS(0) node-addr]]\n");
			bw.write("$node_(3) random-motion 0\n");
			bw.write("$node_(3) set X_ "+(xs+node4_x)+"\n");
			bw.write("$node_(3) set Y_ "+(ys+node4_y)+"\n");
			bw.write("$node_(3) set Z_ 0.0\n");
		}

	}
	public void writeToFile_dynamicLink() throws IOException{
		BufferedReader tbw = new BufferedReader(new FileReader(cellular_ul_rate));
		StringTokenizer st;
		String line;
		String type, ttime;
		
		while((line = tbw.readLine()) != null){
			st = new StringTokenizer(line);
			type = st.nextToken();
			if(type.startsWith("#")) 
				continue;
			else if (type.equalsIgnoreCase("bandwidth")){
				bw.write("$ns_ simplex-link $UE(0) $eNB "+st.nextToken()+" "+st.nextToken()+" "+st.nextToken()+"\n");
			} else {
				ttime = type;
				bw.write("$ns_ at "+ttime+" \"[[$ns_ link $UE(0) $eNB] link] set bandwidth_ "+st.nextToken()+"\"\n");
				bw.write("$ns_ at "+ttime+" \"[[$ns_ link $UE(0) $eNB] link] set delay_ "+st.nextToken()+"\"\n");
			}
		}
		tbw.close();
		
		tbw = new BufferedReader(new FileReader(cellular_dl_rate));
		
		while((line = tbw.readLine()) != null){
			st = new StringTokenizer(line);
			type = st.nextToken();
			if(type.startsWith("#")) 
				continue;
			else if (type.equalsIgnoreCase("bandwidth")){
				bw.write("$ns_ simplex-link $eNB $UE(0) "+st.nextToken()+" "+st.nextToken()+" "+st.nextToken()+"\n");
			} else {
				ttime = type;
				bw.write("$ns_ at "+ttime+" \"[[$ns_ link $eNB $UE(0)] link] set bandwidth_ "+st.nextToken()+"\"\n");
				bw.write("$ns_ at "+ttime+" \"[[$ns_ link $eNB $UE(0)] link] set delay_ "+st.nextToken()+"\"\n");
			}
		}
		tbw.close();
	}
	public void writeToFile_link_input() throws IOException {
		bw.write("$ns_ duplex-link $BS(0) $UE(0) "+apbs_rate+"Mb "+apbs_delay+"ms DropTail\n");
		writeToFile_dynamicLink();
		
	//	bw.write("$ns_ simplex-link $UE(0) $eNB "+cellular_ul_rate+"Mb "+cellular_ul_delay+"ms LTEQueue/ULAirQueue\n");
	//	bw.write("$ns_ simplex-link $eNB $UE(0) "+cellular_dl_rate+"Mb "+cellular_dl_delay+"ms LTEQueue/DLAirQueue\n");
		bw.write("$ns_ duplex-link $eNB $server "+server_rate+"Mb "+server_delay+"ms DropTail\n");

		if(cellular_dl_loss > 0) {
			bw.write("set em1 [new ErrorModel]\n");
			bw.write("$em1 set rate_ "+cellular_dl_loss+"\n");
			bw.write("$em1 drop-target [new Agent/Null]\n");
			bw.write("$ns_ lossmodel $em1 $eNB $UE(0)\n");	
		} 
		if(cellular_ul_loss > 0) {
			bw.write("set em2 [new ErrorModel]\n");
			bw.write("$em2 set rate_ "+cellular_ul_loss+"\n");
			bw.write("$em2 drop-target [new Agent/Null]\n");
			bw.write("$ns_ lossmodel $em2 $UE(0) $eNB\n");	
		}
		if(server_loss > 0) {
			bw.write("set em3 [new ErrorModel]\n");
			bw.write("$em3 set rate_ "+server_loss+"\n");
			bw.write("$em3 drop-target [new Agent/Null]\n");
			bw.write("$ns_ lossmodel $em3 $eNB $server\n");

			bw.write("set em4 [new ErrorModel]\n");
			bw.write("$em4 set rate_ "+server_loss+"\n");
			bw.write("$em4 drop-target [new Agent/Null]\n");
			bw.write("$ns_ lossmodel $em4 $server $eNB\n");
		}

	}
	private void writeToFile_traffic_each(int node) throws IOException{
		String f="";
		if(node == 0) f = node1_traffic;
		else if(node == 1) f= node2_traffic;
		else if(node == 2) f= node3_traffic;
		else if(node == 3) f= node4_traffic;
		BufferedReader tbw = new BufferedReader(new FileReader(f));
		StringTokenizer st;
		String line;
		String type, rate;
		int pktSize;
		double starttime, endtime;
		int filesize;
		
		/*#HTTP num_connection/sec start end
		HTTP 5 10.0 20.0
		#FTP filesize(Byte) start end
		FTP 10000000 10.0 20.0
		#Streaming rate(Mb) packetsize(byte) start end
		Streaming 0.064Mb 100 10.0 20.0*/
		endtime = 0;
		while((line = tbw.readLine()) != null){
			//String line = inholes.readLine();
			st = new StringTokenizer(line);
			type = st.nextToken();
			if(type.startsWith("#")) {
				continue;
			}
			if(type.equalsIgnoreCase("HTTP")) {
				int num = Integer.parseInt(st.nextToken());
				starttime = Double.parseDouble(st.nextToken());
				endtime = Double.parseDouble(st.nextToken());
				if(totalTime < endtime) totalTime = endtime;
		        bw.write("build_webs $node_("+node+") $server "+num+" "+starttime+" "+endtime+"\n");
			} else if(type.equalsIgnoreCase("FTP")) {
				filesize = Integer.parseInt(st.nextToken());
				starttime = Double.parseDouble(st.nextToken());
				endtime = Double.parseDouble(st.nextToken());
				bw.write("set filesize "+filesize+"\n");
				bw.write("build_ftpclient $node_("+node+") $server "+starttime+" "+endtime+" "+node+" "+filesize+"\n");
			} else if(type.equalsIgnoreCase("Streaming")) {
				rate = st.nextToken();
				pktSize = Integer.parseInt(st.nextToken());
				starttime = Double.parseDouble(st.nextToken());
				endtime = Double.parseDouble(st.nextToken());
				bw.write("build_cbr $node_("+node+") $server "+starttime+" "+endtime+" "+node+" "+rate+" "+pktSize+"\n");
			} else {
				System.out.println("Unrecognized traffic type!!!!!");
			}
		}
		if(totalTime < endtime) totalTime = endtime;
		tbw.close();


	}
	private void writeToFile_traffic_prefix() throws IOException {
		//Agent/TCP set window_ [expr $bdp*16]
		//Agent/TCP set segsize_ [expr $psize-40]
		//Agent/TCP set packetSize_ [expr $psize-40]
		//Agent/TCP set windowInit_ 4
		//Agent/TCP set segsperack_ 1
		bw.write("Agent/TCP set timestamps_ true\n");
		bw.write("set delack 0.4\n");
		bw.write("Agent/TCP set interval_ $delack\n");

		//Agent/TCP/FullTcp set window_ [expr $bdp*16]
		//Agent/TCP/FullTcp set segsize_ [expr $psize-40]
		//Agent/TCP/FullTcp set packetSize_ [expr $psize-40]
		//Agent/TCP/FullTcp set windowInit_ 4
		//Agent/TCP/FullTcp set segsperack_ 1
		bw.write("Agent/TCP/FullTcp set timestamps_ true\n");
		bw.write("Agent/TCP/FullTcp set interval_ $delack\n\n");


		bw.write("Agent/TCP/Linux instproc done {} {\n");
		bw.write("global ns_ filesize\n");
		bw.write("#this doesn't seem to work, had to hack tcp-linux.cc to do repeat ftps\n");
		bw.write("$self set closed_ 0\n");
		bw.write("#needs to be delayed by at least .3sec to slow start\n");
		bw.write("puts \"[$ns_ now] TCP/Linux proc done called\"\n");
		bw.write("$ns_ at [expr [$ns_ now] + 0.3] \"$self send $filesize\"\n");
		bw.write("}\n\n");

		bw.write("# problem is that idle() in tcp.cc never seems to get called...\n");
		bw.write("Application/FTP instproc resume {} {\n");
		bw.write("puts \"called resume\"\n");
		bw.write(" global filesize\n");
		bw.write("$self send $filesize\n");
		bw.write("#	$ns_ at [expr [$ns_ now]  0.5] \"[$self agent] reset\"\n");
		bw.write("$ns_ at [expr [$ns_ now] + 0.5] \"[$self agent] send $filesize\"\n");
		bw.write("}\n\n");

		bw.write("Application/FTP instproc fire {} {\n");
		bw.write("global filesize\n");
		bw.write("$self instvar maxpkts_\n");
		bw.write("set maxpkts_ $filesize\n");
		bw.write("[$self agent] set maxpkts_ $filesize\n");
		bw.write("$self send $maxpkts_\n");
		bw.write("puts \"fire() FTP\"\n");
		bw.write("}\n\n");


		bw.write("proc build_cbr {cnd snd startTime timeToStop Flow_id rate pktSize} {\n");
		bw.write("global ns_\n");
		bw.write("set udp [$ns_ create-connection UDP $snd LossMonitor $cnd $Flow_id]\n");
		bw.write("set cbr [new Application/Traffic/CBR]\n");
		bw.write("$cbr attach-agent $udp\n");
		bw.write("# change these for different types of CBRs\n");
		bw.write("$cbr set packetSize_ $pktSize\n");
		bw.write("$cbr set rate_ $rate\n");
		bw.write("$ns_ at $startTime \"$cbr start\"\n");
		bw.write("$ns_ at $timeToStop \"$cbr stop\"\n");
		bw.write("}\n\n");

		bw.write("# cnd is client node, snd is server node\n");
		bw.write("proc build_ftpclient {cnd snd startTime timeToStop Flow_id filesize} {\n\n");

		bw.write("global ns_ \n");
		bw.write("set ctcp [$ns_ create-connection TCP/Linux $snd TCPSink/Sack1 $cnd $Flow_id]\n");
		bw.write("$ctcp select_ca cubic\n");
		bw.write("set ftp [$ctcp attach-app FTP]\n");
		bw.write("$ftp set enableResume_ true\n");
		bw.write("$ftp set type_ FTP \n\n");

		bw.write("#set up a single infinite ftp with smallest RTT\n");
		bw.write("if {$filesize < 0} {\n");
		bw.write("$ns_ at $startTime \"$ftp start\"\n");
		bw.write("} else {\n");
		bw.write("$ns_ at $startTime \"$ftp send $filesize\"\n");
		bw.write("}\n");
		bw.write("$ns_ at $timeToStop \"$ftp stop\"\n");
		bw.write("}\n\n");

		bw.write("proc build_webs {cnd snd rate startTime timeToStop} {\n");
		bw.write("set CLIENT 0\n");
		bw.write("set SERVER 1\n\n");

		bw.write("# SETUP PACKMIME\n");
		bw.write("set pm [new PackMimeHTTP]\n");
		bw.write("$pm set-TCP Sack\n");
		bw.write("$pm set-client $cnd\n");
		bw.write("$pm set-server $snd\n");
		bw.write("$pm set-rate $rate;                    # new connections per second\n");
		bw.write("$pm set-http-1.1;                      # use HTTP/1.1\n\n");

		bw.write("# create RandomVariables\n");
		bw.write("set flow_arrive [new RandomVariable/PackMimeHTTPFlowArrive $rate]\n");
		bw.write("set req_size [new RandomVariable/PackMimeHTTPFileSize $rate $CLIENT]\n");
		bw.write("set rsp_size [new RandomVariable/PackMimeHTTPFileSize $rate $SERVER]\n\n");

		bw.write("# assign RNGs to RandomVariables\n");
		bw.write("$flow_arrive use-rng [new RNG]\n");
		bw.write("$req_size use-rng [new RNG]\n");
		bw.write("$rsp_size use-rng [new RNG]\n\n");

		bw.write("# set PackMime variables\n");
		bw.write("$pm set-flow_arrive $flow_arrive\n");
		bw.write("$pm set-req_size $req_size\n");
		bw.write("$pm set-rsp_size $rsp_size\n\n");

		bw.write("global ns_\n");
		bw.write("$ns_ at $startTime \"$pm start\"\n");
		bw.write("$ns_ at $timeToStop \"$pm stop\"\n");
		bw.write("}\n\n");


		bw.write("proc finish {} {\n");
		bw.write("global ns_\n");
		bw.write("$ns_ halt\n");
		bw.write("$ns_ flush-trace\n");
		bw.write("exit 0\n");
		bw.write("}\n\n");

	}
	public void writeToFile_traffic() throws IOException {
		writeToFile_traffic_prefix();
		//String[] trafficNames = { "TCP", "HTTP", "Streaming"};
		if(totalNum >= 1) 
			writeToFile_traffic_each(0);
		if(totalNum >= 2)
			writeToFile_traffic_each(1);
		if(totalNum >= 3)
			writeToFile_traffic_each(2);
		if(totalNum >= 4)
			writeToFile_traffic_each(3);
	}
	/*
	public void writeToFile_traffic_input() throws IOException {
		//String[] trafficNames = { "TCP", "HTTP", "Streaming"};
		if(totalNum >= 1)
			writeToFile_traffic_input_each(0,node1_traffic,node1_start,node1_end,node1_rate);
		if(totalNum >= 2)
			writeToFile_traffic_input_each(1,node2_traffic,node2_start,node2_end,node2_rate);
		if(totalNum >= 3)
			writeToFile_traffic_input_each(2,node3_traffic,node3_start,node3_end,node3_rate);
		if(totalNum >= 4)
			writeToFile_traffic_input_each(3,node4_traffic,node4_start,node4_end,node4_rate);
	}
	public void writeToFile_traffic_input_each(int node, int mode, double start, double end, double rate) throws IOException {
		if(mode == 0) {
			//TCP
			bw.write("set sink"+node+" [new Agent/TCPSink]\n");
			bw.write("$ns_ attach-agent $node_("+node+") $sink"+node+"\n");
			bw.write("set tcp"+node+" [new Agent/TCP]\n");
			bw.write("$ns_ attach-agent $server $tcp"+node+"\n");
			bw.write("$ns_ connect $sink"+node+" $tcp"+node+"\n");
			bw.write("$tcp"+node+" set class_ 3\n");
			bw.write("set ftp"+node+" [new Application/FTP]\n");
			bw.write("$ftp"+node+" attach-agent $tcp"+node+"\n");
			bw.write("$ns_ at "+start+" \"$ftp"+node+" start\"\n");
			bw.write("$ns_ at "+end+" \"$ftp"+node+" stop\"\n");
		} else if(mode == 1) {
			//HTTP
			bw.write("remove-all-packet-headers;\n");
			bw.write("add-packet-header IP TCP;\n");
			bw.write("#$ns_ use-scheduler Heap;\n");
			bw.write("set rate"+node+" 5\n");
			bw.write("set pm"+node+" [new PackMimeHTTP]\n");
			bw.write("$pm"+node+" set-client $node_("+node+")\n");
			bw.write("$pm"+node+" set-server $server\n");
			bw.write("$pm"+node+" set-rate $rate"+node+"\n");
			bw.write("$pm"+node+" set-http-1.1\n");
			bw.write("set flowRNG"+node+" [ new RNG]\n");
			bw.write("set reqsizeRNG"+node+" [new RNG]\n");
			bw.write("set rspsizeRNG"+node+" [ new RNG]\n");
			bw.write("set flow_arrive"+node+" [new RandomVariable/PackMimeHTTPFlowArrive $rate"+node+"]\n");
			bw.write("set req_size"+node+" [new RandomVariable/PackMimeHTTPFileSize $rate"+node+" 0]\n");
			bw.write("set rsp_size"+node+" [new RandomVariable/PackMimeHTTPFileSize $rate"+node+" 1]\n");
			bw.write("$flow_arrive"+node+" use-rng $flowRNG"+node+"\n");
			bw.write("$req_size"+node+" use-rng $reqsizeRNG"+node+"\n");
			bw.write("$rsp_size"+node+" use-rng $rspsizeRNG"+node+"\n");
			bw.write("$pm"+node+" set-flow_arrive $flow_arrive"+node+"\n");
			bw.write("$pm"+node+" set-req_size $req_size"+node+"\n");
			bw.write("$pm"+node+" set-rsp_size $rsp_size"+node+"\n");

			bw.write("$ns_ at "+start+" \"$pm"+node+" start\"\n");
			bw.write("$ns_ at "+end+" \"$pm"+node+" stop\"\n");

		} else {
			//Streaming
			bw.write("Application/Traffic/CBR set rate_ "+rate+"kb \n");
			bw.write("# streaming traffic\n");
			bw.write("set null"+node+" [new Agent/Null]\n");
			bw.write("$ns_ attach-agent $node_("+node+") $null"+node+"\n");
			bw.write("set udp"+node+" [new Agent/UDP]\n");
			bw.write("$ns_ attach-agent $server $udp"+node+"\n");
			bw.write("$ns_ connect $null"+node+" $udp"+node+"\n");
			bw.write("$udp"+node+" set class_ 1\n");
			bw.write("set cbr"+node+" [new Application/Traffic/CBR]\n");
			bw.write("$cbr"+node+" attach-agent $udp"+node+"\n");
			bw.write("$ns_ at "+start+" \"$cbr"+node+" start\"\n");
			bw.write("$ns_ at "+end+" \"$cbr"+node+" stop\"\n");
		}

	}*/

	public void writeToFile_base() throws IOException{
		bw.write("global opt\n");
		bw.write("set opt(chan)       Channel/WirelessChannel\n");
		bw.write("set opt(prop)       Propagation/InCar\n");
		bw.write("set opt(netif)      Phy/WirelessPhyExt\n");
		bw.write("set opt(mac)        Mac/802_11Ext\n");
		bw.write("set opt(ifq)        Queue/DropTail/PriQueue\n");
		bw.write("set opt(ll)         LL\n");
		bw.write("set opt(ant)        Antenna/OmniAntenna\n");
		bw.write("set opt(x)             670   \n");
		bw.write("set opt(y)              670   \n");
		bw.write("set opt(ifqlen)         50   \n");
		bw.write("set opt(tr)          "+filenameTr+"\n");
		bw.write("set opt(namtr)       "+filenameTr+"nam\n");
		bw.write("set opt(adhocRouting)   DSDV     \n");                 
		bw.write("set num_wired_nodes      1\n");
		bw.write("set num_bs_nodes         1\n");


		bw.write("set ns_   [new Simulator]\n");
		bw.write("# set up for hierarchical routing\n");
		bw.write("$ns_ node-config -addressType hierarchical\n");
		bw.write("AddrParams set domain_num_ 2\n");
		bw.write("lappend cluster_num 4 1 \n");               
		bw.write("AddrParams set cluster_num_ $cluster_num\n");
		bw.write("lappend eilastlevel 1 1 1 1 4     \n");         
		bw.write("AddrParams set nodes_num_ $eilastlevel \n");

		bw.write("set tracefd  [open $opt(tr) w]\n");
		bw.write("$ns_ trace-all $tracefd\n");


		bw.write("set topo   [new Topography]\n");
		bw.write("$topo load_flatgrid $opt(x) $opt(y)\n");
		bw.write("create-god [expr $opt(nn) + $num_bs_nodes]\n");

		bw.write("Queue/LTEQueue set qos_ true\n");
		bw.write("Queue/LTEQueue set flow_control_ false\n");
		bw.write("#create wired nodes\n");
		bw.write("set UE(0) [$ns_ node '0.0.0']\n");
		bw.write("set eNB [$ns_ node '0.1.0'];\n");
		bw.write("set aGW [$ns_ node '0.2.0']\n");
		bw.write("set server [$ns_ node '0.3.0']\n");


		bw.write("$ns_ node-config -adhocRouting $opt(adhocRouting) \\\n");
		bw.write("-llType $opt(ll) \\\n");
		bw.write("-macType $opt(mac) \\\n");
		bw.write("-ifqType $opt(ifq) \\\n");
		bw.write("-ifqLen $opt(ifqlen) \\\n");
		bw.write("-antType $opt(ant) \\\n");
		bw.write("-propInstance [new $opt(prop)] \\\n");
		bw.write("-phyType $opt(netif) \\\n");
		bw.write("-channel [new $opt(chan)] \\\n");
		bw.write("-topoInstance $topo \\\n");
		bw.write("-wiredRouting ON \\\n");
		bw.write("-agentTrace ON \\\n");
		bw.write("-routerTrace OFF \\\n");
		bw.write("-macTrace OFF\n");

		writeToFile_mobile_device();
		writeToFile_link_input();

		writeToFile_traffic();

		bw.write("for {set i 0} {$i < $opt(nn)} {incr i} {\n");
		bw.write("$ns_ initial_node_pos $node_($i) 10\n");
		bw.write("}\n");

		bw.write("set opt(stop) "+totalTime+"\n");
		bw.write("for {set i 0} {$i < $opt(nn) } {incr i} {\n");
		bw.write("$ns_ at $opt(stop).0000010 \"$node_($i) reset\";\n");
		bw.write("}\n");
		bw.write("$ns_ at $opt(stop).0000010 \"$BS(0) reset\";\n");

		bw.write("$ns_ at $opt(stop).1 \"puts \\\"NS EXITING...\\\" ; $ns_ halt; $ns_ flush-trace\"\n");

		bw.write("puts \"Starting Simulation...\"\n");
		bw.write("$ns_ run\n");

		bw.write("#$ns_ at [expr $opt(stop)+.2 ] \"finish\"\n");
		bw.write("exit 0\n");

	}
}