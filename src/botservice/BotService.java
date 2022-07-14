package botservice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

public class BotService {

	String token = "token_Discord";

	// String canal = "928364447897108483";

	String redmineHost = "http://redmine.com.br";

	GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

	public void listFile(File file) throws IOException {
		Scanner ler = new Scanner(file);

		while (ler.hasNext()) {

			String linha = ler.nextLine();

			String[] valores = linha.split("->");

			List<IssuesDTO> listIssues = projects(valores[0]);

			sendMessage(listIssues, valores[1]);

		}
		ler.close();
	}

	public List<IssuesDTO> projects(String userName) throws IOException {

		//project_id=13 referencia ao projeto raiz de sua escolha
		String response = ModuleUtils.getHttpGet(redmineHost, "/issues.json?project_id=13&limit=100");
		
		JSONObject result = new JSONObject(response);

		JSONArray results = result.getJSONArray("issues");

		for (int i = 0; i < results.length(); i++) {

			JSONObject projects = (JSONObject) results.get(i);
			
			Integer assignedToId = null;
			String assignedToName = null;
			
			try {

				assignedToId = projects.getJSONObject("assigned_to").getInt("id");
				assignedToName = projects.getJSONObject("assigned_to").getString("name");

			} catch (Exception e) {

			}
	
				if (assignedToId != null && assignedToName != null) {

					if (assignedToName.equalsIgnoreCase(userName)) {

						System.out.println("Id do usuário: " + assignedToId);
						System.out.println("Tarefas atribuída a: " + assignedToName);

						return issuesByUser(assignedToId);
					}
				}
		}

		return issuesByUser(null);
	}

	public List<IssuesDTO> issuesByUser(Integer assignedToId)
			throws IOException {

		List<IssuesDTO> listIssues = new ArrayList<>();

		//project_id=13 referencia ao projeto raiz de sua escolha
		String response = ModuleUtils.getHttpGet(redmineHost,
				"/issues.json?assigned_to_id=" + assignedToId + "&project_id=13");

		JSONObject result = new JSONObject(response);

		JSONArray results = result.getJSONArray("issues");

		for (int i = 0; i < results.length(); i++) {

			IssuesDTO issuesDTO = new IssuesDTO();

			JSONObject issuesDetail = (JSONObject) results.get(i);
			
			String identifier = (String) issuesDetail.getJSONObject("project").get("name");
			Integer idProject = (Integer) issuesDetail.getJSONObject("project").get("id");
			
			Date data = new Date();

			SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd");

			String dataFormatada = formatador.format(data);

			String removeEspacoBranco;
			String dataIdentifier;

			if (identifier.length() < 8) {
				dataIdentifier = "";
			} else {				
				removeEspacoBranco = identifier.replace(" ", "");
				dataIdentifier = (String) removeEspacoBranco.subSequence(0, 7);
			}

			String dataAtual = (String) dataFormatada.subSequence(0, 7);

			if (!identifier.equals("") && dataIdentifier.equals(dataAtual)) {
				
				System.out.println("Identificador do projeto: " + identifier);
				System.out.println("Id do projeto: " + idProject);

			issuesDTO.setIdIssue(issuesDetail.getInt("id"));
			issuesDTO.setProjectName(issuesDetail.getJSONObject("project").getString("name"));
			issuesDTO.setTypeIssue(issuesDetail.getJSONObject("tracker").getString("name"));
			issuesDTO.setStatus(issuesDetail.getJSONObject("status").getString("name"));
			issuesDTO.setAssignedTo(issuesDetail.getJSONObject("assigned_to").getString("name"));
			issuesDTO.setSubject(issuesDetail.getString("subject"));
			issuesDTO.setSprint(issuesDetail.getJSONObject("fixed_version").getString("name"));

			listIssues.add(issuesDTO);
			
			}
		}

		return listIssues;
	}

	public List<IssuesDTO> sendMessage(List<IssuesDTO> issues, String canal) throws IOException {

		for (int i = 0; i < issues.size(); i++) {

			EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE)
					.title("#" + issues.get(i).getIdIssue().toString())
					.author("Nome_Autor", null, "http://endereco_da_imagem")
					.description("Job de lembrete de apontamento de horas")
					.thumbnail(
							"https://endereco_da_imagem")
					.url("http://redmine.com.br/issues/" + issues.get(i).getIdIssue().toString())
					.addField("#" + issues.get(i).getIdIssue().toString(), "nº da tarefa", true)
					.addField(":calendar: " + issues.get(i).getProjectName(), "Nome do Projeto", true)
					.addField(issues.get(i).getStatus(), "Status", true)
					.addField(issues.get(i).getSubject(), "Descricao", true)
					.addField(issues.get(i).getTypeIssue(), "Tipo", true)
					.addField(issues.get(i).getAssignedTo(), "Atribuido", true)
					.addField(issues.get(i).getSprint(), "Versao", true)
					// .addField(":clock4: *Data e hora*", "*" + dateFormat.format(new Date()) +
					// "*", true)
					.addField("\u200B", "\u200B", false).timestamp(Instant.now())
					.footer("Redmine - 2022",
							"https://endereco_da_imagem")
					.build();

			client.getChannelById(Snowflake.of(canal)).ofType(GuildMessageChannel.class)
					.flatMap(channel -> channel.createMessage(MessageCreateSpec.builder().addEmbed(embed).build()))
					.subscribe();
			
			client.getChannelById(Snowflake.of(canal)).ofType(GuildMessageChannel.class)
					.flatMap(channel -> channel.getLastMessage().flatMap(message -> message.delete()).timeout(Duration.ofSeconds(10)));
					
			
			System.out.println("Tarefa #" + issues.get(i).getIdIssue().toString());

//			client.getEventDispatcher().on(MessageCreateEvent.class)
//	        .map(MessageCreateEvent::getMessage)
//	        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
//	        .filter(message -> message.getContent().equalsIgnoreCase("!aponta"))
//	        .flatMap(Message::getChannel)
//	        .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder().addEmbed(embed).build()))
//	        .subscribe();

		}

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return issues;

	}

}
