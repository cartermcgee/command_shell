import java.lang.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class CommandShell {

    private static long ptime;

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

	    case "ls":
		if(command.length == 1){
                    list("");
                }else{
                    list(command[1]);
                }
		break;

	    case "ptime":
		System.out.print("Total time in child processes: ");
		getPTime();
		System.out.println(" seconds");
		break;

	    default:
		ArrayList<String> commandList = new ArrayList<>(Arrays.asList(command)); // convert commmand to arraylist to easily manipulate it
		startExternalProcess(commandList);
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
		    String parentDir = m.group().substring(0, m.group().length() - 1); // truncate last slash / from the string
		    System.setProperty("user.dir", parentDir);
		}
	    }
	}else{ // change to child directory 'cd myDir'
	    String newDir = currentDir + "/" + targetDir;
	    File f = new File(newDir);

	    if(f.exists() && f.isDirectory())
		System.setProperty("user.dir", newDir);
	    else
		System.out.println("Error: " + targetDir + " is not a valid directory.");
	}
    }

    /**
    * Lists all the files in the user's cuurent directory, use arg -l for a more detailed output
    */
    public static void list(String arg){
	File dir = new File(System.getProperty("user.dir"));
        File[] fileList = dir.listFiles();
	int fileCount = fileList.length;

	if(arg.equals("")){ // simple output
	    System.out.println("Total: " + fileCount);
	    for(File f : fileList){
		System.out.print(f.getName() + "\t");
	    }
	    System.out.println();
	}
	else if(arg.equals("-l")){ // detailed output
	    System.out.println("Total: " + fileCount);
	    for(File f : fileList){
	        String permissions = "";
	        if(f.isDirectory())
		    permissions += "d";
	        else
		    permissions += "-";

	        if(f.canRead())
		    permissions += "r";
	        else
		    permissions += "-";

	        if(f.canWrite())
		    permissions += "w";
	        else
		    permissions += "-";

	        if(f.canExecute())
		    permissions += "x";
	        else
		    permissions += "-";

	        Date lastModified = new Date(f.lastModified());
	        System.out.println(permissions + "\t" + f.length() + "\t" + lastModified.toString() + "\t" + f.getName());
	    }
	}else{
	    System.out.println("Invalid argument: " + arg);

	}
    }

    /**
    * Prints the time spent performing child processes to the screen
    */
    public static void getPTime(){
	double seconds = ptime / 1000.0;
	System.out.printf("%.4f", seconds);
    }

    /**
    * Increments variable ptime, which represents the time spent performing child processes
    */
    public static void incrementPTime(long increment){
	ptime += increment;
    }

    /**
    * Attempts to start an external process given the command
    */
    public static void startExternalProcess(ArrayList<String> command){
	boolean waitForProcess = true;
	if(command.get(command.size() - 1).equals("&")){ // if the last element in the array is &
	    command.remove(command.size() - 1);
	    waitForProcess = false;
	}

	ProcessBuilder pb = new ProcessBuilder(command);
	pb.directory(new File(System.getProperty("user.dir")));
	pb.inheritIO();

	try{
	    Process p = pb.start();
	    if(waitForProcess){ // waits for child process and counts how much time is spent waiting
		long start = System.currentTimeMillis();
                p.waitFor();
                long end = System.currentTimeMillis();
                incrementPTime(end - start);
	    }

        }catch(IOException ioe){
            System.out.println("Invalid Command: " + command.get(0));
        }catch(InterruptedException ie){
            System.out.println("Error: thread interrupted");
        }
    }
}


