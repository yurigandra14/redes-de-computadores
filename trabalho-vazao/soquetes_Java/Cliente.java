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

			// FAZ O PEDIDO
			System.out.println("\t" + idCliente + " Solicitando Teste de Vazão");
			saida.writeInt(1);
			saida.flush();
			
			// RECEBE O SERVICO
			System.out.println("\t" + idCliente + " Resultado: ");
			recebePacotes(entrada);

			// PING
			System.out.println("\t" + idCliente + " Ping");
			saida.writeInt(2);
			saida.flush();

			// RECEBE PING
			System.out.println("\t" + idCliente + " Latência: ");
			recebePing(entrada);

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

	public void recebePacotes(DataInputStream entrada){
		
		long totalBytes = 0;
		
		try{

		// loop: le a entrada do pipe e escreve no arquivo

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

			byte[] buffer = new byte[10*4*1024]; //40 KB	
			int bytesLidos;

			do{
				bytesLidos = entrada.read(buffer);
				endTime = System.currentTimeMillis();
				totalBytes = totalBytes + bytesLidos;
			}while(bytesLidos>0);

			float vazao = (float)totalBytes/(endTime-startTime); // bytes/ms
			vazao = vazao*8; // bits/ms
			vazao = vazao/1000.0F; //bits/seg

			if(vazao > 1000000000){
				vazao = vazao / 1000000000; // Gbit/seg
				System.out.println("\t" + vazao + "Gb/s");
			}
			else if(vazao > 1000000){
				vazao = vazao / 1000000; // Mbit/seg
				System.out.println("\t" + vazao + "Mb/s");
			}
			else if(vazao > 1000){
				vazao = vazao / 1000; // Kbit/seg
				System.out.println("\t" + vazao + "Kb/s");
			}

		}//try
		catch(EOFException erroLeitura){
			System.err.println("Final de arquivo: " + erroLeitura.toString());
		}
		catch(FileNotFoundException fnfe){
			System.err.println("Arquivo nao encontrado: " + fnfe.toString());
		}
		catch(IOException erroEscrita){
			System.err.println(erroEscrita.toString());
		}
		
	}// recebeArquivo

	public void recebePing(DataInputStream entrada) {
		try{

			// loop: le a entrada do pipe e escreve no arquivo

			long startTime = System.currentTimeMillis();

			do{
				continue;
			}while(entrada.read() < 0);

			long endTime = System.currentTimeMillis();

			long latencia = endTime - startTime;

			System.out.println(latencia + "ms");

		}//try
		catch(EOFException erroLeitura){
			System.err.println("Final de arquivo: " + erroLeitura.toString());
		}
		catch(FileNotFoundException fnfe){
			System.err.println("Arquivo nao encontrado: " + fnfe.toString());
		}
		catch(IOException erroEscrita){
			System.err.println(erroEscrita.toString());
		}
	}

}//class SessaoCliente

public class Cliente{
	public static void main( String args[]  ) {
		Runnable cliente;
		Thread t;

		String serverIP = args[0];
		//String serverIP = "localhost";

		int serverPort = Integer.parseInt(args[1]);
		//int serverPort = 4549;

		int qtdeClientes = 1;	// altere para o valor que desejar

		for( int i=0; i < qtdeClientes; i++ ){
			
			cliente = new SessaoCliente("cliente["+ (i+1) +"]", serverIP, serverPort);
			
			t = new Thread( cliente );
			t.start();
		}
	}
}

