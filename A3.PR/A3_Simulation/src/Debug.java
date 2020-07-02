
public class Debug {
	
	// This class implements useful debugging tools for working with
	// the project. Examples of its usage can be found in the Simulation,
	// Renderer, Rover, and ExampleRover classes.
	
	/* Be sure to use the following form when using these functions:
	        if (Debug.ON) {
	            [INSERT DEBUGGING CODE HERE];
	        }
	   This ensures that no debugging code is compiled when Debug.ON is
	   turned off. (Will be important later for evaluation)
	*/

	// Change this value to false to remove debug messages
	// it's final so that the compiler can remove unreachable code
	public static final boolean ON = false;
	
	private static final String stdIndent = "|   ";
	private static int currIndent = 0;
	
	public static void LogEnter(String caller) {
		Log("Entering {" + caller + "}");
		currIndent += 1;
	}
	
	public static void LogExit(String caller) {
		if (currIndent == 0) {
			System.err.println("Cannot log exit from {" + caller + "}, must log entrance first!");
			return;
		}
		currIndent -= 1;
		Log("Exiting {" + caller + "}");
	}
	
	public static void Log(String... messages) {
		String indent = getIndent();
		for (String s : messages) {
			System.out.println(indent + s);
		}
	}
	
	private static String getIndent() {
		String ret = "";
		for (int i = 0; i < currIndent; i++)
			ret = ret + stdIndent;
		return ret;
	}
}
