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

// As classes abaixo implementam um servidor TCP multithread que recebe
// uma mensagem "PING" do cliente e responde com um "PONG" Se a mensagem
// recebido for "TERMINAR", a conexão com o cliente é fechada.


public class Servidor{
	public static void main( String args[]  ) {

		// soquete onde o servidor irá ouvir requisições
		ServerSocket serverSocket;
		// conexão a ser estabelecida com um cliente
		Socket conexao;

		boolean flagContinua = true;
				
		try {	// TENTA receber conexões de clientes, mas pode não haver uma rede ativa ou outro problema ocorrer

			// registra o servidor no porto 4549
			serverSocket = new ServerSocket( 4549 );
						//System.out.println("=== Servidor iniciado!");
		
			// utilizado apenas para contabilizar quantos clientes foram atenentradaaida
			int contador = 0;

			// servidor será um daemon e executará o laço indefinidamente
			while( flagContinua ) {
							//System.out.println("... aguardando alguma conexão...");

				try {	// TENTA aceitar uma conexão de um cliente
					conexao = serverSocket.accept();
					contador++;
					
					System.out.println("+++ cria a sessao nº " + contador + " para atender o novo cliente (" + conexao.getInetAddress() + "," + conexao.getPort() + ")" );
					// Dispara uma nova thread para gerenciar a nova conexao numa sessão própria
					Runnable sessao = new SessaoServidor("sessão[" + contador + "]", conexao);
					Thread t = new Thread( sessao );
					t.start();

				} // tentativa de aceitar uma conexão
				catch( Exception e) {	// CAPTURA algum problema caso ocorra (alguma trap - interrupção de software)
					System.err.println( "ERRO: " + e.toString() );
				}
			}//while

							System.out.println("=== Servidor finalizado!");
			serverSocket.close();

		} //tentativa de criar o soquete 
		catch( Exception e) {	// CAPTURA algum problema caso ocorra (alguma trap - interrupção de software)
			System.err.println( "ERRO: " + e.toString() );
		}
	}//main

}//class ServidorTCP

class SessaoServidor implements Runnable{

	private Socket conexao;
	private String idSessao;
	
	// construtor
	SessaoServidor( String s, Socket con ){
		idSessao = s;
		conexao = con;
	}
	
	// nome obrigatório para o método run() da interface Runnable
	public void run(){

		DataOutputStream saida;
		DataInputStream entrada;

		try {	// TENTA se comunicar com o cliente, mas a rede pode ficar indisponível ou outro problema ocorrer

			// obtém o fluxo de saida da conexao aberta
			saida = new DataOutputStream( conexao.getOutputStream());

			// obtém o fluxo de entrada da conexao aberta
			entrada = new DataInputStream( conexao.getInputStream());
			
			// Implementa abaixo o nosso protocolo de comunicação da aplicação

			// possibilita ao cliente realizar mais de uma solicitacao por sessao
			boolean continua = true;
			while(continua){
				int requisicao = entrada.readInt();
				if(requisicao == 1) {					
					enviaPacotes(saida);
					System.out.println( idSessao + ": \t" + "Teste Concluído!");
					continua = false;
				} else if(requisicao == 2) {
					saida.writeInt(1);
					saida.flush();
					System.out.println( idSessao + ": \t" + "Resposta do Ping");
					continua = false;
				}
				else{
					System.err.println("ERRO: comando invalido!");
					continua = false;
				}
			}//while 

			// Fim da implementação do nosso protocolo de comunicação da aplicação

			// fecha os fluxos de entrada e saída
			saida.close();
			entrada.close();
			// fecha a conexão (Socket) desta sessão (mas não o ServerSocket do servidor)
			conexao.close();

		} //tentativa de se comunicar com o cliente
		catch( Exception e) {	// CAPTURA algum problema caso ocorra (alguma trap - interrupção de software)
			System.err.println( "ERRO: " + e.toString() );
		}

	}//run()

	public void enviaPacotes(DataOutputStream saida){
		try{
			// loop: le a entrada do pipe e escreve no arquivo
			
			byte[] buffer = new byte[4*1024]; // tamanho padrao do cluster de HDD	
			long bytesEscritos = 0;

			// Implementação do teste de vazão aqui //

			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();

			do {
				saida.write(buffer, 0, buffer.length); // caso o buffer nao esteja cheio, envia ate bytesLidos-1
				endTime = System.currentTimeMillis();
				bytesEscritos += buffer.length;
			} while (endTime - startTime < 10000);

			float vazao = (float)bytesEscritos/(endTime-startTime); // bytes/ms
			vazao = vazao*8; // bits/ms
			vazao = vazao/1000.0F; //bits/seg

			if(vazao > 1000000000){
				vazao = vazao / 1000000000; // Gbit/seg
				System.out.println(vazao + "Gb/s");
			}
			else if(vazao > 1000000){
				vazao = vazao / 1000000; // Mbit/seg
				System.out.println(vazao + "Mb/s");
			}
			else if(vazao > 1000){
				vazao = vazao / 1000; // Kbit/seg
				System.out.println(vazao + "Kb/s");
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
		
	}

}//class SessaoServidor