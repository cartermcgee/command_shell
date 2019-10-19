import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public class CommandShell {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
	String rawCommand;
	String[] command;
	while(true){
	    System.out.print("[" + System.getProperty("user.dir") + "]: ");
	    rawCommand = sc.next();
	    command = splitCommand(rawCommand);
	    interperetCommand(command);
	}
    }

    /**
     * Split the user command by spaces, but preserving them when inside double-quotes.
     * Code Adapted from: https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
     */
    public static String[] splitCommand(String command) {
        java.util.List<String> matchList = new java.util.ArrayList<>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(command);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList.toArray(new String[matchList.size()]);
    }

    public static void interperetCommand(String[] command){
        switch(command[0]){
            case "exit":
		System.exit(0);
	    default:
		System.out.println("Invalid Command: " + command[0]);
		return;
        }
    }
}
