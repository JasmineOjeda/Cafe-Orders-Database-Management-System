/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print ((rs.getString (i)).trim() + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");
	 String oldPass = "";
	 String oldType = "";
	 boolean loginChanged = false;
	 boolean passChanged = false;
	 boolean typeChanged = false;
	 String sessionLogin = "";
         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            oldPass = "";
	    oldType = "";
            loginChanged = false;
	    passChanged = false;
	    typeChanged = false;
	    sessionLogin = "";
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql, authorisedUser); break;
                   case 2:
		   sessionLogin = authorisedUser;
		   if(esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = \'Manager\'", authorisedUser)) > 0){
		      oldType = "Manager";
		   } else if(esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = \'Employee\'", authorisedUser)) > 0){
		      oldType = "Employee";
		   } else {
		      oldType = "Customer";
		   }
		   System.out.println("Please enter your password to continue");
		   oldPass = in.readLine();
		   if(esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.password = '%s'", authorisedUser, oldPass)) > 0){ 
		   UpdateProfile(esql, authorisedUser, oldPass, oldType); 
		   loginChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s'", sessionLogin)) == 0);
           	   passChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.password = '%s'", sessionLogin, oldPass)) == 0);
           	   typeChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = '%s'", sessionLogin, oldType)) == 0);
           	   if(loginChanged || passChanged || typeChanged){
              		usermenu = false;
           	   }
		   break;
		   } else {
		   System.out.println("Incorrect password!");
		   break;
		   }
                   case 3: PlaceOrder(esql, authorisedUser); break;
                   case 4: UpdateOrder(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/

   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void Menu(Cafe esql, String sessionLogin){
     try{
     boolean inItemMenu = true; //Boolean to keep looping the menu
     String query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager'", sessionLogin); //Query to check if the logged in user is a manager
     int managerCheck =  esql.executeQuery(query);
     if(managerCheck > 0){ //If they are a manager
         while(inItemMenu){
            System.out.println("SEARCH MENU");
            System.out.println("-----------");
            System.out.println("1. Search item by name");
            System.out.println("2. Search item by type");
            System.out.println("3. Add item");
            System.out.println("4. Update item");
            System.out.println("5. Delete item");
            System.out.println("9. Back to main menu");
            switch (readChoice()){
               case 1: itemNameSearch(esql); break;
               case 2: itemTypeSearch(esql); break;
               case 3: addItem(esql); break;
               case 4: updateItem(esql); break;
               case 5: deleteItem(esql); break;
               case 9: inItemMenu = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }
         }
     } else {
         while(inItemMenu){
            System.out.println("SEARCH MENU");
            System.out.println("-----------");
            System.out.println("1. Search item by name");
            System.out.println("2. Search item by type");
            System.out.println("9. Back to main menu");
            switch (readChoice()){
               case 1: itemNameSearch(esql); break;
               case 2: itemTypeSearch(esql); break;
               case 9: inItemMenu = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }
         }
        
     }
     }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

  public static void printSearchResults(List<List<String>> results){
	for(int i = 0; i < results.size(); i++){
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("Item Name: " + results.get(i).get(0));
		System.out.println("Type: " + results.get(i).get(1));
		System.out.println("Price: " + results.get(i).get(2));
		System.out.println("Description: " + results.get(i).get(3));
		System.out.println("Image URL: " + results.get(i).get(4));


	}

  }

  public static void itemNameSearch(Cafe esql){
     try{
     boolean inputNotRead = true; //bool to check if input is < 50 characters
     String input = "";
     while (inputNotRead){ //gets input
     System.out.println("Enter name of item to search (item name should not be more than 50 characters):");
      input = in.readLine();
      if(input.length() > 50){
         System.out.println("Item name should not be over 50 characters");
      } else {
         inputNotRead = false;
      }
     }
     String query = String.format("SELECT *  FROM Menu M WHERE M.itemName = '%s'", input); //query to find tuples with item name matching input
    printSearchResults(esql.executeQueryAndReturnResult(query));
     }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

  public static void itemTypeSearch(Cafe esql){
   try{
     boolean inputNotRead = true; //bool to check if input is < 20 characters
     String input = "";
     while (inputNotRead){ //gets input
     System.out.println("Enter type of item to search (type name should not be more than 20 characters):");
      input = in.readLine();
      if(input.length() > 20){
         System.out.println("Item type should not be over 20 characters");
      } else {
         inputNotRead = false;
      }
     }
     String query = String.format("SELECT *  FROM Menu M WHERE M.type = '%s'", input); //query to find tuples with item name matching input
     printSearchResults(esql.executeQueryAndReturnResult(query)); 
     }catch(Exception e){
         System.err.println (e.getMessage ());
      }

  }

  public static void addItem(Cafe esql){
    try{
      String itemName = "";
      String type = "";
      double price = 0;
      String description = "";
      String imageURL = "";
      String userInput = "";
      double truncPrice = 0;
      double roundPrice = 0;
      boolean invalidInput = true;
      while(invalidInput){ //Loop to check for valid item name
      	System.out.println("Enter name of item to insert, must be less than 50 characters (type EXIT to quit and cancel item creation)");
      	itemName = in.readLine();
      	if(itemName.equals("EXIT")){ //Checks if user wants to quit
	   return;
        } else
        if(itemName.length() > 50 || itemName.length() < 1){ //Checks if itemName is within domain
	   System.out.println("Item name must not be greater than 50 characters and not empty");
	} else
        if(esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", itemName)) > 0){ //Checks if itemName already exists since it is primary key
	   System.out.println("Item name should be unique, there already exists an item with the same name in the menu.");
        } else { //If every condition is satisfied, break out of loop
           invalidInput = false;
        }
      }

      invalidInput = true;
      while(invalidInput){ //Loop to check for valid item type
        System.out.println("Enter type of item to insert, must be less than 20 characters (type EXIT to quit and cancel item creation)");
        type = in.readLine();
        if(type.equals("EXIT")){ //Checks if user wants to quit
           return;
        } else
        if(type.length() > 20 || type.length() < 1){ //Checks if type is within domain
           System.out.println("Item type must not be greater than 20 characters and not empty");
        } else { //If every condition is satisfied, break out of loop
           invalidInput = false;
        }
      }

      invalidInput = true;
      while(invalidInput){ //Loop to check for valid item price
       do{        
	System.out.println("Enter price of item to insert (type -1 to quit and cancel item creation)");
	try{
           price = Double.parseDouble(in.readLine());
	   break;
        } catch (Exception e){
	   System.out.println("Please enter a numerical value");
           continue;
	}
        }while(true);
        if(price == -1){ //Checks if user wants to quit
           return;
        } else
        if(price < 0){ //Checks if type is within domain
           System.out.println("Item price must not be negative");
        } else 
        if (price == 0){
	   System.out.println("***WARNING*** You seem to be creating an item with a price of 0, type \"CONFIRM\" to procceed, or type anything else to exit item creation");
           userInput = in.readLine();
           if(!userInput.equals("CONFIRM")){
              return;
           }
	   invalidInput = false;    
	}else { //If every condition is satisfied, check if the decimal places is > 2 and fix, then break out of loop
        boolean priceFix = true;
	String text = Double.toString(price); 
        int decimalLocation = text.indexOf('.');
	int numPlaces = text.length() - decimalLocation - 1;  //How many decimal places exist in price 
	while(priceFix){
           if(numPlaces > 2){ //If more than 2 decimal places
	      truncPrice = (Math.floor(price*100.00))/100.00;
	      roundPrice = (Math.round(price*100.00))/100.00;
	      System.out.println("Your price has more than two decimal places, which is not applicable for a price, would you like to truncade the price to two decimal places or round the price to two decimal places?");
	      System.out.println("OPTIONS");
	      System.out.println("1. Truncade (" + price + " --> " + truncPrice + ")");
	      System.out.println("2. Round (" + price + " --> " + roundPrice + ")");
           switch(readChoice()){
                case 1: price = truncPrice; priceFix = false; break;
                case 2: price = roundPrice; priceFix = false; break;
                default: System.out.println("Unrecognized choice!"); break;
           }
	  
	 } else {
	  priceFix = false;
	}	
          }
           invalidInput = false;
        }
      
 
}
    invalidInput = true;
    while(invalidInput){ //Loop to check for valid description
	System.out.println("Enter description of item to insert, type NONE or hit enter for no description (description must be at most 400 characters)");
	description = in.readLine();
	if(description.equals("NONE")){ //Checks if description is NONE
	   description = "";
	   invalidInput = false;
	} else
	if(description.length() > 400){ //Checks if description is within domain
	   System.out.println("Description must be at most 400 characters");
	} else { //Break out of loop otherwise
	   invalidInput = false;
	}
    }

    invalidInput = true;
    while(invalidInput){ //Loop to check for valid image URL
        System.out.println("Enter image URL of item to insert, type NONE or hit enter for no image URL (image URL must be at most 256 characters)");
        imageURL = in.readLine();
        if(imageURL.equals("NONE")){ //Checks if image url is NONE
           imageURL = "";
           invalidInput = false;
        } else
        if(imageURL.length() > 256){ //Checks if image URL is within domain
           System.out.println("Image URL must be at most 256 characters");
        } else { //Break out of loop otherwise
           invalidInput = false;
        }
    }

    esql.executeUpdate(String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES ('%s', '%s', '%s', '%s', '%s')", itemName, type, price, description, imageURL));
 

    }catch(Exception e){
       System.err.println (e.getMessage ());
    }   
  }

  public static void updateItem(Cafe esql){
      while(true){
	System.out.println("UPDATE MENU");
	System.out.println("-----------");
	System.out.println("1. Update Specific Item");
	System.out.println("2. Change Item Type");
	System.out.println("9. Back to main menu");
	switch(readChoice()){
	   case 1: updateSpecific(esql); break;
	   case 2: changeType(esql); break;
	   case 9: return; 
	   default : System.out.println("Unrecognized choice!"); break;
	}
      }   	
  }

  public static void updateSpecific(Cafe esql){
    try{
     boolean invalidItem = true;
     String input = "";
     while(invalidItem){
	System.out.println("Enter name of item to update (item names are not more than 50 characters)");
	System.out.println("Type EXIT to exit update process");
	input = in.readLine();
	if(input.equals("EXIT")){
	   return;
	} else
	if(input.length() > 50){
	   System.out.println("Item name can not be over 50 characters");
	} else
	if(esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", input)) == 0){
	   System.out.println("Item with that name does not exist. Item name is CaSe sensitive.");
	} else {
	   invalidItem = false;
	}
     }

      String itemName = "";
      String type = "";
      double price = 0;
      String description = "";
      String imageURL = "";
      String userInput = "";
      double truncPrice = 0;
      double roundPrice = 0;
      boolean invalidInput = true;
      while(invalidInput){ //Loop to check for valid item name
      	System.out.println("ENTER \"SKIP\" TO KEEP OLD ITEM NAME--Enter new item name, must be less than 50 characters (type EXIT to quit and cancel item update)");
      	itemName = in.readLine();
      	if(itemName.equals("EXIT")){ //Checks if user wants to quit
	   return;
        } else
	if (itemName.equals("SKIP")){
	   invalidInput = false;
	} else
        if(itemName.length() > 50 || itemName.length() < 1){ //Checks if itemName is within domain
	   System.out.println("Item name must not be greater than 50 characters and not empty");
	} else
        if(esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", itemName)) > 0){ //Checks if itemName already exists since it is primary key
	   System.out.println("Item name should be unique, there already exists an item with the same name in the menu.");
        } else { //If every condition is satisfied, break out of loop
           invalidInput = false;
        }
      }

      invalidInput = true;
      while(invalidInput){ //Loop to check for valid item type
        System.out.println("ENTER \"SKIP\" TO KEEP OLD ITEM TYPE--Enter new item type, must be less than 20 characters (type EXIT to quit and cancel item update)");
        type = in.readLine();
        if(type.equals("EXIT")){ //Checks if user wants to quit
           return;
        } else
	if(type.equals("SKIP")){
	   invalidInput = false;
	} else
        if(type.length() > 20 || type.length() < 1){ //Checks if type is within domain
           System.out.println("Item name must not be greater than 20 characters and not empty");
        } else { //If every condition is satisfied, break out of loop
           invalidInput = false;
        }
      }

      invalidInput = true;
      while(invalidInput){ //Loop to check for valid item price
       do{        
	System.out.println("ENTER \"-2\" TO KEEP OLD ITEM PRICE--Enter new item price (type -1 to quit and cancel item update)");
	try{
           price = Double.parseDouble(in.readLine());
	   break;
        } catch (Exception e){
	   System.out.println("Please enter a numerical value");
           continue;
	}
        }while(true);
        if(price == -1){ //Checks if user wants to quit
           return;
        } else
	if(price == -2){
	   invalidInput = false;
	} else
        if(price < 0){ //Checks if type is within domain
           System.out.println("Item price must not be negative");
        } else 
        if (price == 0){
	   System.out.println("***WARNING*** You seem to be setting an item price to 0, type \"CONFIRM\" to procceed, or type anything else to cancel item update");
           userInput = in.readLine();
           if(!userInput.equals("CONFIRM")){
              return;
           }
	   invalidInput = false;    
	}else { //If every condition is satisfied, check if the decimal places is > 2 and fix, then break out of loop
        boolean priceFix = true;
	String text = Double.toString(price); 
        int decimalLocation = text.indexOf('.');
	int numPlaces = text.length() - decimalLocation - 1;  //How many decimal places exist in price 
	while(priceFix){
           if(numPlaces > 2){ //If more than 2 decimal places
	      truncPrice = (Math.floor(price*100.00))/100.00;
	      roundPrice = (Math.round(price*100.00))/100.00;
	      System.out.println("Your price has more than two decimal places, which is not applicable for a price, would you like to truncade the price to two decimal places or round the price to two decimal places?");
	      System.out.println("OPTIONS");
	      System.out.println("1. Truncade (" + price + " --> " + truncPrice + ")");
	      System.out.println("2. Round (" + price + " --> " + roundPrice + ")");
           switch(readChoice()){
                case 1: price = truncPrice; priceFix = false; break;
                case 2: price = roundPrice; priceFix = false; break;
                default: System.out.println("Unrecognized choice!"); break;
           }
	  
	 } else {
	  priceFix = false;
	}	
          }
           invalidInput = false;
        }
	}
    invalidInput = true;
    while(invalidInput){ //Loop to check for valid description
	System.out.println("Enter \"SKIP\" TO KEEP OLD DESCRIPTION--Enter new description of item, type NONE or hit enter for no description (description must be at most 400 characters)");
	description = in.readLine();
	if(description.equals("SKIP")){
	  invalidInput = false;
	} else
	if(description.equals("NONE")){ //Checks if description is NONE
	   description = "";
	   invalidInput = false;
	} else
	if(description.length() > 400){ //Checks if description is within domain
	   System.out.println("Description must be at most 400 characters");
	} else { //Break out of loop otherwise
	   invalidInput = false;
	}
    }

    invalidInput = true;
    while(invalidInput){ //Loop to check for valid image URL
        System.out.println("ENTER \"SKIP\" TO KEEP OLD IMAGE URL--Enter image URL of item to insert, type NONE or hit enter for no image URL (image URL must be at most 256 characters)");
        imageURL = in.readLine();
	if(imageURL.equals("SKIP")){
	   invalidInput = false;
	} else
        if(imageURL.equals("NONE")){ //Checks if image url is NONE
           imageURL = "";
           invalidInput = false;
        } else
        if(imageURL.length() > 256){ //Checks if image URL is within domain
           System.out.println("Image URL must be at most 256 characters");
        } else { //Break out of loop otherwise
           invalidInput = false;
        }
    }

    if(!type.equals("SKIP")){
        esql.executeUpdate(String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", type, input));
    }
    if(price != -2){
        esql.executeUpdate(String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'", price, input));
    }
    if(!description.equals("SKIP")){
        esql.executeUpdate(String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", description, input));
    }
    if(!imageURL.equals("SKIP")){
        esql.executeUpdate(String.format("UPDATE Menu SET imageURL = '%s' WHERE itemName = '%s'", imageURL, input));
    }
    if(!itemName.equals("SKIP")){
        esql.executeUpdate(String.format("UPDATE Menu SET itemName = '%s' WHERE itemName = '%s'", itemName, input));
    }


     } catch(Exception e){
	System.err.println(e.getMessage());
     }
    
  }

  public static void changeType(Cafe esql){
    try{
	String input = "";
	String type = "";
	boolean invalidInput = true;
        while(invalidInput){
	   System.out.println("Enter item type to change (type \"EXIT\" to quit)");
	   input = in.readLine();
	   if(input.equals("EXIT")){
		return;
	   } else if(esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.type = '%s'", input)) == 0){
		System.out.println("Item type does not exist");
	   } else {
		invalidInput = false;
	   }
	}
	invalidInput = true;
	while(invalidInput){
	   System.out.println("Enter new item type, must be less than 20 characters (type EXIT to quit)");
	   type = in.readLine();
           if(type.equals("EXIT")){ //Checks if user wants to quit
           	return;
           } else
           if(type.length() > 20 || type.length() < 1){ //Checks if type is within domain
           	System.out.println("Item type must not be greater than 20 characters and not empty");
           } else { //If every condition is satisfied, break out of loop
           	invalidInput = false;
           }
        }
	esql.executeUpdate(String.format("UPDATE Menu SET type = '%s' WHERE type = '%s'", type, input));
	
    } catch (Exception e){
	System.err.println(e.getMessage());
    }
  }

  public static void deleteItem(Cafe esql){
      try{	
	String input = "";
	boolean invalidInput = true;
	while(invalidInput){
	   System.out.println("Enter name of item to delete, type \"EXIT\" to exit");
	   input = in.readLine();
	   if(input.equals("EXIT")){
		return;
	   } else if (esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", input)) == 0){
		System.out.println("Item name not found");
	   } else {
		esql.executeUpdate(String.format("DELETE FROM Menu M WHERE M.itemname = '%s'", input));
		invalidInput = false;
	   }
	}
      } catch (Exception e){
	System.err.println(e.getMessage());
	}
  }

  public static void UpdateProfile(Cafe esql, String sessionLogin, String oldPass, String oldType){
    try{
    String query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager'", sessionLogin); //Query to check if the logged in user is a manager
    int managerCheck =  esql.executeQuery(query);
    boolean updateMenu = true;
    boolean loginChanged = false;
    boolean passChanged = false;
    boolean typeChanged = false;
    boolean invalidInput = true;
    String input = "";
    while(updateMenu){
    if(managerCheck > 0){
	System.out.println("UPDATE");
	System.out.println("-----------");
	System.out.println("1. Update self");
	System.out.println("2. Update other user");
	System.out.println("9. Back to main menu");
	switch(readChoice()){
	   case 1: 
	   updateManager(esql, sessionLogin);
	   loginChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s'", sessionLogin)) == 0);
	   passChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.password = '%s'", sessionLogin, oldPass)) == 0);
	   typeChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = '%s'", sessionLogin, oldType)) == 0);
	   if(loginChanged || passChanged || typeChanged){
	      return;
	   }
	   break;
	   case 2:
	   invalidInput = true;
	   while(invalidInput){
	   	System.out.println("Enter login of another user to edit their profile, or enter \"EXIT\" to quit");
		input = in.readLine();
		if(input.equals("EXIT")){
		  invalidInput = false;
		} else if (input.equals(sessionLogin)){
		    System.out.println("You cannot choose yourself!");
		} else if(esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s'", input)) == 0){
		    System.out.println("User does not exist!");
		} else {
	   	updateManager(esql, input); break;
		}
	   }
	   break;
	   case 9: updateMenu = false; break;
	}
	
    } else {
        System.out.println("UPDATE");
        System.out.println("-----------");
        System.out.println("1. Update self");
        System.out.println("9. Back to main menu");
        switch(readChoice()){
           case 1: 
           selfUpdate(esql, sessionLogin);
           passChanged = (esql.executeQuery(String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.password = '%s'", sessionLogin, oldPass)) == 0);
           if(passChanged){
              return;
           }
           break;
           case 9: updateMenu = false; break;
        }


    }
   }
        }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }
  public static void selfUpdate(Cafe esql, String sessionLogin){
	try{
	boolean inMenu = true;
	String input = "EXIT";
	String phoneNum = "EXIT";
	String password = "EXIT";
        while(inMenu){
           System.out.println("UPDATE PROFILE");
           System.out.println("-----------");
           System.out.println("1. Update phone number");
           System.out.println("2. Update password - IF PASSWORD CHANGED, YOU WILL BE REQUIRED TO LOGIN AGAIN");
           System.out.println("3. Update favorite items");
           System.out.println("9. Back to main menu");
	   switch(readChoice()){
		case 1:
                System.out.println("Enter new phone number, or enter \"EXIT\" to quit");
                input = in.readLine();
                if(!input.equals("EXIT")){
                  if(esql.executeQuery(String.format("SELECT * From Users U WHERE U.phoneNum = '%s'", input)) > 0){
                        System.out.println("Another user with the same phone number exists!");
                   } else if(input.length() > 16){
                        System.out.println("Login should be less than 16 characters");
                   } else{
                   phoneNum = input;
                   }
                }
                break;
                case 2:
                System.out.println("Enter new password, or enter \"EXIT\" to quit");
                input = in.readLine();
                if(!input.equals("EXIT")){
                   if(input.length() > 50 || input.length() < 1){
                        System.out.println("Password should be between 1-50 characters");
                   } else {
                        password = input;
                   }
                }
                break;
                case 3:
                   changeFavItems(esql, sessionLogin); break;
		default : System.out.println("Unrecognized choice!"); break;
		case 9: inMenu = false; break;
	   }
	}
	if(!phoneNum.equals("EXIT")){
                esql.executeUpdate(String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", phoneNum, sessionLogin));
        }
        if(!password.equals("EXIT")){
                esql.executeUpdate(String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", password, sessionLogin));
        }



        }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

  public static void updateManager(Cafe esql, String sessionLogin){
	try{
	boolean inMenu = true;
	String input = "EXIT";
	String login = "EXIT";
	String phoneNum = "EXIT";
	String password = "EXIT";
	String type = "EXIT";
	
	while(inMenu){
	   System.out.println("UPDATE");
	   System.out.println("-----------");
	   System.out.println("1. Update login - IF YOUR LOGIN CHANGED, YOU WILL BE REQUIRED TO LOGIN AGAIN");
	   System.out.println("2. Update phone number");
	   System.out.println("3. Update password - IF YOUR PASSWORD CHANGED, YOU WILL BE REQUIRED TO LOGIN AGAIN");
	   System.out.println("4. Update favorite items");
	   System.out.println("5. Update type - IF YOUR TYPE CHANGED, YOU WILL BE REQUIRED TO LOGIN AGAIN");
	   System.out.println("9. Back to main menu");
	   switch(readChoice()){
		case 1:
		System.out.println("Enter new login, or enter \"EXIT\" to quit");
		input = in.readLine();
		if(!input.equals("EXIT")){
		   if(esql.executeQuery(String.format("SELECT * From Users U WHERE U.login = '%s'", input)) > 0){
			System.out.println("Another user with the same login exists!");
		   } else if(input.length() > 50 || input.length() < 1){
			System.out.println("Login should be between 1-50 characters");
		   } else{
		   login = input;
		   }
		}
		break;
		case 2:
		System.out.println("Enter new phone number, or enter \"EXIT\" to quit");
		input = in.readLine();
		if(!input.equals("EXIT")){
		  if(esql.executeQuery(String.format("SELECT * From Users U WHERE U.phoneNum = '%s'", input)) > 0){
                        System.out.println("Another user with the same phone number exists!");
                   } else if(input.length() > 16){
                        System.out.println("Phone number should be less than 16 characters");
                   } else{
		   phoneNum = input;
		   }
		}
		break;
                case 3:
                System.out.println("Enter new password, or enter \"EXIT\" to quit");
                input = in.readLine();
                if(!input.equals("EXIT")){
		   if(input.length() > 50 || input.length() < 1){
                        System.out.println("Password should be between 1-50 characters");
		   } else {
                        password = input;
		   }
                }
                break;
		case 4:
		   changeFavItems(esql, sessionLogin);
		break;
		case 5:
                System.out.println("Enter new type, or enter \"EXIT\" to quit");
                input = in.readLine();
                if(!input.equals("EXIT")){
		   if(input.equals("Manager") || input.equals("Employee") || input.equals("Customer")){
                   type = input;
		   } else {
		   System.out.println("Type should be Manager, Employee, or Customer");
		   }
                }
                break;
		case 9:
		inMenu = false; break;
		default : System.out.println("Unrecognized choice!"); break;

	   }
	}
	if(!login.equals("EXIT")){
		esql.executeUpdate(String.format("UPDATE Users SET login = '%s' WHERE login = '%s'", login, sessionLogin));
	}
        if(!phoneNum.equals("EXIT")){
                esql.executeUpdate(String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", phoneNum, sessionLogin));
        }
        if(!password.equals("EXIT")){
                esql.executeUpdate(String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", password, sessionLogin));
        }
	if(!type.equals("EXIT")){
                esql.executeUpdate(String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", type, sessionLogin));
        }

	}catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }


  public static void changeFavItems(Cafe esql, String sessionLogin){
     try{
     boolean menu = true;
     boolean readingInputs = true;
     String input = "";
     String test = "";
     String favItems = null;
	while(menu){
	   System.out.println("Favorite Items Menu");
	   System.out.println("--------------");
	   System.out.println("1. Set list of favorite items (this will remove your current list)");
	   System.out.println("2. Remove list of favorite items");
	   System.out.println("9. Back to main menu");
	   switch(readChoice()){
	      case 1:
		test = "";
	      	while(readingInputs){
		   System.out.println("Enter an item to put on list, or type EXIT to stop adding");
		   input = in.readLine();
		   if(input.equals("EXIT")){
			readingInputs = false;
			break;
		   } else {
			if(test.length() == 0){
			test += input;
			} else {
			test += ", " + input;
			}
			if(esql.executeQuery(String.format("SELECT * FROM Menu M WHERE M.itemname = '%s'", input)) == 0){
			   System.out.println("That item is not on our item menu!");
			} else if(test.length() > 400){
			   System.out.println("List of item names too long!");
			} else {
			   favItems = test;
			}
		   }
		}
		esql.executeUpdate(String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", favItems, sessionLogin));
		break;
	     case 2: esql.executeUpdate(String.format("UPDATE Users SET favItems = NULL WHERE login = '%s'", sessionLogin)); break;
	     case 9: return;
	   }
	}
	     }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

public static boolean existsInOrder(List<String> orderList, String item) {
    for (int i = 0; i < orderList.size(); i++) {
        if (orderList.get(i).equals(item)) {
            return true;
        }
    }
    return false;
}

public static void PlaceOrder(Cafe esql, String sessionLogin){
    List<List<String>> menu = new ArrayList<List<String>> ();
    boolean inItemMenu = true;

        try{
            menu = esql.executeQueryAndReturnResult("SELECT * FROM Menu"); // Get menu options to display to customer
         double total = 0;
         List<String> orderList = new ArrayList<String> (); 

         while(inItemMenu){
            System.out.println("PLACE ORDER MENU");
            System.out.println("-----------");
            System.out.println("Current total: $" + new java.text.DecimalFormat("#.##").format(total)); // Display current total and item list of the order
            System.out.println("Order: " + orderList);
            for (int i = 1; i <= menu.size(); i++) { // Output list of items on the menu
               String itemName = menu.get(i - 1).get(0);
               System.out.println(i + ". " + itemName);
            }
	  
            System.out.println("---------"); 
            int lastChoice = menu.size() + 1; 
            System.out.println(lastChoice + ". Back to main menu");
            if (orderList.size() > 0) {
                System.out.println((lastChoice + 1) + ". Confirm order");
            }            
            System.out.println("//Select an item from above to add// ");
            int a = readChoice();
            
	    if (a == lastChoice) { // Exit Place Order view
                inItemMenu = false;
            }
            else if (a < lastChoice) { // Get information of chosen item
                String item = menu.get(a - 1).get(0).trim();
                float price = Float.parseFloat(menu.get(a - 1).get(2));

		System.out.println("----------------------------------");
                System.out.println("ITEM: " + item);
                System.out.println("PRICE: " + price);
                System.out.println("Would you like to add " + item + " to your order?");
                System.out.println("1. Add item");
                System.out.println("2. Go back");

                a = readChoice();
                boolean confirmation = true;                
                
                while (confirmation) {
                    switch(a) { // Add item to temporary list if confirmed
                        case 1: if (!existsInOrder(orderList, item)) {
				    System.out.println(item + " added!");
                                    total += price;
                                    orderList.add(item);
                                }
                                else {
				    System.out.println(item + " has already been added to your order");
                                }
                                System.out.println("----------------------------------");
                                confirmation = false;
                                break;
                        case 2: confirmation = false; break;
                        default: System.out.println("Unrecognized choice!"); a = readChoice(); break; 
                    } 
                }
            }
            else if (a == (lastChoice + 1) && (orderList.size() > 0)) { // Confirmation of order (if there are items in order)
                boolean confirmation = true;

		while (confirmation) {
                    System.out.println("----------------------------------");
                    System.out.println("Current total: " + new java.text.DecimalFormat("#.##").format(total));
                    System.out.println("Order: " + orderList);
                    System.out.println("Confirm order?");
                    System.out.println("1. Confirm");
                    System.out.println("2. Go back");
               
                    a = readChoice();
                    
                    switch(a) { // Final confirmation. Add order to Orders table and each item in the temporary list to ItemStatus
                        case 1: confirmation = false; inItemMenu = false;
                                
                                esql.executeUpdate(String.format("INSERT INTO Orders (login, paid, timeStampRecieved, total) VALUES ('%s', '%s', '%s', '%s')", sessionLogin.trim(), false, new java.sql.Timestamp(new java.util.Date().getTime()), new java.text.DecimalFormat("#.##").format(total)));
                                                                
                                List<List<String>> order = esql.executeQueryAndReturnResult(String.format("SELECT * FROM Orders O WHERE O.login = '%s' AND O.orderID = (SELECT MAX(O2.orderID) FROM ORDERS O2)", sessionLogin));
                                for (int i = 0; i < orderList.size(); i++) {
                                    esql.executeUpdate(String.format("INSERT INTO ItemStatus(orderId, itemName, lastUpdated, status) VALUES ('%s', '%s', '%s', '%s')", order.get(0).get(0), orderList.get(i).trim(), new java.sql.Timestamp(new java.util.Date().getTime()), "Hasn''t Started"));
                                }

                                System.out.println("Order confirmed!"); break;
                        case 2: confirmation = false; break;
                        default: System.out.println("Unrecognized choice!");  break;
                    }
                }
            }
            else if (a == (lastChoice + 1) && (orderList.size() == 0)) { // User chooses to confirm order but there are no items in the order
            	System.out.println("Your order list is empty!");
            }
            else {
                System.out.println("Unrecognized choice!");
            }
         }

     }catch(Exception e){
         System.err.println (e.getMessage ());
      }
  }

  public static void UpdateOrder(Cafe esql, String sessionLogin){

      try {
          List<List<String>> userOrdersList = esql.executeQueryAndReturnResult(String.format("SELECT * FROM Orders O WHERE O.login = '%s'", sessionLogin));       
          List<List<String>> usersItemStatusList = esql.executeQueryAndReturnResult(String.format("SELECT * FROM ItemStatus I WHERE I.orderID = ANY (SELECT O.orderID FROM ORDERS O WHERE O.login = '%s')", sessionLogin));
          
          String query = String.format("SELECT * FROM Users U WHERE (U.login = '%s' AND (U.type = 'Manager' OR U.type = 'Employee'))", sessionLogin); //Query to check if the logged in user is a manager
          int managerCheck =  esql.executeQuery(query);
          boolean updateMenu = true;

          while(updateMenu){
              System.out.println("");
              System.out.println("UPDATE ORDERS");
              System.out.println("-----------");
              if(managerCheck > 0){
	          System.out.println("1. Update your orders");
	          System.out.println("2. Order History");
                  System.out.println("3. Update Customer's order");
                  System.out.println("4. Customer Order history");
                  System.out.println("---------");
                  System.out.println("9. Back to main menu");                  

	          switch(readChoice()){
	              case 1: updateUserOrder(esql, sessionLogin); break;
	              case 2: displayOrderHistory(esql, sessionLogin); break;
                      case 3: updateCustomerOrder(esql, sessionLogin); break;
                      case 4: displayCustomerOrderHistory(esql, sessionLogin); break;
	              case 9: updateMenu = false; break;
                      default: System.out.println("Unrecognized choice!"); break;
	          }
              } else {
                  System.out.println("1. Update your orders");
                  System.out.println("2. Order History");
                  System.out.println("---------");
                  System.out.println("9. Back to main menu");

                  switch(readChoice()){
                      case 1: updateUserOrder(esql, sessionLogin); break;
                      case 2: displayOrderHistory(esql, sessionLogin); break;
                      case 9: updateMenu = false; break;
                      default: System.out.println("Unrecognized choice!"); break;
                  }
              } 
          }      
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
  }

// User updates an UNPAID Order based on orderid
  public static void updateUserOrder(Cafe esql, String sessionLogin) {
      try {
          boolean updateMenu = true;
          int orderID = 0;

          while (updateMenu) {
              do {
                  System.out.print("Enter the order ID: ");
                  try { // read the integer, parse it and break.
                      orderID = Integer.parseInt(in.readLine());
                      break;
                  }catch (Exception e) {
                      System.out.println("Your input is invalid!");
                      continue;
                  }//end try
              }while (true);
              
              String queryInOrders = String.format("SELECT * FROM Orders O WHERE (O.login = '%s' AND O.orderid = '%s')", sessionLogin, orderID);
              List<List<String>> chosenOrder = esql.executeQueryAndReturnResult(queryInOrders);

              if (chosenOrder.size() == 0) {
                  System.out.println(String.format("Order with ID '%s' does not exist", orderID));
                  updateMenu = false;
              }
              else if(chosenOrder.get(0).get(2).equals("t")) {
                  System.out.println(String.format("Order with ID '%s' is already paid for", orderID));
                  System.out.println("Cannot update order");
                  updateMenu = false;
              }
              else {
                  boolean itemMenu = true;
                  
                  while (itemMenu) {
                      queryInOrders = String.format("SELECT * FROM Orders O WHERE (O.login = '%s' AND O.orderid = '%s')", sessionLogin, orderID);
                      chosenOrder = esql.executeQueryAndReturnResult(queryInOrders);
                      float orderTotal = Float.parseFloat(chosenOrder.get(0).get(4));
                      
                      System.out.println("");
                      System.out.println("------------------");
                      System.out.println("ORDER " + orderID);
                      System.out.println("- - - -");
                      System.out.println("Total: " + orderTotal);
                      System.out.println("- - - -");

                      String queryItemList = String.format("SELECT * FROM ItemStatus I WHERE I.orderID = '%s'", orderID);
                      List<List<String>> itemList = esql.executeQueryAndReturnResult(queryItemList);
                      System.out.println("Items in order " + orderID + ": ");
                      for( int i = 1; i < (itemList.size() + 1); i++) {
   		          String itemName = itemList.get(i - 1).get(1).trim();
                          String itemLastUpdated = itemList.get(i - 1).get(2);
                          String itemStatus = itemList.get(i - 1).get(3).trim();
                          String itemComments = itemList.get(i - 1).get(4);

                          System.out.println(i + ". " + itemName);
                          System.out.println("    Last updated: " + itemLastUpdated);
                          System.out.println("    Status: " + itemStatus);
                          System.out.println("    Comments: " + itemComments);       
                          System.out.println("* * * * * * * * * * *");         
                      }
                     
                      System.out.println("1. Remove an item");
                      System.out.println("2. Add an item");
                      System.out.println("3. Add comment to an item");
                      System.out.println("4. Cancel order");
                      System.out.println("-----");
                      System.out.println("9. Go back");

                     switch (readChoice()) {
                         case 1: if(removeAnItemOrder(esql, sessionLogin, orderID, itemList)) {
                                     itemMenu = false;
                                     updateMenu = false;
                                 }
			         break;
                         case 2: addAnItemOrder(esql, sessionLogin, orderID, itemList); break;
                         case 3: addCommentOrder(esql, sessionLogin, orderID, itemList); break;
                         case 4: if(cancelOrder(esql, sessionLogin, orderID)) {
				     itemMenu = false;
                                     updateMenu = false;
                                 } 
                                 break;
                         case 9: itemMenu = false; updateMenu = false; break;
                         default: System.out.println("Unrecognized choice!");  break;
                     }
                 }
              }
          }
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
  }

// Removes an item from a customer's order
public static boolean removeAnItemOrder (Cafe esql, String sessionLogin, int orderID, List<List<String>> itemList) {
    boolean removeMenu = true;

    try {
        while (removeMenu) {
            System.out.println("");
            System.out.println("REMOVE ITEM FROM ORDER");
            System.out.println("----------------------");

            for (int i = 1; i <= itemList.size(); i++) { // Output list of items on the order
               String itemName = itemList.get(i - 1).get(1);
               System.out.println(i + ". " + itemName);
            }
            System.out.println("------");
            System.out.println((itemList.size() + 2) +". Go back");
	    System.out.println("//Select an item from above to remove// ");
           
            int a = readChoice();
            
            if (a == (itemList.size() + 2)) {
                removeMenu = false;
            }
	    else if ((a > 0) && (a < (itemList.size() + 1))) { // Confirming removal of an item
                boolean confirmRemove = true;

                while (confirmRemove) {
		    String item = itemList.get(a - 1).get(1);
                    System.out.println("");
                    System.out.println("Confirm removal of " + item.trim() + "?");
                    System.out.println("1. Yes, remove this item");
                    System.out.println("2. No, don't remove this item");
                    int b = readChoice();
 
                    switch(b) {
                        case 1: String query = String.format("SELECT M.price FROM Menu M WHERE M.itemName = '%s'", item);
                                List<List<String>> itemInfo = esql.executeQueryAndReturnResult(query);
                                double price = Float.parseFloat(itemInfo.get(0).get(0));
                                
                                query = String.format("DELETE FROM ItemStatus I WHERE (I.orderID = '%s' AND I.itemName = '%s')", orderID, item);
                                esql.executeUpdate(query);
                                itemList.remove(a - 1);

                                query = String.format("UPDATE Orders SET total = total - '%s' WHERE orderID = '%s'", price, orderID);
                                esql.executeUpdate(query);
                                System.out.println(item.trim() + " has been removed from order " + orderID);
                                System.out.println("----------------------------------------------");

                                if (itemList.size() == 0) {
                                    query = String.format("DELETE FROM Orders WHERE orderID = '%s'", orderID);
                                    esql.executeUpdate(query);
                                    System.out.println("Cancelled order " + orderID + " due to all items being removed");
                                    removeMenu = false;
                                    return true;
                                }
                                confirmRemove = false;
                                break;
                        case 2: confirmRemove = false; break;
                        default: System.out.println("Unrecognized choice!"); break;
                    }
                }
            }
            else {
                System.out.println("Unrecognized choice!");
            }
        }
    }catch(Exception e) {
        System.err.println (e.getMessage ());
    }
    return false;
}

// Adds an item to a customer's order
public static void addAnItemOrder (Cafe esql, String sessionLogin, int orderID, List<List<String>> itemList) {
    boolean addMenu = true;
    List<String> orderList = new ArrayList<String> ();

    for (int i = 0; i < itemList.size(); i++) {
        orderList.add(itemList.get(i).get(1).trim());
    }    

    try {
        while (addMenu) {
           System.out.println("");
           System.out.println("ADD ITEM TO ORDER");
           System.out.println("----------------------");
           List<List<String>> menu = esql.executeQueryAndReturnResult("SELECT * FROM Menu"); 
           float total = Float.parseFloat(esql.executeQueryAndReturnResult(String.format("SELECT O.total FROM Orders O WHERE O.orderID = '%s'", orderID)).get(0).get(0));             
          
           for (int i = 1; i <= menu.size(); i++) { // Output list of items on the menu
               String itemName = menu.get(i - 1).get(0);
               System.out.println(i + ". " + itemName);
            }

            System.out.println("---------"); 
            int lastChoice = menu.size() + 1; 
            System.out.println(lastChoice + ". Go back");            
 
            int a = readChoice();
            
	    if (a == lastChoice) { // Exit Place Order view
                addMenu = false;
            } else if (a < lastChoice) { // Get information of chosen item
                String item = menu.get(a - 1).get(0).trim();
                float price = Float.parseFloat(menu.get(a - 1).get(2));

		System.out.println("----------------------------------");
                System.out.println("ITEM: " + item.trim());
                System.out.println("PRICE: " + price);
                System.out.println("Would you like to add " + item.trim() + "to your order?");
                System.out.println("1. Add item");
                System.out.println("2. Go back");

                System.out.println("//Select an item from above to add// ");
                a = readChoice();
                boolean confirmation = true;                
                
                while (confirmation) {
                    switch(a) { // Confirm added item and update order total
                        case 1: if (!existsInOrder(orderList, item)) {
                                    System.out.println(item.trim() + " added!"); 
                                    total += price;
                                    orderList.add(item);

                                    esql.executeUpdate(String.format("INSERT INTO ItemStatus(orderId, itemName, lastUpdated, status) VALUES ('%s', '%s', '%s', '%s')", orderID, item.trim(), new java.sql.Timestamp(new java.util.Date().getTime()), "Hasn''t Started"));
                                    esql.executeUpdate(String.format("UPDATE Orders SET total = '%s' WHERE orderID = '%s'", total, orderID));
                                    addMenu = false;
                                } else {
                                    System.out.println(item.trim() + " has already been added to your order");
                                }
                                confirmation = false;
                                break;
                        case 2: confirmation = false; break;
                        default: System.out.println("Unrecognized choice!"); a = readChoice(); break; 
                    } 
                }
            } else {
                System.out.println("Unrecognized choice!");
            }
        }
    }catch(Exception e) {
        System.err.println (e.getMessage ());
    }
}

// Adds a comment to a customer's order
public static void addCommentOrder (Cafe esql, String sessionLogin, int orderID, List<List<String>> itemList) {
    boolean addMenu = true;

    try {
        while (addMenu) {
            System.out.println("");
            System.out.println("ADD COMMENT");
            System.out.println("-------------");
        
            for (int i = 1; i <= itemList.size(); i++) { // Output list in order
               String itemName = itemList.get(i - 1).get(1);
               System.out.println(i + ". " + itemName);
            }
            
            System.out.println("-------");
            System.out.println((itemList.size() + 2) +". Go back");
            System.out.println("//Select an item to add a comment to// ");

            int a = readChoice();

            if (a == itemList.size() + 2) {
                addMenu = false;
            }
            else if ((a > 0) && (a < (itemList.size() + 1))) {
                String comment = "";                

                String item = itemList.get(a - 1).get(1).trim();
                System.out.println("");
                boolean correctLength = true;

                while (correctLength){
                    System.out.println("Write your comment for " + item + ": ");
                    comment = in.readLine();

                    if (comment.length() >= 130) {
                        System.out.println("Comment is too long!");    
                    }
                    else {
                        correctLength = false;
                    }
                }

                String query = String.format("UPDATE ItemStatus SET comments = '%s' WHERE orderID = '%s' AND itemName = '%s'", comment, orderID, item);
                esql.executeUpdate(query);          
                addMenu = false;  
      
                System.out.println("Comment to " + item + " has been added!");
            }
            else {
                System.out.println("Unrecognized choice!");
            }
        } 
    }catch(Exception e) {
        System.err.println (e.getMessage ());
    }
}

// Cancels and entire order
public static boolean cancelOrder(Cafe esql, String sessionLogin, int orderID) {
    boolean cancelMenu = true;
    try {
    while (cancelMenu) {
        System.out.println("");
        System.out.println("Confirm cancellation of order " + orderID + "?");
        System.out.println("1. Yes, cancel");
        System.out.println("2. No, don't cancel");
	
	switch(readChoice()) {
            case 1: String deleteQuery = String.format("DELETE FROM ItemStatus I WHERE I.orderID = '%s'", orderID);
                    esql.executeUpdate(deleteQuery);
                    deleteQuery = String.format("DELETE FROM Orders O WHERE O.orderID = '%s'", orderID);
		    esql.executeUpdate(deleteQuery);
                    //esql.executeQueryAndPrintResult(String.format("SELECT* FROM ItemStatus I WHERE I.orderID = '%s'", orderID));
                    System.out.println("Your order " + orderID + " has been canceled");
                    cancelMenu = false;
                    return true;
            case 2: cancelMenu = false; break;
            default: System.out.println("Unrecognized choice!");  break;            
        }
    }
    }catch(Exception e) {
          System.err.println (e.getMessage ());
    }
    return false;
}



// Display recent 5 orders from current User
  public static void displayOrderHistory(Cafe esql, String sessionLogin) {
      System.out.println("");
      System.out.println("YOUR ORDERS:");
      System.out.println("-------------");
      String query = String.format("SELECT * FROM Orders O WHERE O.login = '%s' ORDER BY O.timeStampRecieved DESC LIMIT 5", sessionLogin);      
      try{
          esql.executeQueryAndPrintResult(query);
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
  }

  public static void updateCustomerOrder(Cafe esql, String sessionLogin) {
      try {
          boolean updateMenu = true;
          int orderID = 0;

          while (updateMenu) {
              do {
                  System.out.print("Enter the order ID: ");
                  try { // read the integer, parse it and break.
                      orderID = Integer.parseInt(in.readLine());
                      break;
                  }catch (Exception e) {
                      System.out.println("Your input is invalid!");
                      continue;
                  }//end try
              }while (true);
              
              String queryInOrders = String.format("SELECT * FROM Orders O WHERE O.orderid = '%s'", orderID);
              List<List<String>> chosenOrder = esql.executeQueryAndReturnResult(queryInOrders);

              if (chosenOrder.size() == 0) {
                  System.out.println(String.format("Order with ID '%s' does not exist", orderID));
                  updateMenu = false;
              }
              else {
                  boolean itemMenu = true;
                  
                  while (itemMenu) {
                      chosenOrder = esql.executeQueryAndReturnResult(queryInOrders);
                      String orderIsPaid = chosenOrder.get(0).get(2);
                      float orderTotal = Float.parseFloat(chosenOrder.get(0).get(4));
                      
                      System.out.println("");
                      System.out.println("------------------");
                      System.out.println("ORDER " + orderID);
                      System.out.println("- - - -");
                      System.out.println("Paid: " + orderIsPaid);
                      System.out.println("Total: " + orderTotal);
                      System.out.println("- - - -");

                      String queryItemList = String.format("SELECT * FROM ItemStatus I WHERE I.orderID = '%s'", orderID);
                      List<List<String>> itemList = esql.executeQueryAndReturnResult(queryItemList);
                      System.out.println("Items in order " + orderID + ": ");
                      for( int i = 1; i < (itemList.size() + 1); i++) {
   		          String itemName = itemList.get(i - 1).get(1).trim();
                          String itemLastUpdated = itemList.get(i - 1).get(2);
                          String itemStatus = itemList.get(i - 1).get(3).trim();
                          String itemComments = itemList.get(i - 1).get(4);

                          System.out.println(i + ". " + itemName);
                          System.out.println("    Last updated: " + itemLastUpdated);
                          System.out.println("    Status: " + itemStatus);
                          System.out.println("    Comments: " + itemComments);       
                          System.out.println("* * * * * * * * * * *");         
                      }
                     
                      System.out.println("1. Change order status (paid/unpaid)");
                      System.out.println("2. Change item status");
                      System.out.println("-----");
                      System.out.println("9. Go back");

                     switch (readChoice()) {
                         case 1: if (orderIsPaid.equals("f")) {
                                     boolean confirmChange = true;
                                     
                                     while(confirmChange) {
                                         System.out.println("");
                                         System.out.println("Confirm changing order from unpaid to paid?");
                                         System.out.println("1. Yes, order is paid");
                                         System.out.println("2. No, order is still unpaid");
                                         switch(readChoice()) {
                                             case 1: confirmChange = false; 
                                                     System.out.println("Order has been set to Paid!");
                                                     esql.executeUpdate(String.format("UPDATE Orders SET paid = true WHERE orderID = '%s'", orderID));
                                                     break;
                                             case 2: confirmChange = false; break;
                                             default: System.out.println("Unrecognized choice!");  break;
                                         }
                                     }
                                 }
                                 else {
                                     System.out.println("Order is already paid for!");
                                 }
                                 break;
                         case 2: changeItemStatus(esql, sessionLogin, orderID, itemList); break;
                         case 9: itemMenu = false; updateMenu = false; break;
                         default: System.out.println("Unrecognized choice!");  break;
                     }
                 }
              }
          }
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
  }  
 
  // Change status of item
  public static void changeItemStatus (Cafe esql, String sessionLogin, int orderID, List<List<String>> itemList) {
      boolean changeMenu = true;
  
      try {
          while (changeMenu) {
              System.out.println("");
              System.out.println("CHANGE ITEM STATUS");
              System.out.println("---------------------");

              for (int i = 1; i <= itemList.size(); i++) { // Output list of items on the order
                  String itemName = itemList.get(i - 1).get(1);
                  System.out.println(i + ". " + itemName);
              }
              System.out.println("------");
              System.out.println((itemList.size() + 2) +". Go back");
	      System.out.println("//Select an item to change it's status// ");
           
              int a = readChoice();
            
              if (a == (itemList.size() + 2)) {
                  changeMenu = false;
              }
	      else if ((a > 0) && (a < (itemList.size() + 1))) { // Confirming removal of an item
                  boolean confirmRemove = true;
                  while (confirmRemove) {
		      String item = itemList.get(a - 1).get(1).trim();
                      String itemStatus = itemList.get(a - 1).get(3).trim();

                      System.out.println("");
                      System.out.println("Change status of " + item.trim() + " to which? (current status is \"" + itemStatus.trim() +"\")");
                      System.out.println("1. Hasn't started");
                      System.out.println("2. Started");
                      System.out.println("3. Finished");
                      int b = readChoice();

                      switch(b) {
                          case 1: itemStatus = "Hasn't started"; 
                                  esql.executeUpdate(String.format("UPDATE ItemStatus SET status = '%s', lastUpdated = '%s' WHERE orderID = '%s' AND itemName = '%s'", "Hasn''t Started", new java.sql.Timestamp(new java.util.Date().getTime()), orderID, item));
                                  confirmRemove = false; break;
                          case 2: itemStatus = "Started"; 
                                  esql.executeUpdate(String.format("UPDATE ItemStatus SET status = '%s' , lastUpdated = '%s' WHERE orderID = '%s' AND itemName = '%s'", itemStatus, new java.sql.Timestamp(new java.util.Date().getTime()), orderID, item));
                                  confirmRemove = false; break;
                          case 3: itemStatus = "Finished"; 
                                  esql.executeUpdate(String.format("UPDATE ItemStatus SET status = '%s', lastUpdated = '%s' WHERE orderID = '%s' AND itemName = '%s'", itemStatus, new java.sql.Timestamp(new java.util.Date().getTime()), orderID, item));
                                  confirmRemove = false; break;
                          default: System.out.println("Unrecognized choice!"); break;
                      }
                      System.out.println(item + " status set to " + itemStatus);
                  }
            }
            else {
                System.out.println("Unrecognized choice!");
            }
        }
      }catch(Exception e) {
        System.err.println (e.getMessage ());
      }
  }

  public static void displayCustomerOrderHistory (Cafe esql, String sessionLogin) {
      System.out.println("");
      System.out.println("CUSTOMERS' UNPAID ORDERS (WITHIN 24 HOURS)");
      System.out.println("------------------");
      String query = "SELECT * FROM Orders WHERE timeStampRecieved >= NOW() - \'1 DAY\'::INTERVAL AND paid = \'f\'";
      try{
          esql.executeQueryAndPrintResult(query);
      }catch(Exception e) {
          System.err.println (e.getMessage ());
      }
  } 
}//end
