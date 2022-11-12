import java.util.*;
import java.net.*;
import java.io.*;

// Soquete é o nome dado, em um determinado modelo de programação, às 
// extremidades saida links de comunicação entre os processos. Cada soquete
// possui dois fluxos, um de entrada e outro de saída. Um processo pode 
// enviar dados para outro processo na rede apenas escrevendo os dados
// no fluxo de saída de um soquete e pode receber dados da rede apenas lendo
// o fluxo de entrada do soquete.
// Na linguagem java, as conexões do soquete TCP/IP são implementadas como
// classes do pacote "java.net".

// As classes abaixo implementam um cliente TCP multithread que envia
// mensagems "PING" para o servidor que responde "PONG" Quando o cliente
// deseja terminar a sessao ele envia a mensagem "TERMINAR".

class SessaoCliente implements Runnable{

	private String idCliente;
	private String serverIP = "localhost";
	private int serverPort = 4549;
	
	SessaoCliente( String s ){
		idCliente = s;
	}

	SessaoCliente( String s, String serverIP, int serverPort ){
		idCliente = s;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}
	
	public void run(){

		Socket conexao;

		DataOutputStream saida;
		DataInputStream entrada;
	
		try {
			// abre uma conexao com o servidor
			conexao = new Socket(serverIP,serverPort);

			// cada conexão possui dois fluxos, um de entrada e outro de saída.

			// obtém o fluxo de saida da conexao aberta
			saida = new DataOutputStream( conexao.getOutputStream() );
			// obtém o fluxo de entrada da conexao aberta
			entrada = new DataInputStream( conexao.getInputStream() );

			// IMPORTANTE: implementa abaixo o nosso protocolo de comunicação da aplicação

			String mensagem, resposta = ""; 
			Scanner teclado = new Scanner(System.in); 


			do{
				System.out.print("Digite sua mensagem: "); 
				mensagem = teclado.nextLine();
				saida.writeUTF(mensagem); // envia pro servidor

				resposta = entrada.readUTF(); // le a resposta do servidor
				System.out.print("Servidor disse: " + resposta + "\n"); 
				
			}while( !mensagem.equals("fim") ); 

			// fecha os fluxos (entrada e saída)
			saida.close();
			entrada.close();
			// fecha a conexao com o servidor
			conexao.close(); // solicitação de FIN
		}
		// CAPTURA algum problema caso ocorra (ex.: alguma trap - interrupção de software)
		catch( Exception e) {	
			System.err.println( "ERRO: " + e.toString() );
		}

	}//run()

}//class SessaoCliente

public class ClienteTCP{
	public static void main( String args[]  ) {
		Runnable cliente;
		Thread t;

		String serverIP = args[0];
		//String serverIP = "localhost";

		int serverPort = Integer.parseInt(args[1]);
		//int serverPort = 8585;

		int qtdeClientes = 3;	// altere para o valor que desejar

		for( int i=0; i < qtdeClientes; i++ ){
			
			cliente = new SessaoCliente("cliente["+ (i+1) +"]", serverIP, serverPort);
			
			t = new Thread( cliente );
			t.start();
		}
	}
}

