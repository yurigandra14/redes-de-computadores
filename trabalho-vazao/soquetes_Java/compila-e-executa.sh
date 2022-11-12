#!/bin/bash

## Compilando
javac ServidorTCP.java ClienteTCP.java
     ## OBS.: caso obtenha erro sobre codificação (acentuação) de caracteres, utilize: 
     #javac -encoding ISO-8859-1 ServidorTCP.java ClienteTCP.java

## Para executar os programas servidor e cliente em janelas separadas
CMD_SRV='java ServidorTCP'
CMD_CLI='java ClienteTCP localhost 8585'

## Executando o servidor TCP em uma janela separada
( xterm -hold -e $CMD_SRV || x-terminal-emulator -e $CMD_SRV ) &
     ## OBS.: para executar manualmente, digite em um terminal: 
     # java ServidorTCP

sleep 2

## Executando o cliente TCP em uma janela separada
( xterm -hold -e $CMD_CLI || x-terminal-emulator -e $CMD_CLI )
     ## OBS.: para executar manualmente, digite em um outro terminal: 
     # java ClienteTCP localhost 8585