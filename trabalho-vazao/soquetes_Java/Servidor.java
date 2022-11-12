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

			String mensagem = ""; 

			do{
				mensagem = entrada.readUTF(); 
				System.out.println(mensagem); 
				saida.writeUTF( "." ); 
			}while( !mensagem.equals("fim") ); 

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

}//class SessaoServidor