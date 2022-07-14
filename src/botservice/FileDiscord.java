package botservice;

import java.io.File;

public class FileDiscord {
	
	public File listUserDev() {
		
		File dir = new File("/opt/apontaHorasRedmine");
		
		File file = new File(dir, "valoresDiscord.txt");
		
		return file;
		
	}

}
