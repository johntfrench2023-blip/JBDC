// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
//javac Bonus.java 
//java -cp ".:ojdbc17.jar" Bonus     
//333445555
// 987654321
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Bonus {
   public Bonus() {
   }

   public static void main(String[] var0) throws FileNotFoundException {
      Scanner var1 = new Scanner(new File("userinfo.txt"));
      String var2 = var1.nextLine();
      String var3 = var1.nextLine();
      String var4 = "jdbc:oracle:thin:@oracle.cs.ua.edu:1521:xe";

      try {
         System.out.println("Program Started");
         Connection var5 = DriverManager.getConnection(var4, var2, var3);
         Scanner var6 = new Scanner(System.in);

         while (true) {

            System.out.print("Enter a Social Security Number: ");
            String var7 = var6.nextLine().trim();

            if (var7.equals("0")) {
               System.out.println("Good Bye!");
               break;
            } else if (!var7.matches("\\d+")) {
               System.out.println("SSN should only contain numbers.\n");
               continue;
            }

            String empSql = "SELECT Fname, Minit, Lname, Dno FROM Employee WHERE TRIM(Ssn) = ?";
            PreparedStatement empPS = var5.prepareStatement(empSql);
            empPS.setString(1, var7);
            ResultSet empRS = empPS.executeQuery();

            if (!empRS.next()) {
               System.out.println("No employee found\n");
               continue;
            }

            String first = empRS.getString("Fname");
            String mid = empRS.getString("Minit");
            String last = empRS.getString("Lname");
            int dno = empRS.getInt("Dno");
            //System.out.println(first);
            System.out.println("\n" + first + " " + mid + ". " + last);

            String mgrSql = "SELECT Dname FROM Department WHERE MgrSSN = ?";
            PreparedStatement mgrPS = var5.prepareStatement(mgrSql);
            mgrPS.setString(1, var7);
            ResultSet mgrRS = mgrPS.executeQuery();

            boolean isManager = mgrRS.next();
            //System.out.println(isManager);

            if (isManager) {

               String deptName = mgrRS.getString("Dname");

               System.out.println("Manager of " + deptName + " Department");

               PreparedStatement deptCountPS = var5.prepareStatement(
                  "SELECT COUNT(*) FROM Employee WHERE Dno = ?");
               deptCountPS.setInt(1, dno);
               ResultSet deptCountRS = deptCountPS.executeQuery();
               deptCountRS.next();
               System.out.println("There are " + deptCountRS.getInt(1) + " employees in the department");

               System.out.println("\nProjects controlled by the department:");
               printProjects(var5,
                  "SELECT Pnumber, Pname FROM Project WHERE Dnum = ?", dno);

               System.out.println("\nProjects worked on by at least one employee:");
               printProjects(var5,
                  "SELECT DISTINCT P.Pnumber, P.Pname FROM Project P "
                + "JOIN Works_on W ON P.Pnumber = W.Pno "
                + "JOIN Employee E ON W.Essn = E.Ssn WHERE E.Dno = ?", dno);

               System.out.println("\nProjects worked on by all the employees:");
               printProjects(var5,
                  "SELECT P.Pnumber, P.Pname FROM Project P "
                + "WHERE NOT EXISTS("
                + "SELECT * FROM Employee E WHERE E.Dno = ? AND NOT EXISTS("
                + "SELECT * FROM Works_on W WHERE W.Essn = E.Ssn AND W.Pno = P.Pnumber))", dno);

            } else {

               PreparedStatement deptPS = var5.prepareStatement(
                  "SELECT Dname FROM Department WHERE Dnumber = ?");
               deptPS.setInt(1, dno);
               ResultSet deptRS = deptPS.executeQuery();
               deptRS.next();
               System.out.println("Employee of " + deptRS.getString("Dname") + " Department");

               PreparedStatement depPS = var5.prepareStatement(
                  "SELECT COUNT(*) FROM Dependent WHERE TRIM(Essn) = ?");
               depPS.setString(1, var7);
               ResultSet depRS = depPS.executeQuery();
               depRS.next();
               System.out.println("There are " + depRS.getInt(1) + " dependents related to the employee");

               System.out.println("\nProjects worked on by the employee:");
               printProjects(var5,
                  "SELECT P.Pnumber, P.Pname FROM Project P "
                + "JOIN Works_on W ON P.Pnumber = W.Pno WHERE W.Essn = ?", var7);
            }

            System.out.println();
         }
         // keep
      } catch (SQLException var13) {
         var13.printStackTrace();
      }
   }

   // Make the print more neat and clean
   private static void printProjects(Connection conn, String sql, Object param) throws SQLException {
    //System.out.println("Test1");
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setObject(1, param);
      ResultSet rs = ps.executeQuery();

      boolean any = false;
      while (rs.next()) {
        //System.out.println("Test2");
         any = true;
         // format index
         System.out.printf("   %3d %-15s%n", rs.getInt(1), rs.getString(2));
      }
      if (!any) System.out.println("   None");
      //System.out.println("Test3");
   }
}
