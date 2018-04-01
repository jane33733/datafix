package esbWork;

import java.awt.Color;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * 「一般批次作業維護申請單」和「連線系統資料變更申請」 產生DB2或SP文件
 * 
 * @author ESB14646 (Ver1.4)
 */
@SuppressWarnings("unchecked")
public class DCsql extends JFrame implements ActionListener, KeyListener {

	/**
 * 
 */
	private static final long serialVersionUID = 1L;
	private static final String MARK = "^";

	// 可以自行修改的地方
	private String filePath = "D:\\"; // 預設產擋路徑

	private JTextArea inputSqlTxtArea;
	private JTextField enterAddress;

	private JLabel finishLabel = new JLabel();
	private JButton sendButton = new JButton("產生文件");
	private ButtonGroup codeBtnGroup;
	private ButtonGroup txtTypeBtnGroup;
	private List<String> completeContent = new ArrayList();
	private List<String> keycheckContent = new ArrayList();

	public static void main(String[] args) {
		DCsql dcSql = new DCsql("產生資料變更TXT檔");
		dcSql.setBounds(200, 150, 600, 400);
		dcSql.setVisible(true);
	}

	public DCsql(String title) {

		super(title);
		setLayout(null);

		// 選擇產檔種類
		JLabel txtTypeLabel = new JLabel();
		txtTypeLabel.setText("txt檔種類：    　DB2    　 SP  ");
		txtTypeLabel.setBounds(20, 200, 200, 20);
		JRadioButton db2Rbtn = new JRadioButton("DB2", true);
		db2Rbtn.setActionCommand("DB2");
		db2Rbtn.setBounds(90, 200, 20, 20);
		JRadioButton spRbtn = new JRadioButton("SP");
		spRbtn.setActionCommand("SP");
		spRbtn.setBounds(140, 200, 20, 20);
		txtTypeBtnGroup = new ButtonGroup();
		txtTypeBtnGroup.add(db2Rbtn);
		txtTypeBtnGroup.add(spRbtn);
		add(txtTypeLabel);
		add(db2Rbtn);
		add(spRbtn);

		// 選擇編碼
		JLabel dbTypeLabel = new JLabel("編碼：       ANSI　   UTF-8(指令有中文)");
		dbTypeLabel.setBounds(250, 200, 280, 20);
		JRadioButton ansiCodeRbtn = new JRadioButton("ANSI", true);
		ansiCodeRbtn.setActionCommand("ANSI");
		ansiCodeRbtn.setBounds(290, 200, 20, 20);
		JRadioButton utfCodeRbtn = new JRadioButton("UTF-8");
		utfCodeRbtn.setActionCommand("UTF-8");
		utfCodeRbtn.setBounds(340, 200, 20, 20);
		codeBtnGroup = new ButtonGroup();
		codeBtnGroup.add(ansiCodeRbtn);
		codeBtnGroup.add(utfCodeRbtn);
		add(dbTypeLabel);
		add(ansiCodeRbtn);
		add(utfCodeRbtn);

		JLabel addressLabel = new JLabel();
		addressLabel.setText("TXT產檔路徑：");
		addressLabel.setBounds(20, 230, 100, 20);
		add(addressLabel);

		inputSqlTxtArea = new JTextArea("請輸入SQL語法");
		inputSqlTxtArea.setBounds(20, 30, 550, 150);
		inputSqlTxtArea.addKeyListener(this);
		add(inputSqlTxtArea);

		enterAddress = new JTextField(filePath);
		enterAddress.setBounds(110, 230, 450, 20);
		add(enterAddress);

		JLabel outcomeLabel = new JLabel();
		outcomeLabel.setText("結果：");
		outcomeLabel.setBounds(20, 300, 50, 30);
		add(outcomeLabel);

		finishLabel.setBounds(60, 300, 500, 30);
		finishLabel.setVisible(false);
		add(finishLabel);

		sendButton.setBounds(20, 270, 150, 25);
		sendButton.addActionListener(this);
		add(sendButton);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public List<String> generateDB2content(String sqlStr) {
		sqlStr = sqlStr.trim().replace("\n", " ");
		sqlStr = sqlStr.replace("\r", " ");
		String field;
		String contentFront = "";
		String copysqlStr = sqlStr;
		String contentBehind = "0";
		String keyCheck;
		copysqlStr = copysqlStr.toUpperCase();

		if (copysqlStr.contains("WMTDB.")) {
			contentFront = "WMTDB" + MARK;
		}
		if (copysqlStr.contains("WMMDB.")) {
			contentFront = "WMMDB" + MARK;
		}

		if (copysqlStr.startsWith("UPDATE")) {
			contentBehind = MARK + "1";
			contentFront += sqlStr.substring(copysqlStr.indexOf("UPDATE ") + 6,
					copysqlStr.indexOf(" SET ")).trim()
					+ MARK;
			contentBehind = MARK
					+ sqlStr.substring(copysqlStr.indexOf("WHERE") + 5,
							copysqlStr.length()).trim() + contentBehind;
			keyCheck = sqlStr.substring(copysqlStr.indexOf("WHERE") + 5,
					copysqlStr.length()).trim();

			// 判別欲變更的欄位數量
			field = sqlStr.substring(copysqlStr.indexOf(" SET ") + 5,
					copysqlStr.indexOf("WHERE")).trim();
			if (field.matches("[/(].+[/)]\\s*[=]\\s*[/(].+[/)]")) {
				field = field.replace("(", "");
				field = field.replace(")", "");
				field = field.replace("=", ",");
				String[] allField = field.split(",");
				int middleMore = (allField.length) / 2;
				for (int i = 0; i < (middleMore); i++) {
					field = allField[i].trim() + MARK;
					if (allField[middleMore + i].contains("’")) {
						field += "X" + MARK;
						allField[middleMore + i] = allField[middleMore + i]
								.replace("’", "");
					} else {
						field += "9" + MARK;
					}
					field += allField[middleMore + i].trim();
					if (keyCheck.contains(allField[i].trim())
							&& allField.length > 2) {
						keycheckContent.add(contentFront + field
								+ contentBehind);
						continue;
					}
					completeContent.add(contentFront + field + contentBehind);
				}
			} else {
				String[] allField = field.split(",");
				for (String unitField : allField) {
					field = unitField.substring(0, unitField.indexOf("="))
							.trim() + MARK;

					if (unitField.contains("’")) {
						field += "X" + MARK;
						unitField = unitField.replace("’", "");
					} else {
						field += "9" + MARK;
					}
					field += unitField.substring(unitField.indexOf("=") + 1,
							unitField.length()).trim();
					if (keyCheck.contains(unitField.substring(0,
							unitField.indexOf("=")).trim())
							&& allField.length > 1) {
						keycheckContent.add(contentFront + field
								+ contentBehind);
						continue;
					}
					completeContent.add(contentFront + field + contentBehind);
				}
			}
			if (!keycheckContent.isEmpty()) {
				if (keycheckContent.size() > 1) {
					completeContent
							.add("↓提醒↓此update異動多個欄位,且多個欄位名稱和where條件相同，建議使用SP.TXT");
				}
				for (int x = 0; x < keycheckContent.size(); x++) {
					completeContent.add(keycheckContent.get(x));
				}
				if (keycheckContent.size() > 1) {
					completeContent
							.add("↑提醒↑此update異動多個欄位,且多個欄位名稱和where條件相同，建議使用SP.TXT");
				}
			}

		} else if (copysqlStr.startsWith("DELETE")) {
			contentBehind = MARK + "2";
			if (copysqlStr.contains("FROM")) {
				contentFront += sqlStr.substring(
						copysqlStr.indexOf("FROM") + 4,
						copysqlStr.indexOf("WHERE")).trim()
						+ MARK + MARK + MARK;
			} else {
				contentFront += sqlStr.substring(
						copysqlStr.indexOf("DELETE") + 6,
						copysqlStr.indexOf("WHERE")).trim()
						+ MARK + MARK + MARK;
			}
			contentBehind = MARK
					+ sqlStr.substring(copysqlStr.indexOf("WHERE") + 5,
							copysqlStr.length()).trim() + contentBehind;
			completeContent.add(contentFront + contentBehind);
		} else {
			finishLabel.setForeground(Color.RED);
			finishLabel.setText("非異動或刪除語法，不可使用DB2方式產檔");
			finishLabel.setVisible(true);
		}
		return completeContent;
	}

	public void generateSPcontent(String sqlStr) {

		String copySqlStr = sqlStr.toUpperCase();
		String content = "2" + MARK;
		if (copySqlStr.contains("WMTDB.")) {
			content += "WMTDB" + MARK + "WMTDB.SP_EXEC(’";
		}
		if (copySqlStr.contains("WMMDB.")) {
			content += "WMMDB" + MARK + "WMMDB.SP_EXEC(’";
		}

		if (copySqlStr.startsWith("DELETE")) {
			if (!copySqlStr.contains("FROM")) {
				sqlStr = sqlStr.substring(0, copySqlStr.indexOf("DELETE") + 6)
						+ " FROM "
						+ sqlStr.substring(copySqlStr.indexOf("DELETE") + 6,
								copySqlStr.length()).trim();
			}
		}
		sqlStr = sqlStr.replace("’", "’’").trim();
		sqlStr = sqlStr.replaceAll("\n+", " ");
		content += sqlStr + "’)";
		completeContent.add(content);
	}

	public void generateFile(String address, String fileName, String fileCode) {
		try {
			File f = new File(address + fileName + ".TXT");
			OutputStreamWriter osw = null;
			if ("ANSI".equals(fileCode)) {
				osw = new OutputStreamWriter(new FileOutputStream(f), "MS950");
			} else {
				osw = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			}

			for (int i = 0; i < completeContent.size(); i++) {
				osw.write(completeContent.get(i));
				if (i != (completeContent.size() - 1)) {
					osw.append("\r\n");
				}
			}
			osw.close();

		} catch (IOException e) {
			finishLabel.setForeground(Color.RED);
			finishLabel.setText("產生文字檔失敗");
			finishLabel.setVisible(true);
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		completeContent.clear();
		finishLabel.setVisible(false);
		String[] allSQL = inputSqlTxtArea.getText().replaceAll("[-]{2}.+", "")
				.split(";");

		if ("DB2".equals(txtTypeBtnGroup.getSelection().getActionCommand())) {
			for (String sqlStr : allSQL) {
				if (!sqlStr.trim().isEmpty() && !finishLabel.isVisible()) {
					keycheckContent.clear();
					if (!(sqlStr.contains("wmtdb.")
							|| sqlStr.contains("wmmdb.")
							|| sqlStr.contains("WMTDB.") || sqlStr
								.contains("WMMDB."))) {
						finishLabel.setForeground(Color.RED);
						finishLabel.setText("SQL指令沒有Schema (WMTDB或WMMDB) ");
						finishLabel.setVisible(true);
					}
					generateDB2content(sqlStr.trim());
				}
			}
		} else {
			for (String sqlStr : allSQL) {
				if (!sqlStr.trim().isEmpty() && !finishLabel.isVisible()) {
					if (!(sqlStr.contains("wmtdb.")
							|| sqlStr.contains("wmmdb.")
							|| sqlStr.contains("WMTDB.") || sqlStr
								.contains("WMMDB."))) {
						finishLabel.setForeground(Color.RED);
						finishLabel.setText("SQL指令沒有Schema (WMTDB或WMMDB) ");
						finishLabel.setVisible(true);
					}
					generateSPcontent(sqlStr.trim());
				}
			}
		}

		if (!finishLabel.isVisible()) {
			filePath = enterAddress.getText();
			if (!filePath.endsWith("\\")) {
				filePath += "\\";
			}
			generateFile(filePath, txtTypeBtnGroup.getSelection()
					.getActionCommand(), codeBtnGroup.getSelection()
					.getActionCommand());
		}
		if (!finishLabel.isVisible()) {
			finishLabel.setForeground(Color.BLACK);
			finishLabel.setText("文檔已順利產生在指定路徑");
			finishLabel.setVisible(true);
		}
	}

	public void keyPressed(KeyEvent e) {
		if ("請輸入SQL語法".endsWith(inputSqlTxtArea.getText())) {
			inputSqlTxtArea.setText("");
		}

	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
