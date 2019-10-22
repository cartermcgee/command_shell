import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.lang.*;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

public class CommandShell {

    /**
    * Main prompt loop.
    */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
	String rawCommand;
	String[] command;
	ArrayList<String> commandHistory = new ArrayList<>();
	while(true){
	    System.out.print("[" + System.getProperty("user.dir") + "]: "); // prints current dir in the command prompt
	    rawCommand = sc.nextLine();
	    commandHistory.add(rawCommand);
	    if(rawCommand.contains("\"") || rawCommand.contains("'")){ // if command contains double quote (") or single quote (') it gets parsed differently
		command = splitCommand(rawCommand);
	    }else{
		command = rawCommand.split("\\s+");
	    }

	    interperetCommand(command, commandHistory);
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

    /**
    * interperets command array and calls the functions associated with those commands, validating data if needed
    */
    public static void interperetCommand(String[] command, ArrayList<String> commandHistory){
        switch(command[0]){
            case "exit":
	    case "quit":
	    case "q":
		System.exit(0);

	    case "^":
		if(command.length > 1){
		    try{
			int depth = Integer.parseInt(command[1]);
			executeFromHistory(depth, commandHistory);
		    }catch(NumberFormatException nfe){
			System.out.println("Invalid Argument: " + command[1] + "is not an integer.");
		    }

		}else{
		    System.out.println("Invalid Argument: Command '^' requires an integer argument.\nex: ^ 10");
		}

		break;
	    case "history":
		printCommandHistory(commandHistory);
		break;

	    case "pwd":
		printWorkingDir();
		break;

	    case "cd":
		if(command.length == 1){
		    changeDir("");
		}else{
   		    changeDir(command[1]);
		}
		break;

	    default:
		System.out.println("Invalid Command: " + command[0]);
		return;
        }
    }

    /**
    * Executes a past command given its index in the command history.
    */
    public static void executeFromHistory(int depth, ArrayList<String> commandHistory){
	int commandCount = commandHistory.size();
	if(depth - 1 > commandCount || depth - 1 < 0){ // prevents ArrayIndexOutOfBounds Exception
	    System.out.println("Invalid Argument: " + depth);
	    return;
	}

	String rawCommand = commandHistory.get(depth - 1); // index for input starts at one
	String[] command;
	if(rawCommand.contains("\"") || rawCommand.contains("'")){
                command = splitCommand(rawCommand);
        }else{
                command = rawCommand.split("\\s+");
        }

	interperetCommand(command, commandHistory);
    }

    /**
    * Prints out the complete command history in chronological order.
    */
    public static void printCommandHistory(ArrayList<String> commandHistory){
	System.out.println("Command History:");
	for(int i = 1; i <= commandHistory.size(); i++){ // loops through command history
	    System.out.println(i + ": " + commandHistory.get(i - 1));
	}
	System.out.println();
    }

    /**
    * Prints out the current working directory
    */
    public static void printWorkingDir(){
	System.out.println(System.getProperty("user.dir"));
    }

    /**
    * changes the cuurent directory for the user
    */
    public static void changeDir(String targetDir){
	String home = System.getProperty("user.home");
	String currentDir = System.getProperty("user.dir");
	if(targetDir.equals("")){ // change to home directory 'cd'
	    System.setProperty("user.dir", home);
	}else if(targetDir.equals("..")){ // change to parent directory 'cd ..'
	    Pattern p = Pattern.compile(".*/"); // regex that truncates last dir from a pathname
	    Matcher m = p.matcher(currentDir);
	    while(m.find()){
		if(m.group() != null){
		    System.setProperty("user.dir", m.group());
		}
	    }
	}else{ // change to child directory 'cd myDir'
	    String newDir = currentDir + "/" + targetDir;
	    System.setProperty("user.dir", newDir);
	}

    }
}
