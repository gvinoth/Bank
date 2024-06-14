package bank;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.time.temporal.ChronoUnit;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

 
public class Main
 
{
	
	public static void interest(Connection conn) throws FileNotFoundException, SQLException{
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\interest.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> Data = gson.fromJson(reader, Map.class);
        String query = "select account_type from customers where acno='"+(String)Data.get("acno")+"'";
        PreparedStatement get = conn.prepareStatement(query);
        ResultSet rs = get.executeQuery();
        String acc="";
        if(rs.next()) {
        	acc+=rs.getString("account_type");
        }
        get.close();
        if(acc.contains("saving")) {
        	String query2 = "select balance, final_int from "+(String)Data.get("acno")+" order by id desc limit 1";
        	PreparedStatement get1 = conn.prepareStatement(query2);
            ResultSet rs1 = get1.executeQuery();
            double interest=0;
            double balance=0;
            if(rs1.next()) {
            	interest = rs1.getDouble("final_int");
            	balance = rs1.getDouble("balance");
            }
            get1.close();
            String insertAmountQuery = "INSERT INTO "+(String)Data.get("acno")+" (amount, balance, date, time, status, interest, final_int) VALUES (?, ?, ?, ?, ?, ?, ?)";
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            PreparedStatement insertStmt = conn.prepareStatement(insertAmountQuery);
            insertStmt.setDouble(1, interest);
            insertStmt.setDouble(2, interest+balance);
            insertStmt.setObject(3, currentDate);
            insertStmt.setObject(4, currentTime);
            insertStmt.setString(5, "Interested Added");
            insertStmt.setDouble(6,calculateInterest(balance));
            insertStmt.setDouble(7,0.000);
            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Interested added successfully to the account!");
            } else {
                System.out.println("Failed to add.");
            }
        }
        else {
        	System.out.println("Interest calculation is not applicable for this account");
        }
        
	}
	
	public static void EmailSender(Connection conn) throws FileNotFoundException, SQLException{
		Scanner s = new Scanner(System.in);
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\aadhar.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> Data = gson.fromJson(reader, Map.class);
        
        String query = "select aadhar_no, email from customers where acno='"+(String)Data.get("acno")+"'";
        PreparedStatement send = conn.prepareStatement(query);
        ResultSet rs = send.executeQuery();
        String aadhar = "";
        String receiver_email = "";
        if(rs.next()) {
        	aadhar+=rs.getString("aadhar_no");
        	receiver_email+=rs.getString("email");
        }
        send.close();
        if(aadhar != null && !aadhar.equals("")) {
        	System.out.println("Aadhar status : Active");
        }
        else {
        	System.out.println("Aadhar status : Pending");
        	System.out.println("If you want to send a alert mail to corresponding customer then click 1 to send or 2 to Later");
        	int a = s.nextInt();
        	if(a==1) {
        		final String username = "vinothsoftwaredeveloper@outlook.com";
                final String password = "Guru@3545";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "outlook.office365.com");
                props.put("mail.smtp.port", "587");
                
                
                
                Session session = Session.getInstance(props,
                  new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                  });

                try {
                	
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("vinothsoftwaredeveloper@outlook.com"));
                    message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(receiver_email));
                    message.setSubject("Urgent: Update Aadhar Details Required for Account Continuity");
                    message.setText("Dear Customer,"+"\n"+"We write to inform you of an important regulatory update regarding your account with Bank of ABC. As part of government regulations, it is mandatory to link your Aadhar details with your bank account."+"\n"+
                    "To ensure continued access to your account and to comply with these regulations, we kindly request you to update your Aadhar details with us at your earliest convenience. Failure to do so within the next 7 days will result in the deactivation of your account."+"\n"+
                    		"Please visit our nearest branch with the following documents to complete the update process:\r\n"
                    		+ "\r\n"
                    		+ "Your original Aadhar card\r\n"
                    		+ "Your latest bank statement\r\n"
                    		+ "Any other government-issued identification document (e.g., PAN card, passport)\r\n"
                    		+ "Our dedicated staff will assist you throughout the process to ensure a smooth and hassle-free experience.\r\n"
                    		+ "\r\n"
                    		+ "Thank you for your prompt attention to this matter. Should you have any questions or require further assistance, please do not hesitate to contact our customer service team at [Customer Service Number].\r\n"
                    		+ "\r\n"
                    		+ "We appreciate your cooperation and look forward to serving you better."+"\n"+"Thank you"+"\n"+"Warm regards,"+"\n"+"Bank Manager");

                    Transport.send(message);

                    System.out.println("Done");

                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
        	}
        	else if(a==2) {
        		System.out.println("Thank you for your response");
        	}
        	else {
        		System.out.println("Invalid option Plz select correct one");
        		EmailSender(conn);
        	}
        }

    }
	
	public static void pdfGenerator(Connection conn) throws FileNotFoundException{
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\pdf.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> Data = gson.fromJson(reader, Map.class);
		HashMap<Integer, String[]> tableData = new HashMap<>();
		Document document = new Document((PageSize.A4));
		try {
			LocalDate currentDate = LocalDate.now();
			String query1 = "select name, acno, pin from customers where acno = ? and pin = ?";
        	PreparedStatement checkStmt = conn.prepareStatement(query1);
        	checkStmt.setString(1, (String)Data.get("acno"));
        	checkStmt.setDouble(2, (Double)Data.get("pin"));
        	ResultSet rs1 = checkStmt.executeQuery();
        	String acc = "";
        	String customer_name = "";
        	int s_pin = 0;
        	if(rs1.next()) {
        		acc += rs1.getString("acno");
        		s_pin = rs1.getInt("pin");
        		customer_name+=rs1.getString("name");
        	}
        	checkStmt.close();
        	if(((String)Data.get("acno")).contains(acc) && s_pin==((Double)Data.get("pin"))) {
    			String query = "select id, amount, balance, date, time, status from "+(String)Data.get("acno");
            	PreparedStatement checkStmt1 = conn.prepareStatement(query);
            	ResultSet rs = checkStmt1.executeQuery();
            	while (rs.next()) {
            		 int id = rs.getInt("id");
                    String[] rowData = {
                    		String.valueOf(rs.getDouble("amount")),
                    		String.valueOf(rs.getDouble("balance")),
                            rs.getString("date"),
                            rs.getString("time"),
                            rs.getString("status")
                    };
                    tableData.put(id, rowData);
                }
                // Close the database resources
                rs.close();
                
                // Specify the file path where you want to save the PDF
                PdfWriter.getInstance(document, new FileOutputStream("miniStatement.pdf"));

                // Open the Document for writing
                document.open();
                Paragraph heading = new Paragraph("Bank of ABC", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD));
                heading.setAlignment(Element.ALIGN_CENTER);
                document.add(heading);
                Paragraph lineSpace = new Paragraph(" "); // Blank paragraph for space
                lineSpace.setSpacingAfter(20); // Set desired line space (in points)
                document.add(lineSpace);
                Paragraph name = new Paragraph("Name : "+customer_name);
                Paragraph ac = new Paragraph("Account No : "+(String)Data.get("acno"));
                Paragraph type = new Paragraph("Type : Mini Statement");
                Paragraph date = new Paragraph("Date of Statement : "+currentDate);
                document.add(name);
                document.add(ac);
                document.add(type);
                document.add(date);
                Paragraph lineSpace1 = new Paragraph(" "); // Blank paragraph for space
                lineSpace1.setSpacingAfter(20); // Set desired line space (in points)
                document.add(lineSpace1);
                PdfPTable table = new PdfPTable(5);
                table.addCell("Amount");
                table.addCell("Balance");
                table.addCell("Date");
                table.addCell("Time");
                table.addCell("Status");
                for (int id : tableData.keySet()) {
                    String[] rowData = tableData.get(id);
                    for (String data : rowData) {
                        table.addCell(data);
                    }
                }

                // Add table to the document
                document.add(table);
                Paragraph notice = new Paragraph("Important Notice", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD));
                notice.setAlignment(Element.ALIGN_CENTER);
                notice.setSpacingBefore(10); // Add some space before the notice
                notice.setSpacingAfter(10); // Add some space after the notice
                document.add(notice);
                Paragraph statement = new Paragraph("Statements are accepted as correct unless queried within 30 days.");
                statement.setAlignment(Element.ALIGN_CENTER);
                document.add(statement);
                // Close the Document
                document.close();

                System.out.println("PDF generated successfully!");
        	}

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void checkBalance(Connection conn) throws FileNotFoundException {
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\balance.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> Data = gson.fromJson(reader, Map.class);
        try {
        	String query1 = "select acno, pin from customers where acno = ? and pin = ?";
        	PreparedStatement checkStmt = conn.prepareStatement(query1);
        	checkStmt.setString(1, (String)Data.get("acno"));
        	checkStmt.setDouble(2, (Double)Data.get("pin"));
        	ResultSet rs1 = checkStmt.executeQuery();
        	String acc = "";
        	int s_pin = 0;
        	if(rs1.next()) {
        		acc += rs1.getString("acno");
        		s_pin = rs1.getInt("pin");
        	}
        	checkStmt.close();
        	if(((String)Data.get("acno")).contains(acc) && s_pin==((Double)Data.get("pin"))) {
        		String query = "SELECT balance FROM " + (String) Data.get("acno") + " ORDER BY id DESC LIMIT 1";
            	PreparedStatement checkStmt1 = conn.prepareStatement(query);
            	ResultSet rs = checkStmt1.executeQuery();
            	double balance = 0;
            	if(rs.next()) {
            		balance = rs.getDouble("balance");
            	}
            	checkStmt1.close();
            	System.out.println("Your current balance is "+balance);
        	}
        	else {
        		System.out.println("Check your credentials once again Thankyou");
        	}
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public static void transfer(Connection conn) throws FileNotFoundException {
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\transfer.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> Data = gson.fromJson(reader, Map.class);
        try {
        	String acc = (String)Data.get("from");
        	String check = "select pin from customers where acno='" + acc + "'";
        	PreparedStatement pstmt1 = conn.prepareStatement(check);
            ResultSet resultSet1 = pstmt1.executeQuery();
            int pin=0;
            if(resultSet1.next()) {
            	pin = resultSet1.getInt("pin");
            }
            pstmt1.close();
            if(pin == (Double) Data.get("pin")) {
            // Step 1: Fetch amount from the source table
            String fetchAmountQuery = "SELECT balance, date, interest, final_int FROM "+(String)Data.get("from")+" ORDER BY id DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(fetchAmountQuery);
            ResultSet resultSet = pstmt.executeQuery();
            Date d1 = null;
            double inr = 0;
            double fin_inr = 0;

            double amount = 0;
            if (resultSet.next()) {
                amount = resultSet.getDouble("balance");
                d1 = resultSet.getDate("date");
                inr = resultSet.getDouble("interest");
                fin_inr = resultSet.getDouble("final_int");
            }
//            System.out.println(fin_inr+" int1");
            amount -= (Double)Data.get("amount");
            String query = "update "+(String)Data.get("from")+" set final_int=0.000";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.executeUpdate();
            String dateString1 = "2024-03-22";
            LocalDate date1 = LocalDate.parse(dateString1);
            String insertAmountQuery = "INSERT INTO "+(String)Data.get("from")+" (amount, balance, date, time, status, interest, final_int) VALUES (?, ?, ?, ?, ?, ?, ?)";
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            PreparedStatement insertStmt = conn.prepareStatement(insertAmountQuery);
            insertStmt.setDouble(1, (Double)Data.get("amount"));
            insertStmt.setDouble(2, (Double)amount);
            insertStmt.setObject(3, date1);
            insertStmt.setObject(4, currentTime);
            insertStmt.setString(5, "Debited");
            insertStmt.setDouble(6,calculateInterest((Double)amount));
            //insertStmt.setDouble(7,calculateInterest((Double)amount));
            
            long daysDifference = ChronoUnit.DAYS.between(date1, d1.toLocalDate());
//            System.out.println(daysDifference);
            if(daysDifference == 0) {
                insertStmt.setDouble(7,Math.abs(fin_inr));
            }
            else {
            	fin_inr+=((daysDifference-1)*inr);
//            	System.out.println(fin_inr+" fin1");
            	insertStmt.setDouble(7,Math.abs(fin_inr));
            }
            // Execute the update statement
            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Amount Debited successfully from your account!");
            } else {
                System.out.println("Failed to transfer amount.");
            }

            // Close resources
            pstmt.close();
            insertStmt.close();
            String fetchAmountQuery2 = "SELECT balance, date, interest, final_int FROM "+(String)Data.get("to")+" ORDER BY time DESC LIMIT 1";
            PreparedStatement pstmt2 = conn.prepareStatement(fetchAmountQuery2);
            ResultSet resultSet2 = pstmt2.executeQuery();
            Date d2 = null;
            double inr1 = 0;
            double fin_inr1 = 0;
            double amount2 = 0;
            if (resultSet2.next()) {
                amount2 = resultSet2.getDouble("balance");
                d2 = resultSet2.getDate("date");
                inr1 = resultSet2.getDouble("interest");
                fin_inr1 = resultSet2.getDouble("final_int");
            }
//            System.out.println(fin_inr1+" int");
            amount2 += (Double)Data.get("amount");
            String query1 = "update "+(String)Data.get("to")+" set final_int=0.000";
            PreparedStatement ps1 = conn.prepareStatement(query1);
            ps1.executeUpdate();
            String dateString = "2024-03-22";
            LocalDate date = LocalDate.parse(dateString);
            String insertAmountQuery2 = "INSERT INTO "+(String)Data.get("to")+" (amount, balance, date, time, status, interest, final_int) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt2 = conn.prepareStatement(insertAmountQuery2);
            insertStmt2.setDouble(1, (Double)Data.get("amount"));
            insertStmt2.setDouble(2, (Double)amount2);
            insertStmt2.setObject(3, date);
            insertStmt2.setObject(4, currentTime);
            insertStmt2.setString(5, "Credited");
            insertStmt2.setDouble(6,calculateInterest((Double)amount2));
            //insertStmt2.setDouble(7,calculateInterest((Double)amount2));
            
            long daysDifference1 = ChronoUnit.DAYS.between(date, d2.toLocalDate());
//            System.out.println(daysDifference1);
            if(daysDifference1 == 0) {
                insertStmt2.setDouble(7,Math.abs(fin_inr1));
            }
            else {
            	fin_inr1+=((daysDifference1-1)*inr1);
//            	System.out.println(fin_inr1+"fin");
            	insertStmt2.setDouble(7,Math.abs(fin_inr1));
            }
            // Execute the update statement
            int rowsAffected2 = insertStmt2.executeUpdate();

            if (rowsAffected2 > 0) {
                System.out.println("Amount transferred to receiver's account successfully!");
            } else {
                System.out.println("Failed to transfer amount.");
            }

            // Close resources
            pstmt2.close();
            insertStmt2.close();
            }
            else {
            	System.out.println("Please enter the correct pin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	public static void addCard(Connection conn) throws SQLException, FileNotFoundException {
	    // Prepare SQL UPDATE statement
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\addCard.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> cardData = gson.fromJson(reader, Map.class);
	    String sql = "UPDATE customers SET card = ?, card_type = ?, card_no = ?, expiry_date = ?, cvv_no = ?, card_status = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = conn.prepareStatement(sql);
	        
	        pstmt.setString(1, (String) cardData.get("card"));
	        pstmt.setString(2, (String) cardData.get("card_type"));
	        pstmt.setString(3, (String) cardData.get("card_no"));
	        pstmt.setString(4, convertDateFormat((String) cardData.get("expiry_date")));
	        pstmt.setString(5, (String) cardData.get("cvv_no"));
	        pstmt.setString(6, (String) cardData.get("card_status"));
	        pstmt.setString(7, (String) cardData.get("acno"));
	        
	        // Execute UPDATE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Card data added successfully!");
	        } else {
	            System.out.println("No data found with the provided account number: " + (String) cardData.get("acno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	@SuppressWarnings("resource")
	public static void blockCard(Connection conn) throws SQLException, FileNotFoundException {
	    // Prepare SQL UPDATE statement
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\blockCard.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> cardData = gson.fromJson(reader, Map.class);
        String sql1 = "SELECT card_no FROM customers WHERE acno= ?";
	    String sql = "UPDATE customers SET card_status = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	    	pstmt = conn.prepareStatement(sql1);
	    	pstmt.setString(1, (String) cardData.get("acno"));
	    	ResultSet resultSet = pstmt.executeQuery();
	    	
	    	
	    	//pstmt.close();
            if (resultSet.next()) {
            	String card_no = resultSet.getString("card_no");
            	if(card_no!=null) {
            		pstmt = conn.prepareStatement(sql);
        	        
        	        pstmt.setString(1, "Block");
        	        pstmt.setString(2, (String) cardData.get("acno"));
        	        
        	        // Execute UPDATE statement
        	        int rowsAffected = pstmt.executeUpdate();
        	        
        	        if (rowsAffected > 0) {
        	            System.out.println("Card data blocked successfully!");
        	        } else {
        	            System.out.println("No card data found with the provided account number: " + (String) cardData.get("acno"));
        	        }
        	        pstmt.close();
            	}
            	else {
                	System.out.println("No data found with the provided account number: " + (String) cardData.get("acno"));
                	return;
                }
            }
            else {
            	System.out.println("No data found with the provided account number: " + (String) cardData.get("acno"));
            	return;
            }
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void deleteCustomer(Connection con) throws SQLException, FileNotFoundException {
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\deleteCustomer.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> updatedData = gson.fromJson(reader, Map.class);
	    // Prepare SQL DELETE statement
	    String sql = "DELETE FROM customers WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = con.prepareStatement(sql);
	        
	        // Set the account number parameter
	        pstmt.setString(1, (String) updatedData.get("dacno"));
	        
	        // Execute DELETE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Customer with account number " + (String) updatedData.get("dacno") + " deleted successfully!");
	        } else {
	            System.out.println("No customer found with the provided account number: " + (String) updatedData.get("dacno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void deleteNominee(Connection conn) throws SQLException, FileNotFoundException {
		// Prepare SQL UPDATE statement
		Gson gson = new Gson();
		FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\addNominee.json");
        @SuppressWarnings("unchecked")
        Map<String, Object> updatedData = gson.fromJson(reader, Map.class);
	    String sql = "UPDATE customers SET nominee = ?, nom_relation = ?, nom_contact = ?, nom_email = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = conn.prepareStatement(sql);
	        
	        // Set updated values
	        pstmt.setString(1,"");
	        pstmt.setString(2,"");
	        pstmt.setString(3,"");
	        pstmt.setString(4,"");
	        pstmt.setString(5, (String) updatedData.get("acno"));
	        
	        // Execute UPDATE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Nominee data updated successfully!");
	        } else {
	            System.out.println("No customer found with the provided account number: " + (String) updatedData.get("acno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void editNominee(Connection conn) throws SQLException, FileNotFoundException {
	    // Prepare SQL UPDATE statement
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\addNominee.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> updatedData = gson.fromJson(reader, Map.class);
	    String sql = "UPDATE customers SET nominee = ?, nom_relation = ?, nom_contact = ?, nom_email = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = conn.prepareStatement(sql);
	        
	        // Set updated values
	        pstmt.setString(1, (String) updatedData.get("nominee"));
	        pstmt.setString(2, (String) updatedData.get("nom_relation"));
	        pstmt.setString(3, (String) updatedData.get("nom_contact"));
	        pstmt.setString(4, (String) updatedData.get("nom_email"));
	        pstmt.setString(5, (String) updatedData.get("acno"));
	        
	        // Execute UPDATE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Nominee data updated successfully!");
	        } else {
	            System.out.println("No customer found with the provided account number: " + (String) updatedData.get("acno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void addNominee(Connection conn) throws SQLException, FileNotFoundException {
	    // Prepare SQL UPDATE statement
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\addNominee.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> updatedData = gson.fromJson(reader, Map.class);
	    String sql = "UPDATE customers SET nominee = ?, nom_relation = ?, nom_contact = ?, nom_email = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = conn.prepareStatement(sql);
	        
	        // Set updated values
	        pstmt.setString(1, (String) updatedData.get("nominee"));
	        pstmt.setString(2, (String) updatedData.get("nom_relation"));
	        pstmt.setString(3, (String) updatedData.get("nom_contact"));
	        pstmt.setString(4, (String) updatedData.get("nom_email"));
	        pstmt.setString(5, (String) updatedData.get("acno"));
	        
	        // Execute UPDATE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Nominee data inserted successfully!");
	        } else {
	            System.out.println("No customer found with the provided account number: " + (String) updatedData.get("acno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void addDetails(Connection conn)throws SQLException, FileNotFoundException {
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\addDetails.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> addedData = gson.fromJson(reader, Map.class);
        String sql = "SELECT acno,name,date_of_join FROM customers WHERE date_of_join BETWEEN ? AND ?";
        PreparedStatement pstmt = null;
        try {
        	pstmt = conn.prepareStatement(sql);
        	pstmt.setString(1, convertDateFormat((String) addedData.get("from")));
        	pstmt.setString(2, convertDateFormat((String) addedData.get("to")));
        	ResultSet resultSet = pstmt.executeQuery();
        	Map<Integer, Map<String, Object>> dataMap = new HashMap<>();

            // Loop through the ResultSet and store data in the HashMap
            int count = 1;
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();
                String acno = resultSet.getString("acno");
                String name = resultSet.getString("name");
                Date dateOfJoin = resultSet.getDate("date_of_join");

                rowData.put("acno", acno);
                rowData.put("name", name);
                rowData.put("date_of_join", dateOfJoin);

                dataMap.put(count, rowData);
                count++;
            }

            // Close the ResultSet and PreparedStatement
            resultSet.close();
            pstmt.close();
            // Print the data from the HashMap
            for (Map.Entry<Integer, Map<String, Object>> entry : dataMap.entrySet()) {
                System.out.println("Customer : " + entry.getKey());
                Map<String, Object> rowData = entry.getValue();
                for (Map.Entry<String, Object> data : rowData.entrySet()) {
                    System.out.println(data.getKey() + ": " + data.getValue());
                }
                System.out.println("----------------------------------------");
            }
        }
        catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}
	
	public static void updateCustomer(Connection conn) throws SQLException, FileNotFoundException {
	    // Prepare SQL UPDATE statement
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\updateCustomer.json");
        @SuppressWarnings("unchecked")
		Map<String, Object> updatedData = gson.fromJson(reader, Map.class);
	    String sql = "UPDATE customers SET name = ?, dob = ?, address = ?, contact_no = ?, aadhar_no = ?, email = ? WHERE acno = ?";
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = conn.prepareStatement(sql);
	        
	        // Set updated values
	        pstmt.setString(1, (String) updatedData.get("name"));
	        pstmt.setString(2, convertDateFormat((String) updatedData.get("dob")));
	        pstmt.setString(3, (String) updatedData.get("address"));
	        pstmt.setString(4, (String) updatedData.get("contact_no"));
	        pstmt.setString(5, (String) updatedData.get("aadhar_no"));
	        pstmt.setString(6, (String) updatedData.get("email"));
	        pstmt.setString(7, (String) updatedData.get("acno"));
	        
	        // Execute UPDATE statement
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Customer data updated successfully!");
	        } else {
	            System.out.println("No customer found with the provided account number: " + (String) updatedData.get("acno"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Close resources
	        if (pstmt != null) {
	            pstmt.close();
	        }
	    }
	}

	public static String convertDateFormat(String dob) {
        try {
            // Parse the date string using SimpleDateFormat
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = inputFormat.parse(dob);

            // Format the parsed date into MySQL date format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle the parsing error appropriately in your application
        }
    }
	
	public static int generatePIN() {
        Random random = new Random();
        // Generate a random integer between 1000 (inclusive) and 9999 (exclusive)
        return random.nextInt(9000) + 1000;
    }
	
	public static void login(Scanner s, Connection con) {
		s.nextLine();
        System.out.print("Enter email: ");
        String email = s.nextLine();
        System.out.print("Enter password: ");
        String password = s.nextLine();
 
        try {
        	int a;
            PreparedStatement statement = con.prepareStatement("SELECT * FROM signup WHERE email = ? AND password = ?");
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (email.endsWith("manager.com")) {
                	PreparedStatement pstmt1 = con.prepareStatement("select date_of_login, time_details from login where email = ? order by id desc limit 1");
                	pstmt1.setString(1, email);
                	ResultSet rs = pstmt1.executeQuery();
                	System.out.println("Last login details :");
                	if(rs.next()) {
                		System.out.println("Date : "+rs.getString("date_of_login"));
                		System.out.println("Time : "+rs.getString("time_details"));
                	}
                	else {
                		System.out.println("Welcome to our automated banking services for the first time");
                	}
                	pstmt1.close();
                	LocalDate currentDate = LocalDate.now();
                    LocalTime currentTime = LocalTime.now();
                	PreparedStatement pstmt = con.prepareStatement("INSERT INTO login (email, date_of_login, time_details) VALUES ( ?, ?, ?)");
                	pstmt.setString(1, email);
                	pstmt.setObject(2, currentDate);
                	pstmt.setObject(3, currentTime);
                	// Execute the INSERT statement
                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                    	System.out.println("Login successful!"); 
                    }
                    pstmt.close();
                	System.out.println("1.Add Customers \n2.Edit Customers \n3.Remove Customers \n4.Interest Calculations \n5.Add Nominee"
                    		+ " \n6.Edit Nominee \n7.Remove Nominee \n8.Added Details \n9.Check Aadhar \n10.Logout");
                    a = s.nextInt();
                	while (a<=9) {
                        s.nextLine();
         
                        switch (a) {
                            case 1:
                            	System.out.println("Welcome to Add Customer Page");
                            	addCustomer(con);
                                break;
                            case 2:
                            	System.out.println("Welcome to Edit Customer Page");
                            	updateCustomer(con);
                            	break;
                            case 3:
                            	System.out.println("Welcome to Remove Customer Page");
                            	deleteCustomer(con);
                            	break;
                            case 4:
                            	System.out.println("Welcome to Interest Calculation Page");
                            	interest(con);
                            	break;
                            case 5:
                            	System.out.println("Welcome to Add Nominee Page");
                            	addNominee(con);
                            	break;
                            case 6:
                            	System.out.println("Welcome to edit Nominee Page");
                            	editNominee(con);
                            	break;
                            case 7:
                            	System.out.println("Welcome to delete Nominee Page");
                            	deleteNominee(con);
                            	break;
                            case 8:
                            	System.out.println("Get customer added details");
                            	addDetails(con);
                            	break;
                            case 9:
                            	System.out.println("Check customer's Aadhar details");
                            	EmailSender(con);
                            	break;
                            default:
                                System.out.println("Invalid choice!");
                                break;
                        }
                        System.out.println("1.Add Customers \n2.Edit Customers \n3.Remove Customers \n4.Interest Calculations \n5.Add Nominee"
                        		+ " \n6.Edit Nominee \n7.Remove Nominee \n8.Added Details \n9.Check Aadhar \n10.Logout");
                        a = s.nextInt();
                    }
                	if(a!=10) {
                		System.out.println("Invalid choice!.....Plz relogin this");
                		login(s,con);
                	}
                	if(a==10) {
                		System.out.println("Thank you for using this automated banking services");
                	}
                } else if (email.endsWith("customer.com")) {
                	PreparedStatement pstmt1 = con.prepareStatement("select date_of_login, time_details from login where email = ? order by id desc limit 1");
                	pstmt1.setString(1, email);
                	ResultSet rs = pstmt1.executeQuery();
                	System.out.println("Last login details :");
                	if(rs.next()) {
                		System.out.println("Date : "+rs.getString("date_of_login"));
                		System.out.println("Time : "+rs.getString("time_details"));
                	}
                	else {
                		System.out.println("Welcome to our automated banking services for the first time");
                	}
                	pstmt1.close();
                	LocalDate currentDate = LocalDate.now();
                    LocalTime currentTime = LocalTime.now();
                	PreparedStatement pstmt = con.prepareStatement("INSERT INTO login (email, date_of_login, time_details) VALUES ( ?, ?, ?)");
                	pstmt.setString(1, email);
                	pstmt.setObject(2, currentDate);
                	pstmt.setObject(3, currentTime);
                	// Execute the INSERT statement
                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                    	System.out.println("Login successful!"); 
                    }
                    pstmt.close();
                    	System.out.println("1.Check Balance \n2.Send Money \n3.Mini Statement \n4.Add Card Details \n5.Block card \n6.Logout");
                        a = s.nextInt();
                    	while (a<=5) {
                            s.nextLine();
             
                            switch (a) {
                            	case 1:
                            		System.out.println("Welcome to Balance Page");
                            		checkBalance(con);
                            		break;
                            	case 2:
                            		System.out.println("Welcome to Money Transfer Page");
                            		transfer(con);
                            		break;
                            	case 3:
                                	System.out.println("Welcome to Mini Statement Page");
                                	pdfGenerator(con);
                                    break;
                                case 4:
                                	System.out.println("Welcome to Add Card Details Page");
                                	addCard(con);
                                    break;
                                case 5:
                                	System.out.println("Welcome to Block Card Page");
                                	blockCard(con);
                                    break;
                                default:
                                    System.out.println("Invalid choice!");
                                    break;
                            }
                            System.out.println("1.Check Balance \n2.Send Money \n3.Mini Statement \n4.Add Card Details \n5.Block card 6.Logout");
                            a = s.nextInt();
                        }
                    	if(a!=6) {
                    		System.out.println("Invalid choice!.....Plz relogin this");
                    		login(s,con);
                    	}
                    	if(a==6) {
                    		System.out.println("Thank you for using this automated banking services");
                    	}
                	
                } else {
                    System.out.println("Unknown email domain");
                }
            }
            else {
            	System.out.println(email);
            	System.out.println(password);
                System.out.println("Invalid email or password. Please try again.");
            }
 
            resultSet.close();
            statement.close();
        }
            catch (Exception e) {
            e.printStackTrace();
        }
    }
        
 
    public static void signup(Scanner scanner, Connection conn) {
        System.out.println("Would you like to create an account? Please fill in the details below:");
        try {
            // Prompt user to enter employee details
        	scanner.nextLine();
            System.out.println("Username: ");
            String username = scanner.nextLine();
            System.out.println("Password: ");
            String password = scanner.nextLine();
            System.out.println("Confirm Password: ");
            String confirmpassword = scanner.nextLine();
            System.out.println("Email: ");
            String email = scanner.nextLine();
 
            // Check if the employee ID already exists in the database
            PreparedStatement checkStatement = conn.prepareStatement("SELECT * FROM signup WHERE username = ?");
            checkStatement.setString(1, username);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("User already exists. Please choose a different one.");
                return;
            }
            resultSet.close();
            checkStatement.close();
 
            // Store the employee details in a HashMap
            HashMap<String, String> loginDetails = new HashMap<>();
            loginDetails.put("username", username);
            loginDetails.put("password", password);
            loginDetails.put("confirmpassword", confirmpassword);
            loginDetails.put("email", email);
            
 
            // Insert the employee details into the database
            PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO signup (username, password, confirmpassword, email) VALUES ( ?, ?, ?, ?)");
            insertStatement.setString(1, loginDetails.get("username"));
            insertStatement.setString(2, loginDetails.get("password"));
            insertStatement.setString(3, loginDetails.get("confirmpassword"));
            insertStatement.setString(4, loginDetails.get("email"));
            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account created successfully!");
            } else {
                System.out.println("Failed to create account. Please try again.");
            }
            insertStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static double calculateInterest(double amount) {
        // Calculate 3% interest
        double interest = amount * 0.03;
        return interest;
    }
	
	public static String generateAccountNumber() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        // Add 4 random characters
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        // Add 6 random digits
        for (int i = 0; i < 6; i++) {
            sb.append(digits.charAt(random.nextInt(digits.length())));
        }
        return sb.toString();
    }
	@SuppressWarnings("unchecked")
	public static void addCustomer(Connection conn) throws FileNotFoundException, IOException, ParseException, SQLException{
		Gson gson = new Gson();
        FileReader reader = new FileReader("C:\\Users\\vinoth_g\\Documents\\json\\customer.json");
        Map<String, Object> jsonData = gson.fromJson(reader, Map.class);
        String acno = generateAccountNumber();
        int pin = generatePIN();
        // Prepare SQL INSERT statement
        String sql = "INSERT INTO customers (acno, pin, name, dob, gender, nationality, address, contact_no, date_of_join, aadhar_no, email, account_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);

        // Set values from JSON data
		pstmt.setString(1,acno);
		pstmt.setInt(2,pin);
        pstmt.setString(3, (String) jsonData.get("name"));
        pstmt.setString(4, convertDateFormat((String) jsonData.get("dob")));
        pstmt.setString(5, (String) jsonData.get("gender"));
        pstmt.setString(6, (String) jsonData.get("nationality"));
        pstmt.setString(7, (String) jsonData.get("address"));
        pstmt.setString(8, (String) jsonData.get("contact_no"));
        pstmt.setString(9, convertDateFormat((String) jsonData.get("date_of_join")));
        pstmt.setString(10, (String) jsonData.get("aadhar_no"));
        pstmt.setString(11, (String) jsonData.get("email"));
        pstmt.setString(12, (String) jsonData.get("account_type"));

        // Execute INSERT statement
        pstmt.executeUpdate();
       // pstmt.close();
        // Close resources
        //pstmt = conn.prepareStatement(sql);
        String createTableQuery = "CREATE TABLE "+acno+" (id INT AUTO_INCREMENT PRIMARY KEY, amount DECIMAL(10.3), balance DECIMAL(10,3), date DATE, time TIME, status VARCHAR(255), interest DECIMAL(10,3), final_int DECIMAL(10,3))";
        pstmt = conn.prepareStatement(createTableQuery);
        pstmt.executeUpdate();
        //pstmt.close();
        //pstmt = conn.prepareStatement(sql);
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        pstmt = conn.prepareStatement("INSERT INTO "+acno+" (amount, balance, date, time, status, interest, final_int) VALUES (?, ?, ?, ?, ?, ?, ?)");
        pstmt.setDouble(1,(Double) jsonData.get("amt"));
        pstmt.setDouble(2, (Double) jsonData.get("amt"));
        pstmt.setObject(3, currentDate);
        pstmt.setObject(4, currentTime);
        pstmt.setString(5, "Credited");
        pstmt.setDouble(6, calculateInterest((Double) jsonData.get("amt")));
        pstmt.setDouble(7, calculateInterest((Double) jsonData.get("amt")));

        // Execute the INSERT statement
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
        	System.out.println("Data inserted successfully!"); 
        } else {
            System.out.println("Failed to insert values.");
        }
//        conn.close();
        pstmt.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
	    public static void main(String args[]) throws ClassNotFoundException, SQLException {
	       try {
	    	   Class.forName("com.mysql.cj.jdbc.Driver");
		       Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/bank","root","password");
		        int n;
	            Scanner s = new Scanner(System.in);
	            System.out.println("Welcome to our ABC Bank");
	            
	            System.out.println("1.SignIn  2.SignUp\nEnter your choice:");
	            n = s.nextInt();
	            if(n==1){
	            	login(s, con);
	            }
	            else if(n==2) {
	            	signup(s, con);
	            	login(s, con);
	            }
	            else {
	            	System.out.println("Invalid option");
	            }
	       }catch(Exception e) {
	    	   System.out.println("Server down");
	       }
	    }
	}
