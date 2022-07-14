package botservice;

import java.io.File;
import java.io.IOException;;

public class ApontaHoras {

	public static void main(String[] args) throws IOException {

		File file = new FileDiscord().listUserDev();
		
		BotService botService = new BotService();
		botService.listFile(file);
		

	}

}
