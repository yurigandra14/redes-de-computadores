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
            conexao.setSoTimeout(11000);

			// cada conexão possui dois fluxos, um de entrada e outro de saída.

			// obtém o fluxo de saida da conexao aberta
			saida = new DataOutputStream( conexao.getOutputStream() );
			// obtém o fluxo de entrada da conexao aberta
			entrada = new DataInputStream( conexao.getInputStream() );

			// IMPORTANTE: implementa abaixo o nosso protocolo de comunicação da aplicação

			// TESTE PING
			System.out.println("\t" + idCliente + " Solicitando Teste de Ping:");
			saida.writeInt(1);
			saida.flush();

			// PING SERVIDOR CLIENTE
			entrada.readInt();
			recebePing(entrada);

			// ALERTA QUE VAI ENVIAR O PING
			saida.writeInt(1);
			saida.flush();

			// PING CLIENTE SERVIDOR
			saida.writeByte(1);
			saida.flush();

			// TESTE LARGURA DE BANDA UDP
			System.out.println("\t" + idCliente + " Solicitando Teste de Largura de Banda");
			saida.writeInt(2);
			saida.flush();
			
			// RECEBE RESPOSTA SERVIDOR
			recebePacotesUDP();

			// ENVIA PACOTES SERVIDOR
			enviaPacotesUDP(conexao);

			// TESTE VAZAO TCP
			System.out.println("\t" + idCliente + " Solicitando Teste de Vazão");
			saida.writeInt(3);
			saida.flush();
			
			// RESPOSTA SERVIDOR
			recebePacotes(entrada);

			// ENVIA PACOTES SERVIDOR
			enviaPacotes(saida);

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

			byte[] buffer = new byte[10*4*1024]; //40 KB	
			int bytesLidos;

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

            try{
                do{
                    endTime = System.currentTimeMillis();
                    bytesLidos = entrada.read(buffer);
                    totalBytes = totalBytes + bytesLidos;
                }while( (bytesLidos > 0) );
            }catch(SocketTimeoutException e){}


			float vazao = ((float)totalBytes)/(endTime-startTime); // bytes/ms
			vazao = vazao*8; // bits/ms
			vazao = vazao*1000.0F; //bits/seg

			if(vazao > 1000000000){
				vazao = vazao / 1000000000; // Gbit/seg
				System.out.println("\t Vazão: " + vazao + "Gb/s");
			}
			else if(vazao > 1000000){
				vazao = vazao / 1000000; // Mbit/seg
				System.out.println("\t Vazão: " + vazao + "Mb/s");
			}
			else if(vazao > 1000){
				vazao = vazao / 1000; // Kbit/seg
				System.out.println("\t Vazão: " + vazao + "Kb/s");
			}

		}//try
		catch(Exception erroEscrita){
			System.err.println(erroEscrita.toString());
		}
		
	}

	public void enviaPacotes(DataOutputStream saida){
		try{
			// loop: le a entrada do pipe e escreve no arquivo
			
			byte[] buffer = new byte[1440];
			long bytesEscritos = 0;

			// Implementação do teste de vazão aqui //

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

            try{

                do {
                    saida.write(buffer, 0, buffer.length); // caso o buffer nao esteja cheio, envia ate bytesLidos-1
                    endTime = System.currentTimeMillis();
                    bytesEscritos += buffer.length;
                } while ( (endTime - startTime) < 10000);

            } catch(SocketException e) {}

		}//try	
		catch(Exception erroEscrita){
			System.err.println("Final de arquivo: " + erroEscrita.toString());
		}
		
	}

	public void recebePacotesUDP() {

		long totalBytes = 0;
		
		try{

			byte[] buffer = new byte[1470];

			DatagramSocket soquete = new DatagramSocket(8383);
			DatagramPacket resposta = new DatagramPacket(buffer, buffer.length); 

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

			do{
				endTime = System.currentTimeMillis();
				soquete.receive(resposta);
				totalBytes = totalBytes + resposta.getLength();
			}while(resposta.getLength() != 1);

			float larguraBanda = ((float)totalBytes)/(endTime-startTime); // bytes/ms
			larguraBanda = larguraBanda*8; // bits/ms
			larguraBanda = larguraBanda*1000.0F; //bits/seg

			if(larguraBanda > 1000000000){
				larguraBanda = larguraBanda / 1000000000; // Gbit/seg
				System.out.println("\t Largura de Banda: " + larguraBanda + "Gb/s");
			}
			else if(larguraBanda > 1000000){
				larguraBanda = larguraBanda / 1000000; // Mbit/seg
				System.out.println("\t Largura de Banda: " + larguraBanda + "Mb/s");
			}
			else if(larguraBanda > 1000){
				larguraBanda = larguraBanda / 1000; // Kbit/seg
				System.out.println("\t Largura de Banda: " + larguraBanda + "Kb/s");
			}

			soquete.close();

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

	public void enviaPacotesUDP(Socket conexao) {

		try{
			
			byte[] buffer = new byte[1470];
			long bytesEscritos = 0;

			DatagramSocket soquete = new DatagramSocket(8282);
			DatagramPacket resposta;
			resposta = new DatagramPacket( buffer, buffer.length, conexao.getInetAddress(), 8181);

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

			do {
				soquete.send(resposta);
				endTime = System.currentTimeMillis();
				bytesEscritos += resposta.getLength();
			} while (endTime - startTime < 10000);

			resposta = new DatagramPacket( new byte[1], 1, conexao.getInetAddress(), 8181);
			soquete.send(resposta);
			soquete.close();

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

	public void recebePing(DataInputStream entrada) {
		try{

			// loop: le a entrada do pipe e escreve no arquivo

			long startTime = System.nanoTime();

			try{
				entrada.readByte();
			}catch(EOFException erro){}

			long endTime = System.nanoTime();

			double latencia = ((double)(endTime - startTime))/1000000;

			System.out.printf("\t Latência: %.6f ms\n",latencia);

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

